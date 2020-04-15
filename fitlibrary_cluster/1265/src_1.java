/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.xref;

import org.junit.Test;

import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.table.TablesOnParse;
import fitlibrary.utility.SimpleWikiTranslator;
import fitlibrary.utility.TestResults;
import fitlibrary.xref.CrossReferenceFixture;

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
		TablesOnParse tables = new TablesOnParse(new Parse(SimpleWikiTranslator.translate(wiki))); 
		xref.interpretAfterFirstRow(tables.table(0), new TestResults());
	}
}
