package fitlibrary;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.definedAction.ParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.TestResults;

public class DefineAction extends Traverse {
	private String wikiClassName = "";
	private String pageName;
	
	public DefineAction() {
		this.pageName = "from storytest table";
	}
    public DefineAction(String className) {
		this(className,"from storytest table");
	}
    public DefineAction(String className, String pathName) {
		this.wikiClassName = className;
		this.pageName = pathName;
	}
    @Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
    	try {
			return interpret(table, testResults);
		} catch (Exception e) {
			table.error(testResults, e);
			return null;
		}
    }
    public Object interpret(Table table, TestResults testResults) {
		if (table.size() < 2 || table.size() > 3)
    		throw new FitLibraryException("Table for DefineAction needs to be two or three rows, but is "+table.size()+".");
    	boolean hasClass = false;
    	int bodyRow = 1;
    	if (table.size() == 3) {
    		hasClass = true;
    		bodyRow = 2;
    	}
    	if (table.row(1).size() != 1)
    		throw new FitLibraryException("Second row of table for DefineAction needs to contain one cell.");
    	if (hasClass && table.row(2).size() != 1)
    		throw new FitLibraryException("Third row of table for DefineAction needs to contain one cell.");
    	if (!table.row(bodyRow).cell(0).hasEmbeddedTable())
    		throw new FitLibraryException("Second row of table for DefineAction needs to contain nested tables.");
    	if (hasClass)
    		wikiClassName = table.row(1).text(0,this);
    	processDefinition(table.row(1).cell(0).innerTables(), testResults);
    	return null;
	}
    public String getPageName() {
		return pageName;
	}
	private void processDefinition(Tables tables, TestResults testResults) {
		Table headerTable = tables.table(0);
		if (headerTable.size() == 2) {
			processNamedParameterDefinedAction(headerTable,tables.followingTables());
			return;
		}

		if (headerTable.size() > 1)
			throw new FitLibraryException("Unexpected rows in first table of defined action inpage at "+pageName);
		Row parametersRow = headerTable.row(0);
		parametersRow.passKeywords(testResults);
		Tables body = tables.followingTables();
		if (body.parse == null) {
			Row row = new Row();
			row.addCell("comment");
			body = new Tables(new Table(row));
		}
		
		List<String> formalParameters = getDefinedActionParameters(parametersRow);
		ParameterSubstitution parameterSubstitution = new ParameterSubstitution(formalParameters,body.deepCopy(),this,pageName);
		TemporaryPlugBoardForRuntime.definedActionsRepository().define(parametersRow, wikiClassName, parameterSubstitution, this, pageName);
	}
	private void processNamedParameterDefinedAction(Table headerTable, Tables body) {
		String definedActionName = headerTable.row(0).cell(0).text();
		ArrayList<String> parameters = new ArrayList<String>();
		Row parametersRow = headerTable.row(1);
		for (int c = 0; c < parametersRow.size(); c++)
			parameters.add(parametersRow.cell(c).text());
		TemporaryPlugBoardForRuntime.definedActionsRepository().defineMultiDefinedAction(definedActionName, parameters, body.deepCopy(), "");
	}
	private List<String> getDefinedActionParameters(Row parametersRow) {
		List<String> formalParameters = new ArrayList<String>();
    	if (wikiClassBased())
    		formalParameters.add("this");
    	for (int i = 1; i < parametersRow.size(); i += 2)
    		if (i < parametersRow.size())
    			formalParameters.add(parametersRow.text(i,this));
		return formalParameters;
	}
	private boolean wikiClassBased() {
		return !"".equals(wikiClassName);
	}
}
