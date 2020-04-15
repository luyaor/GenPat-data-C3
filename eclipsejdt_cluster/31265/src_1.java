/*******************************************************************************
 * Copyright (c) 2013 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * Implementation of 18.1.3 in JLS8
 */
public class TypeBound extends ReductionResult {
	
	InferenceVariable left;
	
	static TypeBound createBoundOrDependency(InferenceContext18 context, TypeBinding type, InferenceVariable variable) {
        // Part of JLS8 sect 18.1.3:
		return new TypeBound(variable, context.substitute(type), SUBTYPE);
	}

	/** Create a true type bound or a dependency. */
	TypeBound(InferenceVariable inferenceVariable, TypeBinding typeBinding, int relation) {
		this.left = inferenceVariable;
		this.right = typeBinding;
		this.relation = relation;
	}


	/** distinguish bounds from dependencies. */
	boolean isBound() {
		return this.right.isProperType(true);
	}
	public int hashCode() {
		return this.left.hashCode() + this.right.hashCode() + this.relation;
	}
	public boolean equals(Object obj) {
		if (obj instanceof TypeBound) {
			TypeBound other = (TypeBound) obj;
			return this.left == other.left && TypeBinding.equalsEquals(this.right, other.right) && this.relation == other.relation; //$IDENTITY-COMPARISON$ InferenceVariable
		}
		return false;
	}
	
	// debugging:
	public String toString() {
		boolean isBound = this.right.isProperType(true);
		StringBuffer buf = new StringBuffer();
		buf.append(isBound ? "TypeBound  " : "Dependency "); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(this.left.sourceName);
		buf.append(relationToString(this.relation));
		buf.append(this.right.readableName());
		return buf.toString();
	}
}
