/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.closure;

import fitlibrary.parser.Parser;
import fitlibrary.table.Cell;
import fitlibrary.table.ICell;
import fitlibrary.table.IRow;
import fitlibrary.traverse.workflow.DoTraverse.Comparison;
import fitlibrary.utility.TestResults;

public interface ICalledMethodTarget extends MethodTarget {
	Object invoke(Object[] arguments) throws Exception;
	Class<?> getReturnType();
	void invokeAndCheckForSpecial(IRow rowFrom, ICell expectedCell,
			TestResults testResults, IRow row, ICell cell);
	Object getResult(ICell expectedCell, TestResults testResults);
	public Object invokeForSpecial(IRow row, TestResults testResults, 
			boolean catchParseError, ICell operatorCell) throws Exception;
	void notResult(Cell expectedCell, Object result, TestResults testResults);
	Object invoke(IRow row, TestResults testResults, boolean catchParseError) throws Exception;
	public void compare(Cell expectedCell, Comparable actual, TestResults testResults, Comparison compare);
	Parser getResultParser();
	String getResultString(Object result) throws Exception;
}
