/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;

public interface TableElement<From, To> extends Iterable<To> {
	void add(To t);
	int size();
	boolean isEmpty();
	To elementAt(int i);
	boolean elementExists(int i);
	To last();
	Iterable<To> listFrom(int start);
	From deepCopy();
	void setLeader(String leader);
	void setTrailer(String trailer);
	String getLeader();
	String getTrailer();
	Parse parse();
	String getType();
}
