package pconley.vamp.library.action;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.persistence.LoadCollectionTask;
import pconley.vamp.persistence.model.MusicCollection;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.player.PlayerService;
import pconley.vamp.player.view.PlayerActivity;

/**
 * Launch the player: put the displayed tracks in the queue, and start with the
 * clicked item.
 */
public class LibraryPlayAction extends LibraryAction {

	public LibraryPlayAction(Activity activity) {
		super(activity);
	}

	@Override
	public void execute(@NonNull MusicCollection collection, int position) {

		if (collection.getName() == null) {
			playCollection(collection, position);
		} else {
			new LoadCollectionTask(this, null,
			                       collection.getFilter()).execute();
		}
	}

	@Override
	public void onLoadCollection(MusicCollection collection) {
		playCollection(collection, 0);
	}

	@SuppressWarnings("unchecked")
	private void playCollection(MusicCollection collection, int position) {
		/* FIXME: DAOs should create an OpenHelper themselves */
		/* FIXME: PlayerService should take a collection, not a list */
		ArrayList<Track> tracks = new ArrayList<Track>(
				(List<Track>) collection.getContents());

		Intent intent = new Intent(getContext(), PlayerService.class);
		intent.setAction(PlayerService.ACTION_PLAY)
		      .putExtra(PlayerService.EXTRA_START_POSITION, position)
		      .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS, tracks);
		getContext().startService(intent);

		getContext().startActivity(new Intent(getContext(),
		                                      PlayerActivity.class));
	}
}
