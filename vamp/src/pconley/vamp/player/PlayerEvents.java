package pconley.vamp.player;

/**
 * List of Intent data associated with events from the music player.
 *
 * @author pconley
 */
public class PlayerEvents {

	/**
	 * Broadcast filter used by messages for this player's status receiver
	 */
	public static final String FILTER_PLAYER_EVENT = "pconley.vamp.player.event";

	/**
	 * Whether the player is now playing. If the player was stopped
	 * unexpectedly, EXTRA_MESSAGE will contain an explanation.
	 */
	public static final String EXTRA_STATE = "pconley.vamp.player.event.state";

	/**
	 * Message giving a reason/explanation of the event.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.player.event.message";

	private PlayerEvents() {
	}

}
