/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import java.util.Random;

import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class RandomSelectTraverse extends Traverse {
	private static Random random = new Random();
	private String var;

	public RandomSelectTraverse(String var) {
		this.var = var;
	}

	@Override
	public Object interpretAfterFirstRow(Table table, @SuppressWarnings("unused") TestResults testResults) {
		int select = 1+random.nextInt(table.size()-1);
		setDynamicVariable(var, table.row(select).text(0,this));
		for (int i = 1; i < table.size(); i++)
			table.row(i).text(0,this);
		return null;
	}

}
