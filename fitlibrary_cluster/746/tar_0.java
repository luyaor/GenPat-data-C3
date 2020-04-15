/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 16/12/2006
*/

package fitlibrary.suite;

import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class InFlowPageRunner extends PageRunner {
	private DoEvaluator doEvaluator;

	public InFlowPageRunner(DoEvaluator doEvaluator, TestResults runContext) {
		super(runContext);
		this.doEvaluator = doEvaluator;
	}
	public void run(Tables tables, int index, TableListener tableListener) {
		for (int t = index; t < tables.size(); t++) {
			if (ignored(tables,t,tableListener))
				return;
			Table table = tables.table(t);
			doEvaluator.interpretWholeTable(table, tableListener);
			tableListener.tableFinished(table);
		}
	}
}
