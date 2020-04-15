/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow;

import fit.Fixture;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.FitHandler;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;

public interface FlowEvaluator extends DoEvaluator {
	TypedObject interpretRow(Row row, TestResults testResults);
	Fixture fixtureOrDoTraverseByName(Table table, TestResults testResults);
	FitHandler fitHandler();
	void setTypedSystemUnderTest(TypedObject typedResult);
}
