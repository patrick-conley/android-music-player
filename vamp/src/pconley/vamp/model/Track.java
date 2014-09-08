package pconley.vamp.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A piece of music, and its metadata.
 */
public class Track {

	private long id;
	private String uri;
	private Map<String, Set<Tag>> tags;

	/* Private constructor. Use the builder. */
	private Track(long id, String uri, Map<String, Set<Tag>> tags) {
		this.id = id;
		this.uri = uri;
		this.tags = tags;
	}

	public long getId() {
		return id;
	}

	public String getUri() {
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
		// Check if the key exists, as unmodifiableSet doesn't accept null input
		if (!tags.containsKey(name)) {
			return null;
		}

		return Collections.unmodifiableSet(tags.get(name));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(uri).append("\n");

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

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
		private String uri;
		private Map<String, Set<Tag>> tags;

		public Builder(long id, String uri) {
			this.id = id;
			this.uri = uri;

			tags = new HashMap<String, Set<Tag>>();
		}

		public void add(Tag tag) {
			String name = tag.getName();

			if (!tags.containsKey(name)) {
				tags.put(name, new HashSet<Tag>());
			}

			tags.get(name).add(tag);
		}

		public Track build() {
			return new Track(id, uri, tags);
		}
	}
}
