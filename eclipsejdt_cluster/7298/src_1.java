/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;

public class SourceMethod implements ISourceMethod, IConstants {
	private int modifiers;
	private int declarationStart;
	private int declarationEnd;
	private char[] returnTypeName;
	private char[] selector;
	private int nameSourceStart;
	private int nameSourceEnd;
	private char[][] argumentTypeNames;
	private char[][] argumentNames;
	private char[][] exceptionTypeNames;
	private char[] source;
	private String explicitConstructorCall;
public SourceMethod(
	int declarationStart,
	int modifiers,
	char[] returnTypeName,
	char[] selector,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] argumentTypeNames,
	char[][] argumentNames,
	char[][] exceptionTypeNames,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.returnTypeName = returnTypeName;
	this.selector = selector;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.argumentTypeNames = argumentTypeNames;
	this.argumentNames = argumentNames;
	this.exceptionTypeNames = exceptionTypeNames;
	this.source = source;
}
public String displayModifiers(int modifiers) {
	StringBuffer buffer = new StringBuffer();

	if (modifiers == 0)
		return null;
	if ((modifiers & AccPublic) != 0)
		buffer.append("public ");
	if ((modifiers & AccProtected) != 0)
		buffer.append("protected ");
	if ((modifiers & AccPrivate) != 0)
		buffer.append("private ");
	if ((modifiers & AccFinal) != 0)
		buffer.append("final ");
	if ((modifiers & AccStatic) != 0)
		buffer.append("static ");
	if ((modifiers & AccAbstract) != 0)
		buffer.append("abstract ");
	if ((modifiers & AccNative) != 0)
		buffer.append("native ");
	if ((modifiers & AccSynchronized) != 0)
		buffer.append("synchronized ");
	if (buffer.toString().trim().equals(""))
		return null;
	return buffer.toString().trim();
}
public String getActualName() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(source, nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	return buffer.toString();
}
public char[][] getArgumentNames() {
	return argumentNames;
}
public char[][] getArgumentTypeNames() {
	return argumentTypeNames;
}
public int getDeclarationSourceEnd() {
	return declarationEnd;
}
public int getDeclarationSourceStart() {
	return declarationStart;
}
public char[][] getExceptionTypeNames() {
	return exceptionTypeNames;
}
public int getModifiers() {
	return modifiers;
}
public int getNameSourceEnd() {
	return nameSourceEnd;
}
public int getNameSourceStart() {
	return nameSourceStart;
}
public char[] getReturnTypeName() {
	return returnTypeName;
}
public char[] getSelector() {
	return selector;
}
public boolean isConstructor() {
	return returnTypeName == null;
}
protected void setDeclarationSourceEnd(int position) {
	declarationEnd = position;
}
protected void setExplicitConstructorCall(String s) {
	explicitConstructorCall = s;
}
public String tabString(int tab) {
	/*slow code*/

	String s = "";
	for (int i = tab; i > 0; i--)
		s = s + "\t";
	return s;
}
public String toString() {
	return toString(0);
}
public String toString(int tab) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(tabString(tab));
	if (displayModifiers(modifiers) != null) {
		buffer.append(displayModifiers(modifiers)).append(" ");
	}
	if (returnTypeName != null) {
		buffer.append(returnTypeName).append(" ");
	}
	buffer.append(selector).append("(");
	if (argumentTypeNames != null) {
		for (int i = 0, max = argumentTypeNames.length; i < max; i++) {
			buffer.append(argumentTypeNames[i]).append(" ").append(
				argumentNames[i]).append(
				", "); 
		}
	}
	buffer.append(") ");
	if (exceptionTypeNames != null) {
		buffer.append("throws ");
		for (int i = 0, max = exceptionTypeNames.length; i < max; i++) {
			buffer.append(exceptionTypeNames[i]).append(", ");
		}
	}
	if (explicitConstructorCall != null) {
		buffer.append("{\n").append(tabString(tab+1)).append(explicitConstructorCall).append(tabString(tab)).append("}");
	} else {
		buffer.append("{}");
	}
	return buffer.toString();
}
}
