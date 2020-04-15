/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import java.lang.reflect.InvocationTargetException;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.exception.AbandonException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.global.PlugBoard;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.typed.TypedObject;
import fitlibraryGeneric.typed.GenericTypedObject;

public class DoActionCaller extends DoCaller {
	private CalledMethodTarget target;
	private String methodName;

	public DoActionCaller(Row row, DoTraverseInterpreter doEvaluator) {
		methodName = row.methodNameForCamel(doEvaluator);
		try {
			target = doEvaluator.findMethodByActionName(row,row.size()-1);
		} catch (Exception e) {
			setProblem(e);
		}
	}
	@Override
	public boolean isValid() { // This has to be the last one, which is always run
		return target != null;
	}
	@Override
	public TypedObject run(Row row, TestResults testResults) throws Exception {
		try {
			TypedObject typedResult = target.invokeTyped(row.elementsFrom(1),testResults);
			Object result = null;
			if (typedResult != null)
				result = typedResult.getSubject();
			if (result instanceof Boolean)
				target.color(row,((Boolean)result).booleanValue(),testResults);
			return typedResult;
		} catch (AbandonException e) {
			return new GenericTypedObject(null);
		} catch (InvocationTargetException e) {
			Throwable throwable = PlugBoard.exceptionHandling.unwrapThrowable(e);
			if (throwable instanceof FitLibraryShowException)
				row.elementAt(0).error(testResults);
			throw e;
		}
	}
	@Override
	public String ambiguityErrorMessage() {
		return methodName+"()";
	}
}
