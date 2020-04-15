/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow.caller;

import java.util.HashSet;
import java.util.Set;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Row;
import fitlibrary.table.Table;

public class CallManager {
	protected static final ThreadLocal<Set<Object>> definedActionCallsInProgress = new ThreadLocal<Set<Object>>();
	protected static final ThreadLocal<Table> shows = new ThreadLocal<Table>();

	public static void startCall(Object call) {
		clearShowsIfNoCallsInProgress();
		ensureNotInfinite(call);
		get().add(call);
	}
	public static void endCall(Object call) {
		get().remove(call);
	}
	public static Table getShowsTable() {
		return shows.get();
	}
	private static Set<Object> ensureNotInfinite(Object call) {
		Set<Object> set = get();
		if (set.contains(call))
			throw new FitLibraryException("Infinite calling of defined actions");
		return set;
	}
	private static Set<Object> get() {
		Set<Object> set = definedActionCallsInProgress.get();
		if (set == null) {
			set = new HashSet<Object>();
			definedActionCallsInProgress.set(set);
		}
		return set;
	}
	public static void addShow(Row row) {
		if (definedActionCallsInProgress.get() == null || definedActionCallsInProgress.get().isEmpty())
			return;
		Table showTable = shows.get();
		if (showTable == null) {
			showTable = new Table();
			shows.set(showTable);
		}
		Row copy = row.copy();
		copy.last().shown();
		showTable.addRow(copy);
	}
	private static void clearShowsIfNoCallsInProgress() {
		Set<Object> set = definedActionCallsInProgress.get();
		if (set != null && set.isEmpty())
			shows.set(new Table());
	}
	public static boolean readyToShow() {
		return hasNoOutstandingCalls() && hasShows();
	}
	private static boolean hasShows() {
		Table showsTable = getShowsTable();
		return showsTable != null && showsTable.size() > 0;
	}
	private static boolean hasNoOutstandingCalls() {
		return get().isEmpty();
	}
}
