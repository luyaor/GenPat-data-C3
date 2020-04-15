/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.spec;

import fit.Parse;
import fitlibrary.exception.table.NestedTableExpectedException;
import fitlibrary.exception.table.RowWrongWidthException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Cell;
import fitlibrary.table.RowOnParse;
import fitlibrary.table.TableOnParse;
import fitlibrary.table.TablesOnParse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

/**
 * Like SpecifyFixture, except that:
 * o It handles multiple rows, where each row corresponds to a storytest
 * o The first row will usually hold the SuiteSetUp tables, which will register a new FixtureSupplier
 * o It uses BatchFitLibrary to doTables()
 */
public class SpecifySuiteFixture extends SpecifyFixture {
	@Override
	public void doTable(Parse table) {
		doTable(new TableOnParse(table));
	}
    private void doTable(TableOnParse theTable) {
        TestResults testResults = new TestResults(counts);
        BatchFitLibrary batch = new BatchFitLibrary();
    	for (int rowNo = 1; rowNo < theTable.size(); rowNo++) {
            RowOnParse row = theTable.row(rowNo);
            if (row.size() < 2)
				row.error(testResults, new RowWrongWidthException(2));
            Cell test = row.cell(0);
            Cell report = row.cell(1);
            if (!test.hasEmbeddedTable()) {
            	row.error(testResults, new NestedTableExpectedException());
                return;
            }
            Parse actual = test.getEmbeddedTables().parse();
            Parse expected = report.getEmbeddedTables().parse();
            
            batch.doStorytest(new TablesOnParse(actual));
			if (reportsEqual(actual, expected))
                report.pass(testResults);
            else {
                report.fail(testResults);
                ParseUtility.printParse(actual,"actual");
                addTableToBetterShowDifferences(theTable.parse,
                        actual,expected);
            }
        }
    	batch.exit();
    }
}
