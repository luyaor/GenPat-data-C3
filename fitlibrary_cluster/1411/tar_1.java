/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import java.io.IOException;

import org.apache.log4j.Logger;

import fit.FitServerBridge;
import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.flow.DoFlow;
import fitlibrary.flow.GlobalActionScope;
import fitlibrary.flow.ScopeStack;
import fitlibrary.flow.SetUpTearDownCache;
import fitlibrary.log.CustomHtmlLayout;
import fitlibrary.log.ShowAfterTableAppender;
import fitlibrary.parser.lookup.ParseDelegation;
import fitlibrary.runResults.TableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextContainer;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.tableOnParse.TableElementOnParse;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibraryGeneric.typed.GenericTypedObject;

public class BatchFitLibrary implements StorytestRunner {
	private static boolean LOGGER_STARTED = false;
	private TableListener tableListener = new TableListener(TestResultsFactory.testResults());
	private DoFlow doFlow = wiredUpDoFlow();

	public BatchFitLibrary() {
		//
	}
	public BatchFitLibrary(TableListener tableListener) {
		this.tableListener = tableListener;
	}
	public TestResults doStorytest(Tables theTables) {
		ParseDelegation.clearDelegatesForNextStorytest();
		return doTables(theTables);
	}
	private static DoFlow wiredUpDoFlow() {
		FlowEvaluator flowEvaluator = new DoTraverse();
		GlobalActionScope global = new GlobalActionScope();
		TypedObject globalTO = new GenericTypedObject(global);
		ScopeStack scopeStack = new ScopeStack(flowEvaluator,globalTO);
		RuntimeContextContainer runtime = new RuntimeContextContainer(scopeStack,global);
		runtime.setDynamicVariable(Traverse.FITNESSE_URL_KEY,FitServerBridge.FITNESSE_URL);
		global.setRuntimeContext(runtime);
		flowEvaluator.setRuntimeContext(runtime);
		DoFlow doFlow2 = new DoFlow(flowEvaluator,scopeStack,runtime,new SetUpTearDownCache());
		runtime.SetTableEvaluator(doFlow2);
		if (!LOGGER_STARTED)
			Logger.getRootLogger().addAppender(new ShowAfterTableAppender(runtime,new CustomHtmlLayout()));
		LOGGER_STARTED = true;
		return doFlow2;
	}
	public void setCurrentPageName(String name) {
		doFlow.getRuntimeContext().setCurrentPageName(name);
	}
	public TestResults doTables(Tables theTables) {
		tableListener.clearTestResults();
		doFlow.runStorytest(theTables,tableListener);
		DynamicVariablesRecording recorder = doFlow.getRuntimeContext().getDynamicVariableRecorder();
		if (recorder.isRecording()) {
			try {
				recorder.write();
			} catch (IOException e) {
				Table errorTable = TableFactory.table(TableFactory.row("note",TableElementOnParse.label("Problem on writing property file:")+"<hr/>"+e.getMessage()));
				errorTable.at(0).at(1).error(tableListener.getTestResults());
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
