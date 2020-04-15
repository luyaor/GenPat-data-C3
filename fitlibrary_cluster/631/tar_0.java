/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 8/11/2006
*/

package fitlibrary.debug;

import java.io.IOException;

import fit.Counts;
import fit.FitServerBridge;
import fit.FixtureListener;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.batch.fitnesseIn.ParallelFitNesseRepository;
import fitlibrary.runResults.TableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.BatchFitLibrary;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class DebugPage {
	protected int tablesFinished = 0;
	protected int storytestsFinished = 0;
	protected int expectedTablesFinished = 0;
	private static String FITNESSE_URL = "http://localhost:8080/";
	
	protected FixtureListener fixtureListener = new FixtureListener() {
		public void tableFinished(Parse table) {
			tablesFinished++;
		}
		public void tablesFinished(Counts count) {
			storytestsFinished++;
		}
	};
	BatchFitLibrary batchFitLibrary = new BatchFitLibrary(new TableListener(fixtureListener));

	public static void main(String[] args) throws Exception {
		String[] pageNames = new String[] {
				"FitLibrary.SpecifiCations.SpecialisedTables.EmbeddedTables"
		};
		run(pageNames);
	}
	public static void run(String[] pageNames) throws Exception {
		DebugPage runPage = new DebugPage();
		runPage.runs(pageNames);
	}
	public void runs(String[] pageNames) throws FitParseException, IOException {
		tablesFinished = 0;
		storytestsFinished = 0;
		for (int i = 0; i < pageNames.length; i++) {
			run(pageNames[i]);
			if (storytestsFinished != i+1)
				throw new RuntimeException("Wrong # of FixtureListener events fired for "+pageNames[i]+
						": "+storytestsFinished+" instead of "+(i+1));
		}
		if (tablesFinished != expectedTablesFinished)
			throw new RuntimeException("Expected FixtureListener events for "+expectedTablesFinished+
					" tables but instead got "+tablesFinished);
	}
	public void run(String pageName) throws IOException, FitParseException {
		String html = new ParallelFitNesseRepository("fitnesse").getTest(pageName).getContent();
		System.out.println("\n----------\nHTML for "+pageName+"\n----------\n"+html);
		Tables tables = TableFactory.tables(html);
		expectedTablesFinished += tables.size();
		FitServerBridge.setFitNesseUrl(FITNESSE_URL); // Yuck passing important info through a global. See method for links.
		TestResults testResults = batchFitLibrary.doStorytest(tables);
		System.out.println("\n----------\nHTML Report for "+pageName+"\n----------\n"+tables.report());
		System.out.println(testResults);
	}
}
