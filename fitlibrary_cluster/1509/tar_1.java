/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.xref;

import org.junit.Test;

import fit.exception.FitParseException;
import fitlibrary.table.Tables;
import fitlibrary.utility.SimpleWikiTranslator;
import fitlibrary.utility.TestResultsFactory;

public class TestCrossReferenceFixture
{
	@Test
	public void tt() throws FitParseException {
		CrossReferenceFixture xref = new CrossReferenceFixture(".IsBook") {
			@Override
			protected String fitNesseDiry() {
				return "C:/working/FitNesseDocServer";
			}
		};
		String wiki =  "||";
		Tables tables = SimpleWikiTranslator.translateToTables(wiki); 
		xref.interpretAfterFirstRow(tables.elementAt(0), TestResultsFactory.testResults());
	}
}
