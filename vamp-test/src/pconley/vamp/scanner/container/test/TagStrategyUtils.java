package pconley.vamp.scanner.container.test;

import java.util.List;

import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
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
	public static Track buildTrack(Uri uri, List<Tag> tags) {
		Track.Builder builder = new Track.Builder(0, uri);

		for (Tag tag : tags) {
			builder.add(tag);
		}

		return builder.build();
	}

}