/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.suite.StorytestRunner;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class SpecifyFixture2 extends Traverse {
	private final StorytestRunner runner;
	private final SpecifyErrorReport errorReport;
	
	public SpecifyFixture2(StorytestRunner runner, SpecifyErrorReport errorReport) {
		this.runner = runner;
		this.errorReport = errorReport;
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		try {
			Tables actualTables = table.elementAt(0).elementAt(0).getEmbeddedTables();
			Cell expectedCell = expectedOf(table);
			Tables expectedTables = expectedCell.getEmbeddedTables();
			runner.doStorytest(actualTables);
			if (reportsEqual("",actualTables,expectedTables)) {
				expectedCell.pass(testResults);
				testResults.getCounts().right += cellCount(actualTables) - 1;
			}
		} catch (Exception e) {
			table.error(testResults, e);
		}
		return null;
	}

	public boolean reportsEqual(String level, Tables actualTables, Tables expectedTables) {
		if (actualTables.size() != expectedTables.size()) {
			errorReport.sizeWrong(level,"tables",actualTables.size(),expectedTables.size());
			return false;
		}
		return false;
	}


	private Cell expectedOf(Table table) {
		if (table.size() == 2 && table.elementAt(1).size() == 1)
			return table.elementAt(1).elementAt(0);
		else if (table.size() == 1 && table.elementAt(0).size() == 2)
			return table.elementAt(0).elementAt(1);
		throw new FitLibraryException("Table must have one row with two cells or two rows with one cell");
	}
	private int cellCount(Tables actualTables) {
		int count = 0;
		for (Table table: actualTables)
			for (Row row: table)
				count += row.size();
		return count;
	}
	
	interface SpecifyErrorReport {
		void sizeWrong(String level,String type, int actualSize, int expectedSize);
	}
	static class SpecifyErrorReporter implements SpecifyErrorReport {
		public void sizeWrong(String level,String type, int actualSize, int expectedSize) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
