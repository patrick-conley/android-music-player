package io.github.patrickconley.arbutus.scanner.view;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.scanner.visitor.impl.FileScanVisitor;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in a service on a
 * separate handler thread.
 */
public class LibraryScannerService extends IntentService {

    // I might eventually add another action to refresh the library
    private static final String ACTION_SCAN_LIBRARY =
            "io.github.patrickconley.arbutus.settings.view.action.SCAN_LIBRARY";
    private static final String LIBRARY_PATH =
            "io.github.patrickconley.arbutus.settings.view.extra.LIBRARY_PATH";

    public LibraryScannerService() {
        super("LibraryScannerService");
    }

    /**
     * Starts this service to scan the library. If the service is already performing a task this
     * action will be queued.
     *
     * @see IntentService
     */
    public static void startActionScanLibrary(Context context, String libraryPath) {
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
                truncateDatabase();
                FileScanVisitor.execute(this, new File(intent.getStringExtra(LIBRARY_PATH)));
            }
        }
    }

    private void truncateDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        db.tagInTrackDao().truncate();
        db.tagDao().truncate();
        db.trackDao().truncate();
    }

}
