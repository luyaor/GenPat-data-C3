/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import fit.exception.FitParseException;

public class TestTables {
	@Test
	public void fromWiki() throws FitParseException {
		assertThat(TablesOnParse.fromWiki("|a|b|"), is(TableFactory.tables(TableFactory.table(TableFactory.row("a","b")))));
	}
}
