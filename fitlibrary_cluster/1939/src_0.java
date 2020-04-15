/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
 */

package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import fitlibrary.table.IRow;
import fitlibrary.traverse.Evaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.MustBeThreadSafe;

public interface LookupMethodTarget extends MustBeThreadSafe {
	ICalledMethodTarget findSpecialMethod(Evaluator evaluator, String name);
	CalledMethodTarget findPostfixSpecialMethod(Evaluator evaluator, String name);
	Closure findFixturingMethod(Evaluator evaluator, String name, Class<?>[] argTypes);
	CalledMethodTarget findMethodInEverySecondCell(Evaluator evaluator, IRow row, int allArgs) throws Exception;
	CalledMethodTarget findTheMethodMapped(String name, int argCount, Evaluator evaluator) throws Exception;
	CalledMethodTarget findTheMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator) throws Exception;
	CalledMethodTarget findMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator);
	CalledMethodTarget findSetter(String propertyName, Evaluator evaluator);
	CalledMethodTarget findGetterUpContextsToo(TypedObject typedObject, Evaluator evaluator, 
			String propertyName, boolean considerContext);
	List<Class<?>> identifiedClassesInSUTChain(Object firstObject);
	List<Class<?>> identifiedClassesInOutermostContext(Object firstObject, boolean includeSut);
	Class<?> findClassFromFactoryMethod(Evaluator evaluator, Class<?> type, String typeName) throws IllegalAccessException,
			InvocationTargetException;
	Closure findNewInstancePluginMethod(Evaluator evaluator);
}