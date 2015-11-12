package pconley.vamp.persistence.model.test;

import android.net.Uri;
import android.os.Parcel;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.List;
import java.util.Set;

import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class TrackTest {

	private static Uri uri;
	private static Track track;
	private static Tag tag;

	@BeforeClass
	public static void setUp() {
		uri = Uri.fromFile(new File("/sample.ogg"));
		tag = new Tag(0, "name1", "val1");
		track = new Track.Builder(0, uri).add(tag)
				.add(new Tag(1, "name1", "val2"))
				.add(new Tag(2, "name2", "val3")).build();
	}

	/**
	 * The set of tag names is immutable
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testImmutableTagNames() throws UnsupportedOperationException {
		Set<String> tags = track.getTagNames();

		tags.remove(tag.getName());
	}

	/**
	 * The set of tag values is immutable
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testImmutableTagValues() throws UnsupportedOperationException {
		List<Tag> tags = track.getTags(tag.getName());

		tags.remove(tag);
	}

	/**
	 * A non-existent tag returns null
	 */
	@Test
	public void testNonexistentTag() {
		assertNull("A non-existent tag is null", track.getTags("fake tag"));
	}

	/**
	 * Parcel a track without any tags
	 */
	@Test
	public void testParcelTagless() {
		Track track = new Track.Builder(0, uri).build();

		Parcel parcel = Parcel.obtain();
		track.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);

		Track actual = Track.CREATOR.createFromParcel(parcel);
		assertEquals("Track can be parceled and rebuilt", track, actual);
	}

	/**
	 * Parcel a track with distinct tag names
	 */
	@Test
	public void testParcelWithUniqueTags() {
		Track track = new Track.Builder(0, uri)
				.add(new Tag("name1", "value1"))
				.add(new Tag("name2", "value1"))
				.build();

		Parcel parcel = Parcel.obtain();
		track.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);

		Track actual = Track.CREATOR.createFromParcel(parcel);
		assertEquals("Track can be parceled and rebuilt", track, actual);
	}

	/**
	 * Parcel a track with repeated tag names
	 */
	@Test
	public void testParcelWithRepeatedTags() {
		Track track = new Track.Builder(0, uri)
				.add(new Tag("name1", "value1"))
				.add(new Tag("name1", "value2"))
				.build();

		Parcel parcel = Parcel.obtain();
		track.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);

		Track actual = Track.CREATOR.createFromParcel(parcel);
		assertEquals("Track can be parceled and rebuilt", track, actual);
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals(Object)} for
	 * contracts.
	 */
	@Test
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

		/*
		 * Equals is correct
		 */
		assertTrue("Equals is correct (x,y)", x.equals(y));
		assertTrue("Equals is correct (x,z)", x.equals(z));
		assertFalse("Equals is correct (x,d1)", x.equals(d1));
		assertFalse("Equals is correct (x,d2)", x.equals(d2));

		assertEquals("HashCode is correct (x,y)", x.hashCode(), y.hashCode());
		assertEquals("HashCode is correct (x,z)", x.hashCode(), z.hashCode());

		assertFalse("Different types are not equal", x.equals(other));
		assertFalse("Null is never equal (x,null)", x.equals(null));
		assertFalse("Null is never equal (d1,null)", d1.equals(null));
		assertFalse("Null is never equal (d2,null)", d2.equals(null));

		/*
		 * Equals is reflexive
		 */
		assertTrue("Equals is reflexive (x)", x.equals(x));
		assertEquals("HashCode() is consistent (x)", x.hashCode(), x.hashCode());
		assertTrue("Equals is reflexive (d1)", d1.equals(d1));
		assertEquals("HashCod1e() is consistent (d1)", d1.hashCode(), d1.hashCode());
		assertTrue("Equals is reflexive (d2)", d2.equals(d2));
		assertEquals("HashCod2e() is consistent (d2)", d2.hashCode(), d2.hashCode());

		/*
		 * Equals is symmetric
		 */
		assertEquals("Equals is symmetric (x,y)", x.equals(y), y.equals(x));
		assertEquals("Equals is symmetric (x,z)", x.equals(z), z.equals(x));
		assertEquals("Equals is symmetric (x,d1)", x.equals(d1), d1.equals(x));
		assertEquals("Equals is symmetric (x,d2)", x.equals(d2), d2.equals(x));

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
				&& y.equals(z));
		assertEquals("Equals is transitive (x,y,d1)", x.equals(d1), x.equals(y)
				&& y.equals(d1));
		assertEquals("Equals is transitive (x,y,d2)", x.equals(d2), x.equals(y)
				&& y.equals(d2));

	}
}
