package pconley.vamp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A piece of music metadata, represented by a unique key-value pair.
 *
 * @author pconley
 */
public final class Tag implements LibraryItem, Parcelable {

	private long id;
	private String name;
	private String value;

	/**
	 * Use this constructor when the tag's ID is unknown and unneeded.
	 *
	 * @param name
	 * @param value
	 */
	public Tag(String name, String value) {
		this(-1, name, value);
	}

	/**
	 * Constructor for tags coming out of the database.
	 *
	 * @param id
	 * @param name
	 * @param value
	 */
	public Tag(long id, String name, String value) {
		if (name == null) {
			throw new NullPointerException("Null tag name");
		}

		if (value == null) {
			throw new NullPointerException("Null value for tag " + name);
		}

		this.id = id;
		this.name = name;
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + ": " + value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Compare this instance with the specified object. Assuming both objects
	 * are non-null Tags, they are equal if the have the same name and value.
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
		Tag other = (Tag) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	public static final Parcelable.Creator<Tag> CREATOR
			= new Parcelable.Creator<Tag>() {
		@Override
		public Tag createFromParcel(Parcel source) {
			String[] tag = new String[2];

			long id = source.readLong();
			source.readStringArray(tag);

			return new Tag(id, tag[0], tag[1]);
		}

		@Override
		public Tag[] newArray(int size) {
			return new Tag[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeStringArray(new String[] { name, value });
	}

}
