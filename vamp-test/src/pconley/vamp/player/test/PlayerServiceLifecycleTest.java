package pconley.vamp.player.test;

import pconley.vamp.player.PlayerService;
import pconley.vamp.player.test.PlayerServiceLifecycleTest.PlayerServiceWrapper;
import android.content.Intent;
import android.test.ServiceTestCase;

/**
 * Tests against the lifecycle methods of PlayerService: ensure onCreate,
 * onHandleIntent, onDestroy are called at the appropriate times.
 * 
 * Note that because the normal Android instrumentation is not in place,
 * onDestroy is called only by shutDownService: other means of stopping the
 * service (for example stopSelf) will not call onDestroy and increment its
 * counter.
 * 
 * Because neither PlayerService nor ServiceTestCase contain any means of
 * ensuring that these methods are called, I run these tests against a wrapper
 * around PlayerService which increments counters each time the lifecycle
 * methods are completed. Tests on business logic are run in a separate test
 * class to ensure bugs in the wrapper can't affect them.
 * 
 * @author pconley
 */
public class PlayerServiceLifecycleTest extends
		ServiceTestCase<PlayerServiceWrapper> {

	private static int timesCreated;
	private static int timesStarted;
	private static int timesDestroyed;

	private Intent serviceIntent;

	public PlayerServiceLifecycleTest() {
		super(PlayerServiceWrapper.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		timesCreated = 0;
		timesStarted = 0;
		timesDestroyed = 0;

		serviceIntent = new Intent(getContext(), PlayerServiceWrapper.class);
	}

	/**
	 * When I start the service, then onCreate and onStartCommand are called.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void testStartService() {
		// When
		startService(serviceIntent);

		// Then
		assertEquals("The service is created", 1, timesCreated);
		assertEquals("The service is started", 1, timesStarted);
		assertEquals("The service is not stopped", 0, timesDestroyed);
	}

	/**
	 * When I bind to the service, then onCreate is called.
	 */
	public void testBindService() {
		// When
		bindService(serviceIntent);

		// Then
		assertEquals("The service is created", 1, timesCreated);
		assertEquals("The service is started", 0, timesStarted);
		assertEquals("The service is not stopped", 0, timesDestroyed);
	}

	/**
	 * Given the service has been started, when I stop it, then onDestroy is
	 * called.
	 * 
	 * @throws InterruptedException
	 */
	public void testStopService() throws InterruptedException {
		// Given
		startService(serviceIntent);
		timesCreated = timesStarted = timesDestroyed = 0;

		// When
		shutdownService();

		// Then
		assertEquals("The service is created", 0, timesCreated);
		assertEquals("The service is started", 0, timesStarted);
		assertEquals("The service is not stopped", 1, timesDestroyed);
	}

	/**
	 * Given I am bound to the service, when I unbind, then onDestroy is called.
	 */
	public void testUnbind() {
		// Given
		bindService(serviceIntent);
		timesCreated = timesStarted = timesDestroyed = 0;

		// When
		shutdownService();

		// Then
		assertEquals("The service is created", 0, timesCreated);
		assertEquals("The service is started", 0, timesStarted);
		assertEquals("The service is not stopped", 1, timesDestroyed);
	}

	/**
	 * Given the service is running, when I start it, then onStartCommand is
	 * called.
	 */
	public void testMultiStart() {
		// Given
		startService(serviceIntent);
		timesCreated = timesStarted = timesDestroyed = 0;

		// When
		startService(serviceIntent);

		// Then
		assertEquals("The service is created", 0, timesCreated);
		assertEquals("The service is started", 1, timesStarted);
		assertEquals("The service is not stopped", 0, timesDestroyed);
	}

	/**
	 * Given the service is started, when I bind to it, then onStartCommand is
	 * not called.
	 */
	public void testBindAfterStart() {
		// Given
		startService(serviceIntent);
		timesCreated = timesStarted = timesDestroyed = 0;

		// When
		bindService(serviceIntent);

		// Then
		assertEquals("The service is created", 0, timesCreated);
		assertEquals("The service is started", 0, timesStarted);
		assertEquals("The service is not stopped", 0, timesDestroyed);
	}

	/**
	 * Given the service has been started twice, when I stop it, then onDestroy
	 * is called.
	 */
	public void testNonStackingStarts() {
		// Given
		startService(serviceIntent);
		startService(serviceIntent);
		timesCreated = timesStarted = timesDestroyed = 0;

		// When
		shutdownService();

		// Then
		assertEquals("The service is created", 0, timesCreated);
		assertEquals("The service is started", 0, timesStarted);
		assertEquals("The service is not stopped", 1, timesDestroyed);

	}

	/**
	 * Wrapper around PlayerService, which increments some counters every time a
	 * lifecycle method is executed.
	 * 
	 * @author pconley
	 */
	public static class PlayerServiceWrapper extends PlayerService {

		public PlayerServiceWrapper() {
			super();
		}

		@Override
		public void onCreate() {
			super.onCreate();
			timesCreated++;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			int mask = super.onStartCommand(intent, flags, startId);
			timesStarted++;
			return mask;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			timesDestroyed++;
		}
	}
}
