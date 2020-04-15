/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;
import fit.exception.FitParseException;

public class TableFactory {
	private static boolean useOnLists = false;
	
	public static Tables tables() {
		if (useOnLists)
			return new TablesOnList();
		return new TablesOnParse();
	}
	public static Tables tables(Table table) {
		if (useOnLists)
			return new TablesOnList(table);
		return new TablesOnParse(table);
	}
	public static Tables tables(Tables tables) {
		if (useOnLists)
			return new TablesOnList(tables);
		return new TablesOnParse(tables);
	}
	public static Tables tables(String html) throws FitParseException {
		return tables(new Parse(html));
	}
	public static Tables tables(Parse parse) {
		if (useOnLists)
			throw new RuntimeException("Unable to");
		return new TablesOnParse(parse);
	}

	public static Table table() {
		if (useOnLists)
			return new TableOnList();
		return new TableOnParse();
	}
	public static Table table(Row... rows) {
		if (useOnLists)
			return new TableOnList(rows);
		return new TableOnParse(rows);
	}
	public static Table table(Parse parse) {
		if (useOnLists)
			throw new RuntimeException("Unable to");
		return new TableOnParse(parse);
	}

	public static Row row() {
		if (useOnLists)
			return new RowOnList();
		return new RowOnParse();
	}
	public static Row row(String... cellTexts) {
		Row row = row();
		for (String cellText: cellTexts)
			row.add(cell(cellText));
		return row;
	}
	public static Row row(Cell... cells) {
		Row row = row();
		for (Cell cell: cells)
			row.add(cell);
		return row;
	}
	
	public static Cell cell(String cellText) {
		if (useOnLists)
			return new CellOnList(cellText);
		return new CellOnParse(cellText);
	}
	public static Cell cell(Cell cell) {
		if (useOnLists)
			return new CellOnList(cell);
		return new CellOnParse(cell);
	}
	public static Cell cell(Tables innerTables) {
		if (useOnLists)
			return new CellOnList(innerTables);
		return new CellOnParse(innerTables);
	}
	public static Cell cell(String preamble, Tables innerTables) {
		if (useOnLists)
			return new CellOnList(preamble,innerTables);
		return new CellOnParse(preamble,innerTables);
	}
	public static void useOnLists(boolean useLists) {
		useOnLists  = useLists;
	}
}
