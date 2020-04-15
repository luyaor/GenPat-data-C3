/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class TablesOnParse extends ParseNode<Table> implements Tables {

    public TablesOnParse(Parse parse) {
    	super(parse);
    }
    public TablesOnParse() {
    	super(null);
    }
    public TablesOnParse(Table theTable) {
		this(theTable.parse());
	}
	public TablesOnParse(Tables tables) {
		this(ParseUtility.copyParse(tables.parse()));
	}
	@Override
	public Table elementAt(int i) {
        return new TableOnParse(parse.at(i));
    }
    public void add(Table table) {
    	if (parse == null)
    		parse = table.parse();
    	else
            parse.last().more = table.parse();
    }
    @Override
	public int size() {
    	if (parse == null)
    		return 0;
        return parse.size();
    }
    @Override
	public String toString() {
        return toString("Tables",parse);
    }
	public Tables deepCopy() {
		Tables copy = TableFactory.tables();
		for (Table table: this)
			copy.add(table.deepCopy());
		return copy;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TablesOnParse))
			return false;
		TablesOnParse other = (TablesOnParse) obj;
		if (size() != other.size())
			return false;
		for (int i = 0; i < size(); i++)
			if (!elementAt(i).equals(other.elementAt(i)))
				return false;
		return true;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	public Tables followingTables() {
		return new TablesOnParse(parse.more);
	}
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	@Override
	protected void error(TestResults testResults, Throwable e) {
		elementAt(0).error(testResults, e);
	}
}
