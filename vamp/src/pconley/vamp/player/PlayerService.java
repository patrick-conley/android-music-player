package pconley.vamp.player;

import java.io.File;
import java.io.IOException;

import pconley.vamp.PlayerActivity;
import pconley.vamp.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class PlayerService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

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

	public static final String FILTER_PLAYER_WARNINGS = "pconley.vamp.playerService.warnings";
	public static final String EXTRA_WARNING = "pconley.vamp.playerService.warnings.warning";
	public static final int WARNING_MISSING_TRACK = 1;

	private MediaPlayer player = null;

	private final IBinder binder = new PlayerBinder();

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
		playPause();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopForeground(true);

		player.release();
		player = null;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("Player", "Error " + String.valueOf(what));

		player.release();
		player = null;

		return true;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.w("Player", "Info " + String.valueOf(what));
		return false;
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

		// Check for the track
		if (!new File(track.getPath()).exists()) {
			Log.e("Player", "Missing track");
			sendBroadcast(new Intent(FILTER_PLAYER_WARNINGS).setData(track)
					.putExtra(EXTRA_WARNING, WARNING_MISSING_TRACK));
			return;
		}

		try {
			player.setDataSource(getApplicationContext(), track);
			player.prepareAsync();
			Log.d("Player", "Preparing track" + track);
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
		}

		if (player.isPlaying()) {
			player.pause();
			stopForeground(true);

			Log.d("Player", "paused");

			return false;
		} else {

			Notification notification = new Notification.Builder(
					getApplicationContext())
					.setContentTitle(getString(R.string.app_name))
					.setContentText("Now playing...")
					.setSmallIcon(android.R.drawable.ic_media_play)
					.setOngoing(true)
					.setLargeIcon(
							BitmapFactory.decodeResource(this.getResources(),
									android.R.drawable.ic_media_play))
					.setContentIntent(
							PendingIntent.getActivity(getApplicationContext(),
									0, new Intent(getApplicationContext(),
											PlayerActivity.class),
									PendingIntent.FLAG_UPDATE_CURRENT)).build();

			notification.icon = android.R.drawable.ic_media_play;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;

			player.start();
			startForeground(NOTIFICATION_ID, notification);

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

}
