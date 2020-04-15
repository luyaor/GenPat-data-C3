/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */

package fitlibrary.flow;

import java.util.List;

import fit.Fixture;
import fitlibrary.DefineAction;
import fitlibrary.DoFixture;
import fitlibrary.DomainFixture;
import fitlibrary.SelectFixture;
import fitlibrary.SetUpFixture;
import fitlibrary.collection.CollectionSetUpTraverse;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.object.DomainCheckTraverse;
import fitlibrary.object.DomainFixtured;
import fitlibrary.object.DomainInjectionTraverse;
import fitlibrary.object.DomainTraverser;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SuiteEvaluator;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.RuntimeContextual;
import fitlibrary.traverse.TableEvaluator;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.traverse.workflow.PlainTextAnalyser;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;

/**
 * This integrates various pieces of functionality:
 * o Ordinary Do flow
 * o DomainFixture flow, with switching for 3 phases: inject, do, check.
 * o SuiteFixture
 */
public class DoFlow implements DomainTraverser, TableEvaluator {
	public static final boolean IS_ACTIVE = true;
	private final FlowEvaluator flowEvaluator;
	private final IScopeStack scopeStack;
	private final RuntimeContextInternal runtime;
	private final SetUpTearDown setUpTearDown = new SetUpTearDown();
	private Option<SuiteEvaluator> suiteFixtureOption = None.none();
	private DomainInjectionTraverse domainInject = null;
	private DomainCheckTraverse domainCheck = null;
	private TableEvaluator current = this;
	
