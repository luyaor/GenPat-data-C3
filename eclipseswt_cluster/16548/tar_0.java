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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.eclipse.swt.SWT;

public abstract class JNIGenerator {

String delimiter;
PrintStream output;
MetaData metaData;

public JNIGenerator() {
	delimiter = System.getProperty("line.separator");
	output = System.out;
	metaData = new MetaData(new Properties());
}

String fixDelimiter(String str) {
	if (delimiter.equals("\n")) return str;
	int index = 0, length = str.length();
	StringBuffer buffer = new StringBuffer();
	while (index != -1) {
		int start = index;
		index = str.indexOf('\n', start);
		if (index == -1) {
			buffer.append(str.substring(start, length));
		} else {
			buffer.append(str.substring(start, index));
			buffer.append(delimiter);
			index++;
		}
	}
	return buffer.toString();
}

static String getClassName(Class clazz) {
	String name = clazz.getName();
	int index = name.lastIndexOf('.') + 1;
	return name.substring(index, name.length());
}

static String getFunctionName(Method method) {
	return getFunctionName(method, method.getParameterTypes());
}

static String getFunctionName(Method method, Class[] paramTypes) {
	String function = toC(method.getName());
	if (!isNativeUnique(method)) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(function);
		buffer.append("__");
		if (paramTypes.length > 0) {
			for (int i = 0; i < paramTypes.length; i++) {
				Class paramType = paramTypes[i];
				buffer.append(toC(getTypeSignature(paramType)));
			}
		}
		return buffer.toString();
	}
	return function;
}

static int getByteCount(Class clazz) {
	if (clazz == Integer.TYPE) return 4;
	if (clazz == Boolean.TYPE) return 4;
	if (clazz == Long.TYPE) return 8;
	if (clazz == Short.TYPE) return 2;
	if (clazz == Character.TYPE) return 2;
	if (clazz == Byte.TYPE) return 1;
	if (clazz == Float.TYPE) return 4;
	if (clazz == Double.TYPE) return 8;
	return 4;
}

static String getTypeSignature(Class clazz) {
	if (clazz == Integer.TYPE) return "I";
	if (clazz == Boolean.TYPE) return "Z";
	if (clazz == Long.TYPE) return "J";
	if (clazz == Short.TYPE) return "S";
	if (clazz == Character.TYPE) return "C";
	if (clazz == Byte.TYPE) return "B";
	if (clazz == Float.TYPE) return "F";
	if (clazz == Double.TYPE) return "D";
	if (clazz.isArray()) {
		Class componentType = clazz.getComponentType();
		return "[" + getTypeSignature(componentType);
	}
	return "L" + clazz.getName().replace('.', '/') + ";";
}

static String getTypeSignature1(Class clazz) {
	if (clazz == Integer.TYPE) return "Int";
	if (clazz == Boolean.TYPE) return "Boolean";
	if (clazz == Long.TYPE) return "Long";
	if (clazz == Short.TYPE) return "Short";
	if (clazz == Character.TYPE) return "Char";
	if (clazz == Byte.TYPE) return "Byte";
	if (clazz == Float.TYPE) return "Float";
	if (clazz == Double.TYPE) return "Double";
	return "Object";
}

static String getTypeSignature2(Class clazz) {
	if (clazz == Void.TYPE) return "void";
	if (clazz == Integer.TYPE) return "jint";
	if (clazz == Boolean.TYPE) return "jboolean";
	if (clazz == Long.TYPE) return "jlong";
	if (clazz == Short.TYPE) return "jshort";
	if (clazz == Character.TYPE) return "jchar";
	if (clazz == Byte.TYPE) return "jbyte";
	if (clazz == Float.TYPE) return "jfloat";
	if (clazz == Double.TYPE) return "jdouble";
	if (clazz == String.class) return "jstring";
	if (clazz.isArray()) {
		Class componentType = clazz.getComponentType();
		return getTypeSignature2(componentType) + "Array";
	}
	return "jobject";
}

