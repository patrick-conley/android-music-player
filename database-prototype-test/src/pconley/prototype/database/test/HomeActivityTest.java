package pconley.prototype.database.test;

import pconley.prototype.database.HomeActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class HomeActivityTest extends
		ActivityInstrumentationTestCase2<HomeActivity> {

	private HomeActivity activity;

	public HomeActivityTest() {
		super(HomeActivity.class);
	}

	public void setUp() throws Exception {
		super.setUp();

		activity = getActivity();
	}

	public void testDefaultMessage() {
		TextView text = (TextView) activity
				.findViewById(pconley.prototype.database.R.id.home_text_hello);
		assertEquals("Activity holds the right text",
				activity.getText(pconley.prototype.database.R.string.hello),
				text.getText());
	}

}
