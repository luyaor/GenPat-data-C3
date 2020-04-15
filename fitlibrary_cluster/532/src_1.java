/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import java.io.IOException;

import fit.Counts;
import fitlibrary.dynamicVariable.RecordDynamicVariables;
import fitlibrary.flow.DoFlow;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.table.ParseNode;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class BatchFitLibrary {
	private TableListener tableListener = new TableListener(TestResults.create(new Counts()));
	private DoFlow doFlow = new DoFlow(new DoTraverse());

	public BatchFitLibrary() {
		//
	}
	public BatchFitLibrary(TableListener tableListener) {
		this();
		this.tableListener = tableListener;
	}
	public TestResults doStorytest(Tables theTables) {
		ParseDelegation.clearDelegatesForNextStorytest();
		return doTables(theTables);
	}
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		doFlow.runStorytest(theTables,tableListener);
		if (RecordDynamicVariables.recording()) {
			try {
				RecordDynamicVariables.write();
			} catch (IOException e) {
				Table errorTable = new Table(new Row("note",ParseNode.label("Problem on writing property file:")+"<hr/>"+e.getMessage()));
				errorTable.row(0).cell(1).error(tableListener.getTestResults());
				theTables.add(errorTable );
			}
		}
		return tableListener.getTestResults();
	}
	public void doTables(Tables theTables, TableListener listener) {
		this.tableListener = listener;
		doStorytest(theTables);
	}
	public void exit() {
		doFlow.exit();
	}
	public static class DefaultReportage implements Reportage {
		public void showAllReports() {
			//
		}
	}
}
