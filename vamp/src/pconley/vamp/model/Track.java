package pconley.vamp.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A piece of music, and its metadata.
 */
public class Track {

	private long id;
	private String uri;
	private Set<Tag> tags;

	/* Private constructor. Use the builder. */
	private Track(long id, String uri, Set<Tag> tags) {
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

	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	@Override
	public String toString() {
		return uri + ": " + tags;
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

	public static class Builder {

		private long id;
		private String uri;
		private Set<Tag> tags;

		public Builder(long id, String uri) {
			this.id = id;
			this.uri = uri;

			tags = new HashSet<Tag>();
		}

		public void addTag(Tag tag) {
			this.tags.add(tag);
		}

		public Track build() {
			return new Track(id, uri, tags);
		}
	}
}
