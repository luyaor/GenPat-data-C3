/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.util.HashSet;
import java.util.List;

import fitlibrary.dynamicVariable.DynamicVariables;
import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.table.Row;
import fitlibrary.table.Tables;

public class MultiParameterBinder {
	private final List<String> formalParameters;
	private final Tables tables;
	private final String pageName;

	public MultiParameterBinder(List<String> formalParameters, Tables tables, String pageName) {
		this.formalParameters = formalParameters;
		this.tables = tables;
		this.pageName = pageName;
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
	public void bindMulti(Row parameterRow, Row callRow, DynamicVariables dynamicVariables) {
		if (callRow.size() != formalParameters.size())
			throw new FitLibraryException("Expected "+formalParameters.size()+" parameters but there were "+callRow.size());
		for (int c = 0; c < callRow.size(); c++) {
			String parameter = parameterRow.text(c, dynamicVariables);
			if (callRow.at(c).hasEmbeddedTables(dynamicVariables))
				dynamicVariables.putParameter(parameter, callRow.at(c).getEmbeddedTables());
			else
				dynamicVariables.putParameter(parameter, callRow.text(c, dynamicVariables));
		}
	}
	public void bindUni(Row callRow, DynamicVariables dynamicVariables) {
		if (callRow.size() != formalParameters.size())
			throw new FitLibraryException("Expected "+formalParameters.size()+" parameters but there were "+callRow.size());
		for (int c = 0; c < callRow.size(); c++) {
			String parameter = formalParameters.get(c);
			if (callRow.at(c).hasEmbeddedTables(dynamicVariables))
				dynamicVariables.putParameter(parameter, callRow.at(c).getEmbeddedTables());
			else
				dynamicVariables.putParameter(parameter, callRow.text(c, dynamicVariables));
		}
	}
}
