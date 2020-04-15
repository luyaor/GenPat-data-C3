/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.domainAdapter;

import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.traverse.Traverse;

public class RelativeFileHandler extends AbstractFileHandler {
	public RelativeFileHandler(String fileName) {
		fileNameIs(fileName);
	}
	@AnAction(wiki="",actionType=ActionType.IGNORE,
			tooltip="")
	public void fileNameIs(String localFileName) {
		file = Traverse.getLocalFile(localFileName).getFile();
	}
}