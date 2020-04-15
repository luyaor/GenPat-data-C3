/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import fitlibrary.table.Row;
import fitlibrary.utility.TestResults;

public abstract class DoCaller {
	private Exception problem = null;
	
	public abstract boolean isValid();
	public abstract Object run(Row row, TestResults testResults) throws Exception;
	public abstract String ambiguityErrorMessage();

	public Exception problem() {
		return problem;
	}
	public boolean isProblem() {
		return problem != null;
	}
	protected void setProblem(Exception exception) {
		problem = exception;
	}
}