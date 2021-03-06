/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fit.Parse;
import fitlibrary.exception.table.MissingRowException;
import fitlibrary.utility.ITableListener;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class TableOnParse extends ParseNode implements Table {
    private int firstErrorRow = 0;
    
    public TableOnParse(Parse parse) {
    	super(parse);
    }
	public TableOnParse() {
        super(new Parse("table","",null,null));
        parse().addToTag(" border=\"1\" cellspacing=\"0\"");
    }
    public TableOnParse(Row... rows) {
    	this();
    	for (Row row: rows)
    		add(row);
	}
	public Parse parse() {
		return parse;
	}
	public int size() {
		if (parse == null || parse.parts == null)
			return 0;
        return parse.parts.size();
    }
    public Row elementAt(int i) {
        if (!rowExists(i))
            throw new MissingRowException("");
        return new RowOnParse(parse.parts.at(i));
    }
    public boolean rowExists(int i) {
        return i >= 0 && i < size();
    }
    @Override
	public String toString() {
        return "Table["+ParseUtility.toString(parse.parts)+"]";
    }
    @Override
	public void pass(TestResults testResults) {
        elementAt(firstErrorRow).pass(testResults);
    }
    public void wrong(TestResults testResults, String msg) {
        elementAt(firstErrorRow).elementAt(0).fail(testResults,msg);
    }
    public void ignore(TestResults testResults) {
        elementAt(firstErrorRow).ignore(testResults);
    }
    @Override
	public void error(TestResults testResults, Throwable e) {
        elementAt(firstErrorRow).error(testResults,e);
    }
	public void error(ITableListener tableListener, Throwable e) {
		error(tableListener.getTestResults(),e);
	}
    public Row lastRow() {
        return elementAt(size()-1);
    }
    public void add(Row row) {
        if (parse.parts == null)
            parse.parts = row.parse();
        else
            parse.parts.last().more = row.parse();
    }
    public Row newRow() {
        Row row = TableFactory.row();
        add(row);
        return row;
    }
	public TableOnParse withDummyFirstRow() {
		Parse firstRow = new Parse("tr", "", new Parse("td","empty",null,null), parse.parts);
		Parse pseudoTable = new Parse("table", "", firstRow, null);
		TableOnParse table = new TableOnParse(pseudoTable);
		table.setFirstRowIsHidden();
		return table;
	}
	private void setFirstRowIsHidden() {
		this.firstErrorRow  = 1;
		elementAt(0).setIsHidden();
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
	public void addFoldingText(String fold) {
		if (parse.more != null)
			new TableOnParse(parse.more).addToStartOfLeader(fold);
		else
			addToTrailer(fold);
	}
	public TablesOnParse getTables() {
		return new TablesOnParse(parse);
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
		for (Row row : this) {
			row.setColumnSpan(maxRowLength);
		}
	}

	private int getMaxRowColumnSpan() {
		int maxLength = 0;
		for (Row row : this)
			maxLength = Math.max(maxLength, row.getColumnSpan());
		return maxLength;
	}
	// Following is only needed for TestDefinedActionBodyCollector -- remove it when that is gone.
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TableOnParse))
			return false;
		TableOnParse other = (TableOnParse) obj;
		if (size() != other.size())
			return false;
		for (int i = 0; i < size(); i++)
			if (!elementAt(i).equals(other.elementAt(i)))
				return false;
		return true;
	}
	@Override
	public boolean isPlainTextTable() {
		return parse().tag.contains("plain_text_table");
	}
	@Override
	public void replaceAt(int t, Row row) {
		row.parse().more = parse().parts.at(t).more;
		if (t == 0)
			parse().parts = row.parse();
		else
			parse().parts.at(t-1).more = row.parse();
	}
	public Table deepCopy() {
		Table copy = TableFactory.table();
		for (Row row : this)
			copy.add(row.deepCopy());
		copy.setLeader(getLeader());
		copy.setTrailer(getTrailer());
		return copy;
	}
	public String getLeader() {
		return parse().leader;
	}
	public String getTrailer() {
		return parse().trailer;
	}
	@Override
	public void setLeader(String leader) {
		parse().leader = leader;
	}
	@Override
	public void setTrailer(String trailer) {
		parse().trailer = trailer;
	}
	@Override
	public Iterator<Row> iterator() {
		List<Row> list = new ArrayList<Row>();
		for (int i = 0; i < size(); i++)
			list.add(elementAt(i));
		return list.iterator();
	}
//	@Override
//	public Row elementAt(int i) {
//		return row(i);
//	}
	@Override
	public boolean isEmpty() {
		return parse.more == null;
	}
}
