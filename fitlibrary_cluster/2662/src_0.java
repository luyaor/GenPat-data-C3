/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runner;

import java.io.IOException;
import java.text.ParseException;

public class FolderRunnerTest {
    public static void main(String[] argsIgnore) throws ParseException, IOException {
    	String DIRY = "C:/Documents and Settings/Rimu Research/My Documents/creations/MyOwnFitReleases/";
        run(new String[]{ DIRY+"tests", DIRY+"reports" });

        run(new String[]{ DIRY+"spreadsheetTests", DIRY+"spreadsheetReports" });

        String SUITE_DIRY = DIRY+"/suiteTests/";
        run(new String[]{ "-s", SUITE_DIRY+"SuiteFixtureExample.html", 
        		SUITE_DIRY+"tests", SUITE_DIRY+"reports" });
        run(new String[]{ "-s", SUITE_DIRY+"AnotherSuiteFixtureExample.html", 
        		SUITE_DIRY+"tests", SUITE_DIRY+"otherReports" });
    }

	private static void run(String[] args) throws ParseException, IOException {
		Report report = new FolderRunner(args).run();
        report.displayCounts();
	}
}
