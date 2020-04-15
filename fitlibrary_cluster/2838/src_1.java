/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.caller;

import java.util.HashSet;
import java.util.Set;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;

public class DefinedActionCallManager {
	protected final Set<Object> callsInProgress = new HashSet<Object>();
	protected Table shows = TableFactory.table();

	public void startCall(Object call) {
		clearShowsIfNoCallsInProgress();
		ensureNotInfinite(call);
		callsInProgress.add(call);
	}
	public void endCall(Object call) {
		callsInProgress.remove(call);
	}
	public Table getShowsTable() {
		return shows;
	}
	public void addShow(Row row) {
		if (callsInProgress.isEmpty())
			return;
		Row copy = row.deepCopy();
		shows.add(copy);
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
			shows = TableFactory.table();
	}
	private boolean hasShows() {
		return shows.size() > 0;
	}
	private boolean hasNoOutstandingCalls() {
		return callsInProgress.isEmpty();
	}
}
