package pconley.vamp.player.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import pconley.vamp.player.AudioNoisyReceiver;
import pconley.vamp.player.PlayerService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "../vamp/AndroidManifest.xml")
public class AudioNoisyReceiverTest {

	/**
	 * There is a receiver registered for ACTION_AUDIO_BECOMING_NOISY by
	 * default.
	 */
	@Test
	public void testDefaultAudioNoisyReceiver() {
		Intent audioNoisyIntent = new Intent(
				AudioManager.ACTION_AUDIO_BECOMING_NOISY);

		List<BroadcastReceiver> receivers = Robolectric.getShadowApplication()
				.getReceiversForIntent(audioNoisyIntent);

		// Then
		assertEquals(
				"App has a default receiver for audio becoming noisy broadcasts",
				1, receivers.size());
		assertEquals(
				"App has the correct receiver for audio becoming noisy broadcasts",
				AudioNoisyReceiver.class, receivers.get(0).getClass());
	}

	/**
	 * When I invoke the receiver, then the PlayerService is launched with a
	 * Pause action.
	 */
	@Test
	public void testAudioNoisyReceiverPausesPlayer() {
		Intent audioNoisyIntent = new Intent(
				AudioManager.ACTION_AUDIO_BECOMING_NOISY);

		// When
		AudioNoisyReceiver receiver = new AudioNoisyReceiver();
		receiver.onReceive(Robolectric.getShadowApplication()
				.getApplicationContext(), audioNoisyIntent);

		// Then
		Intent serviceIntent = Robolectric.getShadowApplication()
				.getNextStartedService();
		assertEquals("Receiver invokes PlayerService",
				PlayerService.class.getCanonicalName(), serviceIntent
						.getComponent().getClassName());
		assertEquals("Receiver pauses the PlayerService",
				PlayerService.ACTION_PAUSE, serviceIntent.getAction());

	}

}
