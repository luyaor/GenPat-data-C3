/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import static fitlibrary.matcher.TableBuilderForTests.row;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.runResults.TableListener;
import fitlibrary.runResults.TestResults;

@RunWith(JMock.class)
public class TestTableOnListWithTestResults {
	Mockery context = new Mockery();
	TestResults testResults = context.mock(TestResults.class);
	VariableResolver resolver = context.mock(VariableResolver.class);
	Row row0 = row().mock(context, "", 0);
	Row row1 = row().mock(context, "", 1);
	Row row2 = row().mock(context, "", 2);
	Table table12 = table(row0,row1);
	Table table123 = table(row0,row1,row2);
	
	@Before
	public void useListsFactory() {
		TableFactory.useOnLists(true);
	}
	@After
	public void stopUsingListsFactory() {
		TableFactory.pop();
	}
	@Test
	public void pass() {
		context.checking(new Expectations() {{
			oneOf(row0).pass(testResults);
		}});
		table12.pass(testResults);
	}
	@Test
	public void ignore() {
		context.checking(new Expectations() {{
			oneOf(row0).ignore(testResults);
		}});
		table12.ignore(testResults);
	}
	@Test
	public void error() {
		final RuntimeException e = new RuntimeException();
		context.checking(new Expectations() {{
			oneOf(row0).error(testResults,e);
		}});
		table12.error(testResults, e);
	}
	@Test
	public void errorWithTableListener() {
		final RuntimeException e = new RuntimeException();
		context.checking(new Expectations() {{
			oneOf(row0).error(testResults,e);
		}});
		table12.error(new TableListener(testResults), e);
	}
	@Test
	public void newRow() {
		table12.newRow();
		assertThat(table12.size(),is(3));
	}
	@Test
	public void withDummyFirstRow() {
		Table withDummyFirstRow = table12.withDummyFirstRow();
		assertThat(withDummyFirstRow.size(),is(3));
		assertThat(withDummyFirstRow.at(1),is(table12.at(0)));
		assertThat(withDummyFirstRow.at(2),is(table12.at(1)));
	}
	@Test
	public void phaseBoundaryCount() {
		table12.setLeader("12<hr/>34<hr/>456<hr/>");
		assertThat(table12.phaseBoundaryCount(),is(2));
	}
	@Test
	public void addFoldingText() {
		table12.addFoldingText("12");
		assertThat(table12.getTrailer(),is("12"));
	}
	@Test
	public void replaceAt() {
		table12.replaceAt(0,row2);
		assertThat(table12.size(),is(2));
		assertThat(table12.at(0),is(row2));
		assertThat(table12.at(1),is(row1));
	}
	@Test
	public void hasRowsAfter() {
		assertThat(table12.hasRowsAfter(row0),is(true));
		assertThat(table12.hasRowsAfter(row1),is(false));
		assertThat(table12.hasRowsAfter(row2),is(false)); // Not in the table!
	}
	@Test
	public void deepCopy() {
		final Row row1copy = row().mock(context, "", 55);
		final Row row2copy = row().mock(context, "", 66);
		context.checking(new Expectations() {{
			oneOf(row0).deepCopy(); will(returnValue(row1copy));
			oneOf(row1).deepCopy(); will(returnValue(row2copy));
		}});
		table12.setLeader("LL");
		table12.setTrailer("TT");
		Table deepCopy = table12.deepCopy();
		assertThat(deepCopy.size(), is(2));
		assertThat(deepCopy.at(0), is(row1copy));
		assertThat(deepCopy.at(1), is(row2copy));
		assertThat(deepCopy.getLeader(), is("LL"));
		assertThat(deepCopy.getTrailer(), is("TT"));
	}
	@Test public void toHtml() {
		final StringBuilder stringBuilder = new StringBuilder();
		context.checking(new Expectations() {{
			oneOf(row0).toHtml(stringBuilder);
			oneOf(row1).toHtml(stringBuilder);
		}});
		table12.setLeader("LL");
		table12.setTrailer("TT");
		table12.toHtml(stringBuilder);
		assertThat(stringBuilder.toString(),is("LL<table border=\"1\" cellspacing=\"0\"></table>TT"));
	}

	
	
	protected static Table table(Row... rows) {
		Table table = new TableOnList();
		for (Row row: rows)
			table.add(row);
		return table;
	}
}