	public DoFlow(FlowEvaluator flowEvaluator, IScopeStack scopeStack, RuntimeContextInternal runtime) {
		this.flowEvaluator = flowEvaluator;
		this.scopeStack = scopeStack;
		this.runtime = runtime;
	}
	public void runStorytest(Tables tables, ITableListener tableListener) {
		TestResults testResults = tableListener.getTestResults();
		reset();
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.at(t);
			if (current == this && table.isPlainTextTable()) {
				PlainTextAnalyser plainTextAnalyser = new PlainTextAnalyser(runtime,TemporaryPlugBoardForRuntime.definedActionsRepository());
				plainTextAnalyser.analyseAndReplaceRowsIn(table, testResults);
			}
			if (domainCheck != null)
				handleDomainPhases(table);
			current.runTable(table,tableListener);
			if (t < tables.size() - 1)
				tearDown(scopeStack.poppedAtEndOfTable(), table.at(0), testResults);
			else
				tearDown(scopeStack.poppedAtEndOfStorytest(), table.at(0), testResults);
			runtime.addAccumulatedFoldingText(table);
			tableListener.tableFinished(table);
		}
		tableListener.storytestFinished();
	}
	private void reset() {
		runtime.setAbandon(false);
		runtime.setStopOnError(false);
		scopeStack.clearAllButSuite();
		current = this;
		domainInject = null;
		domainCheck = null;
		if (suiteFixtureOption.isSome())
			flowEvaluator.setRuntimeContext(suiteFixtureOption.get().getCopyOfRuntimeContext());
	}
	private void handleDomainPhases(Table table) {
        int phaseBreaks = table.phaseBoundaryCount();
		if (phaseBreaks > 0) {
        	for (int i = 0; i < phaseBreaks; i++) {
        		if (current == domainInject)
        			setCurrentAction();
        		else if (current == this)
        			setCurrentCheck();
        	}
        }
	}
	public void runTable(Table table, ITableListener tableListener) {
		runtime.setCurrentTable(table);
		TestResults testResults = tableListener.getTestResults();
		for (int rowNo = 0; rowNo < table.size(); rowNo++) {
			Row row = table.at(rowNo);
			if (row.at(0).hadError()) {
				// Already failed due to plain text problems
			} else if (runtime.isAbandoned(testResults)) {
//				if (!testResults.problems())
					row.ignore(testResults);
			} else if (domainCheck != null && row.size() == 1 && row.text(0, flowEvaluator).equals("checks")) {
				setCurrentCheck(); // Remove this hack later
			} else {
				try {
//					System.out.println("DoFlow row "+row);
					final Cell cell = row.at(0);
			    	if (cell.hasEmbeddedTables()) { // Doesn't allow for other cells in row...
			    		handleInnerTables(cell, tableListener);
			    	} else {
			    		row = mapOddBalls(row,flowEvaluator);
			    		runtime.setCurrentRow(row);
			    		TypedObject typedResult = flowEvaluator.interpretRow(row,testResults);
			    		Object subject = typedResult.getSubject();
//			    		System.out.println("DoFlow got "+subject);
			    		if (subject instanceof Evaluator)
			    			((Evaluator)subject).setRuntimeContext(flowEvaluator.getRuntimeContext());
			    		if (subject == null) {
			    			// Can't do anything useful with a null
			    		} else if (subject.getClass() == Fixture.class) {
			    			// Ignore it, as it does nothing.
			    		} else if (subject.getClass() == DoFixture.class || subject.getClass() == DoTraverse.class) {
			    			handleActualDoFixture((DoEvaluator)subject,row,testResults);
			    		} else if (subject.getClass() == SelectFixture.class) {
			    			runtime.showAsAfterTable("warning", "This is no longer needed");
			    			handleActualDoFixture((DoEvaluator)subject,row,testResults);
			    		} else if (subject instanceof DomainFixtured || subject instanceof DomainFixture) {
			    			handleDomainFixture(typedResult, subject, row, testResults);
			    		} else if (subject instanceof SuiteEvaluator) {
			    			handleSuiteFixture((SuiteEvaluator)subject, typedResult, row, testResults);
			    		} else {
							if (subject instanceof CollectionSetUpTraverse || subject instanceof SetUpFixture) {
								handleEvaluator(typedResult, (Evaluator) subject, rowNo, table, testResults);
								return;// have finished table
							} else if (subject instanceof DoEvaluator) {
								pushOnScope(typedResult,row,testResults);
							} else if (subject instanceof Evaluator) { // Calculate, etc
								handleEvaluator(typedResult, (Evaluator) subject, rowNo, table, testResults);
								return; // have finished table
							} else if (subject instanceof Fixture) {
								Table remainingTable = tableFromHere(table, rowNo).asTableOnParse();
								flowEvaluator.fitHandler().doTable((Fixture) subject,remainingTable,testResults,flowEvaluator);
								for (int i = 0; i < remainingTable.size(); i++)
										table.replaceAt(rowNo+i, remainingTable.at(i));
								return; // have finished table
							}
						}
			    	}
				} catch (Exception ex) {
					row.error(testResults, ex);
				}
			}
		}
	}
	private Table tableFromHere(Table table, int rowNo) {
		return rowNo == 0 ? table : table.fromAt(rowNo);
	}
	private void handleEvaluator(TypedObject typedResult, Evaluator subject,
			int rowNo, Table table, TestResults testResults) {
		Table restOfTable = tableFromHere(table, rowNo);
		int rest = restOfTable.size();
		Row row = table.at(rowNo);
		setRuntimeContextOf(subject);
		callSetUpSutChain(subject,row,testResults);
		if (!(subject instanceof DefineAction)) // Don't want this as the storytest's main fixture/object
			pushOnScope(typedResult,row,testResults);
		subject.interpretAfterFirstRow(restOfTable, testResults);
		setUpTearDown.callTearDownSutChain(subject, row, testResults);
		if (restOfTable != table && restOfTable.size() > rest)
			for (int i = rest; i < restOfTable.size(); i++)
				table.add(restOfTable.at(i));
	}
	private void handleInnerTables(final Cell cell, ITableListener tableListener) {
		Tables innerTables = cell.getEmbeddedTables();
		IScopeState state = scopeStack.currentState();
		for (Table iTable: innerTables) {
			runTable(iTable,tableListener);
			state.restore();
		}
	}
	private void handleActualDoFixture(DoEvaluator doEvaluator, Row row, TestResults testResults) {
		// Unwrap an auto-wrap, keeping the type information
		if (doEvaluator.getSystemUnderTest() != null)
			pushOnScope(doEvaluator.getTypedSystemUnderTest(),row,testResults);
	}
	private void handleSuiteFixture(SuiteEvaluator suiteEvaluator, TypedObject typedResult, Row row, TestResults testResults) {
		if (suiteFixtureOption.isNone())
			suiteFixtureOption = new Some<SuiteEvaluator>(suiteEvaluator);
		setRuntimeContextOf(suiteEvaluator); // Subsequent tables are global for now.
		setUpTearDown.callSuiteSetUp(suiteEvaluator, row, testResults);
		pushOnScope(typedResult,row,testResults);
	}
	private void handleDomainFixture(TypedObject typedResult, Object subject, Row row, TestResults testResults) {
		TypedObject sut = typedResult;
		if (subject instanceof DomainFixture)
			sut = ((DomainFixture)subject).getTypedSystemUnderTest();
		pushOnScope(typedResult,row,testResults);
		domainInject = new DomainInjectionTraverse(this);
		domainInject.setTypedSystemUnderTest(sut);
		setRuntimeContextOf(domainInject);
		domainCheck = new DomainCheckTraverse(this);
		domainCheck.setTypedSystemUnderTest(sut);
		setRuntimeContextOf(domainCheck);
		current = domainInject;
	}
	private void setRuntimeContextOf(Object object) {
		if (object instanceof RuntimeContextual)
			((RuntimeContextual)object).setRuntimeContext(flowEvaluator.getRuntimeContext());
		if (object instanceof DomainAdapter)
			setRuntimeContextOf(((DomainAdapter)object).getSystemUnderTest());
	}
	private void pushOnScope(TypedObject typedResult, Row row, TestResults testResults) {
		scopeStack.push(typedResult);
		callSetUpSutChain(typedResult.getSubject(), row, testResults);
	}
	private void callSetUpSutChain(Object sutInitially, final Row row, final TestResults testResults) {
		setUpTearDown.callSetUpSutChain(sutInitially, row, testResults);
	}
	private void tearDown(List<TypedObject> typedObjects, Row row, TestResults testResults) {
		for (TypedObject typedObject : typedObjects)
			setUpTearDown.callTearDownSutChain(typedObject.getSubject(), row, testResults);
	}
	@Override
	public void setCurrentAction() {
		current = this;
	}
	@Override
	public void setCurrentCheck() {
		current = domainCheck;
	}
	public void exit() {
		if (suiteFixtureOption.isSome())
			setUpTearDown.callSuiteTearDown(suiteFixtureOption.get(),TestResultsFactory.testResults());
	}
	public RuntimeContextInternal getRuntimeContext() {
		return runtime;
	}
	@Override
	public void addNamedObject(String name, TypedObject typedObject, Row row, TestResults testResults) {
		callSetUpSutChain(typedObject.getSubject(), row, testResults);
		scopeStack.addNamedObject(name, typedObject, row, testResults);
	}
	@Override
	public void select(String name) {
		scopeStack.select(name);
	}
	public static Row mapOddBalls(Row row, Evaluator evaluator) {
		// |add|class|as|name| => |add named|name|class|
		if (row.size() == 4 && "add".equals(row.text(0,evaluator)) && "as".equals(row.text(2,evaluator))) {
			String className = row.text(1,evaluator);
			row.at(0).setText("add named");
			row.at(1).setText(row.text(3,evaluator));
			row.at(2).setText(className);
			row.removeElementAt(3);
		}
		return row;
	}
}
