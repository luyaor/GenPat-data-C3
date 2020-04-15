/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.utility;

import fit.Counts;

public class TestResultsFactory {
	public static TestResults testResults() {
		return new TestResults();
	}
	public static TestResults testResults(Counts counts) {
		return new TestResults(counts);
	}
}
