/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import fit.Fixture;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.utility.TestResults;

public class FixtureCaller extends DoCaller {
	private Fixture fixtureByName;

	public FixtureCaller(Fixture fixtureByName) {
		this.fixtureByName = fixtureByName;
	}
	@Override
	public boolean isValid() {
		return fixtureByName != null;
	}
	@Override
	@SuppressWarnings("unused")
	public Object run(Row row, TestResults testResults) throws Exception {
		return fixtureByName;
	}
	@Override
	public String ambiguityErrorMessage() {
		return "fixture "+fixtureByName.getClass().getName();
	}
}
