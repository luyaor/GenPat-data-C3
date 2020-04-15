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
import fitlibrary.definedAction.DefinedActionBodyCollector.DefineActionBodyConsumer;
import fitlibrary.table.CellOnParse;
import fitlibrary.table.Table;
import fitlibrary.table.RowOnParse;
import fitlibrary.table.TableOnParse;
import fitlibrary.table.TablesOnParse;
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
				parseDefinitions(new TablesOnParse(new Parse(html)),determineClassName(pageName,test.getName()),test.getName());
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
	protected void parseDefinitions(TablesOnParse tables, final String className, final String pageName) {
		new DefinedActionBodyCollector().parseDefinitions(tables, new DefineActionBodyConsumer() {
			@Override
			public void addAction(TablesOnParse innerTables) {
				defineAction(innerTables,className,pageName);
			}
		});
	}
	protected void defineAction(TablesOnParse innerTables, String className, String pageName) {
		DefineAction defineAction = new DefineAction(className,pageName);
		defineAction.setRuntimeContext(getRuntimeContext());
		defineAction.interpret(createDefineActionTable(innerTables), new TestResults());
	}
	private TableOnParse createDefineActionTable(TablesOnParse innerTables) {
		TableOnParse defineActionTable = new TableOnParse();
		RowOnParse row = new RowOnParse();
		row.addCell("DefineAction");
		defineActionTable.addRow(row);
		row = new RowOnParse();
		row.addCell(new CellOnParse(innerTables));
		defineActionTable.addRow(row);
		return defineActionTable;
	}
	protected File fitNesseDiry() {
		return new File(FITNESSE_DIRY);
	}
}
