package pconley.vamp.persistence.util;

import java.text.MessageFormat;

import pconley.vamp.persistence.LibrarySchema.TrackTagRelation;

public class TrackUtil {

	private TrackUtil() { }

	/**
	 * Build a table/subquery to return the track IDs matching a set of tags, by
	 * ID. The tag IDs must be inserted as selection args when the query is
	 * executed.
	 *
	 * @param nTags
	 * 		Number of tags to match
	 * @return An SQL query giving track IDs
	 */
	public static String buildMatchingTracksQuery(int nTags) {
		if (nTags <= 0) {
			return TrackTagRelation.NAME;
		}

		String tracksWithTag = MessageFormat
				.format("(SELECT {1} FROM {0} WHERE {2} = ?)",
				        TrackTagRelation.NAME,
				        TrackTagRelation.TRACK_ID,
				        TrackTagRelation.TAG_ID);

		// (SELECT trackId FROM TrackHasTags WHERE tagId = ?)
		// NATURAL JOIN ([as above]) [repeat as needed]
		StringBuilder query = new StringBuilder(tracksWithTag);
		for (int i = 1; i < nTags; i++) {
			query.append(" NATURAL JOIN ").append(tracksWithTag);
		}

		// INNER JOIN TrackHasTags USING trackId
		query.append(MessageFormat
				             .format(" INNER JOIN {0} USING ({1})",
				                     TrackTagRelation.NAME,
				                     TrackTagRelation.TRACK_ID));

		return query.toString();
	}

}
