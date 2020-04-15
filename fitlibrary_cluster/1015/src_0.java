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
import fitlibrary.utility.option.Option;

public class SpecialCaller extends DoCaller {
	private String methodName;
	private ICalledMethodTarget specialMethod;
	private LazySpecial lazySpecial = null;

	public SpecialCaller(IRow row, Evaluator evaluator, LookupMethodTarget lookupTarget) {
		methodName = row.text(0,evaluator);
		specialMethod = lookupTarget.findSpecialMethod(evaluator, methodName);
		if (specialMethod != null && Option.class.isAssignableFrom(specialMethod.getReturnType())) {
			try {
				Option<LazySpecial> lazyOption = (Option<LazySpecial>) invokeSpecialMethod(row,new TestResults());
				if (lazyOption.isSome())
					lazySpecial = lazyOption.get();
				else
					specialMethod = null;
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
		return invokeSpecialMethod(row, testResults);
	}
	private Object invokeSpecialMethod(IRow row, TestResults testResults) throws Exception {
		return specialMethod.invoke(new Object[] { row, testResults });
	}
	@Override
	public String ambiguityErrorMessage() {
		return 	methodName+"(Row,TestResults)";
	}
}
