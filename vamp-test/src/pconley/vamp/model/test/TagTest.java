package pconley.vamp.model.test;

import static android.test.MoreAsserts.checkEqualsAndHashCodeMethods;
import pconley.vamp.model.Tag;
import android.test.AndroidTestCase;

public class TagTest extends AndroidTestCase {

	/**
	 * When I make a tag without a name, then an exception is thrown.
	 */
	public void testNullName() {
		try {
			new Tag(null, "bar");
			fail("A null tag name is invalid");
		} catch (NullPointerException e) {

		}
	}

	/**
	 * When I make a tag without a value, then an exception is thrown.
	 */
	public void testNullValue() {
		try {
			new Tag("foo", null);
			fail("A null tag value is invalid");
		} catch (NullPointerException e) {

		}
	}

	/**
	 * See {@link Object#hashCode()} and {@link Object#equals()} for contracts.
	 */
	public void testHashCodeEqualsContract() {
		Tag x = new Tag(0, "foo", "bar");
		Tag y = new Tag(0, "foo", "bar");
		Tag z = new Tag(1, "foo", "bar");

		Tag d = new Tag(0, "foo", "baz");
		String other = "foo: bar";

		checkEqualsAndHashCodeMethods("Different types are not equal", x,
				other, false);
		checkEqualsAndHashCodeMethods("Null is never equal (x,null)", x, null,
				false);
		checkEqualsAndHashCodeMethods("Null is never equal (d,null)", d, null,
				false);

		/*
		 * Equals is reflexive
		 */
		checkEqualsAndHashCodeMethods("Equals is reflexive (x)", x, x, true);
		checkEqualsAndHashCodeMethods("Equals is reflexive (d)", d, d, true);

		/*
		 * Equals is symmetric
		 */
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,y)", x, y, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,z)", x, z, true);
		checkEqualsAndHashCodeMethods("Equals is symmetric (x,d)", x, d, false);

		/*
		 * Equals is transitive
		 */
		assertEquals("Equals is transitive (x,y,z)", x.equals(z), x.equals(y)
				&& y.equals(z));
		assertEquals("Equals is transitive (x,y,d)", x.equals(d), x.equals(y)
				&& y.equals(d));

	}
}
