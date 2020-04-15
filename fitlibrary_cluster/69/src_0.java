/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */

package fitlibrary.flow;

import java.util.List;

import org.apache.log4j.Logger;

import fitlibrary.DomainFixture;
import fitlibrary.flow.DoFlowOnTable.DoFlower;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.object.DomainCheckTraverse;
import fitlibrary.object.DomainInjectionTraverse;
import fitlibrary.object.DomainTraverser;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.suite.SuiteEvaluator;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.RuntimeContextual;
import fitlibrary.traverse.TableEvaluator;
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
public class DoFlow implements DomainTraverser, TableEvaluator, DoFlower {
	private static Logger logger = FitLibraryLogger.getLogger(DoFlow.class);
	private final IScopeStack scopeStack;
	protected RuntimeContextInternal runtime;
	private final SetUpTearDown setUpTearDown;
	private Option<SuiteEvaluator> suiteFixtureOption = None.none();
	private DomainInjectionTraverse domainInject = null;
	private DomainCheckTraverse domainCheck = null;
	private TableEvaluator current = this;
	private DoFlowerOnTable doFlowOnTable = null;
	
	public DoFlow(FlowEvaluator flowEvaluator, IScopeStack scopeStack, RuntimeContextInternal runtime, SetUpTearDown setUpTearDown) {
		this.scopeStack = scopeStack;
		this.runtime = runtime;
		this.setUpTearDown = setUpTearDown;
		this.doFlowOnTable = new DoFlowOnTable(flowEvaluator, scopeStack, setUpTearDown, this);
	}
	public void runStorytest(Tables tables, ITableListener tableListener) {
		logger.trace("Running storytest");
		TestResults testResults = tableListener.getTestResults();
		resetToStartStorytest();
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.at(t);
			runSingleTable(testResults, table);
			if (t < tables.size() - 1)
				finishTable(table, testResults);
			else
				finishLastTable(table, testResults);
			addAccumulatedFoldingText(table);
			tableListener.tableFinished(table);
		}
		logger.trace("Finished storytest");
		tableListener.storytestFinished();
	}
	protected void runSingleTable(TestResults testResults, Table table) {
		boolean plainTextFailed = false;
		if (current == this && table.isPlainTextTable()) {
			PlainTextAnalyser plainTextAnalyser = new PlainTextAnalyser(runtime,TemporaryPlugBoardForRuntime.definedActionsRepository());
			TestResults testResults2 = TestResultsFactory.testResults();
			plainTextAnalyser.analyseAndReplaceRowsIn(table, testResults2);
			plainTextFailed = testResults2.problems();
			testResults.add(testResults2);
		}
		if (domainCheck != null)
			handleDomainPhases(table);
		if (!plainTextFailed)
			current.runTable(table,testResults);
	}
	protected void addAccumulatedFoldingText(Table table) {
		runtime.addAccumulatedFoldingText(table);
	}
	protected void finishTable(Table table, TestResults testResults) {
		tearDown(scopeStack.poppedAtEndOfTable(), table.at(0), testResults);
		logger.trace("Finished table");
	}
	protected void finishLastTable(Table table, TestResults testResults) {
		tearDown(scopeStack.poppedAtEndOfStorytest(), table.at(0), testResults);
		logger.trace("Finished last table");
	}
	@Override
	public void runTable(Table table, TestResults testResults) {
		doFlowOnTable.runTable(table, testResults, runtime);
	}
	protected void resetToStartStorytest() {
		scopeStack.setAbandon(false);
		scopeStack.setStopOnError(false);
		scopeStack.clearAllButSuite();
		current = this;
		domainInject = null;
		domainCheck = null;
		if (suiteFixtureOption.isSome()) {
			logger.trace("Use suite fixture runtime");
			runtime = suiteFixtureOption.get().getCopyOfRuntimeContext();
			scopeStack.switchRuntime(runtime);
		} else
			runtime.reset();
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
	@Override
	public void runInnerTables(Tables innerTables, TestResults testResults) {
		IScopeState state = scopeStack.currentState();
		for (Table iTable: innerTables) {
			runTable(iTable,testResults);
			tearDown(state.restore(), iTable.at(0), testResults);
		}
	}
	private void tearDown(List<TypedObject> typedObjects, Row row, TestResults testResults) {
		for (TypedObject typedObject : typedObjects)
			setUpTearDown.callTearDownOnSutChain(typedObject.getSubject(), row, testResults);
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
		typedObject.injectRuntime(runtime);
		setUpTearDown.callSetUpOnSutChain(typedObject.getSubject(), row, testResults);
		scopeStack.addNamedObject(name, typedObject);
	}
	@Override
	public void select(String name) {
		scopeStack.select(name);
	}
	@Override
	public boolean hasDomainCheck() {
		return domainCheck != null;
	}
	@Override
	public void setDomainToCheck() {
		setCurrentCheck();
	}
	@Override
	public void setDomainFixture(TypedObject typedResult) {
		Object subject = typedResult.getSubject();
		TypedObject sut = typedResult;
		if (subject instanceof DomainFixture)
			sut = ((DomainFixture)subject).getTypedSystemUnderTest();
		domainInject = new DomainInjectionTraverse(this);
		domainInject.setTypedSystemUnderTest(sut);
		setRuntimeContextOf(domainInject);
		domainCheck = new DomainCheckTraverse(this);
		domainCheck.setTypedSystemUnderTest(sut);
		setRuntimeContextOf(domainCheck);
		current = domainInject;
	}
	@Override
	public void setSuite(SuiteEvaluator suiteEvaluator) {
		if (suiteFixtureOption.isNone())
			suiteFixtureOption = new Some<SuiteEvaluator>(suiteEvaluator);
	}
	private void setRuntimeContextOf(Object object) {
		if (object instanceof RuntimeContextual)
			((RuntimeContextual)object).setRuntimeContext(runtime);
		if (object instanceof DomainAdapter)
			setRuntimeContextOf(((DomainAdapter)object).getSystemUnderTest());
	}
}
