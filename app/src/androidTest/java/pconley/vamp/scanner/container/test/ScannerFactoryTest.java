package pconley.vamp.scanner.container.test;

import java.io.File;

import pconley.vamp.model.filesystem.MediaFile;
import pconley.vamp.scanner.container.GenericTagStrategy;
import pconley.vamp.scanner.container.Mp4TagStrategy;
import pconley.vamp.scanner.container.ScannerFactory;
import pconley.vamp.scanner.container.TagStrategy;
import pconley.vamp.scanner.container.VorbisCommentTagStrategy;
import android.test.AndroidTestCase;

public class ScannerFactoryTest extends AndroidTestCase {

	/**
	 * Ogg Vorbis → Vorbis strategy
	 */
	public void testOggVorbis() {
		// Given
		File file = new File("sample.ogg");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("Ogg Vorbis uses the Vorbis strategy",
				strategy instanceof VorbisCommentTagStrategy);
	}

	/**
	 * FLAC → Vorbis strategy
	 */
	public void testFlac() {
		// Given
		File file = new File("sample.flac");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("Flac uses the Vorbis strategy",
				strategy instanceof VorbisCommentTagStrategy);
	}

	/**
	 * Matroska → Vorbis strategy
	 */
	public void testMatroska() {
		// Given
		File file = new File("sample.mkv");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("Matroska uses the Vorbis strategy",
				strategy instanceof VorbisCommentTagStrategy);
	}

	/**
	 * MPEG-4 (.mp4) → MP4 strategy
	 */
	public void testMp4() {
		// Given
		File file = new File("sample.mp4");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("MP4 uses the MP4 strategy",
				strategy instanceof Mp4TagStrategy);
	}

	/**
	 * MPEG-4 (.m4a) → MP4 strategy
	 */
	public void testM4a() {
		// Given
		File file = new File("sample.m4a");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("M4A uses the MP4 strategy",
				strategy instanceof Mp4TagStrategy);
	}

	/**
	 * MP3 → generic strategy
	 */
	public void testMp3() {
		// Given
		File file = new File("sample.mp3");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("MP3 uses the generic strategy",
				strategy instanceof GenericTagStrategy);
	}

	/**
	 * JPEG → generic strategy
	 */
	public void testJpeg() {
		// Given
		File file = new File("sample.jpg");

		// When
		TagStrategy strategy = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertTrue("JPEG uses the generic strategy",
				strategy instanceof GenericTagStrategy);
	}

	/**
	 * Test release() works:
	 * 
	 * Given I have used a generic strategy, when I release resources and use a
	 * generic strategy, then a new instance is returned.
	 */
	public void testRelease() {
		// Given
		File file = new File("sample.mp3");
		TagStrategy expected = ScannerFactory.getStrategy(new MediaFile(file));

		// When
		ScannerFactory.release();
		TagStrategy actual = ScannerFactory.getStrategy(new MediaFile(file));

		// Then
		assertNotSame("Generic strategy instance is reset", expected, actual);
	}

}
