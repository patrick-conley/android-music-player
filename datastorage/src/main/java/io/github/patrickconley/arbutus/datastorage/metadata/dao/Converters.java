package io.github.patrickconley.arbutus.datastorage.metadata.dao;

import androidx.room.TypeConverter;
import android.net.Uri;

public class Converters {

    @TypeConverter
    public Uri toUri(String string) {
        return Uri.parse(string);
    }

    @TypeConverter
    public String fromUri(Uri uri) {
        return uri.toString();
    }
}
