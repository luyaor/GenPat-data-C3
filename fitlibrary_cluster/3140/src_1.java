/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 19/08/2006
*/

package fitlibrary.utility;

import junit.framework.TestCase;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.StringUtility;

public class TestClassUtility extends TestCase {
	public void testAFitLibraryClass() {
		assertIsFitLibraryClass(fitlibrary.FitLibraryFixture.class);
		assertIsFitLibraryClass(fitlibrary.DoFixture.class);
		
		assertIsFitLibraryClass(fitlibrary.traverse.Traverse.class);
		assertIsFitLibraryClass(fitlibrary.traverse.workflow.DoTraverse.class);
		assertIsFitLibraryClass(fitlibrary.collection.array.ArrayTraverse.class);
		
		assertIsFitLibraryClass(fit.Fixture.class);

		assertFalse(ClassUtility.aFitLibraryClass(fitlibrary.specify.suite.Simple.class));
		assertFalse(ClassUtility.aFitLibraryClass(fitlibrary.specify.eg.User.class));
	}
	private void assertIsFitLibraryClass(Class<?> type) {
		assertTrue(ClassUtility.aFitLibraryClass(type));
	}
    public void testReplaceString() {
        assertEquals("%20/%20",StringUtility.replaceString(" / "," ","%20"));
    }
}
