package pconley.vamp.library.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.net.Uri;

/**
 * The URI of a track of music, and the metadata associated with the destination
 * file.
 * 
 * @author pconley
 */
public final class Track {

	private long id;
	private Uri uri;
	private Map<String, Set<Tag>> tags;

	/* Private constructor. Use the builder. */
	private Track(long id, Uri uri, Map<String, Set<Tag>> tags) {
		this.id = id;
		this.uri = uri;
		this.tags = tags;
	}

	/**
	 * @return The track ID. Used internally.
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return The path to the track.
	 */
	public Uri getUri() {
		return uri;
	}

	/**
	 * @return The names of tags used by this track.
	 */
	public Set<String> getTagNames() {
		return Collections.unmodifiableSet(tags.keySet());
	}

	/**
	 * @param name
	 *            The name of a tag
	 * @return The tags corresponding to this tag name, or null if the tag isn't
	 *         in the this track. The values' ordering is in no way guaranteed.
	 */
	public Set<Tag> getTags(String name) {
		// Check if the key exists: unmodifiableSet doesn't accept null input
		if (!tags.containsKey(name)) {
			return null;
		} else {
			return Collections.unmodifiableSet(tags.get(name));
		}
	}

	/**
	 * @return A formatted string representation of the tags.
	 */
	public String tagsToString() {
		StringBuilder sb = new StringBuilder();

		List<String> tagNames = new LinkedList<String>(tags.keySet());
		Collections.sort(tagNames);

		for (String name : tagNames) {
			sb.append("\t").append(name).append(":\n");

			for (Tag tag : tags.get(name)) {
				sb.append("\t\t").append(tag.getValue()).append("\n");
			}
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(uri).append("\n").append(tagsToString());

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/**
	 * Compare this instance with the specified object. Assuming both objects
	 * are non-null Tracks, they are equal if they have the same URI and
	 * tags.
	 * 
	 * @see Tag#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Track other = (Track) obj;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	/**
	 * Builder class for a track. Gives a simple means of adding tags from a
	 * Cursor without making the track mutable.
	 */
	public static class Builder {

		private long id;
		private Uri uri;
		private Map<String, Set<Tag>> tags;

		public Builder(long id, Uri uri) {
			this.id = id;
			this.uri = uri;

			tags = new HashMap<String, Set<Tag>>();
		}

		public Builder add(Tag tag) {
			String name = tag.getName();

			if (!tags.containsKey(name)) {
				tags.put(name, new HashSet<Tag>());
			}

			tags.get(name).add(tag);
			
			return this;
		}

		public Track build() {
			return new Track(id, uri, tags);
		}
	}
}
