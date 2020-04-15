/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import fitlibrary.parser.ParserTestCase;
import fitlibrary.table.Row;
import fitlibrary.table.TableFactory;

public class TestDoCaller {
	DoEvaluator evaluator = ParserTestCase.evaluatorWithRuntime();
	
	@Test public void noArgs() {
		Row row = TableFactory.row("act");
		assertThat(row.methodNameForPlain(evaluator),is("act"));
	}
	@Test public void oneArg() {
		Row row = TableFactory.row("act","2");
		assertThat(row.methodNameForPlain(evaluator),is("act|"));
	}
	@Test public void oneArgKeyAfter() {
		Row row = TableFactory.row("act","2","ive");
		assertThat(row.methodNameForPlain(evaluator),is("act|ive"));
	}
	@Test public void twoArgs() {
		Row row = TableFactory.row("act","2","ive","3");
		assertThat(row.methodNameForPlain(evaluator),is("act|ive|"));
	}
	@Test public void twoArgsKeyAfter() {
		Row row = TableFactory.row("act","2","iv","3","e");
		assertThat(row.methodNameForPlain(evaluator),is("act|iv|e"));
	}
}
