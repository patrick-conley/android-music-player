package io.github.patrickconley.arbutus.library.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.github.patrickconley.arbutus.datastorage.AppDatabase;
import io.github.patrickconley.arbutus.datastorage.library.dao.LibraryEntryDao;
import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;

public class LibraryEntryViewModel extends ViewModel {

    private LiveData<List<LibraryEntryText>> entries;

    public LiveData<List<LibraryEntryText>> getEntries(Context context) {
        if (entries == null) {
            entries = loadEntries(context);
        }
        return entries;
    }

    private LiveData<List<LibraryEntryText>> loadEntries(final Context context) {
        LibraryEntryDao libraryEntryDao = AppDatabase.getInstance(context).libraryEntryDao();
        return libraryEntryDao.getChildrenOf((LibraryEntryText) null);
    }
}
