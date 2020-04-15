/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.TableOnParse;
import fitlibrary.table.Tables;
import fitlibrary.table.TablesOnParse;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.TestResultsFactory;

public class UseTemplateTraverse extends Traverse {
	private String templateName;

	public UseTemplateTraverse(String templateName) {
		this.templateName = templateName;
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		TablesOnParse tables = table.getTables();
		for (int t = 0; t < tables.size(); t++) {
			Table defTable = tables.table(t);
			Row firstRow = defTable.row(0);
			if (firstRow.size() == 2 && firstRow.text(0,this).equals("template") && firstRow.text(1,this).equals(templateName)) {
				interpret(defTable, table, testResults);
				return null;
			}
		}
		throw new FitLibraryException("Missing definition for template "+templateName);
	}
	private void interpret(Table definingTable, Table callingTable, TestResults testResults) {
		Row actualParameterNames = callingTable.row(1);
		int parameterCount = actualParameterNames.size();
		DefinedActionTraverse defineTemplateTraverse = createDefinedActionTraverse(definingTable, parameterCount);
		int errors = 0;
		for (int r = 2; r < callingTable.size(); r++) {
			Row row = callingTable.row(r);
			List<Object> parameters = new ArrayList<Object>();
			for (int c = 0; c < row.size(); c++) {
				if (row.cell(c).hasEmbeddedTable())
					parameters.add(row.cell(c).getEmbeddedTable());
				else
					parameters.add(row.text(c,this));
			}
			TestResults results = TestResultsFactory.testResults();
			Tables resultingTables = defineTemplateTraverse.call(parameters,results);
			if (results.passed())
				row.pass(testResults);
			else {
				Row argsRow = appendTableToReport(callingTable, actualParameterNames, errors, parameters, resultingTables);
				passOnColourings(testResults, row, argsRow, results);
				errors++;
			}
		}
	}
	// Added for Jacques Morel
	protected DefinedActionTraverse createDefinedActionTraverse(Table definingTable, int parameterCount) {
		return new DefinedActionTraverse(definingTable,parameterCount);
	}
	private Row appendTableToReport(Table callingTable, Row actualParameterNames, int errors, List<Object> parameters, Tables resultingTables) {
		Table commentTable = TableFactory.table();
		commentTable.newRow().addCell("comment");
		Table paramsTable = TableFactory.table();
		Row templateRow = paramsTable.newRow();
		templateRow.addCell("use template");
		templateRow.addCell(templateName);
		Row paramsRow = paramsTable.newRow();
		for (int c = 0; c < actualParameterNames.size(); c++)
			paramsRow.addCell(TableFactory.cell(actualParameterNames.text(c,this)));
		Row argsRow = paramsTable.newRow();
		for (Object paramValue: parameters) {
			if (paramValue instanceof TableOnParse) {
				Cell newCell = argsRow.addCell();
				newCell.setInnerTables(TableFactory.tables((Table) paramValue));
			} else
				argsRow.addCell((String) paramValue);
		}
		paramsTable.evenUpRows();
		commentTable.newRow().addCell(TableFactory.cell(TableFactory.tables(paramsTable)));
		commentTable.newRow().addCell(TableFactory.cell(resultingTables));
		callingTable.insertTable(errors,commentTable);
		return argsRow;
	}
	private void passOnColourings(TestResults testResults, Row row, Row argsRow, TestResults results) {
		if (results.failed()) {
			row.fail(testResults);
			argsRow.fail(testResults);
		} else if (results.errors()) {
			row.error(testResults, new FitLibraryException(""));
			argsRow.error(testResults, new FitLibraryException(""));
		} else {
			row.ignore(testResults);
			argsRow.ignore(testResults);
		}
	}
}
