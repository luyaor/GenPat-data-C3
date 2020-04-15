/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.parser;

import fitlibrary.DoFixture;
import fitlibrary.runtime.RuntimeContextImplementation;
import junit.framework.TestCase;

public abstract class ParserTestCase extends TestCase {
	public static DoFixture evaluatorWithRuntime() {
		DoFixture evaluator = new DoFixture();
		evaluator.setRuntimeContext(new RuntimeContextImplementation());
		return evaluator;
	}
}
