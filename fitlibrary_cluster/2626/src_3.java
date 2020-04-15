/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import fit.Counts;
import fitlibrary.table.Table;

public class TestResults {
	public static final String PASS_COLOUR = "#cfffcf";
	public static final String FAIL_COLOUR = "#ffcfcf";
	public static final String IGNORE_COLOR = "#efefef";
	public static final String ERROR_COLOUR = "#ffffcf";
	private Counts counts = new Counts();
	private String foldingText = "";
	private boolean abandoned = false;
	private boolean stopOnError = false;
	private TestResults parent = null;
	private boolean suiteFixtureSoDoNotTearDown = false;
	private static ThreadLocal<TestResults> testResultsByThread = new ThreadLocal<TestResults>();
	// The above won't leak much memory because there are unlikely to be many distinct threads executing

	public static TestResults create(Counts counts) {
		TestResults testResults = new TestResults(counts);
		testResultsByThread.set(testResults);
		return testResults;
	}
	public static void logAfterTable(String s) {
		getThreadLocalVersion().foldingText += s;
	}
	private static TestResults getThreadLocalVersion() {
		TestResults testResults = testResultsByThread.get();
		if (testResults == null)
			throw new RuntimeException("ThreadLocal is not working");
		return testResults;
	}
	private TestResults(Counts counts) {
		this.counts = counts;
	}
	public TestResults() {
		// Don't use this constructor when showAfter results need to be captured.
	}
	public TestResults(TestResults testResults) { // Hack for now until context is injected correctly
		parent = testResults;
	}
	public void pass() {
		counts.right++;
	}
	public void fail() {
		counts.wrong++;
		if (isStopOnError())
			setAbandoned();
	}
	public void exception() {
		counts.exceptions++;
		if (isStopOnError())
			setAbandoned();
	}
	public void ignore() {
		counts.ignores++;
	}
	public void clear() {
		counts.right = 0;
		counts.wrong = 0;
		counts.ignores = 0;
		counts.exceptions = 0;
		abandoned = false;
	}
	public void add(TestResults otherResults) {
		counts.tally(otherResults.counts);
		abandoned = abandoned || otherResults.abandoned;
	}
	public boolean passed() {
		return counts.right > 0 && counts.wrong == 0 & counts.exceptions == 0;
	}
	public boolean failed() {
		return counts.wrong > 0;
	}
	public boolean errors() {
		return counts.exceptions > 0;
	}
	public boolean problems() {
		return counts.wrong + counts.exceptions > 0;
	}
	@Override
	public String toString() {
		return counts.toString();
	}
	public boolean matches(String rights, String wrongs, String ignores, String exceptions) {
		return	cellValue(rights) == counts.right &&
				cellValue(wrongs) == counts.wrong &&
				cellValue(ignores) == counts.ignores &&
				cellValue(exceptions) == counts.exceptions;
		}
	private int cellValue(String s) {
		return Integer.parseInt(s);
	}
	public Counts getCounts() {
		return counts;
	}
	public String asHtmlTable() {
		String colour = IGNORE_COLOR;
		if (passed())
			colour = PASS_COLOUR;
		else if (problems())
			if (counts.exceptions > 0)
				colour = ERROR_COLOUR;
			else
				colour = FAIL_COLOUR;
		return "<html><table><tr><td "+backgroundColour(colour)+">"+counts.toString()+"</td></tr></table><html>";
	}
	public static String replaceCss(String html) {
	    String result = html;
		result = result.replaceAll("class=\"pass\"",  backgroundColour(PASS_COLOUR));
	    result = result.replaceAll("class=\"fail\"",  backgroundColour(FAIL_COLOUR));
	    result = result.replaceAll("class=\"ignore\"",backgroundColour(IGNORE_COLOR));
	    result = result.replaceAll("class=\"error\"", backgroundColour(ERROR_COLOUR));
		return result;
	}
	private static String backgroundColour(String colour) {
		return "bgcolor=\""+colour+"\"";
	}
	public void addAccumulatedFoldingText(Table table) {
		table.addFoldingText(foldingText);
		foldingText = "";
	}
	public static void setAbandoned() {
		getThreadLocalVersion().abandoned = true;
	}
	public boolean isAbandoned() {
		if (parent != null)
			return parent.isAbandoned();
		return abandoned;
	}
	public static void setStopOnError(boolean stopOnError) {
		getThreadLocalVersion().stopOnError = stopOnError;
	}
	public boolean isStopOnError() {
		if (parent != null)
			return parent.isStopOnError();
		return stopOnError;
	}
	public void setSuiteFixtureSoDoNotTearDown(boolean suiteFixtureSoDoNotTearDown) {
		this.suiteFixtureSoDoNotTearDown = suiteFixtureSoDoNotTearDown;
	}
	public boolean inSuiteFixtureSoDoNotTearDown() {
		if (parent != null)
			return parent.inSuiteFixtureSoDoNotTearDown();
		return suiteFixtureSoDoNotTearDown;
	}
}
