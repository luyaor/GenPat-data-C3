/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse;

import fit.Fixture;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.utility.TestResults;

public class FitHandler {
	public void doTable(Object result, Table table, TestResults testResults, Evaluator evaluator) {
		Fixture fixture = (Fixture) result;
		fixture.counts = testResults.getCounts();
		substituteDynamicVariables(table, evaluator);
		fixture.doTable(table.parse());
	}
	private void substituteDynamicVariables(Table table, Evaluator evaluator) {
		for (int r = 0; r < table.size(); r++) {
			Row row = table.row(r);
			for (int c = 0; c < row.size(); c++) {
				Cell cell = row.cell(c);
				String text = cell.text(evaluator);
				if (!text.equals(cell.text()))
					cell.setText(text);
			}
		}
	}
}
