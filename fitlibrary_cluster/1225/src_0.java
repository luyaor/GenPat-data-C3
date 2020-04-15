/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import java.util.ArrayList;
import java.util.List;

import fit.Counts;
import fit.Fixture;
import fit.Parse;
import fitlibrary.DoFixture;
import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.exception.AbandonException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.method.AmbiguousActionException;
import fitlibrary.exception.method.AmbiguousNameException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.global.PlugBoard;
import fitlibrary.suite.InFlowPageRunner;
import fitlibrary.table.Cell;
import fitlibrary.table.IRow;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.caller.CreateFromClassNameCaller;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;
import fitlibrary.traverse.workflow.caller.DoActionCaller;
import fitlibrary.traverse.workflow.caller.FixtureCaller;
import fitlibrary.traverse.workflow.caller.MultiDefinedActionCaller;
import fitlibrary.traverse.workflow.caller.PostFixSpecialCaller;
import fitlibrary.traverse.workflow.caller.SpecialCaller;
import fitlibrary.traverse.workflow.caller.ValidCall;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;

public abstract class DoTraverseInterpreter extends Traverse implements DoEvaluator {
	public DoTraverseInterpreter() {
		//
	}
	public DoTraverseInterpreter(Object sut) {
		super(sut);
	}
	public DoTraverseInterpreter(TypedObject typedObject) {
		super(typedObject);
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		Object result = null;
		for (int rowNo = 1; rowNo < table.size(); rowNo++) {
			Row row = table.row(rowNo);
			if (testResults.isAbandoned())
				row.ignore(testResults);
			else
				try {
					result = interpretRow(row,testResults,null);
					if (result instanceof DoEvaluator) {
						DoEvaluator doEvaluator = (DoEvaluator)result;
						doEvaluator.setRuntimeContext(runtimeContext);
						doEvaluator.interpretInFlow(new Table(row),testResults);
						break;
					} else if (result instanceof Evaluator) {
						Evaluator evaluator = (Evaluator)result;
						evaluator.setRuntimeContext(runtimeContext);
						interpretEvaluator(evaluator,new Table(row),testResults);
						break;
					} else if (result instanceof Fixture) {
						getFitHandler().doTable(result, new Table(row),testResults,this);
						break;
					}
				} catch (Exception ex) {
					row.error(testResults,ex);
				}
		}
		return result;
	}
	// overridden in DomainTraverse (with quite separate code)
    public Object interpretWholeTable(Table table, TableListener tableListener) {
    	return interpretWholeTable(table, tableListener.getTestResults());
	}
	public Object interpretWholeTable(Table table, TestResults testResults) {
		try {
			Fixture fixtureByName = fixtureOrDoTraverseByName(table,testResults);
			if (fixtureByName != null && fixtureByName.getClass() == Fixture.class)
				fixtureByName = null;
			Object result = interpretRow(table.row(0),testResults,fixtureByName);
			if (testResults.isAbandoned()) {
				interpretInFlow(table,testResults);
				return null;
			} if (result instanceof Evaluator) {
				Evaluator evaluator = (Evaluator)result;
				evaluator.setRuntimeContext(runtimeContext);
				interpretEvaluator(evaluator, table, testResults);
				return result;
			}
			if (result instanceof Fixture)
				getFitHandler().doTable(result,table,testResults,this);
			else // do the rest of the table with this traverse
				return interpretInFlow(table,testResults);
		} catch (Throwable e) {
            table.error(testResults,e);
		}
		return null;
	}
	private void interpretEvaluator(Evaluator evaluator, Table table, TestResults testResults) {
		evaluator.setRuntimeContext(runtimeContext);
		evaluator.interpretAfterFirstRow(table,testResults);
	}
	public Fixture fixtureOrDoTraverseByName(Table table, TestResults testResults) {
        String className = table.row(0).text(0,this).trim();
		if (className.equals(""))
            return null;
		try {
			return new OpenFixture(testResults.getCounts()).getLinkedFixtureWithArgs(table.parse);
		} catch (Throwable e) {
			if (table.row(0).size() == 1) {
				try {
					Object traverse = Class.forName(className).newInstance();
					if (traverse instanceof DoTraverse) {
						DoFixture doFixture = new DoFixture();
						doFixture.setTraverse((DoTraverse) traverse);
						return doFixture;
					}
				} catch (Throwable e1) {
					//
				}
			}
			return null;
		}
	}
	public static class OpenFixture extends Fixture {
		public OpenFixture(Counts counts) {
			this.counts = counts;
		}
		@Override
		public Fixture getLinkedFixtureWithArgs(Parse tables) throws Throwable {
			return super.getLinkedFixtureWithArgs(tables);
		}
	}
    // @Overridden in CollectionSetUpTraverse
    public Object interpretInFlow(Table table, TestResults testResults) {
    	return interpretAfterFirstRow(table,testResults);
    }
    public Object interpretRow(Row row, TestResults testResults, Fixture fixtureByName) {
    	if (testResults.isAbandoned()) {
			row.ignore(testResults);
			return null;
		}
    	final Cell cell = row.cell(0);
    	if (cell.hasEmbeddedTable()) {
    		interpretInnerTables(cell.innerTables(),testResults);
    		return null;
    	}
    	try {
    		DoCaller[] actions = createDoCallers(row, fixtureByName);
    		Option<Object> result = interpretSimpleRow(row,testResults,actions,fixtureByName);
    		if (result.isSome())
    			return result.get();
    		methodsAreMissing(actions,possibleSeq(row));
    	} catch (IgnoredException ex) {
    		//
    	} catch (AbandonException e) {
    		row.ignore(testResults);
    	} catch (Exception ex) {
    		row.error(testResults, ex);
    	}
    	return null;
    }
    public Option<Object> interpretSimpleRow(Row row, TestResults testResults, DoCaller[] actions, Fixture fixtureByName) throws Exception {
		Option<Object> result = pickCaller(actions, row, testResults);
		if (result.isSome())
			return result;
		if (row.size() > 2) {
			Option<Object> seqResult = trySequenceCall(row, testResults, fixtureByName);
			if (seqResult.isSome())
				return seqResult;
		}
		return None.none();
    }
    // The following is overridden in SequenceTraverse, so it doesn't try again (repeatedly)
    protected Option<Object> trySequenceCall(Row row, TestResults testResults, Fixture fixtureByName) throws Exception {
    	SequenceTraverse sequenceTraverse = new SequenceTraverse(this);
    	sequenceTraverse.setRuntimeContext(runtimeContext);
		return sequenceTraverse.interpretSimpleRow(row, testResults, sequenceTraverse.createDoCallers(row, fixtureByName),fixtureByName);
    }
    private Option<Object> pickCaller(DoCaller[] actions, Row row, TestResults testResults) throws Exception {
		for (int i = 0; i < actions.length; i++)
			if (actions[i].isValid()) {
				Object result = actions[i].run(row, testResults);
				if (testResults.isAbandoned() && !testResults.problems())
					row.ignore(testResults);
				return new Some<Object>(result);
			}
		return None.none();
    }
	public DoCaller[] createDoCallers(Row row, Fixture fixtureByName) {
		DoCaller[] actions = { 
				new DefinedActionCaller(row, this),
				new MultiDefinedActionCaller(row, this),
				new SpecialCaller(row,switchSetUp(),PlugBoard.lookupTarget),
				new PostFixSpecialCaller(row,switchSetUp()),
				new FixtureCaller(fixtureByName),
				new CreateFromClassNameCaller(row,switchSetUp()),
				new DoActionCaller(row,switchSetUp()) };
		checkForAmbiguity(actions);
		return actions;
	}
	private String possibleSeq(Row row) {
		if (row.size() < 3)
			return "";
		String result = "public Type "+ExtendedCamelCase.camel(row.text(0, this))+"(";
		if (row.size() > 0)
			result += "Type p1";
		for (int i = 2; i < row.size(); i++)
			result += ", Type p"+i;
		return result+") {}";
	}
	public ICalledMethodTarget findMethodFromRow(IRow row, int from, int extrasCellsOnEnd) throws Exception {
		return findMethodByActionName(row.rowFrom(from), row.size() - from - extrasCellsOnEnd - 1);
	}
	public ICalledMethodTarget findMethodFromRow222(IRow row, int from, int less) throws Exception {
		return findMethodByActionName(row.rowFrom(from), row.size() - less);
	}
	public void findMethodsFromPlainText(String textCall, List<ValidCall> results) {
		asTypedObject(this).findMethodsFromPlainText(textCall,results);
	}
	/** Is overridden in subclass SequenceTraverse to process arguments differently
	 * @throws Exception 
	 */
	public CalledMethodTarget findMethodByActionName(IRow row, int allArgs) throws Exception {
		return PlugBoard.lookupTarget.findMethodInEverySecondCell(this, row, allArgs);
	}
	private static void checkForAmbiguity(DoCaller[] actions) {
		final String AND = " AND ";
		String message = "";
		List<String> valid = new ArrayList<String>();
		for (int i = 0; i < actions.length; i++) {
			if (actions[i].isValid()) {
				String ambiguityErrorMessage = actions[i].ambiguityErrorMessage();
				valid.add(ambiguityErrorMessage);
				message += AND+ambiguityErrorMessage;
			}
		}
		if (valid.size() > 1)
			throw new AmbiguousActionException(message.substring(AND.length()));
	}
	private void methodsAreMissing(DoCaller[] actions, String possibleSequenceCall) {
		List<String> missingMethods = new ArrayList<String>();
		List<Class<?>> possibleClasses = new ArrayList<Class<?>>();
		String ambiguousMethods = "";
		for (int i = 0; i < actions.length; i++)
			if (actions[i].isProblem()) {
				Exception exception = actions[i].problem();
				if (exception instanceof MissingMethodException) {
					MissingMethodException missingMethodException = (MissingMethodException) exception;
					missingMethods.addAll(missingMethodException.getMethodSignature());
					for (Class<?> c : missingMethodException.getClasses())
						if (!possibleClasses.contains(c))
							possibleClasses.add(c);
				} else if (exception instanceof AmbiguousNameException) {
					AmbiguousNameException ambiguousNameException = (AmbiguousNameException) exception;
					ambiguousMethods += "<li>"+ambiguousNameException.getMessage()+"</li>";
				} else if (exception instanceof ClassNotFoundException) {
					ClassNotFoundException cnf = (ClassNotFoundException) exception;
					if (cnf.getCause() != null) {
						System.out.println("methodsAreMissing(): CNFE: "+exception.getMessage()+": "+cnf.getCause().getMessage());
					} else
						System.out.println("methodsAreMissing(): CNFE: "+exception.getMessage());
					missingMethods.add(exception.getMessage());
				} else
					missingMethods.add(exception.getMessage());
			}
		if (!missingMethods.isEmpty() && !possibleSequenceCall.isEmpty())
			missingMethods.add(possibleSequenceCall);
		String message = "";
		if (ambiguousMethods.isEmpty())
			message += "Missing class or ";
		if (!missingMethods.isEmpty())
			message += "Missing method. Possibly:"+MissingMethodException.htmlListOfSignatures(missingMethods);
		if (!ambiguousMethods.isEmpty())
			message += "<ul>"+ambiguousMethods+"</ul>";
		if (!possibleClasses.isEmpty())
			message += "<hr/>Possibly in class:"+MissingMethodException.htmlListOfClassNames(possibleClasses);
		throw new FitLibraryExceptionInHtml(message.trim());
	}
	private void interpretInnerTables(Tables tables, TestResults testResults) {
		new InFlowPageRunner(this,testResults).run(tables,0,new TableListener(testResults));
	}
	public DoTraverseInterpreter switchSetUp() {
		return this;
	}
	protected Object callMethodInRow(Row row, TestResults testResults, boolean catchError, Cell operatorCell) throws Exception {
		return findMethodFromRow222(row,1, 2).invokeForSpecial(row.rowFrom(2),testResults,catchError,operatorCell);
	}
	public boolean toExpandDefinedActions() {
		return getRuntimeContext().toExpandDefinedActions();
	}
	public void setExpandDefinedActions(boolean expandDefinedActions) {
		getRuntimeContext().setExpandDefinedActions(expandDefinedActions);
	}
}
