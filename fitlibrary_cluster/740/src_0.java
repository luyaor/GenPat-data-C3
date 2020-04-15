/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import java.io.IOException;

import fit.Counts;
import fit.FitServerBridge;
import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.flow.DoFlow;
import fitlibrary.flow.GlobalScope;
import fitlibrary.flow.ScopeStack;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.runtime.RuntimeContextContainer;
import fitlibrary.table.ParseNode;
import fitlibrary.table.RowOnParse;
import fitlibrary.table.TableOnParse;
import fitlibrary.table.TablesOnParse;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;
import fitlibraryGeneric.typed.GenericTypedObject;

public class BatchFitLibrary {
	private TableListener tableListener = new TableListener(new TestResults(new Counts()));
	private DoFlow doFlow = wiredUpDoFlow();

	public BatchFitLibrary() {
		//
	}
	public BatchFitLibrary(TableListener tableListener) {
		this.tableListener = tableListener;
	}
	public TestResults doStorytest(TablesOnParse theTables) {
		ParseDelegation.clearDelegatesForNextStorytest();
		return doTables(theTables);
	}
	private static DoFlow wiredUpDoFlow() {
		FlowEvaluator flowEvaluator = new DoTraverse();
		GlobalScope global = new GlobalScope();
		TypedObject globalTO = new GenericTypedObject(global);
		ScopeStack scopeStack = new ScopeStack(flowEvaluator,globalTO);
		RuntimeContextContainer runtime = new RuntimeContextContainer(scopeStack,global);
		runtime.setDynamicVariable(Traverse.FITNESSE_URL_KEY,FitServerBridge.FITNESSE_URL);
		global.setRuntimeContext(runtime);
		flowEvaluator.setRuntimeContext(runtime);
		DoFlow doFlow2 = new DoFlow(flowEvaluator,scopeStack,runtime);
		runtime.SetTableEvaluator(doFlow2);
		return doFlow2;
	}
	public TestResults doTables(TablesOnParse theTables) {
		tableListener.clearTestResults();
		doFlow.runStorytest(theTables,tableListener);
		DynamicVariablesRecording recorder = doFlow.getRuntimeContext().getDynamicVariableRecorder();
		if (recorder.isRecording()) {
			try {
				recorder.write();
			} catch (IOException e) {
				TableOnParse errorTable = new TableOnParse(new RowOnParse("note",ParseNode.label("Problem on writing property file:")+"<hr/>"+e.getMessage()));
				errorTable.row(0).cell(1).error(tableListener.getTestResults());
				theTables.add(errorTable );
			}
		}
		return tableListener.getTestResults();
	}
	public void doTables(TablesOnParse theTables, TableListener listener) {
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
