/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.SimpleWikiTranslator;

public class TablesOnParse implements Tables {
    public Parse parse;

    public TablesOnParse(Parse parse) {
    	this.parse = parse;
    }
    public TablesOnParse() {
    	// start with an empty parse
    }
    public TablesOnParse(Table theTable) {
		this(theTable.parse());
	}
	public TablesOnParse(Tables tables) {
		this(ParseUtility.copyParse(tables.parse()));
	}
	public Table table(int i) {
        return new TableOnParse(parse.at(i));
    }
    public void add(Table table) {
    	if (parse == null)
    		parse = table.parse();
    	else
            parse.last().more = table.parse();
    }
    public int size() {
    	if (parse == null)
    		return 0;
        return parse.size();
    }
    @Override
	public String toString() {
        return "Tables["+ParseUtility.toString(parse)+"]";
    }
	public Parse parse() {
		return parse;
	}
	public TablesOnParse withExtraTableInFront() {
		Parse tablesWithTableInFront = new Parse("table","",new Parse("tr","",null,null),parse);
		return new TablesOnParse(tablesWithTableInFront);
	}
	public Table last() {
		return new TableOnParse(parse.last());
	}
	public Tables deepCopy() {
		Tables copy = TableFactory.tables();
		for (int i = 0; i < size(); i++)
			copy.add(table(i).copy());
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
			if (!table(i).equals(other.table(i)))
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
	public static Tables fromWiki(String wiki) throws FitParseException {
		return SimpleWikiTranslator.translateToTables(wiki);
	}
	@Override
	public Cell cell(int table, int row, int cell) {
		return table(table).row(row).cell(cell);
	}
}
