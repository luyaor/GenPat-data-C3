/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.parser.self;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import fitlibrary.parser.DelegateParser;
import fitlibrary.typed.Typed;

public class SelfParser extends DelegateParser implements Cloneable {
    private Method parseMethod;
    
    public static SelfParser findSelfParser(Class<?> type) {
        Method parseMethod = findParseMethod(type);
        if (parseMethod == null)
        	return null;
        return new SelfParser(parseMethod);
    }
    public SelfParser(Method parseMethod) {
		this.parseMethod = parseMethod;
	}
	@Override
	public Object parse(String s, @SuppressWarnings("unused") Typed typed) throws Exception {
	    return parseMethod.invoke(null, new Object[] { s });
	}
	public static Method findParseMethod(Class<?> type)  {
		try {
			Method method = type.getMethod("parse", new Class[]{ String.class });
	        int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
			if (Modifier.isStatic(modifiers) &&
	        	   Modifier.isPublic(modifiers) &&
	        	   returnType != Void.class &&
	        	   !returnType.isPrimitive())
            	return method;
		} catch (NoSuchMethodException e) {
			//
		}
		return null;
	}
}