/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.typed.TypedObject;

public abstract class DoCaller {
	private Exception problem = null;
	
	public abstract boolean isValid();
	public abstract TypedObject run(Row row, TestResults testResults) throws Exception;
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
	public boolean partiallyValid() {
		return false;
	}
	public String getPartialErrorMessage() {
		return "NOT AN ERROR";
	}
}