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
import org.eclipse.jdt.internal.compiler.env.ISourceField;

public class SourceField implements ISourceField, IConstants {
	protected int modifiers;
	protected char[] typeName;
	protected char[] name;
	protected int declarationStart;
	protected int declarationEnd;
	protected int nameSourceStart;
	protected int nameSourceEnd;
	protected char[] source;
public SourceField(
	int declarationStart,
	int modifiers,
	char[] typeName,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.typeName = typeName;
	this.name = name;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.source = source;
}
public String displayModifiers() {
	StringBuffer buffer = new StringBuffer();

	if (this.modifiers == 0)
		return null;
	if ((this.modifiers & AccPublic) != 0)
		buffer.append("public ");
	if ((this.modifiers & AccProtected) != 0)
		buffer.append("protected ");
	if ((this.modifiers & AccPrivate) != 0)
		buffer.append("private ");
	if ((this.modifiers & AccFinal) != 0)
		buffer.append("final ");
	if ((this.modifiers & AccStatic) != 0)
		buffer.append("static ");
	if ((this.modifiers & AccAbstract) != 0)
		buffer.append("abstract ");
	if ((this.modifiers & AccNative) != 0)
		buffer.append("native ");
	if ((this.modifiers & AccSynchronized) != 0)
		buffer.append("synchronized ");
	return buffer.toString();
}
public String getActualName() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(source, nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return declarationEnd;
}
public int getDeclarationSourceStart() {
	return declarationStart;
}
public char[] getInitializationSource() {
	return null;
}
public int getModifiers() {
	return modifiers;
}
public char[] getName() {
	return name;
}
public int getNameSourceEnd() {
	return nameSourceEnd;
}
public int getNameSourceStart() {
	return nameSourceStart;
}
public char[] getTypeName() {
	return typeName;
}
protected void setDeclarationSourceEnd(int position) {
	declarationEnd = position;
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
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers);
	}
	buffer.append(typeName).append(" ").append(name);
	buffer.append(";");
	return buffer.toString();
}
}
