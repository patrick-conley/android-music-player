package pconley.vamp.library.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import pconley.vamp.library.LoadCollectionTask;
import pconley.vamp.library.view.LibraryFragment;
import pconley.vamp.library.view.MockLibraryActivity;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class LoadCollectionClassTest {

	@Test
	public void testLoadUnfilteredTags() {
		LibraryFragment fragment = new LibraryFragment();
		FragmentTestUtil.startFragment(fragment, MockLibraryActivity.class);

		new LoadCollectionTask(
				Robolectric.getShadowApplication().getApplicationContext(),
				fragment, null, null).execute();
	}
}
