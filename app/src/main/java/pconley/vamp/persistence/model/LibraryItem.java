package pconley.vamp.persistence.model;

import android.os.Parcelable;

/**
 * Interface for the sort of items that can appear in a music library.
 */
public interface LibraryItem extends Parcelable {

	/**
	 * @return The item's database key.
	 */
	long getId();
}
