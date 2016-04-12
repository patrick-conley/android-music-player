package pconley.vamp.persistence.model.test;

import android.net.Uri;
import android.os.Parcel;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Tag;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.persistence.model.TrackCollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class TrackCollectionTest {

	private static List<Tag> tags;

	@BeforeClass
	public static void setUp() {
		tags = new LinkedList<Tag>();
		tags.add(new Tag("foo 1", "bar 1"));
		tags.add(new Tag("foo 2", "bar 2"));
		tags.add(new Tag("foo 3", "bar 3"));
	}

	/**
	 * A null list of tracks is replaced with an empty list
	 */
	@Test
	public void collectionWrapsNullTracks() {
		MusicCollection collection = new TrackCollection(null, null);

		assertEquals("A missing list of tracks is replaced with an empty list",
		             new ArrayList<Track>(),
		             collection.getContents());
	}

	/**
	 * Parcel a collection without tags or a name
	 */
	@Test
	public void parcelWithoutNameOrTags() {
		MusicCollection collection = new TrackCollection(null, null);

		Parcel parcel = Parcel.obtain();
		collection.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);

		MusicCollection actual =
				TrackCollection.CREATOR.createFromParcel(parcel);
		assertEquals("Collection can be parceled and rebuilt", collection,
		             actual);
	}

	/**
	 * Parcel an empty filtered collection of tracks
	 */
	@Test
	public void parcelWithoutNameWithTags() {
		MusicCollection collection = new TrackCollection(tags, null);

		Parcel parcel = Parcel.obtain();
		collection.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);

		MusicCollection actual =
				TrackCollection.CREATOR.createFromParcel(parcel);
		assertEquals("Collection can be parceled and rebuilt", collection,
		             actual);
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals(Object)} for
	 * contracts.
	 */
	@SuppressWarnings({ "EqualsBetweenInconvertibleTypes", "ObjectEqualsNull",
			                  "EqualsWithItself" })
	@Test
	public void testHashCodeEqualsContract() {
		List<Tag> tags = new LinkedList<Tag>();
		List<Tag> dTags = new LinkedList<Tag>();

		tags.add(new Tag("foo", "bar"));
		tags.add(new Tag("foo 2", "bar 2"));
		dTags.add(new Tag("ham", "spam"));

		List<Track> tracks = new LinkedList<Track>();
		List<Track> dTracks = new LinkedList<Track>();

		tracks.add(new Track.Builder(0, Uri.parse("sample"))
				           .add(tags.get(0))
				           .add(tags.get(1))
				           .build());
		dTracks.add(new Track.Builder(0, Uri.parse("sample"))
				            .add(dTags.get(0))
				            .build());

		MusicCollection x = new TrackCollection(tags, tracks);
		MusicCollection y = new TrackCollection(tags, tracks);
		MusicCollection z = new TrackCollection(tags, tracks);

		// Different tags
		MusicCollection d1 = new TrackCollection(dTags, null);
		// Different name
		MusicCollection d2 = new TrackCollection(tags, dTracks);
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
		assertEquals("HashCode() is consistent (x)",
		             x.hashCode(), x.hashCode());
		assertTrue("Equals is reflexive (d1)", d1.equals(d1));
		assertEquals("HashCod1e() is consistent (d1)",
		             d1.hashCode(), d1.hashCode());
		assertTrue("Equals is reflexive (d2)", d2.equals(d2));
		assertEquals("HashCod2e() is consistent (d2)",
		             d2.hashCode(), d2.hashCode());

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
		assertEquals("Equals is transitive (x,y,z)",
		             x.equals(z), x.equals(y) && y.equals(z));
		assertEquals("Equals is transitive (x,y,d1)",
		             x.equals(d1), x.equals(y) && y.equals(d1));
		assertEquals("Equals is transitive (x,y,d2)",
		             x.equals(d2), x.equals(y) && y.equals(d2));

	}
}
