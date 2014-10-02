package pconley.vamp.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PlayerWarningReceiver extends BroadcastReceiver {

	private Activity activity;

	public PlayerWarningReceiver(Activity activity) {
		super();

		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		switch (intent.getIntExtra(PlayerService.EXTRA_WARNING, 0)) {
		case PlayerService.WARNING_MISSING_TRACK:
			Toast.makeText(activity, "Missing track " + intent.getDataString(),
					Toast.LENGTH_LONG).show();
			break;
		}
	}
}
