package pconley.vamp.scanner;

import pconley.vamp.util.BroadcastConstants;

public enum ScannerEvent {

	/**
	 * The scan is finished. EXTRA_MESSAGE has the scan status.
	 */
	FINISHED,

	/**
	 * A new file or directory has been scanned.
	 * {@link BroadcastConstants#EXTRA_PROGRESS} or
	 * {@link BroadcastConstants#EXTRA_MAX} may have updated values;
	 * {@link BroadcastConstants#EXTRA_MESSAGE} may indicate the directory being
	 * scanned.
	 */
	UPDATE

}
