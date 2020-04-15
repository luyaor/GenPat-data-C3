/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import fit.exception.FitParseException;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;

public class TestTables {
	@Test
	public void fromWiki() throws FitParseException {
		assertThat(Tables.fromWiki("|a|b|"), is(new Tables(new Table(new Row("a","b")))));
	}
}
