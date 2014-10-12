package pconley.vamp.player;

import java.io.IOException;

import pconley.vamp.PlayerActivity;
import pconley.vamp.R;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

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
	public static final String ACTION_PAUSE =
		"pconley.vamp.PlayerService.pause";

	/**
	 * Action for incoming intents. Play/pause the current track. Does nothing
	 * if there is no track in progress.
	 */
	public static final String ACTION_PLAY_PAUSE = "pconley.vamp.playerService.playPause";

	/**
	 * Action for incoming intents. Seek within the current track. Does nothing
	 * if there is no track in progress.
	 */
	public static final String ACTION_SEEK = "pconley.vamp.playerService.seek";
	public static final String EXTRA_SEEK_POSITION = "pconley.vamp.playerService.seek.time";

	/**
	 * Broadcast filter used by messages for this receiver
	 */
	public static final String FILTER_PLAYER_STATUS = "pconley.vamp.player.status";

	private IBinder binder;
	private AudioManager audioManager;
	private MediaPlayer player = null;

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

			case ACTION_PLAY_PAUSE:
				playPause();
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
				"Error " + String.valueOf(what) + ":" + String.valueOf(extra));
		// FIXME: broadcasts do little if we're between activities
		sendBroadcast(new Intent(FILTER_PLAYER_STATUS).putExtra(
				StatusReceiver.EXTRA_STATUS_TYPE, StatusReceiver.STATUS_ERROR)
				.putExtra(StatusReceiver.EXTRA_STATUS_CODE, extra));

		// Return false to call onCompletion
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.e("Player",
				"Info " + String.valueOf(what) + ":" + String.valueOf(extra));
		sendBroadcast(new Intent(FILTER_PLAYER_STATUS).putExtra(
				StatusReceiver.EXTRA_STATUS_TYPE, StatusReceiver.STATUS_INFO)
				.putExtra(StatusReceiver.EXTRA_STATUS_CODE, extra));

		return true;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			play();
			break;
		case AudioManager.AUDIOFOCUS_LOSS:

			sendBroadcast(new Intent(FILTER_PLAYER_STATUS).putExtra(
					StatusReceiver.EXTRA_STATUS_TYPE,
					StatusReceiver.STATUS_MISC).putExtra(
					StatusReceiver.EXTRA_STATUS, "Audio focus lost"));

			onCompletion(player);
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

			sendBroadcast(new Intent(FILTER_PLAYER_STATUS).putExtra(
					StatusReceiver.EXTRA_STATUS_TYPE,
					StatusReceiver.STATUS_MISC)
					.putExtra(StatusReceiver.EXTRA_STATUS,
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
		} catch (IllegalStateException e) {
			Log.e("Player", e.getMessage());
		} catch (IllegalArgumentException | IOException e) {
			Log.e("Player", "Invalid track " + track);
		} catch (SecurityException e) {
			e.printStackTrace();
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
	 * Pause or unpause the current track.
	 *
	 * @return whether the player is now playing
	 */
	public boolean playPause() {
		if (player == null) {
			Log.w("Player", "Can't play/pause: no player");
			return false;
		} else if (player.isPlaying()) {
			return !pause();

		} else {
			return play();
		}
	}

	/*
	 * Try to pause the current track.
	 *
	 * @return whether the track was successfully paused.
	 */
	private boolean pause() {
		if (player == null || !player.isPlaying()) {
			return false;
		}

		player.pause();
		stopForeground(true);

		audioManager.abandonAudioFocus(this);

		Log.d("Player", "paused");

		return true;
	}

	/*
	 * Try to play the current track.
	 *
	 * @return whether the track is now playing
	 */
	private boolean play() {
		if (player == null) {
			return false;
		}

		int focus = audioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (focus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

			sendBroadcast(new Intent(FILTER_PLAYER_STATUS).putExtra(
					StatusReceiver.EXTRA_STATUS_TYPE,
					StatusReceiver.STATUS_ERROR)
					.putExtra(StatusReceiver.EXTRA_STATUS,
							"Could not obtain audio focus"));

			return false;
		} else {

			player.start();
			startForeground(NOTIFICATION_ID, notificationBase.build());

			Log.d("Player", "started");

			return true;
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
		if (player == null) {
			return;
		}

		player.seekTo(time);
	}

	/**
	 * Receiver for general messages sent by the player service. The message is
	 * just displayed as a toast.
	 *
	 * @author pconley
	 */
	public static class StatusReceiver extends BroadcastReceiver {

		/**
		 * Type of the message. Should be one of STATUS_ERROR, STATUS_INFO, or
		 * STATUS_MISC. The first two types need a code (the `extra` provided by
		 * the MediaPlayer.OnErrorListener); the final one needs a message.
		 */
		public static final String EXTRA_STATUS_TYPE = "pconley.vamp.player.status.type";
		public static final int STATUS_MISC = 0;
		public static final int STATUS_ERROR = 1;
		public static final int STATUS_INFO = 2;

		/**
		 * Error/info code.
		 */
		public static final String EXTRA_STATUS_CODE = "pconley.vamp.player.status.code";

		/**
		 * Text of a miscellaneous status message.
		 */
		public static final String EXTRA_STATUS = "pconley.vamp.player.status";

		private Activity activity;

		public StatusReceiver(Activity activity) {
			super();

			this.activity = activity;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(EXTRA_STATUS_TYPE, STATUS_MISC);
			int code = intent.getIntExtra(EXTRA_STATUS_CODE, 0);

			String text;
			switch (type) {
			case STATUS_ERROR:
				text = "Error: " + String.valueOf(code);
				break;
			case STATUS_INFO:
				text = "Info: " + String.valueOf(code);
				break;
			default:
				text = intent.getStringExtra(EXTRA_STATUS);
				break;

			}

			Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
		}
	}

}
