package pconley.vamp.scanner.strategy;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.SparseArray;

/**
 * Read the tags from an MP3 file using {@link MediaMetadataRetriever}.
 * JAudioTagger uses sun.nio.ch.DirectBuffer (which is not available on Android)
 * to work around a Windows bug.
 */
public class GenericTagStrategy implements TagStrategy {
	private static final String TAG = "TagStrategy";

	private SparseArray<String> metadataKeys = null;

	MediaMetadataRetriever metadataRetriever;

	public GenericTagStrategy(MediaMetadataRetriever metadataRetriever) {
		this.metadataRetriever = metadataRetriever;

		buildKeyList();
	}

	@Override
	public Map<String, List<String>> getTags(File file) throws Exception {
		Map<String, List<String>> tags = new HashMap<String, List<String>>();

		// Scan the file. Identifying the MIME type is a bit tricky, so let the
		// retriever determine what it can read.
		try {
			metadataRetriever.setDataSource(file.getAbsolutePath());
		} catch (RuntimeException e) {
			if (e.getMessage().endsWith("0xFFFFFFEA")) {
				Log.w(TAG, "Skipping non-media file");
				return null;
			} else {
				throw e;
			}
		}

		// Check the file is audio
		if (metadataRetriever
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == null) {
			Log.w(TAG, "Skipping non-audio media file");
			return null;
		}

		// Read and store data for each key
		for (int i = 0; i < metadataKeys.size(); i++) {
			String metadata = metadataRetriever.extractMetadata(metadataKeys
					.keyAt(i));
			if (metadata != null) {
				List<String> tag = new LinkedList<String>();
				tags.put(metadataKeys.valueAt(i), tag);
				tag.add(metadata);
			}
		}

		return tags;
	}

	private void buildKeyList() {
		metadataKeys = new SparseArray<String>();
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, "album");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
				"albumartist");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, "artist");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, "author");
		metadataKeys
				.put(MediaMetadataRetriever.METADATA_KEY_BITRATE, "bitrate");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER,
				"tracknumber");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_COMPILATION,
				"compilation");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER,
				"composer");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_DATE, "date");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER,
				"discnumber");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_GENRE, "genre");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_LOCATION,
				"location");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS,
				"tracktotal");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_TITLE, "title");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_WRITER, "writer");
		metadataKeys.put(MediaMetadataRetriever.METADATA_KEY_YEAR, "year");
	}

}
