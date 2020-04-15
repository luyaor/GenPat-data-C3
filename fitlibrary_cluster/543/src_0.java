/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import fitlibrary.utility.ParseUtility;

public class TestParseToTableOnList {
	@Test public void convertToListForm() {
		Tables tables = TableFactory.tables(TableFactory.table(TableFactory.row("a","b")));
		assertThat(tables,is(TablesOnParse.class));
		tables.at(0).setLeader("LL");
		tables.at(0).setTrailer("TT");
		tables.at(0).addToTag("RR");
		tables.at(0).at(0).setLeader("00LL");
		tables.at(0).at(0).setTrailer("00TT");
		tables.at(0).at(0).at(0).addToTag(" 00RR");

		TableFactory.useOnLists(true);
		Tables resultingTables = ParseUtility.convert(tables);
		TableFactory.useOnLists(false);
		
		assertThat(resultingTables,is(TablesOnList.class));
		Table resultingTable = resultingTables.at(0);
		assertThat(resultingTable,is(TableOnList.class));
		assertThat(resultingTable.getLeader(),is("LL"));
		assertThat(resultingTable.getTrailer(),is("TT"));
		assertThat(resultingTable.getTagLine(),is("border=\"1\" cellspacing=\"0\"RR"));
		assertThat(resultingTables.size(),is(1));
		assertThat(resultingTable.size(),is(1));
		Row resultingRow = resultingTable.at(0);
		assertThat(resultingRow,is(RowOnList.class));
		assertThat(resultingRow.getLeader(),is("00LL"));
		assertThat(resultingRow.getTrailer(),is("00TT"));
		assertThat(resultingRow.size(),is(2));
		Cell firstCell = resultingRow.at(0);
		assertThat(firstCell,is(CellOnList.class));
		assertThat(firstCell.getTagLine(),is("00RR"));
		assertThat(firstCell.text(),is("a"));
		assertThat(resultingRow.at(1).text(),is("b"));
	}
	@Test public void convertToParseForm() {
		TableFactory.useOnLists(true);
		Tables tables = TableFactory.tables(TableFactory.table(TableFactory.row("a","b")));
		assertThat(tables,is(TablesOnList.class));
		TableFactory.useOnLists(false);
		tables.at(0).setLeader("LL");
		tables.at(0).setTrailer("TT");
		tables.at(0).addToTag("RR");
		tables.at(0).at(0).setLeader("00LL");
		tables.at(0).at(0).setTrailer("00TT");
		tables.at(0).at(0).at(0).addToTag(" 00RR");

		Tables resultingTables = ParseUtility.convert(tables);
		
		assertThat(resultingTables,is(TablesOnParse.class));
		Table resultingTable = resultingTables.at(0);
		assertThat(resultingTable,is(TableOnParse.class));
		assertThat(resultingTable.getLeader(),is("LL"));
		assertThat(resultingTable.getTrailer(),is("TT"));
		assertThat(resultingTable.getTagLine(),is(" border=\"1\" cellspacing=\"0\"RR"));
		assertThat(resultingTables.size(),is(1));
		assertThat(resultingTable.size(),is(1));
		Row resultingRow = resultingTable.at(0);
		assertThat(resultingRow,is(RowOnParse.class));
		assertThat(resultingRow.getLeader(),is("00LL"));
		assertThat(resultingRow.getTrailer(),is("00TT"));
		assertThat(resultingRow.size(),is(2));
		Cell resultingFirstCell = resultingRow.at(0);
		assertThat(resultingFirstCell,is(CellOnParse.class));
		assertThat(resultingFirstCell.getTagLine(),is(" 00RR"));
		assertThat(resultingFirstCell.text(),is("a"));
		assertThat(resultingRow.at(1).text(),is("b"));
	}
}
