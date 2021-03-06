/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class TypeReferencePattern extends AndPattern implements IIndexConstants {

protected char[] qualification;
protected char[] simpleName;
	
protected char[] currentCategory;

/* Optimization: case where simpleName == null */
public int segmentsSize;
protected char[][] segments;
protected int currentSegment;

protected static char[][] CATEGORIES = { REF };

public TypeReferencePattern(char[] qualification, char[] simpleName, int matchRule) {
	this(matchRule);

	this.qualification = isCaseSensitive() ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive() ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	else
		this.segments = null;
	
	if (this.segments == null)
		if (this.qualification == null)
			this.segmentsSize =  0;
		else
			this.segmentsSize =  CharOperation.occurencesOf('.', this.qualification) + 1;
	else
		this.segmentsSize = this.segments.length;

	((InternalSearchPattern)this).mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
/*
 * Instanciate a type reference pattern with additional information for generics search
 */
public TypeReferencePattern(char[] qualification, char[] simpleName, String signature, int matchRule) {
	this(qualification, simpleName,matchRule);

	if (signature != null) computeSignature(signature);
}
public TypeReferencePattern(char[] qualification, char[] simpleName, IType type, int matchRule) {
	this(qualification, simpleName, matchRule);

	this.typeArguments = typeParameterNames(type);
}
TypeReferencePattern(int matchRule) {
	super(TYPE_REF_PATTERN, matchRule);
}
public void decodeIndexKey(char[] key) {
	this.simpleName = key;
}
public SearchPattern getBlankPattern() {
	return new TypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getIndexKey() {
	if (this.simpleName != null)
		return this.simpleName;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	if (this.currentSegment >= 0) 
		return this.segments[this.currentSegment];
	return null;
}
public char[][] getIndexCategories() {
	return CATEGORIES;
}
protected boolean hasNextQuery() {
	if (this.segments == null) return false;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // index key is not encoded so query results all match
}
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	if (this.segments != null)
		this.currentSegment = this.segments.length - 1;
}
protected StringBuffer print(StringBuffer output) {
	output.append("TypeReferencePattern: qualification<"); //$NON-NLS-1$
	if (qualification != null) 
		output.append(qualification);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		output.append(simpleName);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">"); //$NON-NLS-1$
	return super.print(output);
}
/*
 * Returns the type parameter names of the given type.
 */
private char[][][] typeParameterNames(IType type) {
	char[][][] typeParameters = new char[10][][];
	int ptr = -1;
	try {
		IJavaElement parent = type;
		ITypeParameter[] parameters = null;
		boolean hasParameters = false;
		while (parent != null) {
			if (parent.getElementType() != IJavaElement.TYPE) {
				if (!hasParameters) return null;
				if (++ptr < typeParameters.length)
					System.arraycopy(typeParameters, 0, typeParameters = new char[ptr][][], 0, ptr);
				return typeParameters;
			}
			if (++ptr > typeParameters.length) {
				System.arraycopy(typeParameters, 0, typeParameters = new char[typeParameters.length+10][][], 0, ptr);
			}
			IType parentType = (IType) parent;
			if (parentType.isBinary()) {
				parameters = ((BinaryType) parent).getTypeParameters();
			} else {
				parameters = ((SourceType) parent).getTypeParameters();
			}
			int length = parameters==null ? 0 : parameters.length;
			if (length > 0) {
				hasParameters = true;
				typeParameters[ptr] = new char[length][];
				for (int i=0; i<length; i++)
					typeParameters[ptr][i] = Signature.createTypeSignature(parameters[i].getElementName(), false).toCharArray();
			}
			parent = parent.getParent();
		}
	}
	catch (JavaModelException jme) {
		return null;
	}
	return typeParameters;
}
}
