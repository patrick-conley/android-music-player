package pconley.vamp.persistence.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The URI of a track of music, and the metadata associated with the destination
 * file.
 *
 * @author pconley
 */
public final class Track implements LibraryItem {

	private long id;
	private Uri uri;
	private Set<Tag> tags;
	private Map<String, ArrayList<Tag>> tagsByName;

	/* Private constructor. Use the builder. */
	private Track(long id, Uri uri, Set<Tag> tags) {
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
	@NonNull
	public Set<String> getTagNames() {
		if (tagsByName == null) {
			buildTagsByName();
		}

		return Collections.unmodifiableSet(tagsByName.keySet());
	}

	private void buildTagsByName() {
		tagsByName = new HashMap<String, ArrayList<Tag>>();

		for (Tag tag : tags) {
			String name = tag.getName();
			if (!tagsByName.containsKey(name)) {
				tagsByName.put(name, new ArrayList<Tag>());
			}

			tagsByName.get(name).add(tag);
		}
	}

	/**
	 * @return The tags used by this track
	 */
	@NonNull
	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	/**
	 * Get the tags with the given name. Returns null if the track doesn't have
	 * any appropriate tags. Values' ordering is not guaranteed to be
	 * consistent.
	 *
	 * @param name
	 * 		The name of a tag
	 * @return The tags corresponding to this tag name.
	 */
	public List<Tag> getTags(String name) {
		if (tagsByName == null) {
			buildTagsByName();
		}

		// Check if the key exists: unmodifiableList doesn't accept null input
		if (!tagsByName.containsKey(name)) {
			return null;
		} else {
			return Collections.unmodifiableList(tagsByName.get(name));
		}
	}

	/**
	 * @return A formatted string representation of the tags.
	 */
	public String tagsToString() {
		StringBuilder sb = new StringBuilder();

		List<String> tagNames = new LinkedList<String>(getTagNames());
		Collections.sort(tagNames);

		for (String name : tagNames) {
			sb.append("\t").append(name).append(":\n");

			for (Tag tag : tagsByName.get(name)) {
				sb.append("\t\t").append(tag.getValue()).append("\n");
			}
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return uri + "\n" + tagsToString();
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
	 * are non-null Tracks, they are equal if they have the same URI and tags.
	 *
	 * @see Tag#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Track other = (Track) obj;
		if (tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!tags.equals(other.tags)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

	public static final Parcelable.Creator<Track> CREATOR
			= new Parcelable.Creator<Track>() {

		@Override
		public Track createFromParcel(Parcel source) {
			long id = source.readLong();
			Uri uri = Uri.parse(source.readString());

			ArrayList<Tag> tags = new ArrayList<Tag>();
			source.readTypedList(tags, Tag.CREATOR);

			return new Track(id, uri, new HashSet<Tag>(tags));
		}

		@Override
		public Track[] newArray(int size) {
			return new Track[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(uri.toString());
		dest.writeTypedList(new ArrayList<Tag>(tags));
	}

	/**
	 * Builder class for a track. Gives a simple means of adding tags from a
	 * Cursor without making the track mutable.
	 */
	public static class Builder {

		private long id;
		private Uri uri;
		private Set<Tag> tags;

		public Builder(long id, Uri uri) {
			this.id = id;
			this.uri = uri;

			tags = new HashSet<Tag>();
		}

		public Builder add(Tag tag) {
			tags.add(tag);
			return this;
		}

		public Track build() {
			return new Track(id, uri, tags);
		}
	}
}
