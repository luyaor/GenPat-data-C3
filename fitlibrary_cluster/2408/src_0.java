/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import fitlibrary.DoFixture;
import fitlibrary.parser.ParserTestCase;

public class TestRowOnParse {
	RowOnParse row = new RowOnParse();
	DoFixture evaluator = ParserTestCase.evaluatorWithRuntime();
	
	@Test
	public void sizeOfEmptyRow() {
		assertThat(row.size(),is(0));
		assertThat(row.isEmpty(),is(true));
	}
	@Test public void columnSpanWithNoCellsIsZero() throws Exception {
		assertThat(row.getColumnSpan(),is(0));
	}
	@Test public void columnSpanWithOneCellWithNoColumnSpanSetIsOne() throws Exception {
		row.addCell("1");
		assertThat(row.getColumnSpan(),is(1));
	}
	@Test public void columnSpanWithOneCellWithColumnSpanSet() throws Exception {
		row.addCell("1", 4);
		assertThat(row.getColumnSpan(),is(4));
	}
	@Test public void columnSpanWithMultipleCellsWithAndWithoutColumnSpanSet() throws Exception {
		row.addCell("1", 4);
		row.addCell("2");
		row.addCell("3", 2);
		row.addCell("4");
		assertThat(row.getColumnSpan(),is(8));
	}
	@Test public void setColumnSpanWithNoCellsHadsOneCell() {
		row.setColumnSpan(4);
		assertThat(row.getColumnSpan(),is(4));
		assertThat(row.cell(0).getColumnSpan(),is(4));
	}
	@Test public void setColumnSpanWithOneCellWithNoColumnSpanSet() {
		row.addCell("1");
		row.setColumnSpan(4);
		assertThat(row.getColumnSpan(),is(4));
		assertThat(row.cell(0).getColumnSpan(),is(4));
	}
	@Test public void setColumnSpanWithOneCellWithColumnSpanSet() {
		row.addCell("1", 3);
		row.setColumnSpan(4);
		assertThat(row.getColumnSpan(),is(4));
		assertThat(row.cell(0).getColumnSpan(),is(4));
	}
	@Test public void setColumnSpanWithMultipleCellsWithAndWithoutColumnSpanSetAddToTheColumnSpanOfLastCell() throws Exception {
		row.addCell("1", 4);
		row.addCell("2");
		row.addCell("3", 2);
		row.addCell("4",4);
		row.setColumnSpan(15);
		assertThat(row.getColumnSpan(),is(15));
		assertThat(row.cell(0).getColumnSpan(),is(4));
		assertThat(row.cell(1).getColumnSpan(), is(1));
		assertThat(row.cell(2).getColumnSpan(),is(2));
		assertThat(row.cell(3).getColumnSpan(),is(8));
	}
	@Test
	public void plainMethodNameNoArg() {
		RowOnParse row2 = new RowOnParse("aa");
		assertThat(row2.methodNameForPlain(evaluator),is("aa"));
		assertThat(row2.methodNameForCamel(evaluator),is("aa"));
	}
	@Test
	public void plainMethodNameOneArg() {
		RowOnParse row2 = new RowOnParse("aa","1");
		assertThat(row2.methodNameForPlain(evaluator),is("aa|"));
		assertThat(row2.methodNameForCamel(evaluator),is("aa"));
	}
	@Test
	public void plainMethodNameOneArgTwoKeywords() {
		RowOnParse row2 = new RowOnParse("aa","1","bb");
		assertThat(row2.methodNameForPlain(evaluator),is("aa|bb"));
		assertThat(row2.methodNameForCamel(evaluator),is("aaBb"));
	}
	@Test
	public void plainMethodNameTwoArgs() {
		RowOnParse row2 = new RowOnParse("aa","1","bb","2");
		assertThat(row2.methodNameForPlain(evaluator),is("aa|bb|"));
		assertThat(row2.methodNameForCamel(evaluator),is("aaBb"));
	}
	@Test
	public void plainMethodNameTwoArgsThreeKeywords() {
		RowOnParse row2 = new RowOnParse("aa","1","bb","2",".");
		assertThat(row2.methodNameForPlain(evaluator),is("aa|bb|."));
		assertThat(row2.methodNameForCamel(evaluator),is("aaBbDot"));
	}
	@Test
	public void canReplaceRowAtStart() {
		Row row0 = new RowOnParse("a","b");
		Row row1 = new RowOnParse("x","y");
		Row row2 = new RowOnParse("m","n");
		Table table = new TableOnParse();
		table.addRow(row0);
		table.addRow(row1);
		assertThat(table.row(0),is(row0));
		assertThat(table.row(1),is(row1));
		table.replaceAt(0, row2);
		assertThat(table.row(0),is(row2));
		assertThat(table.row(1),is(row1));
	}
	@Test
	public void canReplaceRowAtEnd() {
		Row row0 = new RowOnParse("a","b");
		Row row1 = new RowOnParse("x","y");
		Row row2 = new RowOnParse("m","n");
		Table table = new TableOnParse();
		table.addRow(row0);
		table.addRow(row1);
		table.replaceAt(1, row2);
		assertThat(table.row(0),is(row0));
		assertThat(table.row(1),is(row2));
	}
}
