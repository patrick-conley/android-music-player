package io.github.patrickconley.arbutus.settings.view;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class LibraryScannerService extends IntentService {
    // might eventually add another action to refresh the library
    private static final String ACTION_SCAN_LIBRARY = "io.github.patrickconley.arbutus.settings.view.action.SCAN_LIBRARY";
    private static final String LIBRARY_PATH = "io.github.patrickconley.arbutus.settings.view.extra.LIBRARY_PATH";

    public LibraryScannerService() {
        super("LibraryScannerService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFoo(Context context, String libraryPath) {
        Intent intent = new Intent(context, LibraryScannerService.class);
        intent.setAction(ACTION_SCAN_LIBRARY);
        intent.putExtra(LIBRARY_PATH, libraryPath);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCAN_LIBRARY.equals(action)) {
                final String libraryPath = intent.getStringExtra(LIBRARY_PATH);
                handleScanLibrary(libraryPath);
            }
        }
    }

    /**
     * Handle action ACTION_SCAN_LIBRARY in the provided background thread with the provided
     * parameters.
     */
    private void handleScanLibrary(String libraryPath) {
        throw new UnsupportedOperationException("Not yet implemented: " + libraryPath);
    }

}
