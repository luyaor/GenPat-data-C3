/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.table.IRow;

public interface SpecialActionContext {
	ICalledMethodTarget findMethodFromRow(IRow row, int i, int less) throws Exception ;
	boolean isGatherExpectedForGeneration();
	void setExpectedResult(Object result);
}
