/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.Fixture;
import fit.Parse;
import fitlibrary.exception.table.NestedTableExpectedException;
import fitlibrary.exception.table.SingleNestedTableExpected;
import fitlibrary.global.PlugBoard;
import fitlibrary.runtime.RuntimeContext;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.HtmlUtils;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class Cell extends ParseNode implements ICell {
    static final Pattern COLSPAN_PATTERN = Pattern.compile(".*\\b(colspan\\s*=\\s*\"?\\s*(\\d+)\\s*\"?).*");
    private boolean cellIsInHiddenRow = false;
    
	public Cell(Parse parse) {
        super(parse);
    }
    public Cell() {
        this("");
    }
    public Cell(String cellText) {
        this(new Parse("td",cellText,null,null));
    }
    public Cell(String cellText, int cols) {
		 this(new Parse("td",cellText,null,null));
		 setColumnSpan(cols);
	}
	public Cell(Tables innerTables) {
		this(new Parse("td","",innerTables.parse,null));
	}
	public Cell(String preamble, Tables innerTables) {
		this(innerTables);
		parse.parts.leader = Fixture.label(preamble)+parse.parts.leader;
		calls();
	}
	public Cell(Table innerTable) {
		this(new Tables(innerTable));
	}
	public Cell(Cell cell) {
		this();
		if (cell.hasEmbeddedTable())
			setInnerTables(cell.getEmbeddedTables());
		else
			setText(cell.fullText());
	}
	public String text(Evaluator evaluator) {
		if (parse.body == null)
			return "";
		if (evaluator == null)
			return parse.text();
		if (evaluator.getRuntimeContext() == null)
			throw new NullPointerException("Runtime is null");
        String text = parse.text();
		String resolve = evaluator.getRuntimeContext().dynamicVariables().resolve(text);
		if (!text.equals(resolve))
			parse.body = resolve;
		return resolve;
    }
	public String text(RuntimeContext runtime) {
		if (runtime == null)
			throw new NullPointerException("Runtime is null");
        if (parse.body == null)
            return "";
        String text = parse.text();
		String resolve = runtime.dynamicVariables().resolve(text);
		if (!text.equals(resolve))
			parse.body = resolve;
		return resolve;
    }
	public String text() {
        if (parse.body == null)
            return "";
        return parse.text();
    }
	public boolean unresolved(Evaluator evaluator) {
		return text().startsWith("@{") && text().indexOf("}") == text().length()-1 &&
				text().equals(text(evaluator));
	}
	public String camelledText(Evaluator evaluator) {
		return ExtendedCamelCase.camel(text(evaluator));
	}
   public String textLower(Evaluator evaluator) {
        return text(evaluator).toLowerCase();
    }
    public boolean matchesText(String text, Evaluator evaluator) {
        return text(evaluator).toLowerCase().equals(text.toLowerCase());
    }
    public boolean isBlank(Evaluator evaluator) {
        return text(evaluator).equals("");
    }
    public boolean hasEmbeddedTable() {
        return parse.parts != null;
    }
    public Tables innerTables() {
        if (!hasEmbeddedTable())
            throw new NestedTableExpectedException();
        return new Tables(parse.parts);
    }
    public Cell copy() {
        return new Cell(ParseUtility.copyParse(parse));
    }
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof Cell))
            return false;
        Cell other = (Cell)object;
        return parse.body.equals(other.parse.body);
    }
	@Override
	public int hashCode() {
		return parse.body.hashCode();
	}
    public void expectedElementMissing(TestResults testResults) {
        fail(testResults);
        addToBody(label("missing"));
    }
    public void actualElementMissing(TestResults testResults) {
        fail(testResults);
        addToBody(label("surplus"));
    }
	public void unexpected(TestResults testResults, String s) {
        fail(testResults);
        addToBody(label("unexpected "+s));
	}
    public void actualElementMissing(TestResults testResults, String value) {
        fail(testResults);
        parse.body = Fixture.gray(Fixture.escape(value.toString()));
        addToBody(label("surplus"));
    }
    @Override
	public void pass(TestResults testResults) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.pass(testResults);
    }
	public void pass(TestResults testResults, String msg) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.pass(testResults);
        addToBody("<hr>" + Fixture.escape(msg) + label("actual"));
    }
    @Override
	public void fail(TestResults testResults) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.fail(testResults);
    }
    public void fail(TestResults testResults, String msg, Evaluator evaluator) {
    	if ("".equals(parse.body) && !hasEmbeddedTable()) {
    		failHtml(testResults,msg);
    		return;
    	}
        fail(testResults);
        String resolved = "";
        if (!text().equals(text(evaluator)))
        	resolved = " = "+text(evaluator);
        addToBody(resolved+label("expected") + "<hr>" + Fixture.escape(msg)
                + label("actual"));
    }
    public void failWithStringEquals(TestResults testResults, String actual, Evaluator evaluator) {
    	if ("".equals(parse.body) && !hasEmbeddedTable()) {
    		failHtml(testResults,actual);
    		return;
    	}
        fail(testResults);
        String resolved = "";
        if (!text().equals(text(evaluator)))
        	resolved = " = "+text(evaluator);
        addToBody(resolved+label("expected") + "<hr>" + Fixture.escape(actual)
                + label("actual")+ differences(Fixture.escape(text(evaluator)),Fixture.escape(actual)));
    }
	public static String differences(String actual, String expected) {
		return PlugBoard.stringDifferencing.differences(actual, expected);
	}
	public void fail(TestResults testResults, String msg) {
    	if ("".equals(parse.body) && !hasEmbeddedTable()) {
    		failHtml(testResults,msg);
    		return;
    	}
        fail(testResults);
        addToBody(label("expected") + "<hr>" + Fixture.escape(msg)
                + label("actual"));
    }
    public void failHtml(TestResults testResults, String msg) {
        fail(testResults);
        addToBody(msg);
    }
    @Override
	public void error(TestResults testResults, Throwable e) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        addToBody(PlugBoard.exceptionHandling.exceptionMessage(e));
        parse.addToTag(ERROR);
        testResults.exception();
    }
   public void error(TestResults testResults, String msg) {
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        addToBody("<hr/>" + Fixture.label(msg));
        parse.addToTag(ERROR);
        testResults.exception();
    }
   public void error(TestResults testResults) {
	   if (cellIsInHiddenRow)
		   System.out.println("Bug: colouring a cell in a hidden table");
	   parse.addToTag(ERROR);
	   testResults.exception();
   }
	public void ignore(TestResults testResults) {
    	if (parse.tag.contains(CALLS))
    		return;
    	if (cellIsInHiddenRow)
    		System.out.println("Bug: colouring a cell in a hidden table");
        ensureBodyNotNull();
        if (parse.tag.indexOf("class") >= 0)
        	throw new RuntimeException("Duplicate cell class in tag. Tag is already: "+
        			parse.tag.substring(1,parse.tag.length()-2));
        parse.addToTag(IGNORE);
        testResults.ignore();
    }
    public void exceptionMayBeExpected(boolean exceptionExpected, Exception e, TestResults testResults) {
    	if (exceptionExpected)
    		pass(testResults);
    	else
    		error(testResults,e);
    }
    public Table getEmbeddedTable() {
        Tables tables = getEmbeddedTables();
        if (tables.size() != 1)
        	throw new SingleNestedTableExpected();
		return tables.table(0);
    }
    public Tables getEmbeddedTables() {
        if (!hasEmbeddedTable())
            throw new NestedTableExpectedException();
		return new Tables(parse.parts);
    }
    @Override
	public String toString() {
        if (hasEmbeddedTable())
            return "Cell["+ParseUtility.toString(parse.parts)+"]";
        return "Cell["+text()+"]";
    }
    public void wrongHtml(TestResults counts, String actual) {
        fail(counts);
        addToBody(label("expected") + "<hr>" + actual
                + label("actual"));
    }
    private void addToBody(String msg) {
        if (hasEmbeddedTable()) {
            if (parse.parts.more == null)
                parse.parts.trailer = msg;
            else
                parse.parts.more.leader += msg;
        }
        else {
            ensureBodyNotNull();
            parse.addToBody(msg);
        }
    }
	public void setText(String text) {
		parse.body = text;
	}
	public void setEscapedText(String text) {
		setText(Fixture.escape(text));
	}
	public void setMultilineEscapedText(String text) {
		setText(HtmlUtils.escape(text));
	}
	public String fullText() {
		return parse.body;
	}
	public void setUnvisitedEscapedText(String s) {
		setUnvisitedText(Fixture.escape(s));
	}
	public void setUnvisitedMultilineEscapedText(String s) {
		setUnvisitedText(HtmlUtils.escape(s));
	}
	public void setUnvisitedText(String s) {
		setText(Fixture.gray(s));
	}
	public void passIfBlank(TestResults counts, Evaluator evaluator) {
		if (isBlank(evaluator))
			pass(counts);
		else
			fail(counts,"",evaluator);
	}
	public void passIfNotEmbedded(TestResults counts) {
		if (!hasEmbeddedTable()) // already coloured
			pass(counts);
	}
	public void setIsHidden() {
		this.cellIsInHiddenRow = true;
	}
	public void setInnerTables(Tables tables) {
		parse.parts = tables.parse();
	}
	public int getColumnSpan() {
		Matcher matcher = COLSPAN_PATTERN.matcher(parse.tag);
		int colspan = 1;
		if (matcher.matches())
			colspan = Integer.parseInt(matcher.group(2));
		return colspan;
	}
	public void setColumnSpan(int colspan) {
		if (colspan < 1)
			return;
		Matcher matcher = COLSPAN_PATTERN.matcher(parse.tag);
		if (matcher.matches())
			parse.tag = parse.tag.replace(matcher.group(1), getColspanHtml(colspan));
		else
			parse.addToTag(getColspanHtml(colspan));
	}
	private static String getColspanHtml(int colspan) {
		return " colspan=\""+colspan+"\"";
	}
}
