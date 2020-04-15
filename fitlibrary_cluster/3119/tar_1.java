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
import fitlibrary.utility.option.Option;

public class SpecialCaller extends DoCaller {
	private String methodName;
	private CalledMethodTarget specialMethod;
	private LazySpecial lazySpecial = null;

	public SpecialCaller(Row row, DoTraverseInterpreter switchSetUp) {
		methodName = row.text(0,switchSetUp);
		specialMethod = PlugBoard.lookupTarget.findSpecialMethod(switchSetUp, methodName);
		if (specialMethod != null && LazySpecial.class.isAssignableFrom(specialMethod.getReturnType())) {
			try {
				Option<LazySpecial> lazyOption = (Option<LazySpecial>) run(row,new TestResults());
				if (lazyOption.isSome())
					lazySpecial = lazyOption.get();
				else
					specialMethod = null;
			} catch (Exception e) {
				specialMethod = null;
			}
		}
	}
	@Override
	public boolean isValid() {
		return specialMethod != null;
	}
	@Override
	public Object run(Row row, TestResults testResults) throws Exception {
		if (lazySpecial != null)
			return lazySpecial.run(testResults);
		return specialMethod.invoke(new Object[] { row, testResults });
	}
	@Override
	public String ambiguityErrorMessage() {
		return 	methodName+"(Row,TestResults)";
	}
}
