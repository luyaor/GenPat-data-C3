/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.runtime;

import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.suite.CollectObjectsForMethodLookup;

public interface RuntimeContextInternal extends RuntimeContext {
	RuntimeContextInternal freshCopy();
	DynamicVariables dynamicVariables();
	void putTimeout(String name, int timeout);
	int getTimeout(String name, int defaultTimeout);
	void pushLocal();
	void popLocal();
	boolean toExpandDefinedActions();
	void setExpandDefinedActions(boolean expandDefinedActions);
	CollectObjectsForMethodLookup getObjectCollector();
	void setObjectCollector(CollectObjectsForMethodLookup collector);
}
