package pconley.vamp.test;

import pconley.vamp.HomeActivity;
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
				.findViewById(pconley.vamp.R.id.home_text_hello);
		assertEquals("Activity holds the right text",
				activity.getText(pconley.vamp.R.string.hello), text.getText());
	}

}
