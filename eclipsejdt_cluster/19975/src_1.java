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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DoubleLiteral extends NumberLiteral {
	double value;
	public DoubleLiteral(char[] token, int s, int e) {
		super(token, s, e);
	}
	public void computeConstant() {
		//the source is correctly formated so the exception should never occurs
		Double computedValue;
		try {
			computedValue = Double.valueOf(String.valueOf(source));
		} catch (NumberFormatException e) {
			/*
			 * this can happen if this is an hexadecimal floating-point literal and the libraries used 
			 * are < 1.5
			 */
			computedValue = new Double(Util.getFloatingPoint(source));
		}

		final double doubleValue = computedValue.doubleValue();
		if (doubleValue > Double.MAX_VALUE)
			return; //may be Infinity
		if (doubleValue < Double.MIN_VALUE) { //only a true 0 can be made of zeros
			//2.00000000000000000e-324 is illegal .... 
			label : for (int i = 0; i < source.length; i++) { //it is welled formated so just test against '0' and potential . D d  
				switch (source[i]) {
					case '0' :
					case '.' :
					case 'd' :
					case 'D' :
					case 'x' :
					case 'X' :
						break;
					case 'e' :
					case 'E' :
					case 'p' :
					case 'P' :
						break label; //exposant are valid....!
					default :
						return;
				}
			}
		} //error

		constant = Constant.fromValue(value = doubleValue);
	}
	/**
	 * Code generation for the double literak
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		int pc = codeStream.position;
		if (valueRequired)
			if ((implicitConversion >> 4) == T_double)
				codeStream.generateInlinedValue(value);
			else
				codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	public TypeBinding literalType(BlockScope scope) {
		return DoubleBinding;
	}
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}
