/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibraryGeneric.object;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import fitlibrary.closure.Closure;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.exception.method.MissingMethodException;
import fitlibrary.global.PlugBoard;
import fitlibrary.object.Finder;
import fitlibrary.ref.EntityReference;
import fitlibrary.traverse.Evaluator;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibraryGeneric.typed.GenericTyped;

public class GenericFinder implements Finder {
	public static final String FIND = "find";
	public static final String SHOW = "show";
	private GenericTyped typed;
    private String findExceptionMessage;
    private Closure findIntMethod, findStringMethod;
	private Closure showMethod;
	private Closure genericFindStringMethod, genericShowMethod;
    private EntityReference referenceParser;

	public GenericFinder(GenericTyped typed, Evaluator evaluator) {
		this.typed = typed;
    	String shortClassName = typed.simpleClassName();
		referenceParser = EntityReference.create(shortClassName.toLowerCase());
		
		final Class<?>[] intArg = { int.class };
		final Class<?>[] stringArg = { String.class };
		final Class<?>[] showArg = { typed.asClass() };
		final String findName = ExtendedCamelCase.camel(FIND+" "+shortClassName);
		String findMethodSignature = "public "+shortClassName+" find"+shortClassName+"(String key) { } ";
		String genericFindMethodSignature = "public "+shortClassName+" find"+shortClassName+"(String key, Type type) { } ";
		final String showMethodName = ExtendedCamelCase.camel(SHOW+" "+shortClassName);
		List<Class<?>> potentialClasses = PlugBoard.lookupTarget.possibleClasses(evaluator.getScope());
		
		findExceptionMessage = "Either "+shortClassName+
			" is <ul><li>A <b>Value Object</b>. So missing parse method: "+
			"public static "+shortClassName+" parse(String s) { }<br/>in class "+typed.getClassName()+
			"; or</li><li><b>An Entity</b>. So missing finder method: "+findMethodSignature;
		if (typed.isGeneric())
			findExceptionMessage += " or</li>Missing generic finder method: "+genericFindMethodSignature;
		findExceptionMessage += ", possibly in classes:"+names(potentialClasses)+"</li></ul>";
				
		findIntMethod = PlugBoard.lookupTarget.findFixturingMethod(evaluator, findName, intArg);
		findStringMethod = PlugBoard.lookupTarget.findFixturingMethod(evaluator, findName, stringArg);
		showMethod = PlugBoard.lookupTarget.findFixturingMethod(evaluator, showMethodName, showArg);
		if (typed.isGeneric()) {
			final Class<?>[] genericStringArg = { String.class, Type.class };
			final Class<?>[] genericShowArg = { typed.asClass(), Type.class };
			genericFindStringMethod = PlugBoard.lookupTarget.findFixturingMethod(evaluator, findName, genericStringArg);
			genericShowMethod = PlugBoard.lookupTarget.findFixturingMethod(evaluator, showMethodName, genericShowArg);
		}
	}
	private String names(List<Class<?>> classes) {
		return MissingMethodException.htmlListOfClassNames(classes);
	}
	private Object callFindStringMethod(String text) throws Exception {
        if (genericFindStringMethod != null)
            return genericFindStringMethod.invoke(new Object[]{ text,typed.asType() });
        if (findStringMethod != null)
        	return findStringMethod.invoke(new String[]{ text });
        if ("".equals(text))
        	return null;
        throw new FitLibraryExceptionInHtml(findExceptionMessage);
    }
	public Object find(final String text) throws Exception, IllegalAccessException, InvocationTargetException {
		if (findIntMethod != null) {
            int index = 0;
            try {
                index = referenceParser.getIndex(text);
            } catch (FitLibraryException e) {
                return callFindStringMethod(text);
            }
			return findIntMethod.invoke(new Integer[]{ new Integer(index) });
        }
        return callFindStringMethod(text);
	}
	public String show(Object result) throws Exception {
		if (genericShowMethod != null) {
			Object[] args = new Object[]{ result, typed.asType() };
            return genericShowMethod.invoke(args).toString();
		}
		if (showMethod != null) {
			Object[] args = new Object[]{ result };
            return showMethod.invoke(args).toString();
		}
		if (result == null)
			return "";
		return result.toString();
	}
	public boolean hasFinderMethod() {
		return findIntMethod != null || findStringMethod != null;
	}
}
