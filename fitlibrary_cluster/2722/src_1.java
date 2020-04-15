/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class TestRuntimeContext {
	@Test(expected=RuntimeException.class)
	public void cannotPutParameterWithGlobal() {
		new RuntimeContextImplementation().dynamicVariables().putParameter("k", "v");
	}
	@Test
	public void canPutParameterOncePushedLocal() {
		RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
		runtimeContext.pushLocal();
		runtimeContext.dynamicVariables().putParameter("k", "v");
		assertThat(runtimeContext.dynamicVariables().get("k"),is((Object)"v"));
	}
	@Test
	public void canPopLocalOncePushedLocal() {
		RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
		runtimeContext.pushLocal();
		runtimeContext.popLocal();
	}
	@Test(expected=RuntimeException.class)
	public void cannotPopLocalIfNotPushedLocal() {
		RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
		runtimeContext.popLocal();
	}
	@Test
	public void parameterBindingsAreDiscardedOnPop() {
		RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
		runtimeContext.pushLocal();
		runtimeContext.dynamicVariables().putParameter("k", "v");
		assertThat(runtimeContext.dynamicVariables().get("k"),is((Object)"v"));
		runtimeContext.popLocal();
		assertThat(runtimeContext.dynamicVariables().get("k") == null,is(true));
	}
	@Test
	public void previousParameterBindingsAreAvailableOnPop() {
		RuntimeContextInternal runtimeContext = new RuntimeContextImplementation();
		runtimeContext.pushLocal();
		runtimeContext.dynamicVariables().putParameter("k", "v");
		runtimeContext.pushLocal();
		assertThat(runtimeContext.dynamicVariables().get("k"),is((Object)"v"));
		runtimeContext.dynamicVariables().putParameter("k", "V");
		assertThat(runtimeContext.dynamicVariables().get("k"),is((Object)"V"));
		runtimeContext.popLocal();
		assertThat(runtimeContext.dynamicVariables().get("k"),is((Object)"v"));
		runtimeContext.popLocal();
		assertThat(runtimeContext.dynamicVariables().get("k") == null,is(true));
	}
}
