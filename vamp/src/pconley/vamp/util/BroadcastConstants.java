package pconley.vamp.util;

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
	 * Extra used in broadcasts about player state changes. Value is one of
	 * NEW_TRACK, PLAY, PAUSE, or STOP.
	 */
	public static final String EXTRA_EVENT = "pconley.vamp.broadcast.player.event.type";

	/**
	 * Reason for an unexpected player stop/pause event. Probably given as an
	 * unreadable error code.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.broadcast.player.event.message";

	/**
	 * Private constructor.
	 */
	private BroadcastConstants() {

	}

}
