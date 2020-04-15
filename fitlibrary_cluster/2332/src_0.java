/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import static fitlibrary.matcher.TableBuilderForTests.cell;
import static fitlibrary.matcher.TableBuilderForTests.row;
import static fitlibrary.matcher.TableBuilderForTests.table;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.matcher.TableBuilderForTests.CellBuilder;
import fitlibrary.matcher.TableBuilderForTests.TableBuilder;
import fitlibrary.runResults.TestResults;
import fitlibrary.spec.SpecifyFixture2.SpecifyErrorReport;
import fitlibrary.suite.StorytestRunner;
import fitlibrary.table.Cell;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

@RunWith(JMock.class)
public class TestSpecifyFixture {
	final Mockery context = new Mockery();
	final StorytestRunner runner = context.mock(StorytestRunner.class);
	final SpecifyErrorReport errorReport = context.mock(SpecifyErrorReport.class);
	final SpecifyFixture2 specifyFixture = new SpecifyFixture2(runner, errorReport);
	final TestResults testResults = context.mock(TestResults.class);
	Tables actual = TableFactory.tables();
	Tables expected = TableFactory.tables();

	@Test
	public void anExceptionIsThrownIfNoTablesForActual() {
		final Table table = specifyingTable(cell(), cell());
		context.checking(new Expectations() {{
			oneOf(table).error(with(testResults),with(any(FitLibraryException.class)));
		}});
		specifyFixture.interpretAfterFirstRow(table,testResults);
	}
	@Test
	public void singleTableAndTextMatches() {
		final Table table = specifyingTable(
				cell("").with(singleRowTable(cell("abc"),cell("de"),cell("fg"))),
				cell("").with(singleRowTable(cell("abc"),cell("de"),cell("fg")))
		);
		storytestIsCalled(table);
		context.checking(new Expectations() {{
			oneOf(expectedCell(table)).pass(testResults);
			oneOf(testResults).addRights(2);
		}});
		specifyFixture.interpretAfterFirstRow(table,testResults);
	}
	@Test
	public void singleTableButTextDoesNotMatch() {
		final Table table = specifyingTable(
				cell("").with(singleRowTable(cell("ab"))),
				cell("").with(singleRowTable(cell("cd")))
		);
		storytestIsCalled(table);
		context.checking(new Expectations() {{
			oneOf(errorReport).cellTextWrong("Table[0].Row[0].Cell[0]","ab","cd");
			oneOf(expectedCell(table)).fail(testResults);
			oneOf(errorReport).actualResult(actualCell(table));
		}});
		specifyFixture.interpretAfterFirstRow(table,testResults);
	}
	
	
	
	protected Cell actualCell(Table table) {
		return table.elementAt(0).elementAt(0);
	}
	protected Cell expectedCell(Table table) {
		return table.elementAt(0).elementAt(1);
	}
	private Table specifyingTable(CellBuilder actualCell, CellBuilder expectedCell) {
		return table().with(row().with(actualCell,expectedCell))
			.expect(context);
	}
	private TableBuilder singleRowTable(CellBuilder... cells) {
		return table().with(row().with(cells));
	}
	private void storytestIsCalled(final Table table) {
		context.checking(new Expectations() {{
			oneOf(runner).doStorytest(table.elementAt(0).elementAt(0));
		}});
	}
}
