/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.util.HashSet;
import java.util.List;

import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.dynamicVariable.LocalDynamicVariables;
import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Row;
import fitlibrary.table.Tables;

public class MultiParameterBinder {
	private List<String> formalParameters;
	private Tables tables;
	private String pageName;

	public MultiParameterBinder(List<String> formalParameters, Tables tables, String pageName) {
		this.formalParameters = formalParameters;
		this.tables = tables;
		this.pageName = pageName;
	}
	public void createMappingsForCall(List<String> actuals, LocalDynamicVariables vars) {
		if (actuals.size() != formalParameters.size())
			throw new RuntimeException("Formals and actuals don't match");
		for (int i = 0; i < actuals.size(); i++)
			vars.putParameter(formalParameters.get(i), actuals.get(i));
	}
	public Tables getCopyOfBody() {
		return tables.deepCopy();
	}
	public String getPageName() {
		return pageName;
	}
	public void verifyHeaderAgainstFormalParameters(Row row, VariableResolver resolver) {
		if (row.size() != formalParameters.size())
			throw new FitLibraryException("Expected "+formalParameters.size()+" parameters but there were "+row.size());
		HashSet<String> set = new HashSet<String>();
		for (int c = 0; c < row.size(); c++) {
			String headerName = row.text(c, resolver);
			if (!formalParameters.contains(headerName))
				throw new FitLibraryException("Unknown parameter: '"+headerName+"'");
			if (set.contains(headerName))
				throw new FitLibraryException("Duplicate parameter: '"+headerName+"'");
			set.add(headerName);
		}
	}
	public void bind(Row parameterRow, Row row, DynamicVariables dynamicVariables,
			VariableResolver resolver) {
		if (row.size() != formalParameters.size())
			throw new FitLibraryException("Expected "+formalParameters.size()+" parameters but there were "+row.size());
		for (int c = 0; c < row.size(); c++) {
			String parameter = parameterRow.text(c, resolver);
			String actual = row.text(c, resolver);
			dynamicVariables.putParameter(parameter, actual);
		}
	}
}
