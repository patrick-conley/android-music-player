package pconley.vamp.persistence.model;

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

	private List<Tag> history;
	private String selection;

	/**
	 * Constructor.
	 *
	 * @param history
	 * 		The set of tags used to filter the library and build the collection.
	 * @param selection
	 * 		Name of the tags in the contents. Should be null iff the collection
	 * 		contains tracks.
	 */
	public MusicCollection(@Nullable List<Tag> history, String selection) {
		if (history == null) {
			this.history = Collections.emptyList();
		} else {
			this.history = Collections.unmodifiableList(history);
		}
		this.selection = selection;
	}

	public List<Tag> getHistory() {
		return history;
	}

	public String getSelection() {
		return selection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((history == null) ? 0 : history.hashCode());
		result = prime * result +
		         ((selection == null) ? 0 : selection.hashCode());
		return result;
	}

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

		MusicCollection other = (MusicCollection) obj;
		if (history == null) {
			if (other.history != null) {
				return false;
			}
		} else if (!history.equals(other.history)) {
			return false;
		}
		if (selection == null) {
			if (other.selection != null) {
				return false;
			}
		} else if (!selection.equals(other.selection)) {
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
		dest.writeString(selection);
		dest.writeTypedList(history);
	}

}
