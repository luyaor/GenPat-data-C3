/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;
import fitlibrary.runResults.TestResults;

public interface TableElement<From, To> extends Iterable<To> {
	void add(To t);
	int size();
	boolean isEmpty();
	To at(int i);
	boolean elementExists(int i);
	To last();
	Iterable<To> iterableFrom(int start);
	From deepCopy();
	void setLeader(String leader);
	void setTrailer(String trailer);
	String getLeader();
	String getTrailer();
	String getTagLine();
	void setTagLine(String tagLine);
	Parse parse();
	String getType();
	void addToTag(String report);
	void error(TestResults testResults, Throwable e);
	void toHtml(StringBuilder builder);
}
