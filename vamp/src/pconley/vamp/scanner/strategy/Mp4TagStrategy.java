package pconley.vamp.scanner.strategy;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;

public class Mp4TagStrategy implements TagStrategy {

	private Map<String, String> metadataKeys = null;

	public Mp4TagStrategy() {
		buildKeyMap();
	}

	@Override
	public Map<String, List<String>> getTags(File file) throws Exception {
		Map<String, List<String>> tags = new HashMap<String, List<String>>();

		Iterator<TagField> tagIter = AudioFileIO.read(file).getTag()
				.getFields();
		while (tagIter.hasNext()) {
			TagField tag = tagIter.next();

			String key;
			if (metadataKeys.containsKey(tag.getId())) {
				key = metadataKeys.get(tag.getId());
			} else {
				key = tag.getId();
			}

			List<String> values = tags.get(key);
			if (values == null) {
				values = new LinkedList<String>();
				tags.put(key, values);
			}

			values.add(tag.toString());

		}
		return tags;
	}

	/*
	 * Reverse JAudioTagger's mapping between field IDs and names.
	 */
	private void buildKeyMap() {
		metadataKeys = new HashMap<String, String>();

		for (Mp4FieldKey key : Mp4FieldKey.values()) {
			metadataKeys.put(key.getFieldName(),
					key.toString().toLowerCase(Locale.US));
		}

		// Replace some keys with my standard names
		metadataKeys.put(Mp4FieldKey.GENRE_CUSTOM.getFieldName(), "genre");
		metadataKeys.put(Mp4FieldKey.TRACK.getFieldName(), "tracknumber");
		metadataKeys.put(Mp4FieldKey.DAY.getFieldName(), "date");
	}

}
