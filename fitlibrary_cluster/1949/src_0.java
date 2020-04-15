/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fitlibrary.runResults.ITableListener;
import fitlibrary.runResults.TestResults;
import fitlibrary.utility.ParseUtility;

public class TableOnList extends TableElementOnList<Row> implements Table {
    private int firstErrorRow = 0;
    
	public TableOnList() {
		super("table");
        addToTag(" border=\"1\" cellspacing=\"0\"");
    }
    public TableOnList(Row... rows) {
    	this();
    	for (Row row: rows)
    		add(row);
	}
    @Override
	public void pass(TestResults testResults) {
        at(firstErrorRow).pass(testResults);
    }
    public void ignore(TestResults testResults) {
        at(firstErrorRow).ignore(testResults);
    }
    @Override
	public void error(TestResults testResults, Throwable e) {
        at(firstErrorRow).error(testResults,e);
    }
	public void error(ITableListener tableListener, Throwable e) {
		error(tableListener.getTestResults(),e);
	}
    public Row newRow() {
        Row row = TableFactory.row();
        add(row);
        return row;
    }
	public Table withDummyFirstRow() {
		TableOnList table = (TableOnList) from(0);
		table.add(0,new RowOnList());
		table.setFirstRowIsHidden();
		return table;
	}
	private void setFirstRowIsHidden() {
		this.firstErrorRow  = 1;
		at(0).setIsHidden();
	}
	public int phaseBoundaryCount() {
		int count = (getLeader()).split("<hr>").length-1;
		if (count == 0)
			count = (getLeader()).split("<hr/>").length-1;
		return count;
	}
	public void addFoldingText(String fold) {
		addToTrailer(fold);
	}
	public TablesOnParse getTables() {
		TablesOnParse tables = new TablesOnParse();
		tables.add(this);
		return tables;
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
	@Override
	public boolean isPlainTextTable() {
		return parse().tag.contains("plain_text_table");
	}
	@Override
	public void replaceAt(int t, Row row) {
		removeElementAt(t);
		add(t,row);
	}
	public Table deepCopy() {
		Table copy = TableFactory.table();
		for (Row row : this)
			copy.add(row.deepCopy());
		copy.setLeader(getLeader());
		copy.setTrailer(getTrailer());
		return copy;
	}
	@Override
	public String getType() {
		return "Table";
	}
	@Override
	protected TableElementOnList<Row> newObject() {
		return new TableOnList();
	}
	@Override
	public boolean hasRowsAfter(Row currentRow) {
		for (int i = 0; i < size()-1; i++)
			if (at(i) == currentRow)
				return true;
		return false;
	}
	public Parse asParse() {
		TableFactory.useOnLists(false);
		try {
			return ParseUtility.convert(this).parse();
		} finally {
			TableFactory.pop();
		}
	}
}
