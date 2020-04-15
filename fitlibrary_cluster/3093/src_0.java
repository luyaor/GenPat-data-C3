/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.Random;

import fit.Parse;
import fitlibrary.exception.table.MissingRowException;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class Table extends ParseNode {
	private static final Random random = new Random();
    private int firstErrorRow = 0;
    
	public Table() {
        super(new Parse("table","",null,null));
    }
	public Table(Parse parse) {
        super(parse);
    }
    public Table(Row row) {
    	super(new Parse("table","",row.parse,null));
	}
	public int size() {
		if (parse == null || parse.parts == null)
			return 0;
        return parse.parts.size();
    }
    public Row row(int i) {
        if (!rowExists(i))
            throw new MissingRowException("");
        return new Row(parse.parts.at(i));
    }
    public boolean rowExists(int i) {
        return i >= 0 && i < size();
    }
    @Override
	public String toString() {
        return "Table["+ParseUtility.toString(parse.parts)+"]";
    }
    public void wrong(TestResults testResults, String msg) {
        row(firstErrorRow).cell(0).fail(testResults,msg);
    }
    public void missing(TestResults testResults) {
        row(firstErrorRow).missing(testResults);
    }
    public void ignore(TestResults testResults) {
        row(firstErrorRow).ignore(testResults);
    }
    @Override
	public void error(TestResults testResults, Throwable e) {
        row(firstErrorRow).error(testResults,e);
    }
	public void error(TableListener tableListener, Exception e) {
		error(tableListener.getTestResults(),e);
	}
    public Row lastRow() {
        return row(size()-1);
    }
    public void addRow(Row row) {
        if (parse.parts == null)
            parse.parts = row.parse;
        else
            parse.parts.last().more = row.parse;
    }
    public Row newRow() {
        Row row = new Row();
        addRow(row);
        return row;
    }
    public void finished(TableListener listener) {
        listener.tableFinished(this);
    }
	public Table withDummyFirstRow() {
		Parse firstRow = new Parse("tr", "", new Parse("td","empty",null,null), parse.parts);
		Parse pseudoTable = new Parse("table", "", firstRow, null);
		Table table = new Table(pseudoTable);
		table.setFirstRowIsHidden();
		return table;
	}
	private void setFirstRowIsHidden() {
		this.firstErrorRow  = 1;
		row(0).setIsHidden();
	}
	public Parse parse() {
		return parse;
	}
	public int phaseBoundaryCount() {
		int count = (parse.leader).split("<hr>").length-1;
		if (count == 0)
			count = (parse.leader).split("<hr/>").length-1;
		return count;
	}
	public void addToLeader(String s) {
		parse.leader += s;
	}
	public void addToStartOfLeader(String s) {
		parse.leader = s + parse.leader;
	}
	public void addToTrailer(String s) {
		if (parse.trailer == null)
			parse.trailer = "";
		parse.trailer += s;
	}
	public void removeNext() {
		parse.more = parse.more.more;
	}
	public String getLeading() {
		return parse.leader;
	}
	public String getTrailing() {
		return parse.trailer;
	}
	public void addFoldingText(String text) {
		if ("".equals(text) || "\n".equals(text))
			return;
		final String id = getId();
		final String fold =
			"<div class=\"included\">\n<div style=\"float: right;\" class=\"meta\">"+
			"<a href=\"javascript:expandAll();\">Expand All</a> | <a href=\"javascript:collapseAll();\">Collapse All</a></div>"+
			"<a href=\"javascript:toggleCollapsable('"+id+"');\">"+
			"<img src=\"/files/images/collapsableClosed.gif\" class=\"left\" id=\"img"+id+"\"/></a>"+
			"&nbsp;<span class=\"meta\">Logs</span><div class=\"hidden\" id=\""+id+"\"><pre>"+
			text+
			"</pre></div></div>";
		if (parse.more != null)
			new Table(parse.more).addToStartOfLeader(fold);
		else
			addToTrailer(fold);
	}
	private static synchronized String getId() {
		return ""+random.nextInt();
	}
	public Tables getTables() {
		return new Tables(parse);
	}
	public void insertTable(int offset, Table table) {
		table.evenUpRows();
		Parse insertPoint = parse.at(offset);
		table.parse().more = insertPoint.more;
		insertPoint.more = table.parse();
	}
	/**
	 * Even up all rows by changing the last cells column span of each row to match
	 *
	 * DESIGN NOTE: this does not shrink the last cell column span. Sometimes one has to add rows below a row and only
	 * has that row column span to work with in order to even out the new rows with the previous ones:
	 * Example:
	 * |aa|aa|aa|
	 * |bb colspan=3|
	 *
	 * The bb fixture wants to add a few rows but is only given the current row. So in order to have an even table,
	 * the only thing the bb fixture can do after having added its rows is to do new Table(bbRow).evenUpRows().
	 * all added rows should at least have a column span of 3. This obviously does not work if the added rows have more
	 * than 3 columns...
	 * NOTE: if there is a better way like getting somehow the handle on the true table (as opposed to creating one on the
	 * fly), please make it shrink the last cells column span..
	 */
	public void evenUpRows() {
		int maxRowLength = getMaxRowColumnSpan();
		for (int rowNo = 0; rowNo < size(); rowNo++) {
			row(rowNo).setColumnSpan(maxRowLength);
		}
	}

	private int getMaxRowColumnSpan() {
		int maxLength = 0;
		for (int rowNo = 0; rowNo < size(); rowNo++) {
			Row row = row(rowNo);
			maxLength = Math.max(maxLength, row.getColumnSpan());
		}
		return maxLength;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Table))
			return false;
		Table other = (Table) obj;
		if (size() != other.size())
			return false;
		for (int i = 0; i < size(); i++)
			if (!row(i).equals(other.row(i)))
				return false;
		return true;
	}
}
