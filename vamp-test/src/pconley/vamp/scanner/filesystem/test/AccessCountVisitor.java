package pconley.vamp.scanner.filesystem.test;

import pconley.vamp.model.filesystem.MediaFile;
import pconley.vamp.model.filesystem.MediaFolder;
import pconley.vamp.model.filesystem.MediaVisitorBase;

public class AccessCountVisitor implements MediaVisitorBase {

	private int folderVisits = 0;
	private int fileVisits = 0;

	@Override
	public void visit(MediaFolder dir) {
		folderVisits++;
	}

	@Override
	public void visit(MediaFile file) {
		fileVisits++;
	}

	public int getFolderVisits() {
		return folderVisits;
	}

	public int getFileVisits() {
		return fileVisits;
	}

}
