/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.caller;

import java.util.HashSet;
import java.util.Set;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Row;
import fitlibrary.table.TableOnParse;

public class DefinedActionCallManager {
	protected final Set<Object> callsInProgress = new HashSet<Object>();
	protected TableOnParse shows = new TableOnParse();

	public void startCall(Object call) {
		clearShowsIfNoCallsInProgress();
		ensureNotInfinite(call);
		callsInProgress.add(call);
	}
	public void endCall(Object call) {
		callsInProgress.remove(call);
	}
	public TableOnParse getShowsTable() {
		return shows;
	}
	public void addShow(Row row) {
		if (callsInProgress.isEmpty())
			return;
		Row copy = row.copy();
		copy.last().shown();
		shows.addRow(copy);
	}
	public boolean readyToShow() {
		return hasNoOutstandingCalls() && hasShows();
	}
	
	private void ensureNotInfinite(Object call) {
		if (callsInProgress.contains(call))
			throw new FitLibraryException("Infinite calling of defined actions");
	}
	private void clearShowsIfNoCallsInProgress() {
		if (callsInProgress.isEmpty())
			shows = new TableOnParse();
	}
	private boolean hasShows() {
		return shows.size() > 0;
	}
	private boolean hasNoOutstandingCalls() {
		return callsInProgress.isEmpty();
	}
}
