/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.global.PlugBoard;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.TestResults;

public class SpecialCaller extends DoCaller {
	private String methodName;
	private CalledMethodTarget specialMethod;

	public SpecialCaller(Row row, DoTraverseInterpreter switchSetUp) {
		methodName = row.text(0,switchSetUp);
		specialMethod = PlugBoard.lookupTarget.findSpecialMethod(switchSetUp, methodName);
	}
	@Override
	public boolean isValid() {
		return specialMethod != null;
	}
	@Override
	public Object run(Row row, TestResults testResults) throws Exception {
		return specialMethod.invoke(new Object[] { row, testResults });
	}
	@Override
	public String ambiguityErrorMessage() {
		return 	methodName+"(Row,TestResults)";
	}
}
