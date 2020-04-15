/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import java.io.IOException;

import fit.Counts;
import fit.FixtureBridge;
import fitlibrary.dynamicVariable.RecordDynamicVariables;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.table.ParseNode;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class BatchFitLibrary {
    private boolean first = true;
	private SuiteRunner suiteRunner = new IndependentSuiteRunner(null);
	private TableListener tableListener = new TableListener(TestResults.create(new Counts()));
	private Reportage reportage;
	private DoFlow doFlow = null; //new DoFlow();

	public BatchFitLibrary() {
		this(new DefaultReportage());
	}
	public BatchFitLibrary(Reportage reportage) {
		this.reportage = reportage;
	}
	public BatchFitLibrary(TableListener tableListener) {
		this.tableListener = tableListener;
	}
	public TestResults doStorytest(Tables theTables) {
		ParseDelegation.clearDelegatesForNextStorytest();
		return doTables(theTables);
	}
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		if (doFlow != null) {
			doFlow.runStorytest(theTables,tableListener);
		} else if (first) {
			first = false;
			FixtureBridge fixtureBridge = new FixtureBridge();
			fixtureBridge.counts = tableListener.getTestResults().getCounts();
			Object firstObjectOfSuite = fixtureBridge.firstObject(theTables.parse(),tableListener.getTestResults());
			if (firstObjectOfSuite == null) {
				theTables.ignoreAndFinished(tableListener);
				return tableListener.getTestResults();
			}
			if (firstObjectOfSuite instanceof SuiteEvaluator) {
				suiteRunner = new IntegratedSuiteRunner((SuiteEvaluator)firstObjectOfSuite);
				reportage.showAllReports();
			} else
				suiteRunner = new IndependentSuiteRunner(firstObjectOfSuite);
			suiteRunner.runFirstStorytest(theTables,tableListener);
		} else
			suiteRunner.runStorytest(theTables,tableListener);
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
		if (suiteRunner != null)
			suiteRunner.exit();
	}
	public static class DefaultReportage implements Reportage {
		public void showAllReports() {
			//
		}
	}
}
