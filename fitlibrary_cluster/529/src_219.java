/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runResults;

import fitlibrary.suite.ReportListener;
import fitlibrary.table.Table;

public class TableListener implements ITableListener {
	private ReportListener listener;
	private TestResults testResults;

	public TableListener() {
		this(new EmptyFixtureListener(),TestResultsFactory.testResults());
	}
	public TableListener(TestResults testResults) {
		this(new EmptyFixtureListener(),testResults);
	}
	public TableListener(ReportListener listener) {
		this(listener,TestResultsFactory.testResults());
	}
	public TableListener(ReportListener listener, TestResults testResults) {
		this.listener = listener;
		this.testResults = testResults;
	}
	public void tableFinished(Table table) {
		listener.tableFinished(table);
	}
	public void storytestFinished() {
		listener.tablesFinished(testResults);
	}
	public TestResults getTestResults() {
		return testResults;
	}
	public static class EmptyFixtureListener implements ReportListener	{
		public void tableFinished(Table table) {
			//
		}
		public void tablesFinished(TestResults testResults) {
			//
		}
	}
	public void clearTestResults() {
		testResults.clear();
	}
}
