package pconley.vamp.model.test;

import static android.test.MoreAsserts.checkEqualsAndHashCodeMethods;

import java.io.File;
import java.util.Set;

import pconley.vamp.model.Tag;
import pconley.vamp.model.Track;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TrackTest extends AndroidTestCase {

	private Uri uri;
	private Track track;
	private Tag tag;

	public void setUp() {
		uri = Uri.fromFile(new File("/sample.ogg"));
		tag = new Tag(0, "name1", "val1");
		track = new Track.Builder(0, uri).add(tag)
				.add(new Tag(1, "name1", "val2"))
				.add(new Tag(2, "name2", "val3")).build();
	}

	/**
	 * The set of tag names is immutable
	 */
	public void testImmutableTagNames() {
		Set<String> tags = track.getTagNames();

		try {
			tags.remove(tag.getName());
			fail("Tag names can't be removed");
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * The set of tag values is immutable
	 */
	public void testImmutableTagValues() {
		Set<Tag> tags = track.getTags(tag.getName());

		try {
			tags.remove(tag);
			fail("Tags can't be removed");
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * A non-existent tag returns null
	 */
	public void testNonexistentTag() {
		assertNull("A non-existent tag is null", track.getTags("faketag"));
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals()} for contracts.
	 */
	public void testHashCodeEqualsContract() {
		Track x = new Track.Builder(0, uri).add(new Tag(1, "foo", "bar"))
				.add(new Tag(2, "foo", "baz")).build();
		Track y = new Track.Builder(1, uri).add(new Tag(3, "foo", "bar"))
				.add(new Tag(4, "foo", "baz")).build();
		Track z = new Track.Builder(2, uri).add(new Tag(5, "foo", "bar"))
				.add(new Tag(6, "foo", "baz")).build();

		// Different tags
		Track d1 = new Track.Builder(0, uri).add(new Tag(1, "foo", "bar"))
				.build();
		// Different URI
		Track d2 = new Track.Builder(0, Uri.fromFile(new File("sample2.ogg")))
				.add(new Tag(1, "foo", "bar")).add(new Tag(2, "foo", "baz"))
				.build();
		String other = "foo: bar";

		checkEqualsAndHashCodeMethods("Different types are not equal", x,
				other, false);
		checkEqualsAndHashCodeMethods("Null is never equal (x,null)", x, null,
				false);
		checkEqualsAndHashCodeMethods("Null is never equal (d,null)", d1, null,
				false);

		/*
		 * Equals is reflexive
		 */
		checkEqualsAndHashCodeMethods("Equals is reflexive (x)", x, x, true);
		checkEqualsAndHashCodeMethods("Equals is reflexive (d)", d1, d1, true);

		/*
		 * Equals is symmetric
		 */
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,y)", x, y, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,z)", x, z, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,d1)", x, d1,
				false);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,d2)", x, d2,
				false);

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
				&& y.equals(z));
		assertEquals("Equals is transitive (x,y,d)", x.equals(d1), x.equals(y)
				&& y.equals(d1));

	}
}
