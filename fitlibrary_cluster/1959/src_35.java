/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

public class TablesOnList extends TableElementOnList<Table> implements Tables {
    public TablesOnList() {
    	super("");
    }
    public TablesOnList(Table theTable) {
		this();
		add(theTable);
	}
	public TablesOnList(Tables tables) {
		this();
		addTables(tables);
	}
	public TablesOnList(String tag) {
		super(tag);
	}
	public Tables deepCopy() {
		Tables copy = TableFactory.tables();
		for (Table table: this)
			copy.add(table.deepCopy());
		copy.setLeader(getLeader());
		copy.setTrailer(getTrailer());
		return copy;
	}
	public Tables followingTables() {
		return (TablesOnList) from(1);
	}
	protected void addTables(Tables tables) {
		for (Table table: tables)
			add(table);
	}
	@Override
	protected TableElementOnList<Table> newObject() {
		return new TablesOnList();
	}
}
