/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 10/12/2006
*/

package fitlibrary.suite;

import fitlibrary.table.TablesOnParse;
import fitlibrary.utility.TableListener;

public interface SuiteRunner {
	void runStorytest(TablesOnParse tables, TableListener tableListener);
	void runFirstStorytest(TablesOnParse tables, TableListener tableListener);
	void exit();
}
