package pconley.vamp.library.action;

import android.content.Intent;
import android.widget.ArrayAdapter;

import pconley.vamp.library.LibraryActivity;
import pconley.vamp.model.LibraryItem;
import pconley.vamp.model.Playlist;
import pconley.vamp.model.Track;
import pconley.vamp.player.PlayerActivity;
import pconley.vamp.player.PlayerService;

/**
 * Launch the player: put the displayed tracks in the queue, and start with the
 * clicked item.
 */
public class PlayAction implements LibraryAction {

	@Override
	public void execute(LibraryActivity activity,
			ArrayAdapter<LibraryItem> adapter, int position) {

		// FIXME: make Track parcelable and put tracks right in the intent
		Playlist playlist = new Playlist();
		for (int i = 0; i < adapter.getCount(); i++) {
			playlist.add((Track) adapter.getItem(i));
		}
		Playlist.setInstance(playlist);

		Intent intent = new Intent(activity, PlayerService.class);
		intent.setAction(PlayerService.ACTION_PLAY)
		      .putExtra(PlayerService.EXTRA_START_POSITION, position);
		activity.startService(intent);

		activity.startActivity(new Intent(activity, PlayerActivity.class));
	}
}
