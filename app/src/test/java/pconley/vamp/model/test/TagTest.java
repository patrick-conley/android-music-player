package pconley.vamp.model.test;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import pconley.vamp.model.Tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class TagTest {

	/**
	 * When I make a tag without a name, then an exception is thrown.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullName() throws NullPointerException {
		new Tag(null, "bar");
	}

	/**
	 * When I make a tag without a value, then an exception is thrown.
	 */
	@Test(expected = NullPointerException.class)
	public void testNullValue() throws NullPointerException {
		new Tag("foo", null);
	}

	/**
	 * Given I've parceled a tag, when I unparcel it, then I get an identical
	 * tag.
	 */
	@Test
	public void testParcelable() {
		// Given
		Tag tag = new Tag(3, "foo", "bar");

		Bundle bundle = new Bundle();
		bundle.putParcelable("tag", tag);

		// When
		Tag actual = bundle.getParcelable("tag");

		// Then
		assertEquals("Tag can be parceled & unparceled", tag, actual);
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals(Object)} for
	 * contracts.
	 */
	@Test
	public void testHashCodeEqualsContract() {
		Tag x = new Tag(0, "foo", "bar");
		Tag y = new Tag(0, "foo", "bar");
		Tag z = new Tag(1, "foo", "bar");

		Tag d = new Tag(0, "foo", "baz");
		String other = "foo: bar";

		/*
		 * Equals is correct
		 */
		assertTrue("Equals is correct (x,y)", x.equals(y));
		assertTrue("Equals is correct (x,z)", x.equals(z));
		assertFalse("Equals is correct (x,d)", x.equals(d));

		assertEquals("HashCode is correct (x,y)", x.hashCode(), y.hashCode());
		assertEquals("HashCode is correct (x,z)", x.hashCode(), z.hashCode());

		assertFalse("Different types are not equal", x.equals(other));
		assertFalse("Null is never equal (x,null)", x.equals(null));
		assertFalse("Null is never equal (d,null)", d.equals(null));

		/*
		 * Equals is reflexive
		 */
		assertTrue("Equals is reflexive (x)", x.equals(x));
		assertEquals("HashCode() is consistent (x)", x.hashCode(),
		             x.hashCode());
		assertTrue("Equals is reflexive (d)", d.equals(d));
		assertEquals("HashCode() is consistent (d)", d.hashCode(),
		             d.hashCode());

		/*
		 * Equals is symmetric
		 */
		assertEquals("Equals is symmetric (x,y)", x.equals(y), y.equals(x));
		assertEquals("Equals is symmetric (x,z)", x.equals(z), z.equals(x));
		assertEquals("Equals is symmetric (x,d)", x.equals(d), d.equals(x));

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
		                                                          && y.equals(
				z));
		assertEquals("Equals is transitive (x,y,d)", x.equals(d), x.equals(y)
		                                                          && y.equals(
				d));

	}
}
