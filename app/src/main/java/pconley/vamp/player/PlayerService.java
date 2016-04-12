package pconley.vamp.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import pconley.vamp.R;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;
import pconley.vamp.player.view.PlayerActivity;
import pconley.vamp.util.BroadcastConstants;

/**
 * Handler for the media player. PlayerService provides controls for interacting
 * with the active track and playlist directly (play/pause, next/previous,
 * scan). When the player changes state automatically (beginning the next track,
 * end of the playlist), a broadcast notifies activities using it. See {@link
 * PlayerService#broadcastEvent(PlayerEvent)}
 *
 * @author pconley
 */
public class PlayerService extends Service implements
		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
		AudioManager.OnAudioFocusChangeListener {
	private static final String TAG = "Player";

	/**
	 * ID used for this service's notifications.
	 */
	public static final int NOTIFICATION_ID = 1;

	/**
	 * If a "previous" action is used after this time, the current track should
	 * be restarted instead of beginning the previous track.
	 */
	public static final int PREV_RESTART_LIMIT = 3;

	/**
	 * Action for incoming intents. Start playing a new track.
	 * <p/>
	 * Use EXTRA_COLLECTION to set the collection of tracks to play, and
	 * EXTRA_START_POSITION to specify the start position in the list (if not
	 * included, begin with the first track).
	 */
	public static final String ACTION_PLAY = "pconley.vamp.player.play";
	public static final String EXTRA_START_POSITION
			= "pconley.vamp.player.position";
	public static final String EXTRA_COLLECTION
			= "pconley.vamp.player.collection";

	/**
	 * Action for incoming intents. Pause the player, provided it's playing.
	 */
	public static final String ACTION_PAUSE
			= "pconley.vamp.player.pause";

	private static final int SEC = 1000;

	private TrackCollection collection;
	private int position;

	private boolean isPlaying = false;
	private boolean isPrepared = false;
	private boolean isFocusLost = false;

	private IBinder binder;
	private AudioManager audioManager;
	private MediaPlayer player;
	private LocalBroadcastManager broadcastManager;

	// Constant content of the notification displayed while a track plays.
	// Content that changes with the track needs to be added in playPause()
	private Notification.Builder notification;

	/**
	 * Bind an activity to control the music player's state.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class PlayerBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		PendingIntent content = PendingIntent.getActivity(
				getApplicationContext(), 0,
				new Intent(getApplicationContext(), PlayerActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		Bitmap icon = BitmapFactory.decodeResource(
				this.getResources(),
				android.R.drawable.ic_media_play);

		notification = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.player_notification_playing))
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setOngoing(true)
				.setLargeIcon(icon)
				.setOngoing(true)
				.setContentIntent(content);

		broadcastManager = LocalBroadcastManager.getInstance(this);
		binder = new PlayerBinder();

		player = PlayerFactory.getInstance(this).createMediaPlayer();
		audioManager = (AudioManager) getApplicationContext().getSystemService(
				Context.AUDIO_SERVICE);
	}

	@Override
	public void onDestroy() {
		if (player != null) {
			stop();
		}

		super.onDestroy();
	}

	/**
	 * Handle intents that interact with the media player: play/pause, new
	 * track, etc.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if (intent == null || intent.getAction() == null) {
			return START_NOT_STICKY;
		}

		Log.i(TAG, "Received control action " + intent.getAction());

		switch (intent.getAction()) {
			case ACTION_PLAY:

				collection = intent.getParcelableExtra(EXTRA_COLLECTION);
				if (collection == null) {
					throw new IllegalArgumentException(
							"EXTRA_COLLECTION not set");
				}

				position = intent.getIntExtra(EXTRA_START_POSITION, 0);
				if (position < 0 ||
				    position >= collection.getContents().size()) {
					throw new IllegalArgumentException(
							"EXTRA_START_POSITION invalid");
				}

				start(true);

				break;

			case ACTION_PAUSE:
				pause();
				break;

			default:
				Log.w(TAG, "Invalid action " + intent.getAction());
				break;
		}

		return START_STICKY;
	}

	/**
	 * When a track completes normally and more tracks are available, start the
	 * next one.
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		next();
	}

	/**
	 * Catch errors, then release the player.
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// FIXME: broadcasts do little if we're between activities
		// FIXME: use actual messages rather than codes (as I figure out what
		// messages mean)
		Log.e(TAG, getString(R.string.player_error_MediaPlayer, what, extra));
		stop(R.string.player_error_MediaPlayer, what, extra);

		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				if (isFocusLost) {
					play();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS:

				Log.w(TAG, getString(R.string.player_focus_lost));
				stop(R.string.player_focus_lost);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

				Log.i(TAG, getString(R.string.player_focus_lost_transient));
				isFocusLost = true;
				pause();
				break;
		}

	}

	/**
	 * @return Data for the currently-playing (or paused) track, provided one is
	 * prepared by the MediaPlayer. Returns null otherwise.
	 */
	public Track getCurrentTrack() {
		return isPrepared ? collection.getContents().get(position) : null;
	}

	/**
	 * @return The current track's current position (in ms), or -1 if nothing is
	 * playing.
	 */
	public int getPosition() {
		return isPrepared ? player.getCurrentPosition() : -1;
	}

	/**
	 * @return The current track's duration (in ms), or -1 if nothing is
	 * playing.
	 */
	public int getDuration() {
		return isPrepared ? player.getDuration() : -1;
	}

	/**
	 * @return Whether the service is playing a track.
	 */
	public boolean isPlaying() {
		return isPlaying;
	}

	/**
	 * Begin playing the selected track from the queue. Changing the queue
	 * requires restarting the service with a new intent.
	 *
	 * @param beginPlayback
	 * 		Begin playing the new track immediately, or load the track and remain
	 * 		paused.
	 */
	private void start(boolean beginPlayback) {
		Track current = collection.getContents().get(position);

		try {

			// Set or reset the player
			if (isPrepared) {
				player.reset();
				isPrepared = false;
				isPlaying = false;
			}

			Log.d(TAG, "Preparing track " + current);

			if (!new File(current.getUri().getPath()).exists()) {
				throw new IOException();
			}

			player.setDataSource(this, current.getUri());
			player.prepare();
			isPrepared = true;

			broadcastEvent(PlayerEvent.NEW_TRACK);

			if (beginPlayback) {
				play();
			}

		} catch (IOException e) {
			Log.e(TAG, getString(R.string.player_error_read, current.getUri()));

			// Skip to next track on error
			if (position < collection.getContents().size() - 1) {
				position++;
				start(beginPlayback);
			} else {
				stop(R.string.player_error_read, current.getUri());
			}

		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException e) {
			Log.e(TAG, e.getMessage());
			stop(R.string.player_error_internal, e.getMessage());
		}

	}

	/**
	 * See {@link #stop(int, Object...)}
	 */
	public void stop() {
		stop(0);
	}

	/**
	 * Stop playback and clean up the player. Use this function instead of an
	 * explicit call to onCompletion if the player can't continue.
	 *
	 * @param resId
	 * 		Reason for stopping.
	 */
	private void stop(int resId, Object... formatArgs) {
		String message = resId > 0 ? getString(resId, formatArgs) : null;

		if (isPlaying) {
			isPlaying = false;
			stopForeground(true);
		}

		if (player != null) {
			isPrepared = false;

			player.reset();
			player.release();
			player = null;
		}

		broadcastEvent(PlayerEvent.STOP, message);
		stopSelf();
	}

	/**
	 * Pause the current track.
	 */
	public boolean pause() {
		return pause(0);
	}

	/**
	 * Pause the current track.
	 *
	 * @param resId
	 * 		Resource ID of the message to include.
	 * @param formatArgs
	 * 		See {@link Context#getString(int, Object...)}
	 * @return True if the service has a track prepared (whether or not the
	 * track is currently playing).
	 */
	private boolean pause(int resId, Object... formatArgs) {
		String message = resId > 0 ? getString(resId, formatArgs) : null;

		if (isPlaying) {
			player.pause();
			isPlaying = false;

			stopForeground(true);
			audioManager.abandonAudioFocus(this);

			Log.d(TAG, "paused");
			broadcastEvent(PlayerEvent.PAUSE, message);
		} else if (!isPrepared && resId > 0) {
			broadcastEvent(PlayerEvent.STOP, message);
		}

		return isPrepared;
	}

	/**
	 * Try to play the current track.
	 *
	 * @return whether the track is now playing
	 */
	public boolean play() {
		if (!isPrepared) {
			Log.w(TAG, "Can't play: player not prepared.");
			return false;
		} else if (isPlaying) {
			return true; // nothing to do
		}

		int focus = audioManager
				.requestAudioFocus(this,
				                   AudioManager.STREAM_MUSIC,
				                   AudioManager.AUDIOFOCUS_GAIN);

		if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			startForeground(NOTIFICATION_ID, notification.build());

			player.start();
			isPlaying = true;
			isFocusLost = false;
			Log.d(TAG, "started");

			broadcastEvent(PlayerEvent.PLAY);

			return true;
		} else {
			broadcastEvent(PlayerEvent.PAUSE,
			               getString(R.string.player_focus_failed));

			return false;
		}
	}

	/**
	 * Seek within the current track. Works in play and pause states.
	 *
	 * @param time
	 * 		target position (in ms). I'm not sure what will happen if the target is
	 * 		invalid.
	 * @return True if the service has a track prepared
	 */
	public boolean seekTo(int time) {
		if (!isPrepared) {
			Log.w(TAG, "Can't seek: player not prepared.");
			return false;
		}

		player.seekTo(time);
		return true;
	}

	/**
	 * If position is less than 3s and the current track is not the first track
	 * in the collection, go to the beginning of the previous track. Otherwise,
	 * go to the beginning of this track.
	 *
	 * @return True if the player has a track prepared
	 */
	public boolean previous() {
		if (!isPrepared) {
			Log.w(TAG, "Can't go to previous: player not prepared.");
			return false;
		}

		if (getPosition() / SEC > PREV_RESTART_LIMIT || position == 0) {
			seekTo(0);

			// Tell the UI to correct its position
			broadcastEvent(
					isPlaying ? PlayerEvent.PLAY : PlayerEvent.NEW_TRACK);
		} else {
			position--;
			start(isPlaying);
		}

		return isPrepared;
	}

	/**
	 * If the current track is not the last track in the collection, got to the
	 * beginning of the next track. Otherwise, stop playing.
	 *
	 * @return True if the player has a track prepared
	 */
	public boolean next() {
		if (!isPrepared) {
			Log.w(TAG, "Can't go to next: player not prepared.");
			return false;
		} else if (position < collection.getContents().size() - 1) {
			position++;
			start(isPlaying);
		} else {
			stop();
		}

		return isPrepared;
	}

	/**
	 * Broadcast a change in player state to clients. The same event may be
	 * broadcast consecutive times.
	 *
	 * @param event
	 */
	private void broadcastEvent(PlayerEvent event) {
		broadcastEvent(event, null);
	}

	/**
	 * Broadcast a change in player state to clients. The same event may be
	 * broadcast consecutive times.
	 *
	 * @param event
	 * @param message
	 */
	private void broadcastEvent(PlayerEvent event, String message) {
		Intent intent = new Intent(BroadcastConstants.FILTER_PLAYER_EVENT)
				.putExtra(BroadcastConstants.EXTRA_EVENT, event);

		if (message != null) {
			intent.putExtra(BroadcastConstants.EXTRA_MESSAGE, message);
		}

		broadcastManager.sendBroadcast(intent);
	}

}
