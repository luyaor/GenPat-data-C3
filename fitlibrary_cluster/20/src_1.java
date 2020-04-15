/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.table.ICell;
import fitlibrary.table.IRow;
import fitlibrary.traverse.workflow.caller.LazySpecial;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;

public class SpecialAction {
	protected final SpecialActionContext actionContext;
	
	public SpecialAction(SpecialActionContext actionContext) {
		this.actionContext = actionContext;
	}
	public Option<LazySpecial> check(final IRow row, final TestResults testResults) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("DoTraverseCheck");
			final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,less);
			final ICell expectedCell = row.last();
			return new Some<LazySpecial>(new LazySpecial() {
				@Override
				public Object run(TestResults testResults) {
					if (actionContext.isGatherExpectedForGeneration()) // This needs to use a copy of the row, otherwise duplicates error messages
						actionContext.setExpectedResult(target.getResult(expectedCell,testResults));
					target.invokeAndCheckForSpecial(row.rowFrom(2),expectedCell,testResults,row,row.cell(0));
					return null;
				}
			});
	}
}
