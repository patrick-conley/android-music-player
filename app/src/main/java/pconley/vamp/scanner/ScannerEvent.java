package pconley.vamp.scanner;

import pconley.vamp.util.BroadcastConstants;

public enum ScannerEvent {

	/**
	 * The scan is finished. {@link BroadcastConstants#EXTRA_MESSAGE} has the
	 * scan status.
	 */
	FINISHED,

	/**
	 * A new file or directory has been scanned.
	 * {@link BroadcastConstants#EXTRA_PROGRESS} or
	 * {@link BroadcastConstants#EXTRA_TOTAL} may have updated values;
	 * {@link BroadcastConstants#EXTRA_MESSAGE} may indicate the directory
	 * being
	 * scanned.
	 */
	UPDATE,

	/**
	 * Something went wrong, but not fatally.
	 * {@link BroadcastConstants#EXTRA_MESSAGE} holds an explanation.
	 */
	ERROR

}
