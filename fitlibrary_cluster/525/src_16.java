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
import fitlibrary.flow.DoAutoWrapper;
import fitlibrary.flow.IDoAutoWrapper;
import fitlibrary.global.PlugBoard;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.caller.CreateFromClassNameCaller;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;
import fitlibrary.traverse.workflow.caller.DoActionCaller;
import fitlibrary.traverse.workflow.caller.FixtureCaller;
import fitlibrary.traverse.workflow.caller.MultiDefinedActionCaller;
import fitlibrary.traverse.workflow.caller.PostFixSpecialCaller;
import fitlibrary.traverse.workflow.caller.SpecialCaller;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.ITableListener;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;
import fitlibraryGeneric.typed.GenericTypedObject;

public abstract class DoTraverseInterpreter extends Traverse implements DoEvaluator {
	private IDoAutoWrapper doAutoWrapper = new DoAutoWrapper(this);
	protected boolean sequencing = false;

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
		TypedObject result = null;
		for (int rowNo = 1; rowNo < table.size(); rowNo++) {
			Row row = table.row(rowNo);
			if (getRuntimeContext().isAbandoned(testResults))
				row.ignore(testResults);
			else
				try {
					result = interpretRow(row,testResults,null);
					if (result != null) {
						Object subject = result.getSubject();
						if (subject instanceof DoEvaluator) {
							DoEvaluator doEvaluator = (DoEvaluator)subject;
							doEvaluator.setRuntimeContext(runtimeContext);
							doEvaluator.interpretInFlow(TableFactory.table(row),testResults);
							break;
						} else if (subject instanceof Evaluator) {
							Evaluator evaluator = (Evaluator)subject;
							evaluator.setRuntimeContext(runtimeContext);
							interpretEvaluator(evaluator,TableFactory.table(row),testResults);
							break;
						} else if (subject instanceof Fixture) {
							getFitHandler().doTable(subject, TableFactory.table(row),testResults,this);
							break;
						}
					}
				} catch (Exception ex) {
					row.error(testResults,ex);
				}
		}
		if (result == null)
			return null;
		return result.getSubject();
	}
	// overridden in DomainTraverse (with quite separate code)
    public Object interpretWholeTable(Table table, ITableListener tableListener) {
    	return interpretWholeTable(table, tableListener.getTestResults());
	}
	public Object interpretWholeTable(Table table, TestResults testResults) {
		System.out.println("DTI.interpretWholeTable used in "+this);
		try {
			Fixture fixtureByName = fixtureOrDoTraverseByName(table,testResults);
			if (fixtureByName != null && fixtureByName.getClass() == Fixture.class)
				fixtureByName = null;
			TypedObject typedResult = interpretRow(table.row(0),testResults,fixtureByName);
			Object result = null;
			if (typedResult != null)
				result = typedResult.getSubject();
			if (getRuntimeContext().isAbandoned(testResults)) {
				interpretInFlow(table,testResults);
				return null;
			}
			if (result instanceof Evaluator) {
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
			return new OpenFixture(testResults.getCounts()).getLinkedFixtureWithArgs(table.parse());
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
    public TypedObject interpretRow(Row row, TestResults testResults) {
    	return doAutoWrapper.wrap(interpretRow(row,testResults,null));
    }
    public TypedObject interpretRow(Row row, TestResults testResults, Fixture fixtureByName) {
    	try {
    		DoCaller[] actions = createDoCallers(row, fixtureByName);
    		Option<TypedObject> result = interpretSimpleRow(row,testResults,actions,fixtureByName);
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
    	return GenericTypedObject.NULL;
    }
    protected Option<TypedObject> interpretSimpleRow(Row row, TestResults testResults, DoCaller[] actions, Fixture fixtureByName) throws Exception {
		Option<TypedObject> result = pickCaller(actions, row, testResults);
		if (result.isSome())
			return result;
		if (row.size() > 2) {
			Option<TypedObject> seqResult = trySequenceCall(row, testResults, fixtureByName);
			if (seqResult.isSome())
				return seqResult;
		}
		return None.none();
    }
    protected Option<TypedObject> trySequenceCall(Row row, TestResults testResults, Fixture fixtureByName) throws Exception {
    	if (sequencing)
    		return None.none();
    	sequencing = true;
    	try {
    		return interpretSimpleRow(row, testResults, createDoCallers(row, fixtureByName),fixtureByName);
    	} finally {
    		sequencing = false;
    	}
    }
    private Option<TypedObject> pickCaller(DoCaller[] actions, Row row, TestResults testResults) throws Exception {
		for (int i = 0; i < actions.length; i++)
			if (actions[i].isValid()) {
				TypedObject result = actions[i].run(row, testResults);
				if (getRuntimeContext().isAbandoned(testResults) && !testResults.problems())
					row.ignore(testResults);
				return new Some<TypedObject>(result);
			}
		return None.none();
    }
	public DoCaller[] createDoCallers(Row row, Fixture fixtureByName) {
		DoCaller[] actions = { 
				new DefinedActionCaller(row, this),
				new MultiDefinedActionCaller(row, this),
				new SpecialCaller(row,this,PlugBoard.lookupTarget),
				new PostFixSpecialCaller(row,this),
				new FixtureCaller(fixtureByName),
				new CreateFromClassNameCaller(row,this),
				new DoActionCaller(row,this) };
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
	public ICalledMethodTarget findMethodFromRow(Row row, int from, int extrasCellsOnEnd) throws Exception {
		return findMethodByActionName(row.rowFrom(from), row.size() - from - extrasCellsOnEnd - 1);
	}
	public ICalledMethodTarget findMethodFromRow222(Row row, int from, int less) throws Exception {
		return findMethodFromRow(row,from,less-from-1);
	}
	public CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		if (sequencing)
			return PlugBoard.lookupTarget.findTheMethodMapped(row.text(0,this), allArgs, this);
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
	public DoTraverseInterpreter switchSetUp() {
		return this;
	}
	protected Object callMethodInRow(Row row, TestResults testResults, boolean catchError, Cell operatorCell) throws Exception {
		return findMethodFromRow222(row,1, 2).invokeForSpecial(row.rowFrom(2),testResults,catchError,operatorCell);
	}
}