static String getTypeSignature3(Class clazz) {
	if (clazz == Void.TYPE) return "void";
	if (clazz == Integer.TYPE) return "int";
	if (clazz == Boolean.TYPE) return "boolean";
	if (clazz == Long.TYPE) return "long";
	if (clazz == Short.TYPE) return "short";
	if (clazz == Character.TYPE) return "char";
	if (clazz == Byte.TYPE) return "byte";
	if (clazz == Float.TYPE) return "float";
	if (clazz == Double.TYPE) return "double";
	if (clazz == String.class) return "String";
	if (clazz.isArray()) {
		Class componentType = clazz.getComponentType();
		return getTypeSignature3(componentType) + "[]";
	}
	return clazz.getName();
}

static String getTypeSignature4(Class clazz) {
	if (clazz == Void.TYPE) return "void";
	if (clazz == Integer.TYPE) return "jint";
	if (clazz == Boolean.TYPE) return "jboolean";
	if (clazz == Long.TYPE) return "jlong";
	if (clazz == Short.TYPE) return "jshort";
	if (clazz == Character.TYPE) return "jchar";
	if (clazz == Byte.TYPE) return "jbyte";
	if (clazz == Float.TYPE) return "jfloat";
	if (clazz == Double.TYPE) return "jdouble";
	if (clazz.isArray()) {
		Class componentType = clazz.getComponentType();
		return getTypeSignature4(componentType) + " *";
	}
	return getClassName(clazz) + " *";
}

static Hashtable uniqueCache = new Hashtable();
static boolean isNativeUnique(Method method) {
	Object unique = uniqueCache.get(method);
	if (unique != null) return ((Boolean)unique).booleanValue();
	boolean result = true;
	Class clazz = method.getDeclaringClass();
	Method[] methods = clazz.getDeclaredMethods();
	for (int i = 0; i < methods.length; i++) {
		Method mth = methods[i];
		if ((method.getModifiers() & Modifier.NATIVE) ==  0) continue;
		if (method == mth || method.equals(mth)) continue;
		if (method.getName().equals(mth.getName())) {
			result = false;
			break;
		}
	}
	uniqueCache.put(method, new Boolean(result));
	return result;
}

static void sort(Method[] methods) {
	Arrays.sort(methods, new Comparator() {
		public int compare(Object a, Object b) {
			Method mth1 = (Method)a;
			Method mth2 = (Method)b;
			int result = mth1.getName().compareTo(mth2.getName());
			return result != 0 ? result : getFunctionName(mth1).compareTo(getFunctionName(mth2));
		}
	});
}

static void sort(Field[] fields) {
	Arrays.sort(fields, new Comparator() {
		public int compare(Object a, Object b) {
			return ((Field)a).getName().compareTo(((Field)b).getName());
		}
	});
}

static void sort(Class[] classes) {
	Arrays.sort(classes, new Comparator() {
		public int compare(Object a, Object b) {
			return ((Class)a).getName().compareTo(((Class)b).getName());
		}
	});	
}

static String toC(String str) {
	int length = str.length();
	StringBuffer buffer = new StringBuffer(length * 2);
	for (int i = 0; i < length; i++) {
		char c = str.charAt(i);
		switch (c) {
			case '_': buffer.append("_1"); break;
			case ';': buffer.append("_2"); break;
			case '[': buffer.append("_3"); break;
			case '.': buffer.append("_"); break;
			case '/': buffer.append("_"); break;
			default: buffer.append(c);
		}
	}
	return buffer.toString();
}

public abstract void generate(Class clazz);

public void generate(Class[] classes) {
	sort(classes);
	for (int i = 0; i < classes.length; i++) {
		Class clazz = classes[i];
		generate(clazz);
	}
}

public void generateMetaData(String key) {
	MetaData mt = getMetaData();
	String data = mt.getMetaData(key, null);
	if (data == null) return;
	output(fixDelimiter(data));
	outputDelimiter();
}

public String getDelimiter() {
	return delimiter;
}

public PrintStream getOutput() {
	return output;
}

public MetaData getMetaData() {
	return metaData;
}

public String getPlatform() {
	return SWT.getPlatform();
}

public void output(String str) {
	output.print(str);
}

public void outputDelimiter() {
	output(getDelimiter());
}

public void setDelimiter(String delimiter) {
	this.delimiter = delimiter;
}

public void setMetaData(MetaData data) {
	metaData = data;
}

public void setOutput(PrintStream output) {
	this.output = output;
}

}
