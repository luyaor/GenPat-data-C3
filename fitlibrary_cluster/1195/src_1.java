/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
 */

package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.MustBeThreadSafe;

public interface LookupMethodTarget extends MustBeThreadSafe {
	CalledMethodTarget findSpecialMethod(Evaluator evaluator, String name);
	CalledMethodTarget findPostfixSpecialMethod(Evaluator evaluator, String name);
	Closure findFixturingMethod(Evaluator evaluator, String name, Class<?>[] argTypes);
	CalledMethodTarget findMethodInEverySecondCell(Evaluator evaluator, Row row, int allArgs);
	CalledMethodTarget findTheMethodMapped(String name, int argCount, Evaluator evaluator);
	CalledMethodTarget findTheMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator);
	CalledMethodTarget findMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator);
	CalledMethodTarget findSetter(String propertyName, Evaluator evaluator);
	CalledMethodTarget findGetterUpContextsToo(TypedObject typedObject, Evaluator evaluator, 
			String propertyName, boolean considerContext);
	String identifiedClassesInSUTChain(Object firstObject);
	String identifiedClassesInOutermostContext(Object firstObject, boolean includeSut);
	Class<?> findClassFromFactoryMethod(Evaluator evaluator, Class<?> type, String typeName) throws IllegalAccessException,
			InvocationTargetException;
	Closure findNewInstancePluginMethod(Evaluator evaluator);
}