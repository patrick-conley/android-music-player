package io.github.patrickconley.arbutus.scanner.model.impl;

import android.util.Log;

import java.io.File;

import io.github.patrickconley.arbutus.scanner.model.MediaFileBase;
import io.github.patrickconley.arbutus.scanner.visitor.MediaVisitor;

public class MediaFile extends MediaFileBase {
    private final String tag = getClass().getName();

    public MediaFile(File file) {
        super(file);
    }

    /**
     * Validate the file (it must be readable), then visit it.
     */
    @Override
    public long accept(MediaVisitor visitor) {

        Log.d(tag, "Scanning file " + getFile().toString());

        if (!getFile().exists() || !getFile().canRead()) {
            return 0L;
        }

        visitor.visit(this);

        return 1L;
    }

}
