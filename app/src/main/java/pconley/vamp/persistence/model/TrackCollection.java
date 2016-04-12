package pconley.vamp.persistence.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TrackCollection implements MusicCollection {

	private final List<Tag> filter;
	private final List<Track> contents;

	public TrackCollection(List<Tag> filter,
			List<Track> contents) {
		this.filter = filter == null ? new LinkedList<Tag>() : filter;
		this.contents = contents == null ? new LinkedList<Track>() : contents;
	}

	@Override
	public List<Tag> getFilter() {
		return Collections.unmodifiableList(filter);
	}

	@Override
	public List<Track> getContents() {
		return Collections.unmodifiableList(contents);
	}

	@Override
	public String toString() {
		return "(" + filter + "): " + contents;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TrackCollection that = (TrackCollection) o;

		return new EqualsBuilder()
				.append(filter, that.filter)
				.append(contents, that.contents)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
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
		dest.writeTypedList(filter);
		dest.writeTypedList(contents);
	}

	public static final Parcelable.Creator<MusicCollection> CREATOR
			= new Parcelable.Creator<MusicCollection>() {

		@Override
		public MusicCollection createFromParcel(Parcel source) {
			List<Tag> filter = new LinkedList<Tag>();
			List<Track> contents = new LinkedList<Track>();

			source.readTypedList(filter, Tag.CREATOR);
			source.readTypedList(contents, Track.CREATOR);

			return new TrackCollection(filter, contents);
		}

		@Override
		public MusicCollection[] newArray(int size) {
			return new TrackCollection[size];
		}
	};
}
