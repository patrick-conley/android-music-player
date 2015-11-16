package pconley.vamp.library.action;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import pconley.vamp.library.LibraryActivity;
import pconley.vamp.persistence.model.LibraryItem;
import pconley.vamp.persistence.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;

/**
 * Launch the player: put the displayed tracks in the queue, and start with the
 * clicked item.
 */
public class LibraryPlayAction implements LibraryAction {

	@Override
	public void execute(LibraryActivity activity,
			List<? extends LibraryItem> contents, int position) {

		ArrayList<Track> tracks = new ArrayList<Track>((List<Track>) contents);

		Intent intent = new Intent(activity, PlayerService.class);
		intent.setAction(PlayerService.ACTION_PLAY)
		      .putExtra(PlayerService.EXTRA_START_POSITION, position)
		      .putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS, tracks);
		activity.startService(intent);

		activity.startActivity(new Intent(activity, PlayerActivity.class));
	}
}
