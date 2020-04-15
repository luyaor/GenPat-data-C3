/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.SimpleWikiTranslator;
import fitlibrary.utility.TableListener;

public class Tables {
    public Parse parse;

    public Tables() {
    	// start with an empty parse
    }
    public Tables(Parse parse) {
        this.parse = parse;
    }
    public Tables(Table theTable) {
		this(theTable.parse);
	}
	public Tables(String html) throws FitParseException {
		this(new Parse(html));
	}
	public Table table(int i) {
        return new Table(parse.at(i));
    }
    public void add(Table table) {
    	if (parse == null)
    		parse = table.parse;
    	else
            parse.last().more = table.parse;
    }
    public int size() {
        return parse.size();
    }
    @Override
	public String toString() {
        return "Tables["+ParseUtility.toString(parse)+"]";
    }
	public Parse parse() {
		return parse;
	}
	public Tables withExtraTableInFront() {
		Parse tablesWithTableInFront = new Parse("table","",new Parse("tr","",null,null),parse);
		return new Tables(tablesWithTableInFront);
	}
	public void ignoreAndFinished(TableListener tableListener) {
		table(0).finished(tableListener);
		for (int i = 1; i < size(); i++) {
			Table table = table(i);
			table.ignore(tableListener.getTestResults());
			table.finished(tableListener);
		}
		tableListener.storytestFinished();
	}
	public Table last() {
		return new Table(parse.last());
	}
	public Tables deepCopy() {
		return new Tables(ParseUtility.copyParse(parse));
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Tables))
			return false;
		Tables other = (Tables) obj;
		if (size() != other.size())
			return false;
		for (int i = 0; i < size(); i++)
			if (!table(i).equals(other.table(i)))
				return false;
		return true;
	}
	public Tables followingTables() {
		return new Tables(parse.more);
	}
	public static Tables fromWiki(String wiki) throws FitParseException {
		return new Tables(new Parse(SimpleWikiTranslator.translate(wiki)));
	}
}
