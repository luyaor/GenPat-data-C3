/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
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
 *     IBM Corporation - initial API and implementation
 *     Jesper S Moller - Contributions for
 *							bug 382701 - [1.8][compiler] Implement semantic analysis of Lambda expressions & Reference expression
 *							Bug 405066 - [1.8][compiler][codegen] Implement code generation infrastructure for JSR335
 *     Stephan Herrmann - Contribution for
 *							Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *							Bug 423504 - [1.8] Implement "18.5.3 Functional Interface Parameterization Inference"
 *							Bug 425142 - [1.8][compiler] NPE in ConstraintTypeFormula.reduceSubType
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBindingVisitor;

public abstract class FunctionalExpression extends Expression {
	
	TypeBinding expectedType;
	public MethodBinding descriptor;
	public MethodBinding binding;                 // Code generation binding. May include synthetics. See getMethodBinding()
	protected MethodBinding actualMethodBinding;  // void of synthetics.
	boolean ignoreFurtherInvestigation;
	protected ExpressionContext expressionContext = VANILLA_CONTEXT;
	static Expression [] NO_EXPRESSIONS = new Expression[0];
	protected Expression [] resultExpressions = NO_EXPRESSIONS;
	protected CompilationResult compilationResult;
	public BlockScope enclosingScope;
	protected boolean ellipsisArgument;
	protected static IErrorHandlingPolicy silentErrorHandlingPolicy = DefaultErrorHandlingPolicies.ignoreAllProblems();

	public FunctionalExpression(CompilationResult compilationResult) {
		this.compilationResult = compilationResult;
	}
	
	public FunctionalExpression() {
		super();
	}
	
	public void setCompilationResult(CompilationResult compilationResult) {
		this.compilationResult = compilationResult;
	}
	
	// Return the actual (non-code generation) method binding that is void of synthetics.
	public MethodBinding getMethodBinding() {
		return null;
	}
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = this.ellipsisArgument ? ((ArrayBinding) expectedType).elementsType() : expectedType;
	}
	
	public void setExpressionContext(ExpressionContext context) {
		this.expressionContext = context;
	}
	public ExpressionContext getExpressionContext() {
		return this.expressionContext;
	}
	public void tagAsEllipsisArgument() {
		this.ellipsisArgument = true;
	}
	public boolean isPolyExpression(MethodBinding candidate) {
		return true;
	}
	public boolean isPolyExpression() {
		return true; // always as per introduction of part D, JSR 335
	}

	public TypeBinding invocationTargetType() {
		if (this.expectedType == null) return null;
		// when during inference this expression mimics as an invocationSite,
		// we simulate an *invocation* of this functional expression,
		// where the expected type of the expression is the return type of the sam:
		MethodBinding sam = this.expectedType.getSingleAbstractMethod(this.enclosingScope, true);
		if (sam != null) {
			if (sam.isConstructor())
				return sam.declaringClass;
			else
				return sam.returnType;
		}
		return null;
	}

	public TypeBinding expectedType() {
		return this.expectedType;
	}
	
	public boolean argumentsTypeElided() { return true; /* only exception: lambda with explicit argument types. */ }

	public TypeBinding resolveType(BlockScope blockScope) {
		this.constant = Constant.NotAConstant;
		MethodBinding sam = this.expectedType == null ? null : this.expectedType.getSingleAbstractMethod(blockScope, argumentsTypeElided());
		if (sam == null) {
			blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
			return null;
		}
		if (!sam.isValidBinding()) {
			switch (sam.problemId()) {
				case ProblemReasons.NoSuchSingleAbstractMethod:
					blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(this);
					break;
				case ProblemReasons.NotAWellFormedParameterizedType:
					blockScope.problemReporter().illFormedParameterizationOfFunctionalInterface(this);
					break;
				case ProblemReasons.IntersectionHasMultipleFunctionalInterfaces:
					blockScope.problemReporter().multipleFunctionalInterfaces(this);
					break;
			}
			return null;
		}
		
		this.descriptor = sam;
		if (kosherDescriptor(blockScope, sam, true)) {
			return this.resolvedType = this.expectedType;		
		}
		
		return this.resolvedType = null;
	}

	public TypeBinding checkAgainstFinalTargetType(TypeBinding targetType) {
		return resolveTypeExpecting(this.enclosingScope, targetType);
	}

	class VisibilityInspector extends TypeBindingVisitor {

		private Scope scope;
		private boolean shouldChatter;
        private boolean visible = true;
		private FunctionalExpression expression;
        
		public VisibilityInspector(FunctionalExpression expression, Scope scope, boolean shouldChatter) {
			this.scope = scope;
			this.shouldChatter = shouldChatter;
			this.expression = expression;
		}

		private void checkVisibility(ReferenceBinding referenceBinding) {
			if (!referenceBinding.canBeSeenBy(this.scope)) {
				this.visible = false;
				if (this.shouldChatter)
					this.scope.problemReporter().descriptorHasInvisibleType(this.expression, referenceBinding);
			}
		}
		
		public boolean visit(ReferenceBinding referenceBinding) {
			checkVisibility(referenceBinding);
			return true;
		}

		
		public boolean visit(ParameterizedTypeBinding parameterizedTypeBinding) {
			checkVisibility(parameterizedTypeBinding);
			return true;
		}
		
		public boolean visit(RawTypeBinding rawTypeBinding) {
			checkVisibility(rawTypeBinding);
			return true;
		}

		public boolean visible(TypeBinding type) {
			TypeBindingVisitor.visit(this, type);
			return this.visible;
		}

		public boolean visible(TypeBinding[] types) {
			TypeBindingVisitor.visit(this, types);
			return this.visible;
		}
		
	}

	public boolean kosherDescriptor(Scope scope, MethodBinding sam, boolean shouldChatter) {
	
		VisibilityInspector inspector = new VisibilityInspector(this, scope, shouldChatter);
		
		boolean status = true;
		
		if (!inspector.visible(sam.returnType))
			status = false;
		if (!inspector.visible(sam.parameters))
			status = false;
		if (!inspector.visible(sam.thrownExceptions))
			status = false;
		
		return status;
	}

	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.NON_NULL;
	}

	public int diagnosticsSourceEnd() {
		return this.sourceEnd;
	}
}
