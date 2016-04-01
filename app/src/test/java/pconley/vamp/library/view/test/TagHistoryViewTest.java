package pconley.vamp.library.view.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import pconley.vamp.library.view.TagHistoryView;
import pconley.vamp.persistence.model.Tag;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
public class TagHistoryViewTest {

	private Context context;
	private DataObserver observer;

	private TagHistoryView tagHistory;

	@Before
	public void setUpTest() {
		context = Robolectric.getShadowApplication().getApplicationContext();
		observer = new DataObserver();
	}

	@After
	public void unregisterObservers() {
		if (tagHistory != null && observer != null) {
			tagHistory.getAdapter().unregisterAdapterDataObserver(observer);
		}
	}

	/**
	 * When I add several tags to the history, then I am notified for each one
	 * and the history has the correct views.
	 */
	@Test
	public void testAddTags() {
		tagHistory = new TagHistoryView(context);
		tagHistory.getAdapter().registerAdapterDataObserver(observer);

		for (int i = 0; i < 5; i++) {
			Tag tag = new Tag(String.valueOf(i), String.valueOf(i * 2));
			tagHistory.push(tag);
		}

		assertEquals("The correct number of tags are inserted", 5,
		             observer.itemInsertedCount);
		assertEquals("The history has the correct number of tags", 5,
		             tagHistory.getAdapter().getItemCount());
	}

	/**
	 * Given the history contains several tags, when I remove some, then I am
	 * notified for each one and the history has the correct views.
	 */
	@Test
	public void testRemoveTags() {
		tagHistory = new TagHistoryView(context);

		for (int i = 0; i < 5; i++) {
			Tag tag = new Tag(String.valueOf(i), String.valueOf(i * 2));
			tagHistory.push(tag);
		}

		tagHistory.getAdapter().registerAdapterDataObserver(observer);

		for (int i = 5; i > 2; i--) {
			tagHistory.pop();
		}

		assertEquals("The history has the correct number of tags", 2,
		             tagHistory.getAdapter().getItemCount());
		assertEquals("The correct number of tags are removed", 3,
		             observer.itemRemovedCount);
	}

	/**
	 * Given the history is empty, when I remove tags, then nothing happens.
	 */
	@Test
	public void testRemoveTagsFromEmptyHistory() {
		tagHistory = new TagHistoryView(context);
		tagHistory.getAdapter().registerAdapterDataObserver(observer);

		for (int i = 0; i < 3; i++) {
			tagHistory.pop();
		}

		assertEquals("No tags are removed from an empty history", 0,
		             observer.itemRemovedCount);
	}

	/*
	 * Count the number of items added and removed
	 */
	private static class DataObserver extends RecyclerView.AdapterDataObserver {

		public int itemInsertedCount = 0;
		public int itemRemovedCount = 0;

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			super.onItemRangeInserted(positionStart, itemCount);
			itemInsertedCount += itemCount;
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			super.onItemRangeRemoved(positionStart, itemCount);
			itemRemovedCount += itemCount;
		}
	}

}
