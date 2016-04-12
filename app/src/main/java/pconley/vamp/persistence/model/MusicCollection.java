package pconley.vamp.persistence.model;

import android.os.Parcelable;

import java.util.List;

/**
 * A group of related items which define content displayable in the library. If
 * the items are tracks, then they have a set of tags in common; if the items
 * are tags, then they have a common name and belong to tracks with a set of
 * tags in common.
 */
public interface MusicCollection extends Parcelable {

	/**
	 * @return The set of tags that the library was filtered against to build this collection
	 */
	List<Tag> getFilter();

	/**
	 * @return Displayable tracks or tags in the collection.
	 */
	List<? extends LibraryItem> getContents();
}
