/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary;

import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.caller.CreateFromClassNameCaller;
import fitlibrary.utility.TestResults;

public class DefaultPackages extends Traverse {
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		for (int r = 1; r < table.size(); r ++) {
			CreateFromClassNameCaller.addDefaultPackage(table.elementAt(r).text(0, this));
		}
		return null;
	}

}
