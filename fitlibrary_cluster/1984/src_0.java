package fitlibrary;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.definedAction.ParameterSubstitution;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.caller.DefinedActionCaller;

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
    	if (table.elementAt(1).size() != 1)
    		throw new FitLibraryException("Second row of table for DefineAction needs to contain one cell.");
    	if (hasClass && table.elementAt(2).size() != 1)
    		throw new FitLibraryException("Third row of table for DefineAction needs to contain one cell.");
    	if (!table.elementAt(bodyRow).elementAt(0).hasEmbeddedTables())
    		throw new FitLibraryException("Second row of table for DefineAction needs to contain nested tables.");
    	if (hasClass)
    		wikiClassName = table.elementAt(1).text(0,this);
    	processDefinition(table.elementAt(1).elementAt(0).getEmbeddedTables(), testResults);
    	return null;
	}
    public String getPageName() {
		return pageName;
	}
	private void processDefinition(Tables tables, TestResults testResults) {
		Table headerTable = tables.elementAt(0);
		if (headerTable.size() == 2) {
			processMultiDefinedAction(headerTable,tables.followingTables());
			return;
		}
		Row parametersRow = headerTable.elementAt(0);
		if (headerTable.size() > 1)
			error("Unexpected rows in first table of defined action",parametersRow);
		parametersRow.passKeywords(testResults);
		Tables body = tables.followingTables();
		if (body.parse() == null) {
			Row row = TableFactory.row();
			row.addCell("comment");
			body = TableFactory.tables(TableFactory.table(row));
		}
		
		List<String> formalParameters = getDefinedActionParameters(parametersRow);
		ParameterSubstitution parameterSubstitution = new ParameterSubstitution(formalParameters,body.deepCopy(),pageName);
		TemporaryPlugBoardForRuntime.definedActionsRepository().define(parametersRow, wikiClassName, parameterSubstitution, this, pageName);
	}
	private void processMultiDefinedAction(Table headerTable, Tables body) {
		String definedActionName = headerTable.elementAt(0).elementAt(0).text();
		ArrayList<String> formalParameters = new ArrayList<String>();
		Row parametersRow = headerTable.elementAt(1);
		for (int c = 0; c < parametersRow.size(); c++) {
			String parameter = parametersRow.elementAt(c).text();
			if ("".equals(parameter))
				error("Parameter name is blank",parametersRow);
			if (formalParameters.contains(parameter))
				error("Parameter name '<b>"+parameter+"</b>' is duplicated",parametersRow);
			formalParameters.add(parameter);
		}
		TemporaryPlugBoardForRuntime.definedActionsRepository().defineMultiDefinedAction(definedActionName, formalParameters, body.deepCopy(), "");
	}
	private void error(String msg, Row parametersRow) {
		throw new FitLibraryExceptionInHtml(msg +" in <b>"+parametersRow.methodNameForCamel(this)+
				"</b> in "+DefinedActionCaller.link2(pageName));
	}
	private List<String> getDefinedActionParameters(Row parametersRow) {
		List<String> formalParameters = new ArrayList<String>();
    	if (wikiClassBased())
    		formalParameters.add("this");
    	for (int i = 1; i < parametersRow.size(); i += 2)
    		if (i < parametersRow.size()) {
    			String parameter = parametersRow.text(i,this);
    			if ("".equals(parameter))
    				error("Parameter name is blank",parametersRow);
    			if (formalParameters.contains(parameter))
    				error("Parameter name '<b>"+parameter+"</b>' is duplicated",parametersRow);
				formalParameters.add(parameter);
    		}
		return formalParameters;
	}
	private boolean wikiClassBased() {
		return !"".equals(wikiClassName);
	}
}
