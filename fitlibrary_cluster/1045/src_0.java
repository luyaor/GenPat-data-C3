/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.matcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Iterator;

import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import fitlibrary.exception.FitLibraryException;
import static fitlibrary.matcher.TableBuilderForTests.*;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoTraverse;

@RunWith(JMock.class)
public class TestTableBuilderForTests {
	final Mockery context = new Mockery();

	@Test
	public void emptyTablesHasSizeOf0() {
		Tables tables = tables().expect(context);
		assertThat(tables.size(), is(0));
	}
	@Test(expected=FitLibraryException.class)
	public void emptyTablesHasNoLast() {
		Tables tables = tables().expect(context);
		tables.last();
	}
	@Test(expected=ExpectationError.class)
	public void emptyTablesHasNoFirst() {
		Tables tables = tables().expect(context);
		tables.elementAt(0);
	}
	@Test
	public void emptyTablesHasEmptyIterator() {
		Tables tables = tables().expect(context);
		assertThat(tables.iterator().hasNext(), is(false));
	}
	@Test
	public void cellHasTextBasedOnPath() {
		assertThat(cell().expect(context).text(), is("Cell[0]"));
		assertThat(row().with(cell()).expect(context).elementAt(0).text(), is("Row[0].Cell[0]"));
	}
	@Test
	public void cellHasNoEmbedded() {
		assertThat(cell().expect(context).hasEmbeddedTables(), is(false));
	}
	@Test
	public void cellHasSpecifiedText() {
		assertThat(cell("abc").expect(context).text(), is("abc"));
	}
	@Test
	public void rowOfOneHasSizeOf1() {
		RowBuilder rowBuilder = row().
			with(cell());
		Row row = rowBuilder.expect(context);
		assertThat(row.size(), is(1));
		assertThat(row.last(), sameInstance(row.elementAt(0)));
		Iterator<Cell> iterator = row.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), sameInstance(row.elementAt(0)));
		assertThat(iterator.hasNext(), is(false));
	}
	@Test
	public void rowOfTwoHasSizeOf2() {
		RowBuilder rowBuilder = row().
			with(cell(),cell());
		Row row = rowBuilder.expect(context);
		assertThat(row.size(), is(2));
		assertThat(row.last(), is(row.elementAt(1)));
		Iterator<Cell> iterator = row.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), sameInstance(row.elementAt(0)));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), sameInstance(row.elementAt(1)));
		assertThat(iterator.hasNext(), is(false));
		assertThat(row.elementAt(0), not(sameInstance(row.elementAt(1))));
	}
	@Test
	public void largeTablesHasAllTheRightValues() {
		TablesBuilder tablesBuilder = tables().
			with(table().with(
					row().with(cell(),cell()),
					row().with(cell().with(
							table().with(
									row().with(cell()),
									row().with(cell()),
									row().with(cell())
									)
							)
					)
			)
		);
		Tables tables = tablesBuilder.expect(context);
		assertThat(tables.size(), is(1));
		assertThat(tables.last(), sameInstance(tables.elementAt(0)));
		Iterator<Table> iterator = tables.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), sameInstance(tables.elementAt(0)));
		assertThat(iterator.hasNext(), is(false));
		
		Table table = tables.elementAt(0);
		assertThat(table.size(), is(2));

		Row row0 = table.elementAt(0);
		assertThat(row0.size(), is(2));

		Cell cell0 = row0.elementAt(0);
		assertThat(cell0.text(),
				is("Tables[0].Table[0].Row[0].Cell[0]"));
		assertThat(cell0.text(new DoTraverse()),
				is("Tables[0].Table[0].Row[0].Cell[0]"));

		Row row1 = table.elementAt(1);
		assertThat(row1.size(), is(1));
		
		Cell cell2 = row1.elementAt(0);
		Iterator<Table> iterator2 = cell2.iterator();
		assertThat(iterator2.hasNext(), is(true));
		assertThat(iterator2.next(), sameInstance(cell2.elementAt(0)));
		assertThat(iterator2.hasNext(), is(false));
		
		Table innerTable = cell2.elementAt(0);
		assertThat(innerTable.size(), is(3));

		assertThat(innerTable.elementAt(0).size(), is(1));
		assertThat(innerTable.elementAt(0).elementAt(0).text(),
				is("Tables[0].Table[0].Row[1].Cell[0].Table[0].Row[0].Cell[0]"));
	}
}
