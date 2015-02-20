package pconley.vamp.player;

import java.io.IOException;

import pconley.vamp.PlayerActivity;
import pconley.vamp.R;
import pconley.vamp.db.TrackDAO;
import pconley.vamp.model.Track;
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
		MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {

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

	/**
	 * Broadcast filter used by messages for this player's status receiver
	 */
	public static final String FILTER_PLAYER_EVENT = "pconley.vamp.player.event";

	/**
	 * Extra used in broadcasts about player state changes. Value is one of
	 * EVENT_NEW_TRACK, EVENT_PLAY, EVENT_PAUSE, or EVENT_STOP.
	 */
	public static final String EXTRA_EVENT = "pconley.vamp.player.event";

	/**
	 * A new track has been loaded. It may or may not be playing.
	 */
	public static final String EVENT_NEW_TRACK = "pconley.vamp.player.event.new_track";

	public static final String EVENT_PLAY = "pconley.vamp.player.event.play";

	/**
	 * The current track has been paused.
	 */
	public static final String EVENT_PAUSE = "pconley.vamp.player.event.pause";

	/**
	 * The player is done with all tracks. If it stopped because of an error,
	 * the error will be described by the string EXTRA_MESSAGE.
	 */
	public static final String EVENT_STOP = "pconley.vamp.player.event.stop";

	/**
	 * Reason for an unexpected stop/pause event. Probably given as an
	 * unreadable error code.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.player.event.message";

	private static final int SEC = 1000;

	private long[] trackIds = null;
	private int currentPosition = -1;
	private Track currentTrack = null;

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
			player.reset();
			player.release();
			player = null;
			currentTrack = null;
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

				trackIds = intent.getLongArrayExtra(EXTRA_TRACKS);

				beginTrack(intent.getIntExtra(EXTRA_START_POSITION, 0));
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

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopForeground(true);

		isPlaying = false;
		isPrepared = false;

		player.reset();
		player.release();
		player = null;
		currentTrack = null;

		broadcastManager.sendBroadcast(new Intent(FILTER_PLAYER_EVENT)
				.putExtra(EXTRA_EVENT, EVENT_STOP));
	}

	/**
	 * Catch errors, then release the player through the
	 * MediaPlayer.OnCompletionListener.
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("Player",
				"Error " + String.valueOf(what) + "," + String.valueOf(extra));
		// FIXME: broadcasts do little if we're between activities
		// FIXME: use actual messages rather than codes (as I figure out what
		// messages mean)

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT).putExtra(
				EXTRA_EVENT, EVENT_STOP).putExtra(EXTRA_MESSAGE,
				String.valueOf(what) + "," + String.valueOf(extra));

		broadcastManager.sendBroadcast(broadcast);

		// Return false in order to call onCompletion
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.e("Player",
				"Info " + String.valueOf(what) + "," + String.valueOf(extra));
		// FIXME: use actual messages rather than codes (as I figure out what
		// messages mean)

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT).putExtra(
				EXTRA_MESSAGE,
				String.valueOf(what) + "," + String.valueOf(extra));

		if (isPlaying) {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);
		} else if (isPrepared) {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);
		} else {
			broadcast.putExtra(EXTRA_EVENT, EVENT_STOP);
		}

		broadcastManager.sendBroadcast(broadcast);

		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_LOSS:

			pause("Audio focus lost");
			onCompletion(player);
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

			pause("Audio focus lost temporarily");
			break;
		}

	}

	/**
	 * @return Data for the currently-playing (or paused) track, provided one is
	 *         prepared by the MediaPlayer. Returns null otherwise.
	 */
	public Track getCurrentTrack() {
		return isPrepared ? currentTrack : null;
	}

	/**
	 * @return The current track's current position (in ms), or -1 if nothing is
	 *         playing.
	 */
	public int getProgress() {
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
	 * Begin playing a new track from the current collection. Changing the
	 * collection requires restarting the service with a new intent.
	 * 
	 * @param position
	 */
	public void beginTrack(int position) {

		if (player != null) {
			player.reset();
			isPrepared = false;
			isPlaying = false;
		} else {
			player = new MediaPlayer();
		}

		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(PlayerService.this);
		player.setOnErrorListener(PlayerService.this);
		player.setOnInfoListener(PlayerService.this);
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);

		try {
			currentPosition = position;
			currentTrack = new TrackDAO(PlayerService.this)
					.getTrack(trackIds[position]);

			Log.d("Player", "Preparing track " + currentTrack);
			player.setDataSource(getApplicationContext(), currentTrack.getUri());
			player.prepare();
			isPrepared = true;

			broadcastManager.sendBroadcast(new Intent(FILTER_PLAYER_EVENT)
					.putExtra(EXTRA_EVENT, EVENT_NEW_TRACK));

			play();

		} catch (IOException e) {
			broadcastManager.sendBroadcast(new Intent(FILTER_PLAYER_EVENT)
					.putExtra(EXTRA_EVENT, EVENT_STOP).putExtra(
							EXTRA_MESSAGE,
							"Track " + currentTrack.getUri()
									+ " could not be read."));

			onCompletion(player);
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException e) {
			Log.e("Player", e.getMessage());

			onCompletion(player);
		}
	}

	/**
	 * Pause the current track.
	 */
	public void pause() {
		pause(null);
	}

	/**
	 * Pause the current track.
	 */
	private void pause(String message) {

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT);

		if (isPlaying) {
			player.pause();
			isPlaying = false;

			stopForeground(true);
			audioManager.abandonAudioFocus(this);

			Log.d("Player", "paused");

			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);

			if (message != null) {
				broadcast.putExtra(EXTRA_MESSAGE, message);
			}

			broadcastManager.sendBroadcast(broadcast);
		} else if (!isPrepared && message != null) {

			broadcast.putExtra(EXTRA_EVENT, EVENT_STOP).putExtra(EXTRA_MESSAGE,
					message);
			broadcastManager.sendBroadcast(broadcast);
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

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT);

		if (hasFocus) {
			startForeground(NOTIFICATION_ID, notificationBase.build());

			player.start();
			isPlaying = true;
			Log.d("Player", "started");

			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);

		} else {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);
			broadcast.putExtra(EXTRA_MESSAGE, "Could not obtain audio focus");
		}

		broadcastManager.sendBroadcast(broadcast);
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

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT);

		if (isPlaying) {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);
		} else {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);
		}

		broadcastManager.sendBroadcast(broadcast);
	}

	/**
	 * If progress is less than 3s and the current track is not the first track
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

		if (getProgress() / SEC > PREV_RESTART_LIMIT || currentPosition == 0) {
			seekTo(0);
		} else {
			beginTrack(currentPosition - 1);
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
		}

		if (currentPosition < trackIds.length - 1) {
			beginTrack(currentPosition + 1);
		} else {
			onCompletion(player);
		}

	}

}
