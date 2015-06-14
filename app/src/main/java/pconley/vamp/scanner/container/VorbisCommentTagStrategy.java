package pconley.vamp.scanner.container;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import pconley.vamp.model.Tag;

public class VorbisCommentTagStrategy implements TagStrategy {

	/**
	 * Read Vorbis Comments.
	 *
	 * @throws CannotReadException
	 * @throws IOException
	 * @throws TagException
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	@Override
	public List<Tag> getTags(File file) throws Exception {
		List<Tag> comments = new LinkedList<Tag>();

		Iterator<TagField> tags = AudioFileIO.read(file).getTag().getFields();
		while (tags.hasNext()) {
			TagField tag = tags.next();
			comments.add(new Tag(tag.getId().toLowerCase(Locale.US), tag
					.toString()));
		}

		return comments;
	}

}
