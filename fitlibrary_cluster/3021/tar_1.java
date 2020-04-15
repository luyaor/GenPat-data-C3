/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Table;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public interface Evaluator extends RuntimeContextual {
	Object getOutermostContext();
	Evaluator getNextOuterContext();
	void setOuterContext(Evaluator outerContext);
	Object interpretAfterFirstRow(Table table, TestResults testResults);
	TypedObject getTypedSystemUnderTest();
    RuntimeContextInternal getRuntimeContext();
	void setDynamicVariable(String key, Object value);
}