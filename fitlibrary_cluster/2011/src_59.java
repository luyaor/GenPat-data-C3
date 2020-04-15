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
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.TestResults;

public class ActionCaller extends DoCaller {
	private CalledMethodTarget target;
	private String methodName;

	public ActionCaller(Row row, DoTraverseInterpreter switchSetUp) {
		methodName = row.methodNameForCamel(switchSetUp);
		try {
			target = switchSetUp.findMethodByActionName(row,row.size()-1);
		} catch (Exception e) {
			setProblem(e);
		}
	}
	@Override
	public boolean isValid() { // This has to be the last one, which is always run
		return target != null;
	}
	@Override
	public Object run(Row row, TestResults testResults) throws Exception {
		try {
			Object result = target.invokeAndWrap(row.rowFrom(1),testResults);
			if (result instanceof Boolean)
				target.color(row,((Boolean)result).booleanValue(),testResults);
			return result;
		} catch (AbandonException e) {
			return null;
		} catch (InvocationTargetException e) {
    		Throwable throwable = PlugBoard.exceptionHandling.unwrapThrowable(e);
    		if (throwable instanceof FitLibraryShowException)
    			row.cell(0).error(testResults);
    		throw e;
		}
	}
	@Override
	public String ambiguityErrorMessage() {
		return methodName+"()";
	}
}
