package io.github.patrickconley.arbutus.settings.view;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.codekidlabs.storagechooser.StorageChooser;

public class LibraryPathChooserActivity extends AppCompatActivity
        implements StorageChooser.OnSelectListener, StorageChooser.OnCancelListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StorageChooser chooser = new StorageChooser.Builder().withActivity(this)
                .withFragmentManager(getFragmentManager())
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .filter(StorageChooser.FileType.AUDIO)
                .build();

        chooser.setOnSelectListener(this);
        chooser.setOnCancelListener(this);
        chooser.show();
    }

    @Override
    public void onSelect(String path) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString("library path", path)
                .apply();
        finish();
    }

    @Override
    public void onCancel() {
        finish();
    }
}
