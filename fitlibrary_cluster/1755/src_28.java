/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;

public interface Table extends TableElement<Table,Row> {
	void error(TestResults testResults, Throwable e);
	void error(ITableListener tableListener, Throwable e);
	int phaseBoundaryCount();
	Row newRow();
	void ignore(TestResults testResults);
	void pass(TestResults testResults);
	TablesOnParse getTables();
	void replaceAt(int r, Row newRow);
	Table withDummyFirstRow();
	void evenUpRows();
	void addFoldingText(String foldingText);
	boolean isPlainTextTable();
	boolean hasRowsAfter(Row currentRow);
	Parse asParse();
}
