package pconley.vamp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A group of related items which define content displayable in the library. If
 * the items are tracks, then they have a set of tags in common; if the items
 * are tags, then they have a common name and belong to tracks with a set of
 * tags in common.
 */
public class MusicCollection implements Parcelable {

	private List<Tag> tags;
	private String name;

	/**
	 * Constructor.
	 *
	 * @param tags
	 * 		The set of tags used to filter the library and build the collection.
	 * @param name
	 * 		Name of the tags in the contents. Should be null iff the collection
	 * 		contains tracks.
	 */
	public MusicCollection(@Nullable List<Tag> tags, String name) {
		if (tags == null) {
			this.tags = Collections.emptyList();
		} else {
			this.tags = Collections.unmodifiableList(tags);
		}
		this.name = name;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		} if (getClass() != obj.getClass()) {
			return false;
		}

		MusicCollection other = (MusicCollection) obj;
		if (tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!tags.equals(other.tags)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public static final Parcelable.Creator<MusicCollection> CREATOR
			= new Parcelable.Creator<MusicCollection>() {

		@Override
		public MusicCollection createFromParcel(Parcel source) {
			String name = source.readString();

			List<Tag> tags = new LinkedList<Tag>();
			source.readTypedList(tags, Tag.CREATOR);

			return new MusicCollection(tags, name);
		}

		@Override
		public MusicCollection[] newArray(int size) {
			return new MusicCollection[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeTypedList(tags);
	}

}
