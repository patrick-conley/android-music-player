package pconley.vamp.player;

import pconley.vamp.library.db.TrackDAO;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

/**
 * Singleton injector for dependencies that may need to be mocked during unit
 * testing.
 * 
 * @author pconley
 */
public class PlayerFactory {

	private PlayerService service;

	private static PlayerFactory instance = null;

	private PlayerFactory() {
	}

	/**
	 * Get the current instance of the singleton.
	 * 
	 * @param service
	 *            Player running this factory
	 * @return
	 */
	public static PlayerFactory getInstance(PlayerService service) {
		if (instance == null) {
			instance = new PlayerFactory();
		}
		instance.service = service;
		return instance;
	}

	/**
	 * Set the current instance of the singleton. Use this to replace a live
	 * factory with a mock.
	 * 
	 * @param factory
	 */
	public static void setInstance(PlayerFactory factory) {
		PlayerFactory.instance = factory;
	}

	/**
	 * Remove a mocked instance of the factory.
	 */
	public static void resetInstance() {
		instance = new PlayerFactory();
	}

	/**
	 * @return A configured {@link MediaPlayer}
	 */
	public MediaPlayer createMediaPlayer() {
		MediaPlayer player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(service);
		player.setOnErrorListener(service);
		player.setWakeMode(service.getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		return player;
	}

	public TrackDAO createDAO() {
		return new TrackDAO(service.getApplicationContext());
	}

}
