/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.spec;

import fit.Parse;
import fitlibrary.exception.table.NestedTableExpectedException;
import fitlibrary.exception.table.RowWrongWidthException;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.utility.ParseUtility;

/**
 * Like SpecifyFixture, except that:
 * o It handles multiple rows, where each row corresponds to a storytest
 * o The first row will usually hold the SuiteSetUp tables, which will register a new FixtureSupplier
 * o It uses BatchFitLibrary to doTables()
 */
public class SpecifySuiteFixture extends SpecifyFixture3 {
	@Override
	public void doTable(Parse parseTable) {
		doTable(TableFactory.table(parseTable));
	}
    private void doTable(Table theTable) {
        TestResults testResults = TestResultsFactory.testResults(counts);
        BatchFitLibrary batch = new BatchFitLibrary();
    	for (int rowNo = 1; rowNo < theTable.size(); rowNo++) {
            Row row = theTable.at(rowNo);
            if (row.size() < 2)
				row.error(testResults, new RowWrongWidthException(2));
            Cell test = row.at(0);
            Cell report = row.at(1);
            if (!test.hasEmbeddedTables()) {
            	row.error(testResults, new NestedTableExpectedException());
                return;
            }
            Parse actual = test.getEmbeddedTables().parse();
            Parse expected = report.getEmbeddedTables().parse();
            
            batch.doStorytest(TableFactory.tables(actual));
			if (reportsEqual(actual, expected))
                report.pass(testResults);
            else {
                report.fail(testResults);
                ParseUtility.printParse(actual,"actual");
                addTableToBetterShowDifferences(theTable.parse(),
                        actual,expected);
            }
        }
    	batch.exit();
    }
}
