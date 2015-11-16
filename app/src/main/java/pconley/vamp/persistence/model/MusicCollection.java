package pconley.vamp.persistence.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

	private String name;
	private List<Tag> filter;
	private List<? extends LibraryItem> contents;

	/**
	 * Constructor.
	 *  @param name
	 * 		Name of the tags in the contents. Should be null iff the collection
	 * 		contains tracks
	 * @param filter
	 * 		The set of tags used to filter the library and build the collection.
	 * @param contents
	 */
	public MusicCollection(String name, @Nullable List<Tag> filter,
			List<? extends LibraryItem> contents) {

		if (name == null && !contents.isEmpty() && contents.get(
				0) instanceof Tag) {
			throw new IllegalArgumentException(
					"Tags used in nameless collection");
		}

		this.name = name;
		this.filter = filter == null ? new LinkedList<Tag>() : filter;
		this.contents = contents;
	}

	public String getName() {
		return name;
	}

	public List<Tag> getFilter() {
		return Collections.unmodifiableList(filter);
	}

	public List<? extends LibraryItem> getContents() {
		return Collections.unmodifiableList(contents);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(name)
				.append(filter)
				.append(contents)
				.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MusicCollection other = (MusicCollection) o;

		return new EqualsBuilder()
				.append(name, other.name)
				.append(filter, other.filter)
				.append(contents, other.contents)
				.isEquals();
	}

	public static final Parcelable.Creator<MusicCollection> CREATOR
			= new Parcelable.Creator<MusicCollection>() {

		@Override
		public MusicCollection createFromParcel(Parcel source) {
			String name = source.readString();
			if (name.equals("")) {
				name = null;
			}

			List<Tag> tags = new LinkedList<Tag>();
			source.readTypedList(tags, Tag.CREATOR);

			if (name == null) {
				List<Track> contents = new LinkedList<Track>();
				source.readTypedList(contents, Track.CREATOR);
				return new MusicCollection(null, tags, contents);
			} else {
				List<Tag> contents = new LinkedList<Tag>();
				source.readTypedList(contents, Tag.CREATOR);
				return new MusicCollection(name, tags, contents);
			}
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
		dest.writeTypedList(filter);
		dest.writeTypedList(contents);
	}

}
