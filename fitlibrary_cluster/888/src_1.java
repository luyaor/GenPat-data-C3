/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.table;

import fit.Parse;
import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.utility.TestResults;

public interface Row extends TableElement<Row,Cell> {
	int size();
	boolean isEmpty();
	Cell last();
	boolean hasFurtherRows();
	boolean cellExists(int i);
	Cell addCell();
	Cell addCell(String s);
	Cell addCell(String text, int cols);
	
	String text(int i, VariableResolver resolver);
	
	void pass(TestResults testResults);
	void passKeywords(TestResults testResults);
	void fail(TestResults testResults);
	void error(TestResults testResults, Throwable e);
	void ignore(TestResults testResults);
	void missing(TestResults testResults);
	void shown();
	Row rowFrom(int i);
	
	int argumentCount();
	String methodNameForCamel(VariableResolver resolver);
	String methodNameForPlain(VariableResolver resolver);
	
	Parse parse();
	int getColumnSpan();
	void setColumnSpan(int span);
	void setIsHidden();
	boolean didPass();
	boolean didFail();
	void removeCell(int i);
}
