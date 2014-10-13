package pconley.vamp.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Listen for the AudioManager's AUDIO_BECOMING_NOISY intent, and pause the
 * player if it's running.
 */
public class AudioNoisyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction() != null
				&& intent.getAction().equals(
						AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			context.startService(new Intent(context, PlayerService.class)
					.setAction(PlayerService.ACTION_PAUSE));
		}

	}

}
