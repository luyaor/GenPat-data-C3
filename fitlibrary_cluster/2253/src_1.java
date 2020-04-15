/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.matcher;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;

import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;

public class TableBuilderForTests {
	static class TablesBuilder {
		final List<TableBuilder> elements;

		public TablesBuilder() {
			this(new ArrayList<TableBuilder>());
		}
		public TablesBuilder(List<TableBuilder> copy) {
			elements = copy;
		}
		public TablesBuilder with(TableBuilder... els) {
			List<TableBuilder> copy = new ArrayList<TableBuilder>(elements);
			for (TableBuilder element : els)
				copy.add(element);
			return new TablesBuilder(copy);
		}
		public Tables expect(final Mockery context) {
			final List<Table> listOfTable = new ArrayList<Table>();
			for (TableBuilder builder : elements)
				listOfTable.add(builder.expect(context));
			final Tables tables = context.mock(Tables.class);
			context.checking(new Expectations() {{
				allowing(tables).size(); will(returnValue(listOfTable.size()));
				for (int i = 0; i < elements.size(); i++) {
					final int ii = i;
					allowing(tables).elementAt(i); will(returnValue(listOfTable.get(ii)));
				}
				allowing(tables).last(); will(returnValue(listOfTable.get(elements.size()-1)));
				allowing(tables).iterator(); will(returnValue(listOfTable.iterator()));
			}});
			return tables;
		}
	}
	static class TableBuilder {
		final List<RowBuilder> elements;

		public TableBuilder() {
			this(new ArrayList<RowBuilder>());
		}
		public TableBuilder(List<RowBuilder> copy) {
			elements = copy;
		}
		public TableBuilder with(RowBuilder... els) {
			List<RowBuilder> copy = new ArrayList<RowBuilder>(elements);
			for (RowBuilder element : els)
				copy.add(element);
			return new TableBuilder(copy);
		}
		public Table expect(final Mockery context) {
			final List<Row> listOfTable = new ArrayList<Row>();
			for (RowBuilder builder : elements)
				listOfTable.add(builder.expect(context));
			final Table table = context.mock(Table.class);
			context.checking(new Expectations() {{
				allowing(table).size(); will(returnValue(listOfTable.size()));
				for (int i = 0; i < elements.size(); i++) {
					final int ii = i;
					allowing(table).elementAt(i); will(returnValue(listOfTable.get(ii)));
				}
//				allowing(table).last(); will(returnValue(listOfTable.get(elements.size()-1)));
				allowing(table).iterator(); will(returnValue(listOfTable.iterator()));
			}});
			return table;
		}
	}
	static class RowBuilder {

		public Row expect(Mockery context) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	//	static class Build <T, ElementBuilder> {
//		final T tables;
//		final List<ElementBuilder> elements = new ArrayList<ElementBuilder>();
//
//		public Build(T tables) {
//			this(tables, new ArrayList<ElementBuilder>());
//		}
//		public Build(T tables, List<ElementBuilder> els) {
//			this.tables = tables;
//			for (ElementBuilder element : els)
//				this.elements.add(element);
//		}
//		public Build<T,ElementBuilder> with(ElementBuilder... els) {
//			List<ElementBuilder> copy = new ArrayList<ElementBuilder>(elements);
//			for (ElementBuilder element : els)
//				copy.add(element);
//			return new Build<T,ElementBuilder>(tables, copy);
//		}
//		public void expect(Mockery context) {
//			context.checking(new Expectations() {{
//				allowing(tables).size(); will(returnValue(elements.size()));
//				for (int i = 0; i < elements.size(); i++) {
//					final int ii = i;
//					allowing(tables).table(i); will(returnValue(elements.get(ii).table));
//				}
//				allowing(tables).last(); will(returnValue(elements.get(elements.size()-1).table));
//			}});
//		}
//	}
//	private void trial() {
//		tables().with(
//				table().with(
//						row().with(
//								cell(),
//								cell().with(
//										tables().with(
//												table().with(
//														row().with(
//																cell()
//														)
//												)
//										)
//								)
//						),
//						row()
//				)
//		);
//	}
}
