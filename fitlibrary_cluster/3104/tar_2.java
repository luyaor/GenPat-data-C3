/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitlibrary.definedAction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.global.TemporaryPlugBoardForRuntime;
import fitlibrary.runtime.RuntimeContextInternal;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.caller.ValidCall;

// This could easily be split into separate repositories.
// But I suspect the map for camel approach may disappear
public class DefinedActionsRepositoryStandard implements DefinedActionsRepository {
	private Map<DefinedAction, ParameterBinder> definedActionMapForPlainText = new ConcurrentHashMap<DefinedAction, ParameterBinder>();
	private Map<String, Map<DefinedAction, ParameterBinder>> classMapForPlainText = new ConcurrentHashMap<String, Map<DefinedAction, ParameterBinder>>();

	private Map<DefinedAction, ParameterBinder> definedActionMapForCamel = new ConcurrentHashMap<DefinedAction, ParameterBinder>();
	private Map<String, Map<DefinedAction, ParameterBinder>> classMapForCamel = new ConcurrentHashMap<String, Map<DefinedAction, ParameterBinder>>();

	private Map<DefinedMultiAction, MultiParameterBinder> definedMultiActionMap = new ConcurrentHashMap<DefinedMultiAction, MultiParameterBinder>();
	private Map<String, Map<DefinedMultiAction, MultiParameterBinder>> classMultiActionMap = new ConcurrentHashMap<String, Map<DefinedMultiAction, MultiParameterBinder>>();

	public void define(Row parametersRow, String wikiClassName, ParameterBinder binder, VariableResolver resolver,
			String absoluteFileName) {
		defineCamel(parametersRow, wikiClassName, binder,resolver, absoluteFileName);
		definePlain(parametersRow, wikiClassName, binder,resolver, absoluteFileName);
	}
	public ParameterBinder lookupByCamel(String name, int argCount) {
		return definedActionMapForCamel.get(new DefinedAction(name, argCount));
	}
	public ParameterBinder lookupByClassByCamel(String className,
			String name, int argCount, RuntimeContextInternal variables) {
		DefinedAction macro = new DefinedAction(name, argCount);
		Map<DefinedAction, ParameterBinder> map = classMapForCamel
				.get(className);
		if (map != null) {
			ParameterBinder macroSubstitution = map.get(macro);
			if (macroSubstitution != null)
				return macroSubstitution;
		}
		String superClass = variables.getDynamicVariables().getAsString(
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
	@Override
	public void defineMultiDefinedAction(String name, MultiParameterBinder binder) {
		definedMultiActionMap.put(new DefinedMultiAction(name),binder);
	}
	public MultiParameterBinder lookupMulti(String name) {
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
			ParameterBinder parameterSubstitution, VariableResolver resolver, String absoluteFileName) {
		String name = parametersRow.methodNameForPlain(resolver);
		DefinedAction definedAction = new DefinedAction(name, parametersRow
				.argumentCount());
		Map<DefinedAction, ParameterBinder> map = getClassMapForPlain(wikiClassName);
		if (map.get(definedAction) != null)
			throw new FitLibraryException("Duplicate defined action: " + name
					+ " defined in " + absoluteFileName
					+ " but already defined in "
					+ map.get(definedAction).getPageName());
		map.put(definedAction, parameterSubstitution);
	}
	protected void defineCamel(Row parametersRow, String wikiClassName,
			ParameterBinder parameterSubstitution, VariableResolver resolver, String absoluteFileName) {
		String name = parametersRow.methodNameForCamel(resolver);
		DefinedAction definedAction = new DefinedAction(name, parametersRow.argumentCount());
		Map<DefinedAction, ParameterBinder> map = getClassMapForCamel(wikiClassName);
		if (map.get(definedAction) != null)
			throw new FitLibraryException("Duplicate defined action: " + name
					+ "/" + parametersRow.argumentCount() + " defined in "
					+ absoluteFileName + " but already defined in "
					+ map.get(definedAction).getPageName());
		map.put(definedAction, parameterSubstitution);
	}
	protected Map<DefinedAction, ParameterBinder> getClassMapForPlain(String wikiClassName) {
		Map<DefinedAction, ParameterBinder> currentMap = definedActionMapForPlainText;
		if (wikiClassBased(wikiClassName)) {
			currentMap = classMapForPlainText.get(wikiClassName);
			if (currentMap == null) {
				currentMap = new ConcurrentHashMap<DefinedAction, ParameterBinder>();
				classMapForPlainText.put(wikiClassName, currentMap);
			}
		}
		return currentMap;
	}
	protected Map<DefinedAction, ParameterBinder> getClassMapForCamel(String wikiClassName) {
		Map<DefinedAction, ParameterBinder> currentMap = definedActionMapForCamel;
		if (wikiClassBased(wikiClassName)) {
			currentMap = classMapForCamel.get(wikiClassName);
			if (currentMap == null) {
				currentMap = new ConcurrentHashMap<DefinedAction, ParameterBinder>();
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
