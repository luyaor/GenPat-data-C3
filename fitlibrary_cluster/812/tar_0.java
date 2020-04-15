/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import java.util.Iterator;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.StorytestRunner;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableElement;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;

public class SpecifyFixture2 extends Traverse {
	private final StorytestRunner runner;
	private final SpecifyErrorReport errorReport;
	
	public SpecifyFixture2(StorytestRunner runner, SpecifyErrorReport errorReport) {
		this.runner = runner;
		this.errorReport = errorReport;
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		try {
			Cell actualTables = table.elementAt(0).elementAt(0);
			if (actualTables.isEmpty())
				throw new FitLibraryException("Missing nested tables to be run");
			Cell expectedCell = expectedOf(table);
			Tables expectedTables = expectedCell.getEmbeddedTables();
			runner.doStorytest(actualTables);
			if (reportsEqual("",actualTables,expectedTables)) {
				expectedCell.pass(testResults);
				testResults.addRights(cellCount(actualTables) - 1);
			} else {
				expectedCell.fail(testResults);
				errorReport.actualResult(actualTables);
			}
		} catch (Exception e) {
			table.error(testResults, e);
		}
		return null;
	}

	public boolean reportsEqual(String path, TableElement actual, TableElement expected) {
		if (actual.getClass() != expected.getClass()) {
			errorReport.classesWrong(path,actual,expected);
			return false;
		}
		if (actual.size() != expected.size()) {
			errorReport.sizeWrong(path,actual,expected);
			return false;
		}
		Iterator<TableElement> actuals = actual.iterator();
		Iterator<TableElement> expecteds = expected.iterator();
		int count = 0;
		while (actuals.hasNext()) {
			TableElement act = actuals.next();
			String nameOfElement = act.getType()+"["+count+"]";
			String pathFurther = path.isEmpty() ? nameOfElement : path + "." + nameOfElement;
			if (!reportsEqual(pathFurther,act,expecteds.next()))
				return false;
			count++;
		}
		if (actual instanceof Cell) {
			Cell actualCell = (Cell) actual;
			Cell expectedCell = (Cell) expected;
			if (!actualCell.text().equals(expectedCell.text())) {
				errorReport.cellTextWrong(path,actualCell.text(),expectedCell.text());
				return false;
			}
		}
		return true;
	}


	private Cell expectedOf(Table table) {
		if (table.size() == 2 && table.elementAt(1).size() == 1)
			return table.elementAt(1).elementAt(0);
		else if (table.size() == 1 && table.elementAt(0).size() == 2)
			return table.elementAt(0).elementAt(1);
		throw new FitLibraryException("Table must have one row with two cells or two rows with one cell");
	}
	private int cellCount(Tables tables) {
		int count = 0;
		for (Table table: tables)
			for (Row row: table)
				for (Cell cell: row) {
					count++;
					count += cellCount(cell);
				}
		return count;
	}
	
	interface SpecifyErrorReport {
		void sizeWrong(String path, TableElement actual, TableElement expected);
		void cellTextWrong(String path, String text, String text2);
		void classesWrong(String path, TableElement actual, TableElement expected);
		void actualResult(Cell actualTables);
	}
}
