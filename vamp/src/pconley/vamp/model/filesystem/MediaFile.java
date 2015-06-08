package pconley.vamp.model.filesystem;

import java.io.File;

public class MediaFile extends MediaFileBase {

	public MediaFile(File file) {
		super(file);
	}

	/**
	 * Validate the file (it must be readable), then visit it.
	 */
	@Override
	public void accept(MediaVisitorBase visitor) {

		if (!getFile().exists() || !getFile().canRead()) {
			return;
		}

		visitor.visit(this);
	}

}
