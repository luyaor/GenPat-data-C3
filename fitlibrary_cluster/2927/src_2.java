/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
*/

package fitlibrary.definedAction;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.runtime.RuntimeContext;
import fitlibrary.table.Row;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.caller.ValidCall;

public interface DefinedActionsRepository {
	void clear();
	void define(Row parametersRow, String wikiClassName,
			ParameterSubstitution parameterSubstitution, Evaluator evaluator, String absoluteFileName);
	ParameterSubstitution lookupByCamel(String name, int argCount);
	ParameterSubstitution lookupByClassByCamel(String className, String name, int argCount, RuntimeContext variables);
	void findPlainTextCall(String textCall, List<ValidCall> results);
	void defineMultiDefinedAction(String name, ArrayList<String> formalParameters, 
			Tables body, String absoluteFileName);
	MultiParameterSubstitution lookupMulti(String name);
}
