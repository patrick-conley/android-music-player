package pconley.vamp.scanner.strategy;

import pconley.vamp.scanner.filesystem.model.MediaFile;

public class StrategyFactory {

	private static TagStrategy defaultVorbisStrategy;
	private static TagStrategy defaultGenericStrategy;
	private static TagStrategy defaultMp4Strategy;

	/**
	 * Private constructor.
	 */
	private StrategyFactory() {
	}

	/**
	 * Identify the appropriate tag-reading strategy using the media file's
	 * extension.
	 *
	 * @param file
	 */
	public static TagStrategy getStrategy(MediaFile file) {
		int index = file.toString().lastIndexOf('.');
		String extension = index >= 0 ? file.toString().substring(index) : "";

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
		if (defaultGenericStrategy != null) {
			((GenericTagStrategy) defaultGenericStrategy).release();
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
			defaultGenericStrategy = new GenericTagStrategy();
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
