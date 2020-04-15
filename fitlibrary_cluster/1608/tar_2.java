/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fit.Parse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public abstract class ParseNode<To> {
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
    public Parse parse() {
    	return parse;
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
	public String getLeader() {
		String leader = parse.leader;
		if (leader == null)
			return "";
		return leader;
	}
	public String getTrailer() {
		String trailer = parse.trailer;
		if (trailer == null)
			return "";
		return trailer;
	}
	protected void addToTrailer(String s) {
		if (parse.trailer == null)
			parse.trailer = "";
		parse.trailer += s;
	}
	protected void addToStartOfLeader(String s) {
		parse.leader = s + parse.leader;
	}
	public void setLeader(String leader) {
		parse.leader = leader;
	}
	public void setTrailer(String trailer) {
		parse.trailer = trailer;
	}
    public boolean elementExists(int i) {
        return i >= 0 && i < size();
    }
    public To last() {
        return elementAt(size()-1);
    }
    public List<To> listFrom(int start) {
		List<To> list = new ArrayList<To>();
		for (int i = start; i < size(); i++)
			list.add(elementAt(i));
		return list;
	}
	public Iterator<To> iterator() {
		return listFrom(0).iterator();
	}
	protected String toString(String type, Parse theParse) {
		return type+"["+ParseUtility.toString(theParse)+"]";
	}

    protected abstract To elementAt(int i);
    protected abstract int size();
    protected abstract void error(TestResults testResults, Throwable e);
}
