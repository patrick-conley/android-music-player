package pconley.vamp.persistence.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TagCollection implements MusicCollection {

	private final String name;
	private final List<Tag> filter;
	private final List<Tag> contents;

	public TagCollection(String name, List<Tag> filter,
			List<Tag> contents) {
		this.name = name;
		this.filter = filter == null ? new LinkedList<Tag>() : filter;
		this.contents = contents == null ? new LinkedList<Tag>() : contents;
	}

	/**
	 * @return Tag name describing the collection.
	 */
	public String getName() {
		return name;
	}

	@Override
	public List<Tag> getFilter() {
		return Collections.unmodifiableList(filter);
	}

	@Override
	public List<Tag> getContents() {
		return Collections.unmodifiableList(contents);
	}

	@Override
	public String toString() {
		return "(" + name + ", " + filter + "): " + contents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TagCollection that = (TagCollection) o;

		return new EqualsBuilder()
				.append(name, that.name)
				.append(filter, that.filter)
				.append(contents, that.contents)
				.isEquals();
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
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeTypedList(filter);
		dest.writeTypedList(contents);
	}

	public static final Parcelable.Creator<MusicCollection> CREATOR
			= new Parcelable.Creator<MusicCollection>() {

		@Override
		public MusicCollection createFromParcel(Parcel source) {
			String name = source.readString();
			if (name.equals("")) {
				name = null;
			}

			List<Tag> filter = new LinkedList<Tag>();
			List<Tag> contents = new LinkedList<Tag>();

			source.readTypedList(filter, Tag.CREATOR);
			source.readTypedList(contents, Tag.CREATOR);

			return new TagCollection(name, filter, contents);
		}

		@Override
		public MusicCollection[] newArray(int size) {
			return new TagCollection[size];
		}
	};
}
