/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.table.IRow;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.traverse.workflow.DoTraverseInterpreter;
import fitlibrary.utility.TestResults;

public class DoActionCaller extends DoCaller {
	private CalledMethodTarget target;
	private String methodName;

	public DoActionCaller(Row row, DoTraverseInterpreter switchSetUp) {
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
	public Object run(IRow row, TestResults testResults) throws Exception {
		return new ActionCaller(target).run(row, testResults);
	}
	@Override
	public String ambiguityErrorMessage() {
		return methodName+"()";
	}
}
