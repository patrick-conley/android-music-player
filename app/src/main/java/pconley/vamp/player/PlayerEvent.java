package pconley.vamp.player;

import pconley.vamp.util.BroadcastConstants;

/**
 * Events that can be broadcast from the player to clients within the app.
 * <p/>
 * Retrieve events with
 * {@code (PlayerEvent) intent.getSerializableExtra(EXTRA_EVENT)}.
 *
 * @author pconley
 */
public enum PlayerEvent {

	/**
	 * A new track has been loaded. It may or may not be playing.
	 */
	NEW_TRACK,

	/**
	 * The player is playing a track.
	 */
	PLAY,

	/**
	 * The current track has been paused. If it wasn't paused by a user
	 * {@link BroadcastConstants#EXTRA_MESSAGE} will have an explanation.
	 */
	PAUSE,

	/**
	 * The player has finished all tracks or encountered an error. If the
	 * latter, {@link BroadcastConstants#EXTRA_MESSAGE} will have an
	 * explanation.
	 */
	STOP

}
