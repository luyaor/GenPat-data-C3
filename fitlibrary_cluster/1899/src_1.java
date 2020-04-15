/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.global.PlugBoard;
import fitlibrary.table.IRow;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;

public class PostFixSpecialCaller extends DoCaller {
	private String methodName;
	private CalledMethodTarget specialMethod;

	public PostFixSpecialCaller(Row row, DoTraverseInterpreter interpreter) {
		// Warning: Hack to fix conflict between "set" and "=", by giving "set" precedence.
		String firstCell = row.text(0,interpreter);
		if (row.size() == 4 && "=".equals(row.text(2,interpreter)) && 
		    ("set".equals(firstCell) || "setSymbolNamed".equals(ExtendedCamelCase.camel(firstCell))))
				return;
		if (row.size() >= 2) {
			methodName = row.text(row.size()-2,interpreter);
			specialMethod = PlugBoard.lookupTarget.findPostfixSpecialMethod(interpreter, methodName);
			if (specialMethod != null)
				findMethodForInnerAction(row, interpreter);
		}
	}
	private void findMethodForInnerAction(Row row, DoTraverseInterpreter interpreter) {
		try {
			interpreter.findMethodFromRow222(row,0,3);
		} catch (Exception e) {
			setProblem(e);
		}
	}
	@Override
	public boolean isValid() {
		return specialMethod != null && !isProblem();
	}
	@Override
	public Object run(IRow row, TestResults testResults) throws Exception {
		return specialMethod.invoke(new Object[] { testResults, row });
	}
	@Override
	public String ambiguityErrorMessage() {
		return 	methodName+"(TestResults,Row)";
	}
}
