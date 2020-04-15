/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.runtime;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.table.Table;

public class FoldingTexts {
	private int nextId = 12345;
	private Map<String,String> folds = new HashMap<String, String>();
	
	public void logAsAfterTable(String title, String message) {
		String messages = folds.get(title);
		if (messages == null)
			folds.put(title,message);
		else
			folds.put(title,messages+message);
	}
	public void addAccumulatedFoldingText(Table table) {
		for (String key: folds.keySet())
			addAccumulatedFoldingText(key,table);
	}
	private void addAccumulatedFoldingText(String title, Table table) {
		String text = folds.get(title);
		if (text == null || text.trim().isEmpty())
			return;
		final int id = nextId;
		nextId++;
		final String foldText =
			"<div class=\"included\">\n<div style=\"float: right;\" class=\"meta\">\n"+
			"<a href=\"javascript:expandAll();\">Expand All</a> | <a href=\"javascript:collapseAll();\">Collapse All</a></div>\n"+
			"<a href=\"javascript:toggleCollapsable('"+id+"');\">\n"+
			"<img src=\"/files/images/collapsableClosed.gif\" class=\"left\" id=\"img"+id+"\"/></a>\n"+
			"&nbsp;<span class=\"meta\">"+title+"</span><div class=\"hidden\" id=\""+id+"\">\n<pre>"+
			text+
			"</pre>\n</div></div>";
		table.addFoldingText(foldText);
		folds.put(title,"");
	}
}
