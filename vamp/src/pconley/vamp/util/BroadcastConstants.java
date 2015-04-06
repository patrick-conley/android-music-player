package pconley.vamp.util;

import pconley.vamp.player.PlayerEvent;
import pconley.vamp.player.PlayerService;
import pconley.vamp.scanner.ScannerService;

public final class BroadcastConstants {

	/**
	 * Filter for broadcast receivers: messages from {@link ScannerService}.
	 */
	public static final String FILTER_SCANNER = "pconley.vamp.broadcast.scanner";

	/**
	 * Filter for broadcast receivers: messages from {@link PlayerService}.
	 */
	public static final String FILTER_PLAYER_EVENT = "pconley.vamp.broadcast.player.event";

	/**
	 * Extra used in broadcasts about player state changes. See
	 * {@link PlayerEvent} for possible values. If the event is an unexpected
	 * PAUSE or STOP, the reason will be in the extra {@link #EXTRA_MESSAGE}.
	 */
	public static final String EXTRA_EVENT = "pconley.vamp.broadcast.player.event.type";

	/**
	 * String explanation for an event from either the scanner or player.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.broadcast.player.event.message";

	/**
	 * Private constructor.
	 */
	private BroadcastConstants() {

	}

}
