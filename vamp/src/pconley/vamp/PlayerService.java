package pconley.vamp;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PlayerService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

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

	/**
	 * Intent filter for outgoing broadcasts. Current track's metadata,
	 * play/pause state, and elapsed/total time.
	 */
	public static final String FILTER_PROGRESS = "pconley.vamp.playerService.progress";

	/**
	 * In outgoing broadcasts, what's changed about the player's state. Done
	 * state is only sent if there are no more tracks queued.
	 */
	public static final String EXTRA_PROGRESS_STATE = "pconley.vamp.playerService.progress.state";
	public static final int PROGRESS_STATE_PAUSED = -1;
	public static final int PROGRESS_STATE_PLAYING = 1;
	public static final int PROGRESS_STATE_DONE = 0;

	/**
	 * In outgoing broadcasts, the current track's current position.
	 */
	public static final String EXTRA_PROGRESS_POSITION = "pconley.vamp.playerService.progress.position";

	/**
	 * In outgoing broadcasts, the current track's duration.
	 */
	public static final String EXTRA_PROGRESS_DURATION = "pconley.vamp.playerService.progress.duration";

	private MediaPlayer player = null;

	/**
	 * Unimplemented.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
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

		return Service.START_STICKY;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		playPause();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Intent intent = new Intent(FILTER_PROGRESS).putExtra(
				EXTRA_PROGRESS_STATE, PROGRESS_STATE_DONE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

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

		if (player != null) {
			player.release();
		}

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnInfoListener(this);

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

	// (Un)pause the current track
	private void playPause() {
		if (player == null) {
			Log.w("Player", "Can't play/pause: no player");
			return;
		}

		Intent broadcastIntent = new Intent(FILTER_PROGRESS).putExtra(
				EXTRA_PROGRESS_POSITION, player.getCurrentPosition()).putExtra(
				EXTRA_PROGRESS_DURATION, player.getDuration());

		if (player.isPlaying()) {
			player.pause();
			broadcastIntent.putExtra(EXTRA_PROGRESS_STATE,
					PROGRESS_STATE_PAUSED);
			Log.d("Player", "paused");
		} else {
			player.start();
			broadcastIntent.putExtra(EXTRA_PROGRESS_STATE,
					PROGRESS_STATE_PLAYING);
			Log.d("Player", "started");
		}

		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

	}

	// Seek within the current track. Works in either play/pause states.
	private void seekTo(int time) {
		if (player == null) {
			return;
		}

		player.seekTo(time);

		// Broadcast the current position to reset the timer
		Intent broadcastIntent = new Intent(FILTER_PROGRESS)
				.putExtra(EXTRA_PROGRESS_POSITION, player.getCurrentPosition())
				.putExtra(EXTRA_PROGRESS_DURATION, player.getDuration())
				.putExtra(
						EXTRA_PROGRESS_STATE,
						player.isPlaying() ? PROGRESS_STATE_PLAYING
								: PROGRESS_STATE_PAUSED);

		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

	}

}
