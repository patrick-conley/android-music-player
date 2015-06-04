package pconley.vamp.scanner.strategy.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pconley.vamp.library.model.Tag;
import pconley.vamp.library.model.Track;
import android.net.Uri;

public class TagStrategyUtils {

	private TagStrategyUtils() {
	}

	/**
	 * Build a track out of a raw map of its tags.
	 * 
	 * @param uri
	 * @param tags
	 * @return
	 */
	public static Track buildTrack(Uri uri, Map<String, List<String>> tags) {
		Track.Builder builder = new Track.Builder(0, uri);

		for (Entry<String, List<String>> entry : tags.entrySet()) {
			for (String value : entry.getValue()) {
				builder.add(new Tag(0, entry.getKey(), value));
			}
		}

		return builder.build();
	}

}
