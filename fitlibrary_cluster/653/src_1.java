/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
 */

package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import fitlibrary.flow.IScope;
import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.caller.ValidCall;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.MustBeThreadSafe;

public interface LookupMethodTarget extends MustBeThreadSafe {
	ICalledMethodTarget findSpecialMethod(Evaluator evaluator, String name);
	CalledMethodTarget findPostfixSpecialMethod(Evaluator evaluator, String name);
	Closure findFixturingMethod(Evaluator evaluator, String name, Class<?>[] argTypes);
	CalledMethodTarget findMethodInEverySecondCell(Evaluator evaluator, Row row, int allArgs) throws Exception;
	CalledMethodTarget findTheMethodMapped(String name, int argCount, Evaluator evaluator) throws Exception;
	CalledMethodTarget findMethodOrGetter(String name, List<String> methodArgs, String returnType, Evaluator evaluator) throws Exception;
	CalledMethodTarget findMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator);
	CalledMethodTarget findSetterOnSut(String propertyName, Evaluator evaluator);
	CalledMethodTarget findGetterOnSut(String propertyName, Evaluator evaluator, String returnType);
	CalledMethodTarget findGetterUpContextsToo(TypedObject typedObject, Evaluator evaluator, 
			String propertyName, boolean considerContext);
	List<Class<?>> possibleClasses(Evaluator firstObject);
	Class<?> findClassFromFactoryMethod(Evaluator evaluator, Class<?> type, String typeName) throws IllegalAccessException,
			InvocationTargetException;
	Closure findNewInstancePluginMethod(Evaluator evaluator);
	void findMethodsFromPlainText(String textCall, List<ValidCall> results, IScope scope);
}