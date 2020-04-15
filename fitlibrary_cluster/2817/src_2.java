/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.caller;

import org.apache.log4j.Logger;

import fitlibrary.closure.LookupMethodTarget;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.special.PositionedTarget;
import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.typed.TypedObject;

public class ActionSpecial extends DoCaller {
	@SuppressWarnings("unused")
	private static Logger logger = FitLibraryLogger.getLogger(ActionSpecial.class);
	private PositionedTarget positionedTarget;
	private Evaluator evaluator;

	public ActionSpecial(Row row, Evaluator evaluator, boolean sequencing, LookupMethodTarget lookupTarget) {
		this.evaluator = evaluator;
		String[] cells = new String[row.size()];
		for (int i = 0; i < row.size(); i++)
			cells[i] = row.text(i, evaluator);
		positionedTarget = lookupTarget.findActionSpecialMethod(evaluator,cells,sequencing);
	}
	@Override
	public String ambiguityErrorMessage() {
		return positionedTarget.ambiguityErrorMessage();
	}
	@Override
	public boolean isValid() {
		return positionedTarget.isFound();
	}
	@Override
	public boolean partiallyValid() {
		return positionedTarget.partiallyValid();
	}
	@Override
	public String getPartialErrorMessage() {
		return positionedTarget.getPartialErrorMessage();
	}
	@Override
	public TypedObject run(Row row, TestResults testResults) throws Exception {
		return positionedTarget.run(row,testResults,evaluator.getRuntimeContext());
	}
}
