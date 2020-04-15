/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class SpecifyFixture2 extends Traverse {
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		try {
		Tables actualTables = table.row(0).cell(0).getEmbeddedTables();
		Tables expectedTables = expectedOf(table).getEmbeddedTables();
		new BatchFitLibrary().doStorytest(actualTables);
		} catch (Exception e) {
			table.error(testResults, e);
		}
		return null;
	}

	private Cell expectedOf(Table table) {
		if (table.size() == 2 && table.row(1).size() == 1)
			return table.row(1).cell(0);
		else if (table.size() == 1 && table.row(0).size() == 2)
			return table.row(0).cell(1);
		throw new FitLibraryException("Table must have one row with two cells or two rows with one cell");
	}

}
