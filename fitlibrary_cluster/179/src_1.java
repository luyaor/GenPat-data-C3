/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.matcher;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;

import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableElement;
import fitlibrary.table.Tables;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;

public class TableBuilderForTests {
	static class TableElementBuilder<From extends TableElement, Builder extends TableElementBuilder, To extends TableElement> {
		protected final Class<From> type;
		protected final List<Builder> elements;

		public TableElementBuilder(Class<From> type) {
			this(type,new ArrayList<Builder>());
		}
		protected TableElementBuilder(Class<From> type, List<Builder> copy) {
			this.type = type;
			elements = copy;
		}
		public From expect(final Mockery context) {
			return expect(context,"",0);
		}
		public From expect(final Mockery context,String path, int index) {
			String localPath = localPath(path, index);
			final From from = context.mock(type,localPath);
			final List<To> listOfMockElements = new ArrayList<To>();
			int count = 0;
			for (Builder builder : elements)
				listOfMockElements.add((To) builder.expect(context,localPath,(count++)));
			context.checking(new Expectations() {{
				allowing(from).isEmpty(); will(returnValue(listOfMockElements.isEmpty()));
				allowing(from).size(); will(returnValue(listOfMockElements.size()));
				allowing(from).iterator(); will(returnValue(listOfMockElements.iterator()));
				allowing(from).getType(); will(returnValue(type.getSimpleName()));
				for (int i = 0; i < elements.size(); i++) {
					final int ii = i;
					allowing(from).elementAt(i); will(returnValue(listOfMockElements.get(ii)));
				}
			}});
			if (listOfMockElements.isEmpty())
				context.checking(new Expectations() {{
					allowing(from).last(); will(throwException(new FitLibraryException("It's empty.")));
				}});
			else 
				context.checking(new Expectations() {{
					allowing(from).last(); will(returnValue(listOfMockElements.get(elements.size()-1)));
				}});
			return from;
		}
		protected String localPath(String path, int index) {
			if (path.isEmpty())
				return type.getSimpleName()+"["+index+"]";
			return path+"."+type.getSimpleName()+"["+index+"]";
		}
		protected List<Builder> withCopy(Builder... els) {
			List<Builder> copy = new ArrayList<Builder>(elements);
			for (Builder builder : els)
				copy.add(builder);
			return copy;
		}
	}
	public static class TablesBuilder extends TableElementBuilder<Tables,TableBuilder,Row> {
		public TablesBuilder() {
			super(Tables.class);
		}
		public TablesBuilder with(TableBuilder... els) {
			return new TablesBuilder(withCopy(els));
		}
		public TablesBuilder(List<TableBuilder> copy) {
			super(Tables.class,copy);
		}
	}
	public static class TableBuilder extends TableElementBuilder<Table,RowBuilder,Cell> {
		public TableBuilder() {
			super(Table.class);
		}
		public TableBuilder with(RowBuilder... els) {
			return new TableBuilder(withCopy(els));
		}
		public TableBuilder(List<RowBuilder> copy) {
			super(Table.class,copy);
		}
	}
	public static class RowBuilder extends TableElementBuilder<Row,CellBuilder,Cell> {
		public RowBuilder() {
			super(Row.class);
		}
		public RowBuilder with(CellBuilder... els) {
			return new RowBuilder(withCopy(els));
		}
		public RowBuilder(List<CellBuilder> copy) {
			super(Row.class,copy);
		}
	}
	public static class CellBuilder extends TableElementBuilder<Cell,TableBuilder,Row> {
		protected Option<String> optionalText = None.none();
		
		public CellBuilder() {
			super(Cell.class);
		}
		public CellBuilder with(TableBuilder... els) {
			return new CellBuilder(optionalText,withCopy(els));
		}
		public CellBuilder(Option<String> optionalText, List<TableBuilder> copy) {
			super(Cell.class,copy);
			this.optionalText = optionalText;
		}
		public CellBuilder(String text) {
			this();
			optionalText = new Some<String>(text);
		}
		@Override
		public Cell expect(final Mockery context,final String path, final int index) {
			final Cell cell = super.expect(context, path, index);
			final String name = optionalText.isSome() ? optionalText.get() : localPath(path, index);
			context.checking(new Expectations() {{
				allowing(cell).text(); 
				will(returnValue(name));
				allowing(cell).text(with(any(VariableResolver.class))); will(returnValue(name));
				allowing(cell).hasEmbeddedTables(); will(returnValue(!cell.isEmpty()));
				allowing(cell).getEmbeddedTables(); will(returnValue(cell));
			}});
			return cell;
		}
	}
	public static TablesBuilder tables() {
		return new TablesBuilder();
	}
	public static TableBuilder table() {
		return new TableBuilder();
	}
	public static RowBuilder row() {
		return new RowBuilder();
	}
	public static CellBuilder cell() {
		return new CellBuilder();
	}
	public static CellBuilder cell(String text) {
		return new CellBuilder(text);
	}
}
