package pconley.vamp.library.action;

import android.app.Activity;
import android.content.Intent;

import pconley.vamp.persistence.LoadTrackCollectionTask;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.TrackCollection;
import pconley.vamp.player.PlayerService;
import pconley.vamp.player.view.PlayerActivity;

/**
 * Launch the player: put the displayed tracks in the queue, and start with the
 * clicked item.
 */
public class LibraryPlayAction extends LibraryAction {

	public LibraryPlayAction(Activity activity, MusicCollection collection) {
		super(activity, collection);
	}

	@Override
	public void execute(int position) {

		if (getCollection() instanceof TrackCollection) {
			playCollection(getCollection(), position);
		} else {
			new LoadTrackCollectionTask(this, getCollection().getFilter()).execute();
		}
	}

	@Override
	public void onLoadCollection(MusicCollection child) {
		playCollection(child, 0);
	}

	@SuppressWarnings("unchecked")
	private void playCollection(MusicCollection collection, int position) {
		/* FIXME: DAOs should create an OpenHelper themselves */
		Intent intent = new Intent(getContext(), PlayerService.class);
		intent.setAction(PlayerService.ACTION_PLAY)
		      .putExtra(PlayerService.EXTRA_START_POSITION, position)
		      .putExtra(PlayerService.EXTRA_COLLECTION, collection);
		getContext().startService(intent);

		getContext().startActivity(new Intent(getContext(),
		                                      PlayerActivity.class));
	}
}
