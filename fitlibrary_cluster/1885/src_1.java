/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.dynamicVariable;

import java.io.IOException;
import java.util.Map;

public class LocalDynamicVariables extends DynamicVariablesMap {
	private DynamicVariables outer;

	public LocalDynamicVariables(DynamicVariables outer) {
		this.outer = outer;
	}
	public boolean addFromPropertiesFile(String fileName) {
		return outer.addFromPropertiesFile(fileName);
	}
	public void addFromUnicodePropertyFile(String fileName) throws IOException {
		outer.addFromUnicodePropertyFile(fileName);
	}
	@Override
	public void clearAll() {
		outer.clearAll();
	}
	@Override
	public Object get(String key) {
		Object result = super.get(key);
		if (result != null)
			return result;
		return outer.get(key);
	}
	@Override
	public void put(String key, Object value) {
		if (super.get(key) != null)
			putParameter(key,value);
		else
			outer.put(key, value);
	}
	public void putParameter(String key, Object value) {
		super.put(key,value);
	}
	public DynamicVariables popLocal() {
		return outer;
	}
	@Override
	public Map<Object,Object> getMap() {
		return outer.getMap();
	}
	public DynamicVariables top() {
		return outer.top();
	}
}
