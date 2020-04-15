/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import fitlibrary.CalculateFixture;

public class CalculateFixtureSetUpWithException extends CalculateFixture {
    @Override
	public void setUp() {
        throw new RuntimeException("setUp exception.");
    }
    public int resultA(int a) { 
        return a+1;
    }
}
