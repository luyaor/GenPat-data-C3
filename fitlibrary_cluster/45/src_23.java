/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.batch.testRun;

import fitlibrary.table.TablesOnParse;
import fitlibrary.utility.TableListener;

public interface FitLibraryBatching {
	void doTables(TablesOnParse tables, TableListener listener);
}
