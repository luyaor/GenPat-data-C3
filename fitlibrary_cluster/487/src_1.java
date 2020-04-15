/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fitlibrary.exception.NoSystemUnderTestException;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.flow.IScope;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Row;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.caller.ValidCall;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.option.Option;
import fitlibraryGeneric.typed.GenericTypedObject;

public class LookupMethodTargetStandard implements LookupMethodTarget {
	public void mustBeThreadSafe() {
		//
	}
	public CalledMethodTarget findSpecialMethod(Evaluator evaluator, String name) {
		if (name.equals(""))
			return null;
		Closure findEntityMethod = findFixturingMethod(evaluator,camel(name),new Class[]{ Row.class, TestResults.class });
		if (findEntityMethod == null)
			findEntityMethod = findFixturingMethod(evaluator,camel(name),new Class[]{ Row.class });
		if (findEntityMethod == null)
			return null;
		return new CalledMethodTarget(findEntityMethod,evaluator);
	}
	public CalledMethodTarget findPostfixSpecialMethod(Evaluator evaluator, String name) {
		if (name.equals(""))
			return null;
		Closure findEntityMethod = findFixturingMethod(evaluator,camel(name),new Class[]{ TestResults.class, Row.class });
		if (findEntityMethod == null)
			return null;
		return new CalledMethodTarget(findEntityMethod,evaluator);
	}
	public Closure findFixturingMethod(Evaluator evaluator, String name, Class<?>[] argTypes) {
		IScope scope = scopeOf(evaluator);
		for (TypedObject typedObject : scope.objectsForLookup()) {
			Closure target = typedObject.findPublicMethodClosureForTypedObject(name,argTypes);
			if (target != null)
				return target;
		}
		return null;
	}
	public CalledMethodTarget findMethodInEverySecondCell(Evaluator evaluator, Row row, int allArgs) throws Exception {
		int parms = allArgs / 2 + 1;
		int argCount = (allArgs + 1) / 2;
		String name = row.text(0,evaluator);
		for (int i = 1; i < parms; i++)
			name += " "+row.text(i*2,evaluator);
		CalledMethodTarget target = findTheMethodMapped(name,argCount,evaluator);
		target.setEverySecond(true);
		return target;
	}
	public CalledMethodTarget findTheMethodMapped(String name, int argCount, Evaluator evaluator) throws Exception {
		return findMethodOrGetter(camel(name), unknownParameterNames(argCount),"Type",evaluator);
	}
	private static List<String> unknownParameterNames(int argCount) {
		List<String> methodArgs = new ArrayList<String>();
		for (int i = 0; i < argCount; i++)
			methodArgs.add("arg"+(i+1));
		return methodArgs;
	}
	public CalledMethodTarget findMethodOrGetter(String name, List<String> methodArgs, String returnType, Evaluator evaluator) throws Exception {
		int argCount = methodArgs.size();
		IScope scope = scopeOf(evaluator);
		for (TypedObject typedObject : scope.objectsForLookup()) {
			Option<CalledMethodTarget> target =
				typedObject.new_findSpecificMethod(name,argCount,evaluator);
			if (target.isSome())
				return target.get();
			if (argCount == 0) {
				String getMethodName = ExtendedCamelCase.camel("get " + name);
				target = typedObject.new_findSpecificMethod(getMethodName, argCount, evaluator);
				if (target.isSome())
					return target.get();
				String isMethodName = ExtendedCamelCase.camel("is " + name);
				target = typedObject.new_findSpecificMethod(isMethodName, argCount, evaluator);
				if (target.isSome())
					return target.get();
			}
		}
		List<String> signatures = ClassUtility.methodSignatures(name, methodArgs, returnType);
		throw new MissingMethodException(signatures,scope.possibleClasses());
	}
	private IScope scopeOf(Evaluator evaluator) {
		if (evaluator.getRuntimeContext().hasScope())
			return evaluator.getRuntimeContext().getScope();
		throw new RuntimeException("No scope in runtime");
	}
	public CalledMethodTarget findMethod(String name, List<String> methodArgs, String returnType, Evaluator evaluator) {
		int argCount = methodArgs.size();
		IScope scope = scopeOf(evaluator);
		for (TypedObject typedObject : scope.objectsForLookup()) {
			Option<CalledMethodTarget> target = typedObject.new_findSpecificMethod(name,argCount,evaluator);
			if (target.isSome())
				return target.get();
		}
		List<String> signatures = ClassUtility.methodSignatures(name, methodArgs, returnType);
		throw new MissingMethodException(signatures,scope.possibleClasses());
	}
	public CalledMethodTarget findSetterOnSut(String propertyName, Evaluator evaluator) {
		return findMethodOnSut(camel("set "+propertyName), 1, evaluator,"ArgType "+camel(propertyName),"void");
	}
	public CalledMethodTarget findGetterOnSut(String propertyName, Evaluator evaluator, String returnType) {
		return findMethodOnSut(camel("get "+propertyName),0, evaluator,"",returnType);
	}
	private CalledMethodTarget findMethodOnSut(String methodName, int argCount, Evaluator evaluator, String arg, String returnType) {
		TypedObject typedObject = evaluator.getTypedSystemUnderTest();
		while (true) {
			if (typedObject.isNull())
				throw new NoSystemUnderTestException();
			Option<CalledMethodTarget> targetOption = typedObject.new_findSpecificMethod(methodName,argCount,evaluator);
			if (targetOption.isSome())
				return targetOption.get();
			if (typedObject instanceof Evaluator) {
				typedObject = ((Evaluator)typedObject).getTypedSystemUnderTest();
			}
			else if (typedObject.getSubject() instanceof DomainAdapter) {
				typedObject = new GenericTypedObject(((DomainAdapter)typedObject.getSubject()).getSystemUnderTest());
			}
			else break;
		}
		throw new MissingMethodException(signatures("public "+returnType+" "+methodName+"("+arg+") { }"),scopeOf(evaluator).possibleClasses());
	}
	public CalledMethodTarget findGetterUpContextsToo(TypedObject typedObject, Evaluator evaluator, String propertyName, boolean considerContext) {
		CalledMethodTarget target;
		if (considerContext)
			target = searchForMethodTargetUpOuterContext(propertyName,evaluator);
		else
			target =  typedObject.new_optionallyFindGetterOnTypedObject(propertyName,evaluator);
		if (target != null)
			return target;
		List<Class<?>> possibleClasses = new ArrayList<Class<?>>();
		if (considerContext)
			possibleClasses = scopeOf(evaluator).possibleClasses();
		else
			possibleClasses.add(typedObject.classType());
		throw new MissingMethodException(signatures("public ResultType "+ camel("get "+propertyName)+"() { }"),possibleClasses);
	}
	private List<String> signatures(String... signature) {
		return Arrays.asList(signature);
	}
    private CalledMethodTarget searchForMethodTargetUpOuterContext(String name, Evaluator evaluator) {
		IScope scope = scopeOf(evaluator);
		for (TypedObject typedObject : scope.objectsForLookup()) {
			CalledMethodTarget target = typedObject.new_optionallyFindGetterOnTypedObject(name,evaluator);
			if (target != null)
				return target;
		}
		return null;
    }
	public List<Class<?>> possibleClasses(Evaluator evaluator) {
		return scopeOf(evaluator).possibleClasses();
	}
	public Class<?> findClassFromFactoryMethod(Evaluator evaluator, Class<?> type, String typeName) throws IllegalAccessException, InvocationTargetException {
		String methodName = "concreteClassOf"+ClassUtility.simpleClassName(type);
		Closure method = findFixturingMethod(evaluator, methodName, new Class[] { String.class});
		if (method == null) {
			throw new MissingMethodException(signatures("public Class "+methodName+"(String typeName) { }"),
					scopeOf(evaluator).possibleClasses());
		}
		return (Class<?>)method.invoke(new Object[]{ typeName });
	}
	public Closure findNewInstancePluginMethod(Evaluator evaluator) {
		return findFixturingMethod(evaluator,"newInstancePlugin", new Class[] {Class.class});
	}
	private static String camel(String name) {
		return ExtendedCamelCase.camel(name);
	}
	public void findMethodsFromPlainText(String textCall, List<ValidCall> results, IScope scope) {
		int size = results.size();
		for (TypedObject typedObject : scope.objectsForLookup()) {
			typedObject.findMethodsFromPlainText(textCall, results);
			if (results.size() > size)
				return;
		}
	}
}
