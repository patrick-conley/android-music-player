package pconley.vamp.scanner.strategy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

public class VorbisCommentTagStrategy implements TagStrategy {

	/**
	 * Read Vorbis Comments.
	 * 
	 * @throws CannotreadException
	 * @throws IOException
	 * @throws TagException
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	@Override
	public Map<String, List<String>> getTags(File file) throws Exception {
		Map<String, List<String>> comments = new HashMap<String, List<String>>();

		AudioFile audioFile = AudioFileIO.read(file);

		Iterator<TagField> tags = audioFile.getTag().getFields();
		while (tags.hasNext()) {
			TagField tag = tags.next();
			String key = tag.getId().toLowerCase(Locale.US);

			List<String> values = comments.get(key);
			if (values == null) {
				values = new LinkedList<String>();
				comments.put(key, values);
			}

			values.add(tag.toString());
		}

		return comments;
	}

}
