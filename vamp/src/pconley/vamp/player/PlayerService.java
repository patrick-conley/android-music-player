package pconley.vamp.player;

import java.io.IOException;

import pconley.vamp.PlayerActivity;
import pconley.vamp.R;
import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;
import pconley.vamp.util.Playlist;
import pconley.vamp.util.PlaylistIterator;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayerService extends Service implements
		MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
		AudioManager.OnAudioFocusChangeListener {

	/**
	 * ID used for this service's notifications.
	 */
	public static final int NOTIFICATION_ID = 1;

	/**
	 * If a "previous" action is used after this time, the current track should
	 * be restarted instead of beginning the previous track.
	 */
	public static final int PREV_RESTART_LIMIT = 2;

	/**
	 * Action for incoming intents. Start playing a new track.
	 * 
	 * Use EXTRA_TRACK_LIST to set an array of tracks to play, and
	 * EXTRA_START_POSITION to specify the start position in the list (if not
	 * included, begin with the first track).
	 */
	public static final String ACTION_PLAY = "pconley.vamp.PlayerService.play";
	public static final String EXTRA_TRACKS = "pconley.vamp.player.PlayerService.track_list";
	public static final String EXTRA_START_POSITION = "pconley.vamp.player.PlayerService.position";

	/**
	 * Action for incoming intents. Pause the player, provided it's playing.
	 */
	public static final String ACTION_PAUSE = "pconley.vamp.PlayerService.pause";

	/**
	 * Action for incoming intents. Seek within the current track. Does nothing
	 * if there is no track in progress.
	 */
	public static final String ACTION_SEEK = "pconley.vamp.playerService.seek";
	public static final String EXTRA_SEEK_POSITION = "pconley.vamp.playerService.seek.time";

	private static final int SEC = 1000;

	private Playlist playlist;
	private PlaylistIterator trackIterator;

	private boolean isPlaying = false;
	private boolean isPrepared = false;

	private IBinder binder;
	private AudioManager audioManager;
	private MediaPlayer player = null;
	private LocalBroadcastManager broadcastManager;

	// Constant content of the notification displayed while a track plays.
	// Content that changes with the track needs to be added in playPause()
	private Notification.Builder notificationBase;

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

		notificationBase = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Now playing...")
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setOngoing(true)
				.setLargeIcon(
						BitmapFactory.decodeResource(this.getResources(),
								android.R.drawable.ic_media_play))
				.setOngoing(true)
				.setContentIntent(
						PendingIntent.getActivity(getApplicationContext(), 0,
								new Intent(getApplicationContext(),
										PlayerActivity.class),
								PendingIntent.FLAG_UPDATE_CURRENT));

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		broadcastManager = LocalBroadcastManager.getInstance(this);

		binder = new PlayerBinder();
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

		if (intent != null) {

			if (intent.getAction() == null) {
				Log.e("Player",
						"Intent to start the player missing a required action");
				return START_NOT_STICKY;
			}

			Log.i("Player", "Received control action " + intent.getAction());

			switch (intent.getAction()) {
			case ACTION_PLAY:

				if (!intent.hasExtra(EXTRA_TRACKS)) {
					throw new IllegalArgumentException(
							"Play action given with no tracks");
				}

				TrackDAO dao = new TrackDAO(this);

				playlist = new Playlist();
				for (long id : intent.getLongArrayExtra(EXTRA_TRACKS)) {
					playlist.add(dao.getTrack(id));
				}
				trackIterator = playlist.playlistIterator(intent
						.getIntExtra(EXTRA_START_POSITION, 0));
				trackIterator.next();


				dao.close();

				start(true);

				break;

			case ACTION_PAUSE:
				pause();
				break;

			case ACTION_SEEK:
				seekTo(intent.getIntExtra(EXTRA_SEEK_POSITION, 0));
				break;

			default:
				Log.w("Player", "Invalid action " + intent.getAction());
				break;
			}
		}

		return Service.START_STICKY;
	}

	/**
	 * When a track completes normally and more tracks are available, start the
	 * next one.
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (isPrepared && trackIterator.hasNext()) {
			trackIterator.next();
			start(true);
		}
	}

	/**
	 * Catch errors, then release the player.
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("Player",
				"Error " + String.valueOf(what) + "," + String.valueOf(extra));
		// FIXME: broadcasts do little if we're between activities
		// FIXME: use actual messages rather than codes (as I figure out what
		// messages mean)
		String error = "Info: " + String.valueOf(what) + ","
				+ String.valueOf(extra);
		stop(error);

		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_LOSS:

			Log.w("Player", "Audio focus lost");
			stop("Audio focus lost");
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

			Log.i("Player", "Audio focus lost temporarily");
			pause();
			break;
		}

	}

	/**
	 * @return Data for the currently-playing (or paused) track, provided one is
	 *         prepared by the MediaPlayer. Returns null otherwise.
	 */
	public Track getCurrentTrack() {
		return isPrepared ? trackIterator.current() : null;
	}

	/**
	 * @return The current track's current position (in ms), or -1 if nothing is
	 *         playing.
	 */
	public int getPosition() {
		return isPrepared ? player.getCurrentPosition() : -1;
	}

	/**
	 * @return The current track's duration (in ms), or -1 if nothing is
	 *         playing.
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
	 *            Begin playing the new track immediately, or load the track and
	 *            remain paused.
	 */
	private void start(boolean beginPlayback) {
		Track current = trackIterator.current();

		// Set or reset the player
		if (player != null) {
			player.reset();
			isPrepared = false;
			isPlaying = false;
		} else {
			player = new MediaPlayer();

			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnCompletionListener(PlayerService.this);
			player.setOnErrorListener(PlayerService.this);
			player.setWakeMode(getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);
		}

		try {

			Log.d("Player", "Preparing track " + current);
			player.setDataSource(getApplicationContext(), current.getUri());
			player.prepare();
			isPrepared = true;

			broadcastEvent(PlayerEvent.NEW_TRACK);

			if (beginPlayback) {
				play();
			}

		} catch (IOException e) {
			stop("Track " + current.getUri() + " could not be read.");
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException e) {
			Log.e("Player", e.getMessage());
			stop("Internal error " + e.getMessage());
		}

	}

	/**
	 * See {@link #stop(String)}
	 */
	private void stop() {
		stop(null);
	}

	/**
	 * Stop playback and clean up the player. Use this function instead of an
	 * explicit call to onCompletion if the player can't continue.
	 * 
	 * @param message
	 *            Reason for stopping.
	 */
	private void stop(String message) {
		stopForeground(true);

		isPlaying = false;
		isPrepared = false;

		player.reset();
		player.release();
		player = null;

		broadcastEvent(PlayerEvent.STOP, message);
	}

	/**
	 * Pause the current track.
	 */
	public void pause() {
		pause(null);
	}

	/**
	 * Pause the current track.
	 * 
	 * @param message
	 *            Reason for pausing; null if by user request.
	 */
	private void pause(String message) {

		if (isPlaying) {
			player.pause();
			isPlaying = false;

			stopForeground(true);
			audioManager.abandonAudioFocus(this);

			Log.d("Player", "paused");
			broadcastEvent(PlayerEvent.PAUSE, message);

		} else if (!isPrepared && message != null) {
			broadcastEvent(PlayerEvent.STOP, message);
		}
	}

	/**
	 * Try to play the current track.
	 *
	 * @return whether the track is now playing
	 */
	public void play() {
		if (!isPrepared) {
			Log.w("Player", "Can't play: player not prepared.");
			return;
		} else if (isPlaying) {
			return; // nothing to do
		}

		boolean hasFocus = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

		if (hasFocus) {
			startForeground(NOTIFICATION_ID, notificationBase.build());

			player.start();
			isPlaying = true;
			Log.d("Player", "started");

			broadcastEvent(PlayerEvent.PLAY);

		} else {
			broadcastEvent(PlayerEvent.PAUSE, "Could not obtain audio focus");
		}
	}

	/**
	 * Seek within the current track. Works in play and pause states.
	 *
	 * @param time
	 *            target position (in ms). I'm not sure what will happen if the
	 *            target is invalid.
	 */
	public void seekTo(int time) {
		if (!isPrepared) {
			Log.w("Player", "Can't seek: player not prepared.");
			return;
		}

		player.seekTo(time);

		broadcastEvent(isPlaying ? PlayerEvent.PLAY : PlayerEvent.PAUSE);
	}

	/**
	 * If position is less than 3s and the current track is not the first track
	 * in the collection, go to the beginning of the previous track. Otherwise,
	 * go to the beginning of this track.
	 *
	 * Does nothing if the player is not prepared.
	 */
	public void previous() {
		if (!isPrepared) {
			Log.w("Player", "Can't go to previous: player not prepared.");
			return;
		}

		if (getPosition() / SEC > PREV_RESTART_LIMIT
				|| !trackIterator.hasPrevious()) {
			seekTo(0);
		} else {
			trackIterator.previous();
			start(isPlaying);
		}

	}

	/**
	 * If the current track is not the last track in the collection, got to the
	 * beginning of the next track. Otherwise, stop playing.
	 */
	public void next() {
		if (!isPrepared) {
			Log.w("Player", "Can't go to next: player not prepared.");
			return;
		} else if (trackIterator.hasNext()) {
			trackIterator.next();
			start(isPlaying);
		} else {
			stop();
		}

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
		Intent intent = new Intent(PlayerEvent.FILTER_PLAYER_EVENT).putExtra(
				PlayerEvent.EXTRA_EVENT, event);

		if (message != null) {
			intent.putExtra(PlayerEvent.EXTRA_MESSAGE, message);
		}

		broadcastManager.sendBroadcast(intent);
	}

}
