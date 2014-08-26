package pconley.vamp.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A piece of music, and its metadata.
 */
public class Track {

	private int id;
	private String uri;
	private Map<String, Tag> tags;

	public Track(String uri) {
		this(-1, uri);
	}

	public Track(int id, String uri) {
		this.id = id;
		this.uri = uri;
		tags = new HashMap<String, Tag>();
	}


	public int getId() {
		return id;
	}

	public String getUri() {
		return uri;
	}

	/**
	 * @return The track's metadata, identified by tag name
	 */
	public Map<String, Tag> getTags() {
		return tags;
	}

	/**
	 * Add a tag to the track. The track should not have already loaded a tag
	 * with the same name.
	 *
	 * @return Whether the tag was added
	 */
	public boolean addTag(Tag tag) {
		return tags.put(tag.getName(), tag) == null;
	}

	@Override
	public String toString() {
		return "track " + uri + ": " + tags;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
