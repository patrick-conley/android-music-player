package pconley.vamp.player;

/**
 * Events that can be broadcast from the player to clients within the app.
 * 
 * Retrieve events with
 * {@code (PlayerEvent) intent.getSerializableExtra(EXTRA_EVENT)}.
 * 
 * @author pconley
 *
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
	 * EXTRA_MESSAGE will have an explanation.
	 */
	PAUSE,

	/**
	 * The player has finished all tracks or encountered an error. If the
	 * latter, EXTRA_MESSAGE will have an explanation.
	 */
	STOP;

	/**
	 * Filter for broadcast receivers.
	 */
	public static final String FILTER_PLAYER_EVENT = "pconley.vamp.player.event";

	/**
	 * Extra used in broadcasts about player state changes. Value is one of
	 * NEW_TRACK, PLAY, PAUSE, or STOP.
	 */
	public static final String EXTRA_EVENT = "pconley.vamp.player.event";

	/**
	 * Reason for an unexpected stop/pause event. Probably given as an
	 * unreadable error code.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.player.event.message";

}
