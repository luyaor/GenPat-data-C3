/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import fit.Counts;
import fit.FixtureListener;
import fit.Parse;
import fitlibrary.table.Table;

public class TableListener implements ITableListener {
	private FixtureListener listener;
	private TestResults testResults;

	public TableListener() {
		this(new EmptyFixtureListener(),new TestResults(new Counts()));
	}
	public TableListener(TestResults testResults) {
		this(new EmptyFixtureListener(),testResults);
	}
	public TableListener(FixtureListener listener) {
		this(listener,new TestResults(new Counts()));
	}
	public TableListener(FixtureListener listener, TestResults testResults) {
		this.listener = listener;
		this.testResults = testResults;
	}
	public void tableFinished(Table table) {
		listener.tableFinished(table.parse());
	}
	public FixtureListener getListener() {
		return listener;
	}
	public void storytestFinished() {
		listener.tablesFinished(testResults.getCounts());
	}
	public TestResults getTestResults() {
		return testResults;
	}
	public static class EmptyFixtureListener implements FixtureListener	{
		public void tableFinished(Parse table) {
			//
		}
		public void tablesFinished(Counts count) {
			//
		}
	}
	public void clearTestResults() {
		testResults.clear();
	}
}
