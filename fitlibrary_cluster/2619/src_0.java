/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fitlibrary.exception.AbandonException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.method.AmbiguousActionException;
import fitlibrary.exception.method.AmbiguousNameException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.global.PlugBoard;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.caller.ActionSpecial;
import fitlibrary.traverse.workflow.caller.CreateFromClassNameCaller;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;
import fitlibrary.traverse.workflow.caller.DoActionCaller;
import fitlibrary.traverse.workflow.caller.MultiDefinedActionCaller;
import fitlibrary.traverse.workflow.caller.PostFixSpecialCaller;
import fitlibrary.traverse.workflow.caller.SpecialCaller;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;
import fitlibraryGeneric.typed.GenericTypedObject;

public class DispatchRowInFlow {
	@SuppressWarnings("unused")
	private static Logger logger = FitLibraryLogger.getLogger(DispatchRowInFlow.class);
	private final Evaluator evaluator;
	private final boolean sequencing;
	private boolean dynamicSequencing;
	
	public DispatchRowInFlow(Evaluator evaluator, boolean sequencing) {
		this.evaluator = evaluator;
		this.sequencing = sequencing;
		this.dynamicSequencing = sequencing;
	}
    public TypedObject interpretRowBeforeWrapping(Row row, TestResults testResults) {
    	try {
    		Option<TypedObject> result = pickCaller(createDoCallers(row,sequencing), row, testResults);
    		if (result.isSome())
    			return result.get();
    		if (row.size() > 2 && !sequencing) {
        		dynamicSequencing = true;
        		try {
        			Option<TypedObject> seqResult = pickCaller(createDoCallers(row,true), row, testResults);
        			if (seqResult.isSome())
        				return seqResult.get();
        		} finally {
        			dynamicSequencing = false;
        		}
    		}
    		Option<String> partialError = pickPartial(createDoCallers(row,sequencing));
    		if (partialError.isSome())
    			throw new FitLibraryExceptionInHtml(partialError.get());
    		methodsAreMissing(createDoCallers(row,sequencing),possibleSeq(row));
    	} catch (IgnoredException ex) {
    		//
    	} catch (AbandonException e) {
    		row.ignore(testResults);
    	} catch (Exception ex) {
    		row.error(testResults, ex);
    	}
    	return GenericTypedObject.NULL;
    }
	public boolean isDynamicSequencing() {
		return dynamicSequencing;
	}
    private Option<String> pickPartial(DoCaller[] actions) {
		for (int i = 0; i < actions.length; i++)
			if (actions[i].partiallyValid())
				return new Some<String>(actions[i].getPartialErrorMessage());
		return None.none();
	}
	private Option<TypedObject> pickCaller(DoCaller[] actions, Row row, TestResults testResults) throws Exception {
		for (int i = 0; i < actions.length; i++)
			if (actions[i].isValid()) {
				TypedObject result = actions[i].run(row, testResults);
				if (evaluator.getRuntimeContext().isAbandoned(testResults) && !testResults.problems())
					row.ignore(testResults);
				return new Some<TypedObject>(result);
			}
		return None.none();
    }
	private DoCaller[] createDoCallers(Row row, boolean sequenced) {
		DoCaller[] actions = { 
				new DefinedActionCaller(row, evaluator.getRuntimeContext()),
				new MultiDefinedActionCaller(row, evaluator.getRuntimeContext()),
				new SpecialCaller(row,evaluator,PlugBoard.lookupTarget),
				new PostFixSpecialCaller(row,evaluator,sequenced),
				new CreateFromClassNameCaller(row,evaluator),
				new ActionSpecial(row,evaluator,sequenced,PlugBoard.lookupTarget),
				new DoActionCaller(row,evaluator,sequenced,PlugBoard.lookupTarget)
		};
		checkForAmbiguity(actions);
		return actions;
	}
	private String possibleSeq(Row row) {
		if (row.size() < 3)
			return "";
		String result = "public Type "+ExtendedCamelCase.camel(row.text(0,evaluator))+"(";
		if (row.size() > 0)
			result += "Type p1";
		for (int i = 2; i < row.size(); i++)
			result += ", Type p"+i;
		return result+") {}";
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
}
