/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fitlibrary.utility.TestResults;

public abstract class ParseNode {
    public final static String PASS = " class=\"pass\"";
    public final static String FAIL = " class=\"fail\"";
    public final static String IGNORE = " class=\"ignore\"";
    public final static String ERROR = " class=\"error\"";
    public final static String SHOWN = " bgcolor=#C0C0FF";
    public final static String CALLS = " bgcolor=#DADAFF";
    public Parse parse;

    public ParseNode(Parse parse) {
        this.parse = parse;
    }
    public void pass(TestResults testResults) {
        ensureBodyNotNull();
        parse.addToTag(PASS);
        testResults.pass();
    }
    public void fail(TestResults testResults) {
    	ensureBodyNotNull();
    	if (!hadError()) {
    		parse.addToTag(FAIL);
    		testResults.fail();
    	}
    }
	public void passOrFail(TestResults testResults, boolean right) {
		if (right)
			pass(testResults);
		else
			fail(testResults);
	}
    public void shown() {
        ensureBodyNotNull();
        parse.addToTag(SHOWN);
    }
    public void calls() {
        ensureBodyNotNull();
        parse.addToTag(CALLS);
    }
    public static String label(String string) {
        return " <span class=\"fit_label\">" + string + "</span>";
      }
    public boolean didPass() {
		return tagContains(PASS);
    }
    public boolean didFail() {
        return tagContains(FAIL);
    }
    public boolean wasIgnored() {
        return tagContains(IGNORE);
    }
    public boolean hadError() {
        return tagContains(ERROR);
    }
    protected void ensureBodyNotNull() {
        if (parse.body == null)
            parse.body = "";
    }
	private boolean tagContains(String label) {
		return parse.tag.indexOf(label) >= 0;
	}
	
    protected abstract void error(TestResults counts, Throwable e);
}
