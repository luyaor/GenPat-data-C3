/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import fit.Parse;
import fitlibrary.DefineAction;
import fitlibrary.batch.fitnesseIn.ParallelFitNesseRepository;
import fitlibrary.batch.trinidad.TestDescriptor;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ParseUtility;
import fitlibrary.utility.TestResults;

public class DefineActionsOnPageSlowly extends Traverse {
	protected String topPageName;
	private static String FITNESSE_DIRY = ".";

	public static void setFitNesseDiry(String diry) {
		FITNESSE_DIRY = diry;
	}
	public DefineActionsOnPageSlowly(String topPageName) {
		this.topPageName = topPageName;
	}
	@Override
	public Object interpretAfterFirstRow(Table tableWithPageName, TestResults testResults) {
		try {
			processPages(topPageName.substring(1));
		} catch (Exception e) {
			tableWithPageName.error(testResults, e);
		}
		return null;
	}
	private void processPages(String pageName) throws Exception {
		ParallelFitNesseRepository parallelFitNesseRepository = new ParallelFitNesseRepository(FITNESSE_DIRY);
		BlockingQueue<TestDescriptor> queue = parallelFitNesseRepository.getDefinedActions(pageName);
		while (true){
			TestDescriptor test = queue.take();
			if (ParallelFitNesseRepository.isSentinel(test))
				break;
			String html = ParseUtility.tabulize(test.getContent());
			if (html.contains("<table")) {
				parseDefinitions(new Tables(new Parse(html)),determineClassName(pageName,test.getName()),test.getName());
			}
		}
	}
	protected String determineClassName(String prefix, String pageName) {
		String fullPageName = prefix+"."+pageName;
		if ("".equals(pageName))
			fullPageName = prefix;
		int classPos = fullPageName.lastIndexOf(".Class");
		if (classPos >= 0) {
			int nextDotPos = fullPageName.indexOf(".",classPos+1);
			if (nextDotPos >= 0)
				return fullPageName.substring(classPos+6,nextDotPos);
		}
		return "";
	}
	// Warning: 'orrible code due to Parse!
	protected void parseDefinitions(Tables tables, String className, String absoluteFileName) {
		Tables innerTables = tables;
		for (int i = 0; i < tables.size(); i++) {
			Table nextTable = tables.table(i);
			if (i > 0 && isHR(nextTable.parse.leader)) {
				Table lastTableInDefinition = tables.table(i-1);
				Parse more = lastTableInDefinition.parse.more;
				String trailer = lastTableInDefinition.parse.trailer;
				lastTableInDefinition.parse.more = null;
				lastTableInDefinition.parse.trailer = "";
				defineAction(innerTables,className,absoluteFileName);
				lastTableInDefinition.parse.more = more;
				lastTableInDefinition.parse.trailer = trailer;
				innerTables = new Tables(nextTable);
			} else if (isHR(nextTable.parse.trailer) || i == tables.size() - 1)
				defineAction(innerTables,className,absoluteFileName);
		}
	}
	private void defineAction(Tables innerTables, String className, String absoluteFileName) {
		Table defineActionTable = createDefineActionTable(innerTables);
		DefineAction defineAction = new DefineAction(className,absoluteFileName);
		defineAction.interpret(defineActionTable, new TestResults());
	}
	private Table createDefineActionTable(Tables innerTables) {
		Table defineActionTable = new Table();
		Row row = new Row();
		row.addCell("DefineAction");
		defineActionTable.addRow(row);
		row = new Row();
		row.addCell(new Cell(innerTables));
		defineActionTable.addRow(row);
		return defineActionTable;
	}
	private boolean isHR(String leader) {
		return leader != null && (leader.contains("<hr>") || leader.contains("<hr/>"));
	}
	protected File fitNesseDiry() {
		return new File(FITNESSE_DIRY);
	}
}
