/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Iterator;

import org.junit.Test;

import fit.exception.FitParseException;
import fitlibrary.utility.SimpleWikiTranslator;

public class TestTables {
	final Table table1 = TableFactory.table(TableFactory.row("first"));
	final Table table2 = TableFactory.table(TableFactory.row("second"));
	final Tables tables = TableFactory.tables();
	
	@Test
	public void fromWiki() throws FitParseException {
		assertThat(SimpleWikiTranslator.translateToTables("|a|b|"), is(TableFactory.tables(TableFactory.table(TableFactory.row("a","b")))));
	}
	@Test
	public void iteratorIsEmptyWhenNoElements() {
		assertThat(TableFactory.tables().iterator().hasNext(), is(false));
	}
	@Test
	public void iteratorHasOneWhenOneElement() {
		tables.add(table1);
		Iterator<Table> iterator = tables.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(table1));
		assertThat(iterator.hasNext(), is(false));
	}
	@Test
	public void iteratorHasTwoWhenTwoElements() {
		tables.add(table1);
		tables.add(table2);
		Iterator<Table> iterator = tables.iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(table1));
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(table2));
		assertThat(iterator.hasNext(), is(false));
	}
	@Test
	public void iterableAfterFirstIsEmptyWhenNoElements() {
		Iterator<Table> iterator = tables.listFrom(1).iterator();
		assertThat(iterator.hasNext(), is(false));
	}
	@Test
	public void iterableAfterFirstIsEmptyWhenOneElement() {
		Iterator<Table> iterator = TableFactory.tables(table1).listFrom(1).iterator();
		assertThat(iterator.hasNext(), is(false));
	}
	@Test
	public void iterableAfterFirstHasOneWhenTwoElements() {
		tables.add(table1);
		tables.add(table2);
		Iterator<Table> iterator = tables.listFrom(1).iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next(), is(table2));
		assertThat(iterator.hasNext(), is(false));
	}
}
