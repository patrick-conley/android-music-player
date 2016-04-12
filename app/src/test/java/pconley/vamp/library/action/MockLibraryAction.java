package pconley.vamp.library.action;

import android.app.Activity;

import pconley.vamp.persistence.model.MusicCollection;

public class MockLibraryAction extends LibraryAction {

	private MusicCollection collection;

	public MockLibraryAction(Activity activity, MusicCollection collection) {
		super(activity, collection);
	}

	@Override
	public void execute(int position) {
		// Does nothing
	}

	@Override
	public void onLoadCollection(MusicCollection child) {
		this.collection = child;
	}

	public MusicCollection getCollection() {
		return collection;
	}
}
