package pconley.vamp.scanner.container;

import pconley.vamp.scanner.filesystem.MediaFile;
import android.media.MediaMetadataRetriever;

public class ScannerFactory {

	private static TagStrategy defaultVorbisStrategy;
	private static TagStrategy defaultGenericStrategy;
	private static TagStrategy defaultMp4Strategy;

	private static MediaMetadataRetriever metadataRetriever;

	/**
	 * Private constructor.
	 */
	private ScannerFactory() {
	}

	/**
	 * Identify the appropriate tag-reading strategy using the media file's
	 * extension.
	 * 
	 * @param file
	 * @return
	 */
	public static TagStrategy getStrategy(MediaFile file) {
		String extension = file.toString().substring(
				file.toString().lastIndexOf('.'));

		switch (extension) {
		case ".ogg":
		case ".mkv":
		case ".flac":
			return getDefaultVorbisCommentTagStrategy();
		case ".mp4":
		case ".m4a":
			return getDefaultMp4TagStrategy();
		default:
			return getDefaultGenericTagStrategy();
		}
	}

	/**
	 * Release native resources. Call this when finished.
	 */
	public static void release() {
		if (metadataRetriever != null) {
			metadataRetriever.release();
			defaultGenericStrategy = null;
		}
	}

	private static TagStrategy getDefaultVorbisCommentTagStrategy() {
		if (defaultVorbisStrategy == null) {
			defaultVorbisStrategy = new VorbisCommentTagStrategy();
		}

		return defaultVorbisStrategy;
	}

	private static TagStrategy getDefaultGenericTagStrategy() {
		if (defaultGenericStrategy == null) {
			metadataRetriever = new MediaMetadataRetriever();
			defaultGenericStrategy = new GenericTagStrategy(metadataRetriever);
		}

		return defaultGenericStrategy;
	}

	private static TagStrategy getDefaultMp4TagStrategy() {
		if (defaultMp4Strategy == null) {
			defaultMp4Strategy = new Mp4TagStrategy();
		}

		return defaultMp4Strategy;
	}

}
