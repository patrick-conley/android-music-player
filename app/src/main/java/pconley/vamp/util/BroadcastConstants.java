package pconley.vamp.util;

import pconley.vamp.player.PlayerEvent;
import pconley.vamp.player.PlayerService;
import pconley.vamp.scanner.ScannerEvent;
import pconley.vamp.scanner.ScannerService;
import pconley.vamp.scanner.filesystem.FileScanVisitor;

public final class BroadcastConstants {

	/**
	 * Filter for broadcast receivers: messages from {@link ScannerService}.
	 */
	public static final String FILTER_SCANNER
			= "pconley.vamp.broadcast.scanner";

	/**
	 * Filter for broadcast receivers: messages from {@link PlayerService}.
	 */
	public static final String FILTER_PLAYER_EVENT
			= "pconley.vamp.broadcast.player.event";

	/**
	 * Extra describing the event that triggered the broadcast. It contains an
	 * enum value from {@link PlayerEvent} or {@link ScannerEvent}
	 */
	public static final String EXTRA_EVENT
			= "pconley.vamp.broadcast.player.event.type";

	/**
	 * String explanation for an event from either the scanner or player.
	 */
	public static final String EXTRA_MESSAGE = "pconley.vamp.broadcast" +
	                                           ".message";

	/**
	 * Number of files scanned so far by {@link FileScanVisitor}. If either
	 * this or {@link #EXTRA_TOTAL} is set, then a broadcast with
	 * FILTER_SCANNER is assumed to be an update. Otherwise a broadcast
	 * announces the scanner is finished. {@link #EXTRA_MESSAGE} might be used
	 * for information about the current file.
	 */
	public static final String EXTRA_PROGRESS
			= "pconley.vamp.broadcast.scanner.progress";

	/**
	 * Estimated total number of files and directories to be scanned by
	 * {@link FileScanVisitor}. See {@link #EXTRA_PROGRESS}.
	 */
	public static final String EXTRA_TOTAL
			= "pconley.vamp.broadcast.scanner.max";

	/**
	 * Private constructor.
	 */
	private BroadcastConstants() {

	}

}
