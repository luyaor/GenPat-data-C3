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
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SuiteEvaluator;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableOnParse;
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
import fitlibrary.utility.ITableListener;
import fitlibrary.utility.TestResults;
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
			Table table = tables.table(t);
			if (current == this && table.isPlainTextTable()) {
				PlainTextAnalyser plainTextAnalyser = new PlainTextAnalyser(runtime,TemporaryPlugBoardForRuntime.definedActionsRepository());
				plainTextAnalyser.analyseAndReplaceRowsIn(table, testResults);
			}
			if (domainCheck != null)
				handleDomainPhases(table);
			current.runTable(table,tableListener);
			if (t < tables.size() - 1)
				tearDown(scopeStack.poppedAtEndOfTable(), table.row(0), testResults);
			else
				tearDown(scopeStack.poppedAtEndOfStorytest(), table.row(0), testResults);
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
		TestResults testResults = tableListener.getTestResults();
		for (int rowNo = 0; rowNo < table.size(); rowNo++) {
			Row row = table.row(rowNo);
			if (row.cell(0).hadError()) {
				// Already failed due to plain text problems
			} else if (runtime.isAbandoned(testResults)) {
//				if (!testResults.problems())
					row.ignore(testResults);
			} else if (domainCheck != null && row.size() == 1 && row.text(0, flowEvaluator).equals("checks")) {
				setCurrentCheck(); // Remove this hack later
			} else {
				try {
//					System.out.println("DoFlow row "+row);
					final Cell cell = row.cell(0);
			    	if (cell.hasEmbeddedTable()) { // Doesn't allow for other cells in row...
			    		handleInnerTables(cell, tableListener);
			    	} else {
			    		row = mapOddBalls(row,flowEvaluator);
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
			    		} else if (subject instanceof CollectionSetUpTraverse || subject instanceof SetUpFixture) {
			    			handleOtherEvaluator(typedResult,(Evaluator)subject, row, testResults);
			    			return;// have finished table
			    		} else if (subject instanceof DoEvaluator) {
			    			pushOnScope(typedResult,row,testResults);
			    		} else if (subject instanceof Evaluator) { // Calculate, etc
			    			handleOtherEvaluator(typedResult,(Evaluator)subject, row, testResults);
			    			return; // have finished table
			    		} else if (subject instanceof Fixture) {
			    			flowEvaluator.fitHandler().doTable(subject,new TableOnParse(row),testResults,flowEvaluator);
			    			return; // have finished table
			    		}
			    	}
				} catch (Exception ex) {
					row.error(testResults, ex);
				}
			}
		}
	}
	public static Row mapOddBalls(Row row, Evaluator evaluator) {
		// |add|class|as|name| => |add named|name|class|
		if (row.size() == 4 && "add".equals(row.text(0,evaluator)) && "as".equals(row.text(2,evaluator))) {
			String className = row.text(1,evaluator);
			row.cell(0).setText("add named");
			row.cell(1).setText(row.text(3,evaluator));
			row.cell(2).setText(className);
			row.cell(2).parse().more = null; // Hack to remove the cell
			row.cell(2).parse().trailer = "";
		}
		return row;
	}
	private void handleInnerTables(final Cell cell, ITableListener tableListener) {
		Tables innerTables = cell.getEmbeddedTables();
		IScopeState state = scopeStack.currentState();
		for (int iTableNo = 0; iTableNo < innerTables.size(); iTableNo++) {
			Table iTable = innerTables.table(iTableNo);
			runTable(iTable,tableListener);
			state.restore();
		}
	}
	private void handleActualDoFixture(DoEvaluator doEvaluator, Row row, TestResults testResults) {
		// Unwrap an auto-wrap, keeping the type information
		if (doEvaluator.getSystemUnderTest() != null)
			pushOnScope(doEvaluator.getTypedSystemUnderTest(),row,testResults);
	}
	private void handleOtherEvaluator(TypedObject typedResult, Evaluator evaluator, Row row, TestResults testResults) {
		setRuntimeContextOf(evaluator);
		callSetUpSutChain(evaluator,row,testResults);
		if (!(evaluator instanceof DefineAction)) // Don't want this as the storytest's main fixture/object
			pushOnScope(typedResult,row,testResults);
		evaluator.interpretAfterFirstRow(new TableOnParse(row), testResults); // It could be any row
		setUpTearDown.callTearDownSutChain(evaluator, row, testResults);
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
			setUpTearDown.callSuiteTearDown(suiteFixtureOption.get(),new TestResults());
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
}
