/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import fit.Fixture;
import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.global.PlugBoard;
import fitlibrary.table.IRow;
import fitlibrary.table.Row;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;

public class SequenceTraverse extends DoTraverse {
	public SequenceTraverse(Object sut) {
		super(sut);
	}
	@Override
	public CalledMethodTarget findMethodByActionName(IRow row, int allArgs) throws Exception {
		return PlugBoard.lookupTarget.findTheMethodMapped(row.text(0,this), allArgs, this);
	}
    @Override
	protected Option<TypedObject> trySequenceCall(Row row, TestResults testResults, Fixture fixtureByName) throws Exception {
		return None.none();
    }
}
