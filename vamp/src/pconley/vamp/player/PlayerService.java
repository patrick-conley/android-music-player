package pconley.vamp.player;

import java.io.IOException;

import pconley.vamp.PlayerActivity;
import pconley.vamp.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayerService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
		AudioManager.OnAudioFocusChangeListener {

	/**
	 * ID used for this service's notifications.
	 */
	public static final int NOTIFICATION_ID = 1;

	/**
	 * Action for incoming intents. Start playing a new track. Specify the track
	 * with the Intent's data.
	 */
	public static final String ACTION_PLAY = "pconley.vamp.PlayerService.play";

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
		}

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		if (intent != null) {
			Log.i("Player", intent.getAction());

			switch (intent.getAction()) {
			case ACTION_PLAY:
				playTrack(intent.getData());
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
	public void onPrepared(MediaPlayer mp) {
		play();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopForeground(true);

		player.release();
		player = null;
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
		broadcastManager.sendBroadcast(new Intent(
				PlayerEvents.FILTER_PLAYER_EVENT).putExtra(
				PlayerEvents.EXTRA_STATE, false).putExtra(
				PlayerEvents.EXTRA_MESSAGE,
				String.valueOf(what) + "," + String.valueOf(extra)));

		// Return false to call onCompletion
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.e("Player",
				"Info " + String.valueOf(what) + "," + String.valueOf(extra));
		// FIXME: use actual messages rather than codes (as I figure out what
		// messages mean)
		broadcastManager.sendBroadcast(new Intent(
				PlayerEvents.FILTER_PLAYER_EVENT).putExtra(
				PlayerEvents.EXTRA_STATE, player.isPlaying()).putExtra(
				PlayerEvents.EXTRA_MESSAGE,
				String.valueOf(what) + "," + String.valueOf(extra)));

		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			play();
			break;
		case AudioManager.AUDIOFOCUS_LOSS:

			broadcastManager.sendBroadcast(new Intent(
					PlayerEvents.FILTER_PLAYER_EVENT).putExtra(
					PlayerEvents.EXTRA_STATE, false).putExtra(
					PlayerEvents.EXTRA_MESSAGE, "Audio focus lost"));

			onCompletion(player);
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

			broadcastManager
					.sendBroadcast(new Intent(PlayerEvents.FILTER_PLAYER_EVENT)
							.putExtra(PlayerEvents.EXTRA_STATE, false)
							.putExtra(PlayerEvents.EXTRA_MESSAGE,
									"Audio focus lost temporarily"));

			pause();
			break;
		}

	}

	// Play a new track
	private void playTrack(Uri track) {

		if (player == null) {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnPreparedListener(this);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			player.setOnInfoListener(this);
			player.setWakeMode(getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);

		} else {
			player.reset();
		}

		try {
			player.setDataSource(getApplicationContext(), track);
			player.prepareAsync();
			Log.d("Player", "Preparing track " + track);
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			Log.e("Player", e.getMessage());
		}
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

	/**
	 * Try to pause the current track.
	 *
	 * @return whether the track was successfully paused.
	 */
	public boolean pause() {
		return pause(null);
	}

	/*
	 * Pause the current track, and broadcast an event advertising the event.
	 */
	private boolean pause(String reason) {
		if (player != null && player.isPlaying()) {
			player.pause();

			stopForeground(true);
			audioManager.abandonAudioFocus(this);

			Log.d("Player", "paused");
		}

		Intent broadcast = new Intent(PlayerEvents.FILTER_PLAYER_EVENT)
				.putExtra(PlayerEvents.EXTRA_STATE, false);
		if (reason != null) {
			broadcast.putExtra(PlayerEvents.EXTRA_MESSAGE, reason);
		}
		broadcastManager.sendBroadcast(broadcast);

		return true;
	}

	/**
	 * Try to play the current track.
	 *
	 * @return whether the track is now playing
	 */
	public boolean play() {
		if (player == null) {
			Log.w("Player", "Can't play: no player");
			return false;
		}

		boolean focus = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) ==
			AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

		Intent broadcast = new
			Intent(PlayerEvents.FILTER_PLAYER_EVENT).putExtra(PlayerEvents.EXTRA_STATE,
					focus);

		if (focus) {
			broadcast.putExtra(PlayerEvents.EXTRA_MESSAGE, "Could not obtain audio focus");
		} else {
			player.start();
			startForeground(NOTIFICATION_ID, notificationBase.build());

			Log.d("Player", "started");
		}

		broadcastManager.sendBroadcast(broadcast);

		return focus;
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

		broadcastManager.sendBroadcast(new Intent(
				PlayerEvents.FILTER_PLAYER_EVENT).putExtra(
				PlayerEvents.EXTRA_STATE, player.isPlaying()));
	}

}
