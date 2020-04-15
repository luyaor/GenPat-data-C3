/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class MetaData {
	
	Properties data;

public MetaData(Properties data) {
	this.data = data;
}

public ClassData getMetaData(Class clazz) {
	String key = JNIGenerator.toC(clazz.getName());
	String value = getMetaData(key, "");
	return new ClassData(clazz, value);
}

public FieldData getMetaData(Field field) {
	String className = JNIGenerator.getClassName(field.getDeclaringClass());
	String key = className + "_" + field.getName();
	String value = getMetaData(key, "");
	return new FieldData(field, value);
}

boolean convertTo32Bit(Class[] paramTypes) {
	boolean changed = false;
	for (int i = 0; i < paramTypes.length; i++) {
		Class paramType = paramTypes[i];
		if (paramType == Long.TYPE) {
			paramTypes[i] = Integer.TYPE;
			changed = true;
		}
		if (paramType == long[].class) {
			paramTypes[i] = int[].class;
			changed = true;
		}
	}
	return changed;	
}

public MethodData getMetaData(Method method) {
	String className = JNIGenerator.getClassName(method.getDeclaringClass());
	String key = className + "_" + JNIGenerator.getFunctionName(method);
	String value = getMetaData(key, null);
	if (value == null) {
		key = className + "_" + method.getName();
		value = getMetaData(key, null);
	}
	/*
	* Support for 64 bit port.
	*/
	if (value == null) {
		Class[] paramTypes = method.getParameterTypes();
		if (convertTo32Bit(paramTypes)) {
			key = className + "_" + JNIGenerator.getFunctionName(method, paramTypes);
			value = getMetaData(key, null);
		}
	}	
	if (value == null) value = "";	
	return new MethodData(method, value);
}

public ParameterData getMetaData(Method method, int parameter) {
	String className = JNIGenerator.getClassName(method.getDeclaringClass());
	String key = className + "_" + JNIGenerator.getFunctionName(method) + "_" + parameter;
	String value = getMetaData(key, null);
	if (value == null) {
		key = className + "_" + method.getName() + "_" + parameter;
		value = getMetaData(key, null);
	}	
	/*
	* Support for 64 bit port.
	*/
	if (value == null) {
		Class[] paramTypes = method.getParameterTypes();
		if (convertTo32Bit(paramTypes)) {
			key = className + "_" + JNIGenerator.getFunctionName(method, paramTypes) + "_" + parameter;
			value = getMetaData(key, null);
		}
	}	
	if (value == null) value = "";	
	return new ParameterData(method, parameter, value);
}

public String getMetaData(String key, String defaultValue) {
	return data.getProperty(key, defaultValue);
}

public void setMetaData(Class clazz, ClassData value) {
	String key = JNIGenerator.toC(clazz.getName());
	setMetaData(key, value.toString());
}

public void setMetaData(Field field, FieldData value) {
	String className = JNIGenerator.getClassName(field.getDeclaringClass());
	String key = className + "_" + field.getName();
	setMetaData(key, value.toString());
}

public void setMetaData(Method method, MethodData value) {
	String key;
	String className = JNIGenerator.getClassName(method.getDeclaringClass());
	if (JNIGenerator.isUnique(method, Modifier.NATIVE)) {
		key = className + "_" + method.getName ();
	} else {
		key = className + "_" + JNIGenerator.getFunctionName(method);
	}
	setMetaData(key, value.toString());
}

public void setMetaData(Method method, int arg, ParameterData value) {
	String key;
	String className = JNIGenerator.getClassName(method.getDeclaringClass());
	if (JNIGenerator.isUnique(method, Modifier.NATIVE)) {
		key = className + "_" + method.getName () + "_" + arg;
	} else {
		key = className + "_" + JNIGenerator.getFunctionName(method) + "_" + arg;
	}
	setMetaData(key, value.toString());
}

public void setMetaData(String key, String value) {
	data.setProperty(key, value);
}

}
