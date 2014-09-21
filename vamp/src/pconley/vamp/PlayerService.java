package pconley.vamp;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

	/**
	 * Intent action. Start playing a new track. Specify the track with the
	 * Intent's data.
	 */
	public static final String ACTION_PLAY = "pconley.vamp.PlayerService.play";

	/**
	 * Intent action. Play/pause the current track. Does nothing if there is no
	 * track in progress.
	 */
	public static final String ACTION_PLAY_PAUSE = "pconley.vamp.playerService.playPause";

	private MediaPlayer player = null;

	/**
	 * Unimplemented.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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

		default:
			Log.w("Player", "Invalid action " + intent.getAction());
			break;
		}

		return Service.START_STICKY;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		player.start();
		Log.d("Player", "started");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
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
		}
		if (player != null && player.isPlaying()) {
			player.pause();
			Log.d("Player", "paused");
		} else if (player != null) {
			player.start();
			Log.d("Player", "started");
		}
	}

}
