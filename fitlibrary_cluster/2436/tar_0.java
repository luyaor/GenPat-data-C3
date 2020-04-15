/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import java.lang.reflect.InvocationTargetException;

import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.table.IRow;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.utility.TestResults;

public class SpecialCaller extends DoCaller {
	private String methodName;
	private ICalledMethodTarget specialMethod;
	private TwoStageSpecial lazySpecial = null;

	public SpecialCaller(IRow row, Evaluator evaluator, LookupMethodTarget lookupTarget) {
		methodName = row.text(0,evaluator);
		specialMethod = lookupTarget.findSpecialMethod(evaluator, methodName);
		if (specialMethod != null && TwoStageSpecial.class.isAssignableFrom(specialMethod.getReturnType())) {
			try {
				lazySpecial = (TwoStageSpecial) specialMethod.invoke(new Object[]{row});
			} catch (InvocationTargetException e) {
				specialMethod = null;
				if (e.getCause() instanceof Exception)
					setProblem((Exception)e.getCause());
			} catch (MissingMethodException e) {
				specialMethod = null;
				setProblem(e);
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
	public Object run(IRow row, TestResults testResults) throws Exception {
		if (lazySpecial != null)
			return lazySpecial.run(testResults);
		return specialMethod.invoke(new Object[] { row, testResults });
	}
	@Override
	public String ambiguityErrorMessage() {
		return 	methodName+"(Row,TestResults)";
	}
}
