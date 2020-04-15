/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitlibrary.definedAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.runtime.RuntimeContext;
import fitlibrary.table.Row;
import fitlibrary.table.Tables;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.caller.ValidCall;

// This could easily be split into separate repositories.
// But I suspect the map for camel approach may disappear
public class DefinedActionsRepositoryStandard implements DefinedActionsRepository {
	private Map<DefinedAction, ParameterSubstitution> definedActionMapForPlainText = new ConcurrentHashMap<DefinedAction, ParameterSubstitution>();
	private Map<String, Map<DefinedAction, ParameterSubstitution>> classMapForPlainText = new ConcurrentHashMap<String, Map<DefinedAction, ParameterSubstitution>>();

	private Map<DefinedAction, ParameterSubstitution> definedActionMapForCamel = new ConcurrentHashMap<DefinedAction, ParameterSubstitution>();
	private Map<String, Map<DefinedAction, ParameterSubstitution>> classMapForCamel = new ConcurrentHashMap<String, Map<DefinedAction, ParameterSubstitution>>();

	private Map<DefinedMultiAction, MultiParameterSubstitution> definedMultiActionMap = new ConcurrentHashMap<DefinedMultiAction, MultiParameterSubstitution>();
	private Map<String, Map<DefinedMultiAction, MultiParameterSubstitution>> classMultiActionMap = new ConcurrentHashMap<String, Map<DefinedMultiAction, MultiParameterSubstitution>>();

	public void define(Row parametersRow, String wikiClassName,
			ParameterSubstitution parameterSubstitution, Evaluator evaluator,
			String absoluteFileName) {
		defineCamel(parametersRow, wikiClassName, parameterSubstitution,
				evaluator, absoluteFileName);
		definePlain(parametersRow, wikiClassName, parameterSubstitution,
				evaluator, absoluteFileName);
	}
	public ParameterSubstitution lookupByCamel(String name, int argCount) {
		return definedActionMapForCamel.get(new DefinedAction(name, argCount));
	}
	public ParameterSubstitution lookupByClassByCamel(String className,
			String name, int argCount, RuntimeContext variables) {
		DefinedAction macro = new DefinedAction(name, argCount);
		Map<DefinedAction, ParameterSubstitution> map = classMapForCamel
				.get(className);
		if (map != null) {
			ParameterSubstitution macroSubstitution = map.get(macro);
			if (macroSubstitution != null)
				return macroSubstitution;
		}
		String superClass = variables.dynamicVariables().getAsString(
				(className + ".super"));
		if (superClass != null && !"".equals(superClass))
			return TemporaryPlugBoardForRuntime
					.definedActionsRepository()
					.lookupByClassByCamel(superClass, name, argCount, variables);
		return definedActionMapForCamel.get(macro);
	}
	public void findPlainTextCall(String textCall, List<ValidCall> results) {
		for (DefinedAction action : definedActionMapForPlainText.keySet())
			action.findCall(textCall, results);
	}
	public void defineMultiDefinedAction(String name, ArrayList<String> formalParameters,
			Tables body, String absoluteFileName) {
		definedMultiActionMap.put(new DefinedMultiAction(name),
				new MultiParameterSubstitution(formalParameters, body,
						absoluteFileName));
	}
	public MultiParameterSubstitution lookupMulti(String name) {
		return definedMultiActionMap.get(new DefinedMultiAction(name));
	}
	public void clear() {
		definedActionMapForPlainText.clear();
		classMapForPlainText.clear();
		definedActionMapForCamel.clear();
		classMapForCamel.clear();
		definedMultiActionMap.clear();
		classMultiActionMap.clear();
	}
	protected void definePlain(Row parametersRow, String wikiClassName,
			ParameterSubstitution parameterSubstitution, Evaluator evaluator, String absoluteFileName) {
		String name = parametersRow.methodNameForPlain(evaluator);
		DefinedAction definedAction = new DefinedAction(name, parametersRow
				.argumentCount());
		Map<DefinedAction, ParameterSubstitution> map = getClassMapForPlain(wikiClassName);
		if (map.get(definedAction) != null)
			throw new FitLibraryException("Duplicate defined action: " + name
					+ " defined in " + absoluteFileName
					+ " but already defined in "
					+ map.get(definedAction).getPageName());
		map.put(definedAction, parameterSubstitution);
	}
	protected void defineCamel(Row parametersRow, String wikiClassName,
			ParameterSubstitution parameterSubstitution, Evaluator evaluator, String absoluteFileName) {
		String name = parametersRow.methodNameForCamel(evaluator);
		DefinedAction definedAction = new DefinedAction(name, parametersRow
				.argumentCount());
		Map<DefinedAction, ParameterSubstitution> map = getClassMapForCamel(wikiClassName);
		if (map.get(definedAction) != null)
			throw new FitLibraryException("Duplicate defined action: " + name
					+ "/" + parametersRow.argumentCount() + " defined in "
					+ absoluteFileName + " but already defined in "
					+ map.get(definedAction).getPageName());
		map.put(definedAction, parameterSubstitution);
	}
	protected Map<DefinedAction, ParameterSubstitution> getClassMapForPlain(String wikiClassName) {
		Map<DefinedAction, ParameterSubstitution> currentMap = definedActionMapForPlainText;
		if (wikiClassBased(wikiClassName)) {
			currentMap = classMapForPlainText.get(wikiClassName);
			if (currentMap == null) {
				currentMap = new ConcurrentHashMap<DefinedAction, ParameterSubstitution>();
				classMapForPlainText.put(wikiClassName, currentMap);
			}
		}
		return currentMap;
	}
	protected Map<DefinedAction, ParameterSubstitution> getClassMapForCamel(String wikiClassName) {
		Map<DefinedAction, ParameterSubstitution> currentMap = definedActionMapForCamel;
		if (wikiClassBased(wikiClassName)) {
			currentMap = classMapForCamel.get(wikiClassName);
			if (currentMap == null) {
				currentMap = new ConcurrentHashMap<DefinedAction, ParameterSubstitution>();
				classMapForCamel.put(wikiClassName, currentMap);
			}
		}
		return currentMap;
	}
	protected boolean wikiClassBased(String wikiClassName) {
		return !"".equals(wikiClassName);
	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Plain Text:\n");
		for (DefinedAction action : definedActionMapForPlainText.keySet()) {
			s.append(action.toString());
			s.append("\n");
		}
		s.append("\n\nCamel:\n");
		for (DefinedAction action : definedActionMapForCamel.keySet()) {
			s.append(action.toString());
			s.append("\n");
		}
		return s.toString();
	}
}
