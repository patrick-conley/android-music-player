package io.github.patrickconley.arbutus.scanner.view;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

import io.github.patrickconley.arbutus.domain.AppDatabase;
import io.github.patrickconley.arbutus.scanner.visitor.impl.FileScanVisitor;
import io.github.patrickconley.arbutus.scanner.model.impl.MediaFolder;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class LibraryScannerService extends IntentService {
    private final String tag = getClass().getName();

    // might eventually add another action to refresh the library
    private static final String ACTION_SCAN_LIBRARY
            = "io.github.patrickconley.arbutus.settings.view.action.SCAN_LIBRARY";
    private static final String LIBRARY_PATH
            = "io.github.patrickconley.arbutus.settings.view.extra.LIBRARY_PATH";

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
                truncateDatabase();
                handleScanLibrary(intent.getStringExtra(LIBRARY_PATH));
            }
        }
    }

    private void truncateDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        db.trackTagDAO().truncate();
        db.tagDao().truncate();
        db.trackDao().truncate();
    }

    /**
     * Handle action ACTION_SCAN_LIBRARY in the provided background thread with the provided
     * parameters.
     */
    private void handleScanLibrary(String libraryPath) {
        MediaFolder library = new MediaFolder(new File(libraryPath));

        FileScanVisitor visitor = new FileScanVisitor(this);
        long fileCount = library.accept(visitor);
        visitor.close();

        Log.i(tag, "Scanned " + fileCount + " files");
    }

}
