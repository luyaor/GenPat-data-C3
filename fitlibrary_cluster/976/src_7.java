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

import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.StringUtility;

public class ParameterBinder {
	private Tables tables;
	private String pageName;

	public String getPageName() {
		return pageName;
	}
	public ParameterBinder(List<String> formalParameters, Tables tables, String pageName) {
		this.tables = tables;
		this.pageName = pageName;
		Map<String,Object> mapToRef = new HashMap<String,Object>();
		for (int c = 0; c < formalParameters.size(); c++) {
			String formal = formalParameters.get(c);
			mapToRef.put(formal,paramRef(c));
		}
		macroReplace(tables,mapToRef);
	}
	public Tables substitute(List<Object> actualParameters) {
		Tables copy = tables.deepCopy();
		Map<String,Object> mapFromRef = new HashMap<String,Object>();
		for (int i = 0; i < actualParameters.size(); i++)
			mapFromRef.put(paramRef(i), actualParameters.get(i));
		macroReplace(copy, mapFromRef);
		return copy;
	}
	private static void macroReplace(Tables tables, Map<String,Object> mapToRef) {
		List<String> reverseSortOrder = new ArrayList<String>(mapToRef.keySet());
		Collections.sort(reverseSortOrder,new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				return arg1.compareTo(arg0);
			}
		});
		for (String key : reverseSortOrder)
			macroReplaceTables(tables, key, mapToRef.get(key));
	}
	private static void macroReplaceTables(Tables tables, String key, Object value) {
		for (Table table: tables) {
			for (Row row : table) {
				for (Cell cell: row)
					macroReplaceCell(cell, key, value);
			}
		}
	}
	private static void macroReplaceCell(Cell cell, String key, Object value) {
		// Do NOT do dynamic variable substitution at this stage; it has to be done dynamically.
		if (cell.hasEmbeddedTables())
			macroReplaceTables(cell.getEmbeddedTables(),key,value);
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
			cell.addTables(valueTables.deepCopy());
			cell.setLeader(text.substring(0,at));
			cell.last().setTrailer(text.substring(at+key.length()));
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
