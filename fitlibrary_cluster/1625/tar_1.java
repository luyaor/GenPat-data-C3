/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.definedAction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import fit.exception.FitParseException;
import fitlibrary.definedAction.DefineActionsOnPage;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.runtime.RuntimeContextImplementation;
import fitlibrary.table.Tables;
import fitlibrary.utility.TestResults;

public class TestDefineActionsOnPage {
	protected File fitNesseDir;
	@Before
	public void setFitNesseDirectory() {
		fitNesseDir = new File("fitnesse");
		assertThat("This test relies on files in the fitnesse directory - which cannot be found: current directory is "+
				new File(".").getAbsolutePath(), fitNesseDir.exists(), is(true));
	}
	
	@Test public void actionsAreDefinedThroughFileSystem() throws FitParseException {
		String pageName = ".FitLibrary.SpecifiCations.PlainTextInsteadOfTables.DefinedActions";
		String wiki = "|''define actions at''|"+pageName+"|";
		Tables tables = Tables.fromWiki(wiki);
		DefineActionsOnPage defineActionsOnPage = new DefineActionsOnPage(pageName) {
			@Override
			protected File fitNesseDiry() {
				return fitNesseDir;
			}
		};
		defineActionsOnPage.interpretAfterFirstRow(tables.table(0), new TestResults());
		assertThat(TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByCamel("addressIsAt", 1),is(notNullValue()));
		assertThat(TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByCamel("addressIs", 1),is(nullValue()));
		assertThat(TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel("Person", "addressIs", 1, new RuntimeContextImplementation()),is(notNullValue()));
	}
	@Test public void actionsAreDefinedThroughFileSystemExample2() throws FitParseException {
		String pageName = ".FitLibrary.SpecifiCations.DefinedActions.BasedOnClass.DefinedActions";
		String wiki = "|''define actions at''|"+pageName+"|";
		Tables tables = Tables.fromWiki(wiki);
		DefineActionsOnPage defineActionsOnPage = new DefineActionsOnPage(pageName) {
			@Override
			protected File fitNesseDiry() {
				return fitNesseDir;
			}
		};
		defineActionsOnPage.interpretAfterFirstRow(tables.table(0), new TestResults());
		assertThat(TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByCamel("nameIs", 1),is(notNullValue()));
		assertThat(TemporaryPlugBoardForRuntime.definedActionsRepository().lookupByClassByCamel("Person", "nameIs", 1, new RuntimeContextImplementation()),is(notNullValue()));
	}
}
