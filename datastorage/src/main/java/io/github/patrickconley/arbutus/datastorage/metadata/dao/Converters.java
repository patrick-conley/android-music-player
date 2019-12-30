package io.github.patrickconley.arbutus.datastorage.metadata.dao;

import android.net.Uri;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public Uri toUri(String uriString) {
        return Uri.parse(uriString);
    }

    @TypeConverter
    public String fromUri(Uri uri) {
        return uri.toString();
    }
}
