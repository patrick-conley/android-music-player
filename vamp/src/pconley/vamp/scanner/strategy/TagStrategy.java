package pconley.vamp.scanner.strategy;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 
 * Read the metadata from a file.
 * 
 * @author pconley
 */
public interface TagStrategy {

	/**
	 * Read a file's metadata and return it in a common representation.
	 * 
	 * @param file
	 * @return Key/value pairs, supporting multi-valued keys.
	 */
	public Map<String, List<String>> getTags(File file) throws Exception;

}
