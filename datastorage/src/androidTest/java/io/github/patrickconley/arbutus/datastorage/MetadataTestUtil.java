package io.github.patrickconley.arbutus.datastorage;

import java.util.HashMap;
import java.util.Map;

import io.github.patrickconley.arbutus.datastorage.metadata.model.Tag;

public class MetadataTestUtil {

    public Map<String, Tag> buildTagMap(Tag... tags) {
        Map<String, Tag> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag);
        }

        return map;
    }

}
