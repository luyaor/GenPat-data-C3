/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow;

public abstract class AbstractDoCaller implements DoCaller {
	private Exception problem = null;
	
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
	public boolean isAmbiguous() {
		return false;
	}
	public String getPartialErrorMessage() {
		return "NOT AN ERROR";
	}

}
