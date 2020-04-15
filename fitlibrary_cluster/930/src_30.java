/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestCell {
	final Table table0 = TableFactory.table(TableFactory.row("0"));
	final Table table1 = TableFactory.table(TableFactory.row("1"));
	final Cell cellA = TableFactory.cell("a");
	
	@Test public void elementAddedToEmptyCell() {
		cellA.add(table0);
		assertThat(cellA.size(),is(1));
		assertThat(cellA.elementAt(0),is(table0));
	}
	@Test public void elementAddedToCellWithEmbedded() {
		cellA.add(table0);
		cellA.add(table1);
		assertThat(cellA.size(),is(2));
		assertThat(cellA.elementAt(0),is(table0));
		assertThat(cellA.elementAt(1),is(table1));
	}
}
