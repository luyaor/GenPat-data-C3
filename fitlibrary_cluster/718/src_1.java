/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.spec.SpecifyFixture2.SpecifyErrorReport;
import fitlibrary.suite.StorytestRunner;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.utility.TestResults;

@RunWith(JMock.class)
public class TestSpecifyFixture {
	final Mockery context = new Mockery();
	final StorytestRunner runner = context.mock(StorytestRunner.class);
	final SpecifyErrorReport errorReport = context.mock(SpecifyErrorReport.class);
	final SpecifyFixture2 specifyFixture = new SpecifyFixture2(runner, errorReport);
	final TestResults testResults = context.mock(TestResults.class);
	Tables actual = TableFactory.tables();
	Tables expected = TableFactory.tables();
	private Table table = TableFactory.table(TableFactory.row(
			TableFactory.cell(actual), TableFactory.cell(expected)));

	@Test
	public void twoEmptyTablesAreEqual() {
//		specifyFixture.interpretAfterFirstRow(table,testResults);
	}
}
