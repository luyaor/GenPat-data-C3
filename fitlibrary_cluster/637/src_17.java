/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import fitlibrary.table.Tables;

public class TablesMatcher extends TypeSafeMatcher<Tables>{
	private final Tables expected;
	
	public TablesMatcher(Tables expected) {
		this.expected = expected;
	}
	@Override
	public boolean matchesSafely(Tables item) {
		return false;
	}
	@Override
	public void describeTo(Description description) {
		description.appendText("a table the same as ").appendValue(expected);
	}

}
