/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fit.Parse;
import fitlibrary.runResults.TestResults;

public abstract class TableElementOnList<To extends TableElement> {
    public final static String PASS = " class=\"pass\"";
    public final static String FAIL = " class=\"fail\"";
    public final static String IGNORE = " class=\"ignore\"";
    public final static String ERROR = " class=\"error\"";
    public final static String SHOWN = " bgcolor=#C0C0FF";
    public final static String CALLS = " bgcolor=#DADAFF";
    private final String tag;
    protected String tagAnnotation = "";
    private String leader = "";
    private String trailer = "";
    private List<To> elements = new ArrayList<To>();

    public boolean elementExists(int i) {
        return i >= 0 && i < size();
    }
    public int size() {
		return elements.size();
	}
	public To last() {
        return at(size()-1);
    }
    public To at(int i) {
		return elements.get(i);
	}
    public void add(To to) {
    	elements.add(to);
    }
    public void add(int i, To to) {
    	elements.add(i, to);
    }
	public void removeElementAt(int i) {
		elements.remove(i);
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	public Iterator<To> iterator() {
		return iterableFrom(0).iterator();
	}
	public Iterable<To> iterableFrom(int start) {
		List<To> list = new ArrayList<To>();
		for (int i = start; i < size(); i++)
			list.add(at(i));
		return list;
	}
	public TableElementOnList<To> from(int start) {
		TableElementOnList<To> result = newObject();
		for (To to: iterableFrom(start))
			result.add(to);
		return result;
	}
	public void clear() {
		elements.clear();
	}
    public TableElementOnList(String tag) {
		this.tag = tag;
	}
	public String getLeader() {
		return leader;
	}
	public String getTrailer() {
		return trailer;
	}
	protected void addToTrailer(String s) {
		trailer += s;
	}
	protected void addToStartOfLeader(String s) {
		leader = s + leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public void setTrailer(String trailer) {
		this.trailer = trailer;
	}
    public void pass(TestResults testResults) {
        addToTag(PASS);
        testResults.pass();
    }
	public void fail(TestResults testResults) {
    	if (!hadError()) {
    		addToTag(FAIL);
    		testResults.fail();
    	}
    }
	public void passOrFail(TestResults testResults, boolean right) {
		if (right)
			pass(testResults);
		else
			fail(testResults);
	}
	public void error(TestResults testResults, Throwable e) {
		at(0).error(testResults, e);
	}
   public void shown() {
        addToTag(SHOWN);
    }
    public void calls() {
        addToTag(CALLS);
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
	private boolean tagContains(String label) {
		return tagAnnotation.indexOf(label) >= 0;
	}
	public String getTagLine() {
		return tagAnnotation;
	}
	public void setTagLine(String tagLine) {
		tagAnnotation = tagLine;
	}
    public void addToTag(String report) {
    	if (tag.isEmpty())
    		at(0).addToTag(report);
    	tagAnnotation += report;
	}
	public Parse parse() {
		return null;
	}
	public String getType() {
		return tag;
	}
	@Override
    public String toString() {
    	return "<"+getTagLine()+">";
    }
	public void toHtml(StringBuilder builder) {
		builder.append(getLeader());
		if (!tag.isEmpty())
			builder.append("<").append(tag).append(getTagLine()).append(">");
		appendBody(builder);
		for (To to : elements)
			to.toHtml(builder);
		if (!tag.isEmpty())
			builder.append("</").append(tag).append(">");
		builder.append(getTrailer());
	}
    protected void appendBody(StringBuilder builder) {
		//
	}
	protected abstract TableElementOnList<To> newObject();
}
