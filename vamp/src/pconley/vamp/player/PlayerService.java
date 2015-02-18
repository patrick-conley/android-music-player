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
	 * Action for incoming intents. Start playing a new track. Specify the track
	 * with EXTRA_TRACK_ID.
	 */
	public static final String ACTION_PLAY = "pconley.vamp.PlayerService.play";
	public static final String EXTRA_TRACK_ID = "pconley.vamp.player.PlayerService.track_id";

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
	public static final String EVENT_NEW_TRACK = "pconley.vamp.player.event.new";

	/**
	 * The player has started or resumed playing the current track.
	 */
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

	private Track currentTrack = null;

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

				if (!intent.hasExtra(EXTRA_TRACK_ID)) {
					throw new IllegalArgumentException(
							"PLAY action given with no track");
				}

				beginTrack(intent.getLongExtra(EXTRA_TRACK_ID, -1));
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

		if (isPlaying()) {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);
		} else {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);
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
		return currentTrack;
	}

	/**
	 * @return The current track's current position (in ms), or -1 if nothing is
	 *         playing.
	 */
	public int getCurrentPosition() {
		return player == null ? -1 : player.getCurrentPosition();
	}

	/**
	 * @return The current track's duration (in ms), or -1 if nothing is
	 *         playing.
	 */
	public int getDuration() {
		return player == null ? -1 : player.getDuration();
	}

	/**
	 * @return Whether the service is playing a track.
	 */
	public boolean isPlaying() {
		return player != null && player.isPlaying();
	}

	public void beginTrack(long trackId) {

		if (player == null) {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnCompletionListener(PlayerService.this);
			player.setOnErrorListener(PlayerService.this);
			player.setOnInfoListener(PlayerService.this);
			player.setWakeMode(getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);

		} else {
			player.reset();
		}

		try {
			currentTrack = new TrackDAO(PlayerService.this).getTrack(trackId);

			Log.d("Player", "Preparing track " + currentTrack);
			player.setDataSource(getApplicationContext(), currentTrack.getUri());
			player.prepare();

			broadcastManager.sendBroadcast(new Intent(FILTER_PLAYER_EVENT)
					.putExtra(EXTRA_EVENT, EVENT_NEW_TRACK));

			play();

		} catch (IOException e) {
			broadcastManager.sendBroadcast(new Intent(FILTER_PLAYER_EVENT)
					.putExtra(EXTRA_EVENT, EVENT_STOP).putExtra(
							EXTRA_MESSAGE,
							"Track " + currentTrack.getUri()
									+ " could not be read."));
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException e) {
			Log.e("Player", e.getMessage());
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

		if (player != null && player.isPlaying()) {
			player.pause();

			stopForeground(true);
			audioManager.abandonAudioFocus(this);

			Log.d("Player", "paused");

			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);

			if (message != null) {
				broadcast.putExtra(EXTRA_MESSAGE, message);
			}

			broadcastManager.sendBroadcast(broadcast);
		} else if (message != null) {

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
		if (player == null) {
			Log.w("Player", "Can't play: no player");
			return;
		}

		boolean hasFocus = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT);

		if (hasFocus) {
			player.start();
			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);
			startForeground(NOTIFICATION_ID, notificationBase.build());

			Log.d("Player", "started");
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
		if (player == null) {
			return;
		}

		player.seekTo(time);

		Intent broadcast = new Intent(FILTER_PLAYER_EVENT);

		if (isPlaying()) {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PLAY);
		} else {
			broadcast.putExtra(EXTRA_EVENT, EVENT_PAUSE);
		}

		broadcastManager.sendBroadcast(broadcast);
	}

}