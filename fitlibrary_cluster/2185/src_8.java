/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
/*
 * Includes extension by Jacques Morel to allow for tables to be passed as parameters
 */
package fitlibrary.definedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class DefinedActionTraverse extends Traverse {
	private Tables body;

	public DefinedActionTraverse() {
		//
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		// Just check the parameter names here. Things happen in the call...
		Row header = table.row(1);
		Set<String> parameterNames = new HashSet<String>();
		for (int c = 0; c < header.size(); c++) {
			Cell parameterCell = header.cell(c);
			String parameterName = parameterCell.text(this);
			if (parameterNames.contains(parameterName))
				parameterCell.error(testResults, new FitLibraryException("Duplicate parameter names"));
			parameterNames.add(parameterName);
		}
		return null;
	}
	protected DefinedActionTraverse(Table defTable, int parameterCount) {
		Row header = defTable.row(1);
		if (header.size() != parameterCount)
			throw new FitLibraryException("Mismatch in number of parameters to template");
		Map<String,Object> mapToRef = new HashMap<String,Object>();
		for (int c = 0; c < header.size(); c++)
			mapToRef.put(header.text(c,this),paramRef(c));
		body = new Tables(ParseUtility.copyParse(defTable.row(2).cell(0).innerTables().parse()));
		macroReplace(body,mapToRef);
	}
	public Tables call(List<Object> parameters, TestResults results) {
		Tables copy = new Tables(ParseUtility.copyParse(body.parse()));
		substitute(parameters, copy);
		executeInstantiatedAction(results, copy);
		return copy;
	}
	// Added for Jacques Morel
	protected void executeInstantiatedAction(TestResults results, Tables copy) {
		new BatchFitLibrary(new TableListener(results)).doTables(copy);
	}
	private void substitute(List<Object> parameters, Tables copy) {
		Map<String,Object> mapFromRef = new HashMap<String,Object>();
		for (int i = 0; i < parameters.size(); i++)
			mapFromRef.put(paramRef(i), parameters.get(i));
		macroReplace(copy, mapFromRef);
	}
	private void macroReplace(Tables tables, Map<String,Object> mapToRef) {
		List<String> reverseSortOrder = new ArrayList<String>(mapToRef.keySet());
		Collections.sort(reverseSortOrder,new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				return arg1.compareTo(arg0);
			}
		});
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.table(t);
			for (int r = 0 ; r < table.size(); r++) {
				Row row = table.row(r);
				for (int c = 0; c < row.size(); c++) {
					Cell cell = row.cell(c);
					String text = cell.text(this);
					for (String key : reverseSortOrder) {
						if (text.contains(key)) {
							Object value = mapToRef.get(key);
							if (value instanceof Table) {
								cell.setInnerTables(new Tables((Table) value));
							}
							else  {
								text = text.replaceAll(key,(String) value);
								cell.setText(text);
							}
						}
					}
				}
			}
		}
	}
	private static String paramRef(int c) {
		return "%__%"+c+"%__%";
	}
}
