/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fit.exception.FitFailureException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.StringUtility;

public class ParameterSubstitution {
	private Tables tables;
	private String pageName;

	public String getPageName() {
		return pageName;
	}
	public ParameterSubstitution(List<String> formalParameters, Tables tables, Evaluator evaluator, String pageName) {
		this.tables = tables;
		this.pageName = pageName;
		Map<String,Object> mapToRef = new HashMap<String,Object>();
		for (int c = 0; c < formalParameters.size(); c++) {
			String formal = formalParameters.get(c);
			if (mapToRef.get(formal) != null)
				throw new FitFailureException("Duplicated parameter: "+formal);
			mapToRef.put(formal,paramRef(c));
		}
		macroReplace(tables,mapToRef,evaluator);
	}
	public Tables substitute(List<Object> actualParameters, Evaluator evaluator) {
		Tables copy = tables.deepCopy();
		Map<String,Object> mapFromRef = new HashMap<String,Object>();
		for (int i = 0; i < actualParameters.size(); i++)
			mapFromRef.put(paramRef(i), actualParameters.get(i));
		macroReplace(copy, mapFromRef,evaluator);
		return copy;
	}
	private static void macroReplace(Tables tables, Map<String,Object> mapToRef, Evaluator evaluator) {
		List<String> reverseSortOrder = new ArrayList<String>(mapToRef.keySet());
		Collections.sort(reverseSortOrder,new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				return arg1.compareTo(arg0);
			}
		});
		for (String key : reverseSortOrder)
			macroReplaceTables(tables, key, mapToRef.get(key),evaluator);
	}
	private static void macroReplaceTables(Tables tables, String key, Object value, Evaluator evaluator) {
		for (int t = 0; t < tables.size(); t++) {
			Table table = tables.table(t);
			for (int r = 0 ; r < table.size(); r++) {
				Row row = table.row(r);
				for (int c = 0; c < row.size(); c++)
					macroReplaceCell(row.cell(c), key, value,evaluator);
			}
		}
	}
	private static void macroReplaceCell(Cell cell, String key, Object value, Evaluator evaluator) {
		// Do NOT do dynamic variable substitution at this stage; it has to be done dynamically.
		if (cell.hasEmbeddedTable())
			macroReplaceTables(cell.getEmbeddedTables(),key,value,evaluator);
		String text = cell.fullText();
		if (value instanceof String) {
			String update = StringUtility.replaceString(text, key, (String)value);
			if (!update.equals(text))
				cell.setText(update);
		} else { // Embedded tables: Just replace once
			Tables valueTables = (Tables) value;
			int at = text.indexOf(key);
			if (at < 0)
				return;
			Tables addedTables = valueTables.deepCopy();
			if (cell.hasEmbeddedTable())
				cell.getEmbeddedTables().parse.last().more = addedTables.parse;
			else
				cell.setInnerTables(addedTables);
			cell.getEmbeddedTables().parse.leader = text.substring(0,at);
			cell.getEmbeddedTables().parse.last().trailer = text.substring(at+key.length());
		}
	}
	private static String paramRef(int c) {
		return "%__%"+c+"%__%";
	}
	@Override
	public String toString() {
		return "MacroSubstitution["+tables.toString()+"]";
	}
}
