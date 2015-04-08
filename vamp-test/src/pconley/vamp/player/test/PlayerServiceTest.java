package pconley.vamp.player.test;

import pconley.vamp.player.PlayerService;
import android.content.Intent;
import android.test.ServiceTestCase;

public class PlayerServiceTest extends ServiceTestCase<PlayerService> {

	private Intent serviceIntent;

	public PlayerServiceTest() {
		super(PlayerService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		serviceIntent = new Intent(getContext(), PlayerService.class);
	}

	/**
	 * When I start the service with an empty intent, then it starts.
	 */

	/**
	 * Given the service is not running, when I start the service with a PAUSE
	 * action, then nothing happens.
	 */

	/**
	 * When I start the service with a PLAY action and without any tracks, then
	 * an exception is thrown.
	 */

	/**
	 * When I start the service with a PLAY action and an invalid track ID, then
	 * the service broadcasts an error.
	 */

	/**
	 * When I start the service with a PLAY action and a track which is in the
	 * database but not on disk, then the service broadcasts an error.
	 */

	/**
	 * Given the service is not running, when I bind to it, then it is not in
	 * the Playing state and it has no current track and it has no duration or
	 * position.
	 */

	/**
	 * When I start the service with a PLAY action and a valid track and an
	 * invalid playlist position, then it throws an exception.
	 */

	/**
	 * When I start the service with a PLAY action and a valid track and a valid
	 * playlist position, then it is in the Playing state.
	 */

	/**
	 * Given I can obtain audio focus, when I start the service with a PLAY
	 * action and a valid track and no playlist position, then it is in the
	 * Playing state and it has the correct track and it has the correct
	 * position and duration and it broadcasts the new track and play events.
	 */

	/**
	 * Given I cannot obtain audio focus, when I start the service with a PLAY
	 * action and a valid track, then it not in the Playing state and it has the
	 * correct track and it has the correct position and duration and it
	 * broadcasts the new track and pause events.
	 */

	/**
	 * Given the service is not running and I am bound to it, when I try to
	 * pause, then it returns false.
	 */

	/**
	 * Given the service is playing, when I pause it, then it is not in the
	 * Playing state and it has the correct track and it has the correct
	 * position and duration and it broadcasts the pause event.
	 */

	/**
	 * Given the service is playing, when I play, then it returns true and does
	 * not broadcast an event (I can verify the latch times out).
	 */

	/**
	 * Given the service is not running and I am bound to it, when I try to
	 * play, then it returns false.
	 */

	/**
	 * Given the service is paused, when I play, then it is in the Playing state
	 * and it broadcasts the play event.
	 */

	/**
	 * Given the service is paused, when I pause, then it returns true and does
	 * not broadcast an event.
	 */

	/**
	 * Given the service is not running and I am bound to it, when I go to the
	 * next track, then it returns false.
	 */

	/**
	 * Given the service is playing a single track, when I go to the next track,
	 * then it is not in the Playing state and it has no current track and it
	 * has no position or duration and it broadcasts a stop event.
	 */

	/**
	 * Given the service is not running and I am bound to it, when I go to the
	 * previous track, then it returns false.
	 */

	/**
	 * Given the service is playing a single track, when I go to the previous
	 * track, then it is not in the Playing state and it has no current track
	 * and it has no position or duration and it broadcasts a stop event.
	 */

	/**
	 * Given the service is playing a single track and the position is above the
	 * restart limit, when I go to the previous track, then it is in the Playing
	 * state and it has the correct track.
	 */

	/**
	 * Given the service is not running and I am bound to it, when I seek, then
	 * it returns false.
	 */

	/**
	 * Given the service is playing a single track, when I seek, then it is in
	 * the Playing state and it returns true.
	 */

	/**
	 * Given the service is paused in a single track, when I seek, then it is in
	 * the Paused state and it returns true.
	 */

	/**
	 * Given the service is playing a single track, when an error occurs, then
	 * it is not in the Playing state etc. and it broadcasts a stop event
	 * containing the correct error codes.
	 */

	/**
	 * Given the service is playing a single track, when the track ends, then it
	 * is not in the Playing state etc. and it broadcasts a stop event.
	 */

	/**
	 * Given the service is playing a single track, when it loses audio focus
	 * temporarily, then it is not in the Playing state and it has the correct
	 * track and it broadcasts a pause event.
	 */

	/**
	 * Given the service is playing a single track, when it loses audio focus
	 * permanently, then it is not in the Playing state and it has no current
	 * track and it broadcasts a stop event.
	 */

	/**
	 * Given the service is playing a single track and it has lost audio focus
	 * temporarily, when it regains audio focus, then it is in the Playing state
	 * etc. and broadcasts a play event.
	 */

	/**
	 * When I start the service with a PLAY action, two valid tracks, and no
	 * playlist position, then it is in the Playing state and the first track is
	 * current.
	 */

	/**
	 * When I start the service with a PLAY action, two valid tracks, and a
	 * pointer to the second track, then it is in the Playing state and the
	 * second track is current.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the first, when I go to the next track, then it is in the Playing state
	 * and the second track is current and it broadcasts new track and play
	 * events.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the second and it has not reached the restart limit, when I go to the
	 * previous track, then it is in the Playing state and the first track is
	 * current and it broadcasts new track and play events.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is playing
	 * the second and it has passed the restart limit, when I go to the previous
	 * track, then it is in the Playing state and the second track is current
	 * and it broadcasts nothing.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the first track, when I go to the next track, then it is in the Paused
	 * state and the second track is current and it broadcasts a new track
	 * event.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the second track and it has not reached the restart limit, when I go
	 * to the previous track, then it is in the Paused state and the first track
	 * is current and it broadcasts a new track event.
	 */

	/**
	 * Given the service's playlist contains two valid tracks and it is paused
	 * in the second track and it has passed the restart limit, when I go to the
	 * previous track, then it is in the Paused state and the second track is
	 * current and it broadcasts nothing.
	 */

	/**
	 * When I have started the service with an invalid track followed by a valid
	 * track, then it is in the Playing state and the second track is current
	 * and it broadcasts a single new track and play event.
	 */

}