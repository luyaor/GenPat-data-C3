/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;

public interface Tables {
	int size();
	Table table(int t);
	Table last();
	Tables followingTables();
	Parse parse();
	Tables deepCopy();
	void add(Table table);
	Cell cell(int table, int row, int cell);
}
