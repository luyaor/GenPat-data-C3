/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.runtime;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.table.Table;

public class FoldingTexts {
	private static int NEXT_ID = 12345;
	private static int nextId = NEXT_ID++;
	private Map<String,StringBuilder> folds = new HashMap<String, StringBuilder>();
	
	public void logAsAfterTable(String title, String message) {
		synchronized(folds) {
			StringBuilder messages = folds.get(title);
			if (messages == null)
				folds.put(title,new StringBuilder(message));
			else
				messages.append(message);
		}
}
	public void addAccumulatedFoldingText(Table table) {
		synchronized(folds) {
			for (String key: folds.keySet())
				addAccumulatedFoldingText(key,table);
		}
	}
	private void addAccumulatedFoldingText(String title, Table table) {
		StringBuilder sb = folds.get(title);
		String text = sb.toString();
		folds.put(title, new StringBuilder());
		if (text == null || text.trim().isEmpty())
			return;
		final int id = nextId;
		nextId++;
		final String foldText =
			"<div class=\"included\">\n<div style=\"float: right;\" class=\"meta\">\n"+
			"<a href=\"javascript:expandAll();\">Expand All</a> |\n <a href=\"javascript:collapseAll();\">Collapse All</a></div>\n"+
			"<a href=\"javascript:toggleCollapsable('"+id+"');\">\n"+
			"<img src=\"/files/images/collapsableClosed.gif\" class=\"left\" id=\"img"+id+"\"/></a>\n"+
			"&nbsp;<span class=\"meta\">"+title+"</span><div class=\"hidden\" id=\""+id+"\">\n"+
			tabled(text,title)+
			"\n</div></div>\n";
		table.addFoldingText(foldText);
	}
	private String tabled(String text, String title) {
		if (title.equals("Logging"))
			return "<table border='1' cellspacing='0'>"+text+"</table>";
		return "<pre>"+text+"</pre>";
	}
}
