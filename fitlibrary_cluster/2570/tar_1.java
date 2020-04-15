/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.runtime;

import java.io.IOException;

import fitlibrary.dynamicVariable.DynamicVariables;

public interface RuntimeContext {
	Object getDynamicVariable(String key);
	void setDynamicVariable(String key, Object value);
	DynamicVariables dynamicVariables();
	void startLogging(String fileName);
	void printToLog(String s) throws IOException;
}
