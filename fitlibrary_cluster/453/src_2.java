/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;
import fit.exception.FitParseException;

public class TableFactory {
	public static Tables tables() {
		return new TablesOnParse();
	}
	public static Tables tables(Table table) {
		return new TablesOnParse(table);
	}
	public static Tables tables(Tables tables) {
		return new TablesOnParse(tables);
	}
	public static Tables tables(String html) throws FitParseException {
		return new TablesOnParse(new Parse(html));
	}
	public static Tables tables(Parse parse) {
		return new TablesOnParse(parse);
	}

	public static Table table() {
		return new TableOnParse();
	}
	public static Table table(Row... rows) {
		return new TableOnParse(rows);
	}
	public static Table table(Parse parse) {
		return new TableOnParse(parse);
	}

	public static Row row() {
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
		return new CellOnParse(cellText);
	}
	public static Cell cell(Cell cell) {
		return new CellOnParse(cell);
	}
	public static Cell cell(Tables innerTables) {
		return new CellOnParse(innerTables);
	}
	public static Cell cell(String preamble, Tables innerTables) {
		return new CellOnParse(preamble,innerTables);
	}
}
