/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
 *                          Bug 384687 - [1.8] Wildcard type arguments should be rejected for lambda and reference expressions
 *							Bug 416885 - [1.8][compiler]IncompatibleClassChange error (edit)
 *	   Stephan Herrmann - Contribution for
 *							bug 402028 - [1.8][compiler] null analysis for reference expressions 
 *							bug 404649 - [1.8][compiler] detect illegal reference to indirect or redundant super via I.super.m() syntax
 *							Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *							Bug 415850 - [1.8] Ensure RunJDTCoreTests can cope with null annotations enabled
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contribution for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class ReferenceExpression extends FunctionalExpression implements InvocationSite {
	
	public Expression lhs;
	public TypeReference [] typeArguments;
	public char [] selector;
	
	private TypeBinding receiverType;
	private boolean haveReceiver;
	public TypeBinding[] resolvedTypeArguments;
	private boolean typeArgumentsHaveErrors;
	
	MethodBinding syntheticAccessor;	// synthetic accessor for inner-emulation
	private int depth;
	private MethodBinding exactMethodBinding; // != null ==> exact method reference.
	
	public ReferenceExpression() {
		super();
	}
	
	public void initialize(CompilationResult result, Expression expression, TypeReference [] optionalTypeArguments, char [] identifierOrNew, int sourceEndPosition) {
		super.setCompilationResult(result);
		this.lhs = expression;
		this.typeArguments = optionalTypeArguments;
		this.selector = identifierOrNew;
		this.sourceStart = expression.sourceStart;
		this.sourceEnd = sourceEndPosition;
	}
 
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.actualMethodBinding = this.binding; // grab before synthetics come into play.
		SourceTypeBinding sourceType = currentScope.enclosingSourceType();
		if (this.receiverType.isArrayType()) {
			if (isConstructorReference()) {
				this.actualMethodBinding = this.binding = sourceType.addSyntheticArrayMethod((ArrayBinding) this.receiverType, SyntheticMethodBinding.ArrayConstructor);
			} else if (CharOperation.equals(this.selector, TypeConstants.CLONE)) {
				this.actualMethodBinding = this.binding = sourceType.addSyntheticArrayMethod((ArrayBinding) this.receiverType, SyntheticMethodBinding.ArrayClone);
			}
		} else if (this.syntheticAccessor != null) {
			if (this.lhs.isSuper() || isMethodReference())
				this.binding = this.syntheticAccessor;
		}
		
		int pc = codeStream.position;
		StringBuffer buffer = new StringBuffer();
		int argumentsSize = 0;
		buffer.append('(');
		if (this.haveReceiver) {
			this.lhs.generateCode(currentScope, codeStream, true);
			if (this.lhs.isSuper()) {
				if (this.lhs instanceof QualifiedSuperReference) {
					QualifiedSuperReference qualifiedSuperReference = (QualifiedSuperReference) this.lhs;
					TypeReference qualification = qualifiedSuperReference.qualification;
					if (qualification.resolvedType.isInterface()) {
						buffer.append(sourceType.signature());
					} else {
						buffer.append(((QualifiedSuperReference) this.lhs).currentCompatibleType.signature());
					}
				} else { 
					buffer.append(sourceType.signature());
				}
			} else {
				buffer.append(this.receiverType.signature());
			}
			argumentsSize = 1;
		} else {
			if (this.isConstructorReference()) {
				ReferenceBinding[] enclosingInstances = Binding.UNINITIALIZED_REFERENCE_TYPES;
				if (this.receiverType.isNestedType()) {
					NestedTypeBinding nestedType = null;
					if (this.receiverType instanceof ParameterizedTypeBinding) {
						nestedType = (NestedTypeBinding)((ParameterizedTypeBinding) this.receiverType).genericType();
					} else {
						nestedType = (NestedTypeBinding) this.receiverType;
					}
					if ((enclosingInstances = nestedType.syntheticEnclosingInstanceTypes()) != null) {
						int length = enclosingInstances.length;
						argumentsSize = length;
						for (int i = 0 ; i < length; i++) {
							ReferenceBinding syntheticArgumentType = enclosingInstances[i];
							buffer.append(syntheticArgumentType.signature());
							Object[] emulationPath = currentScope.getEmulationPath(
									syntheticArgumentType,
									false /* allow compatible match */,
									true /* disallow instance reference in explicit constructor call */);
							codeStream.generateOuterAccess(emulationPath, this, syntheticArgumentType, currentScope);
						}
					}
					// Reject types that capture outer local arguments, these cannot be manufactured by the metafactory.
					if (nestedType.syntheticOuterLocalVariables() != null) {
						currentScope.problemReporter().noSuchEnclosingInstance(nestedType.enclosingType, this, false);
						return;
					}
				}
				if (this.syntheticAccessor != null) {
					this.binding = sourceType.addSyntheticFactoryMethod(this.binding, this.syntheticAccessor, enclosingInstances);
				}
			}
		}
		buffer.append(')');
		buffer.append('L');
		buffer.append(this.resolvedType.constantPoolName());
		buffer.append(';');
		int invokeDynamicNumber = codeStream.classFile.recordBootstrapMethod(this);
		codeStream.invokeDynamic(invokeDynamicNumber, argumentsSize, 1, this.descriptor.selector, buffer.toString().toCharArray(), 
				this.isConstructorReference(), (this.lhs instanceof TypeReference? (TypeReference) this.lhs : null), this.typeArguments);
		if (!valueRequired)
			codeStream.pop();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {
		
		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE_OR_DEAD) != 0 || this.binding == null || !this.binding.isValidBinding()) 
			return;
		
		MethodBinding codegenBinding = this.binding.original();
		SourceTypeBinding enclosingSourceType = currentScope.enclosingSourceType();
		
		if (this.isConstructorReference()) {
			ReferenceBinding allocatedType = codegenBinding.declaringClass;
			if (codegenBinding.isPrivate() && TypeBinding.notEquals(enclosingSourceType, (allocatedType = codegenBinding.declaringClass))) {
				if ((allocatedType.tagBits & TagBits.IsLocalType) != 0) {
					codegenBinding.tagBits |= TagBits.ClearPrivateModifier;
				} else {
					this.syntheticAccessor = ((SourceTypeBinding) allocatedType).addSyntheticMethod(codegenBinding, false);
					currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
				}
			}
			return;
		}
	
		// -----------------------------------   Only method references from now on -----------
		if (this.binding.isPrivate()) {
			if (TypeBinding.notEquals(enclosingSourceType, codegenBinding.declaringClass)){
				this.syntheticAccessor = ((SourceTypeBinding)codegenBinding.declaringClass).addSyntheticMethod(codegenBinding, false /* not super access */);
				currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
			}
			return;
		}
		
		if (this.lhs.isSuper()) {
			SourceTypeBinding destinationType = enclosingSourceType;
			if (this.lhs instanceof QualifiedSuperReference) { 	// qualified super
				QualifiedSuperReference qualifiedSuperReference = (QualifiedSuperReference) this.lhs;
				TypeReference qualification = qualifiedSuperReference.qualification;
				if (!qualification.resolvedType.isInterface()) // we can't drop the bridge in I, it may not even be a source type.
					destinationType = (SourceTypeBinding) (qualifiedSuperReference.currentCompatibleType);
			}
			
			this.syntheticAccessor = destinationType.addSyntheticMethod(codegenBinding, true);
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
			return;
		}
		
		if (this.binding.isProtected() && (this.bits & ASTNode.DepthMASK) != 0 && codegenBinding.declaringClass.getPackage() != enclosingSourceType.getPackage()) {
			SourceTypeBinding currentCompatibleType = (SourceTypeBinding) enclosingSourceType.enclosingTypeAt((this.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
			this.syntheticAccessor = currentCompatibleType.addSyntheticMethod(codegenBinding, isSuperAccess());
			currentScope.problemReporter().needToEmulateMethodAccess(codegenBinding, this);
			return;
		}
	}
	
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		// static methods with receiver value never get here
		if (this.haveReceiver) {
			this.lhs.checkNPE(currentScope, flowContext, flowInfo);
			this.lhs.analyseCode(currentScope, flowContext, flowInfo, true);
		}
		manageSyntheticAccessIfNecessary(currentScope, flowInfo);
		return flowInfo;
	}

	public TypeBinding resolveType(BlockScope scope) {
		
		final CompilerOptions compilerOptions = scope.compilerOptions();
		TypeBinding lhsType;
    	if (this.constant != Constant.NotAConstant) {
    		this.constant = Constant.NotAConstant;
    		this.enclosingScope = scope;
    		if (isConstructorReference())
    			this.lhs.bits |= ASTNode.IgnoreRawTypeCheck; // raw types in constructor references are to be treated as though <> were specified.

    		lhsType = this.lhs.resolveType(scope);
    		if (this.typeArguments != null) {
    			int length = this.typeArguments.length;
    			this.typeArgumentsHaveErrors = compilerOptions.sourceLevel < ClassFileConstants.JDK1_5;
    			this.resolvedTypeArguments = new TypeBinding[length];
    			for (int i = 0; i < length; i++) {
    				TypeReference typeReference = this.typeArguments[i];
    				if ((this.resolvedTypeArguments[i] = typeReference.resolveType(scope, true /* check bounds*/)) == null) {
    					this.typeArgumentsHaveErrors = true;
    				}
    				if (this.typeArgumentsHaveErrors && typeReference instanceof Wildcard) { // resolveType on wildcard always return null above, resolveTypeArgument is the real workhorse.
    					scope.problemReporter().illegalUsageOfWildcard(typeReference);
    				}
    			}
    			if (this.typeArgumentsHaveErrors)
    				return this.resolvedType = null;
    		}
    	} else {
    		if (this.typeArgumentsHaveErrors)
				return this.resolvedType = null;
    		lhsType = this.lhs.resolvedType;
    	}

    	if (this.expectedType == null && this.expressionContext == INVOCATION_CONTEXT) {
    		this.exactMethodBinding = isMethodReference() ? scope.getExactMethod(lhsType, this.selector, this) : scope.getExactConstructor(lhsType, this);
    		return this.resolvedType = new PolyTypeBinding(this);
		}
		super.resolveType(scope);
		
    	if (lhsType == null) 
			return this.resolvedType = null; 	// no hope
		if (lhsType.problemId() == ProblemReasons.AttemptToBypassDirectSuper)
			lhsType = lhsType.closestMatch();	// improve resolving experience
    	if (!lhsType.isValidBinding()) 
			return this.resolvedType = null;	// nope, no useful type found
		
		final TypeBinding[] descriptorParameters = this.descriptor != null ? this.descriptor.parameters : Binding.NO_PARAMETERS;
		if (lhsType.isBaseType()) {
			scope.problemReporter().errorNoMethodFor(this.lhs, lhsType, this.selector, descriptorParameters);
			return this.resolvedType = null;
		}
		
		if (isConstructorReference() && !lhsType.canBeInstantiated()) {
			scope.problemReporter().cannotInstantiate(this.lhs, lhsType);
			return this.resolvedType = null;
		}
		
		/* 15.28: "It is a compile-time error if a method reference of the form super :: NonWildTypeArgumentsopt Identifier or of the form 
		   TypeName . super :: NonWildTypeArgumentsopt Identifier occurs in a static context.": This is nop since the primary when it resolves
		   itself will complain automatically.
		
		   15.28: "The immediately enclosing instance of an inner class instance (15.9.2) must be provided for a constructor reference by a lexically 
		   enclosing instance of this (8.1.3)", we will actually implement this check in code generation. Emulation path computation will fail if there
		   is no suitable enclosing instance. While this could be pulled up to here, leaving it to code generation is more consistent with Java 5,6,7 
		   modus operandi.
		*/
		
		// handle the special case of array construction first.
        this.receiverType = lhsType;
		final int parametersLength = descriptorParameters.length;
        if (isConstructorReference() && lhsType.isArrayType()) {
        	final TypeBinding leafComponentType = lhsType.leafComponentType();
			if (!leafComponentType.isReifiable()) {
        		scope.problemReporter().illegalGenericArray(leafComponentType, this);
        		return this.resolvedType = null;
        	}
        	if (parametersLength != 1 || scope.parameterCompatibilityLevel(descriptorParameters[0], TypeBinding.INT) == Scope.NOT_COMPATIBLE) {
        		scope.problemReporter().invalidArrayConstructorReference(this, lhsType, descriptorParameters);
        		return this.resolvedType = null;
        	}
        	if (!lhsType.isCompatibleWith(this.descriptor.returnType) && this.descriptor.returnType.id != TypeIds.T_void) {
        		scope.problemReporter().constructedArrayIncompatible(this, lhsType, this.descriptor.returnType);
        		return this.resolvedType = null;
        	}
        	this.binding = this.exactMethodBinding = scope.getExactConstructor(lhsType, this);
        	return this.resolvedType;
        }
		
		this.haveReceiver = true;
		if (this.lhs instanceof NameReference) {
			if ((this.lhs.bits & ASTNode.RestrictiveFlagMASK) == Binding.TYPE) {
				this.haveReceiver = false;
			}
		} else if (this.lhs instanceof TypeReference) {
			this.haveReceiver = false;
		}

		/* For Reference expressions unlike other call sites, we always have a receiver _type_ since LHS of :: cannot be empty. 
		   LHS's resolved type == actual receiver type. All code below only when a valid descriptor is available.
		 */
        if (this.descriptor == null || !this.descriptor.isValidBinding())
        	return this.resolvedType =  null;
        
        // 15.28.1
        final boolean isMethodReference = isMethodReference();
        this.depth = 0;
        MethodBinding someMethod = isMethodReference ? scope.getMethod(this.receiverType, this.selector, descriptorParameters, this) :
        											       scope.getConstructor((ReferenceBinding) this.receiverType, descriptorParameters, this);
        int someMethodDepth = this.depth, anotherMethodDepth = 0;
    	if (someMethod != null && someMethod.isValidBinding()) {
        	final boolean isStatic = someMethod.isStatic();
        	if (isStatic && (this.haveReceiver || this.receiverType.isParameterizedType())) {
    			scope.problemReporter().methodMustBeAccessedStatically(this, someMethod);
    			return this.resolvedType = null;
    		}
        	if (!this.haveReceiver) {
        		if (!isStatic && !someMethod.isConstructor()) {
        			scope.problemReporter().methodMustBeAccessedWithInstance(this, someMethod);
        			return this.resolvedType = null;
        		}
        	} 
        } else {
        	if (this.lhs instanceof NameReference && !this.haveReceiver && isMethodReference() && this.receiverType.isRawType()) {
        		if ((this.lhs.bits & ASTNode.IgnoreRawTypeCheck) == 0 && compilerOptions.getSeverity(CompilerOptions.RawTypeReference) != ProblemSeverities.Ignore) {
        			scope.problemReporter().rawTypeReference(this.lhs, this.receiverType);
        		}
        	}
        }
    	if (this.lhs.isSuper() && this.lhs.resolvedType.isInterface()) {
    		scope.checkAppropriateMethodAgainstSupers(this.selector, someMethod, this.descriptor.parameters, this);
    	}

        MethodBinding anotherMethod = null;
        int paramOffset = 0;
        if (!this.haveReceiver && isMethodReference && parametersLength > 0) {
        	final TypeBinding potentialReceiver = descriptorParameters[0];
        	if (potentialReceiver.isCompatibleWith(this.receiverType, scope)) {
        		TypeBinding typeToSearch = this.receiverType;
        		if (this.receiverType.isRawType()) {
        			TypeBinding superType = potentialReceiver.findSuperTypeOriginatingFrom(this.receiverType);
        			if (superType != null)
        				typeToSearch = superType;
        		}
        		TypeBinding [] parameters = Binding.NO_PARAMETERS;
        		if (parametersLength > 1) {
        			parameters = new TypeBinding[parametersLength - 1];
        			System.arraycopy(descriptorParameters, 1, parameters, 0, parametersLength - 1);
        		}
        		this.depth = 0;
        		anotherMethod = scope.getMethod(typeToSearch, this.selector, parameters, this);
        		anotherMethodDepth = this.depth;
        		this.depth = 0;
        		paramOffset = 1; // 0 is receiver, real parameters start at 1
        	}
        	if (anotherMethod != null && anotherMethod.isValidBinding() && anotherMethod.isStatic()) {
        		scope.problemReporter().methodMustBeAccessedStatically(this, anotherMethod);
        		return this.resolvedType = null;
        	}
        }
        
        if (someMethod != null && someMethod.isValidBinding() && anotherMethod != null && anotherMethod.isValidBinding()) {
        	scope.problemReporter().methodReferenceSwingsBothWays(this, anotherMethod, someMethod);
        	return this.resolvedType = null;
        }
        
        if (someMethod != null && someMethod.isValidBinding()) {
        	this.binding = someMethod;
        	this.bits &= ~ASTNode.DepthMASK;
        	if (someMethodDepth > 0) {
        		this.bits |= (someMethodDepth & 0xFF) << ASTNode.DepthSHIFT;
        	}
        } else if (anotherMethod != null && anotherMethod.isValidBinding()) {
        	this.binding = anotherMethod;
        	this.bits &= ~ASTNode.DepthMASK;
        	if (anotherMethodDepth > 0) {
        		this.bits |= (anotherMethodDepth & 0xFF) << ASTNode.DepthSHIFT;
        	}
        } else {
        	this.binding = null;
        	this.bits &= ~ASTNode.DepthMASK;
        }

        if (this.binding == null) {
        	char [] visibleName = isConstructorReference() ? this.receiverType.sourceName() : this.selector;
        	scope.problemReporter().danglingReference(this, this.receiverType, visibleName, descriptorParameters);
			return this.resolvedType = null;
        }
        
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382350#c2, I.super::abstractMethod will be handled there.

        if (this.binding.isAbstract() && this.lhs.isSuper())
        	scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, this.binding);
        
        if (this.binding.isStatic()) {
        	if (TypeBinding.notEquals(this.binding.declaringClass, this.receiverType))
        		scope.problemReporter().indirectAccessToStaticMethod(this, this.binding);
        } else {
        	AbstractMethodDeclaration srcMethod = this.binding.sourceMethod();
        	if (srcMethod != null && srcMethod.isMethod())
        		srcMethod.bits &= ~ASTNode.CanBeStatic;
        }
        
    	if (isMethodUseDeprecated(this.binding, scope, true))
    		scope.problemReporter().deprecatedMethod(this.binding, this);

    	if (this.typeArguments != null && this.binding.original().typeVariables == Binding.NO_TYPE_VARIABLES)
    		scope.problemReporter().unnecessaryTypeArgumentsForMethodInvocation(this.binding, this.resolvedTypeArguments, this.typeArguments);
    	
    	if ((this.binding.tagBits & TagBits.HasMissingType) != 0)
    		scope.problemReporter().missingTypeInMethod(this, this.binding);
    	

        // OK, we have a compile time declaration, see if it passes muster.
        TypeBinding [] methodExceptions = this.binding.thrownExceptions;
        TypeBinding [] kosherExceptions = this.descriptor.thrownExceptions;
        next: for (int i = 0, iMax = methodExceptions.length; i < iMax; i++) {
        	if (methodExceptions[i].isUncheckedException(true)) {
        		continue next;
    		}
        	for (int j = 0, jMax = kosherExceptions.length; j < jMax; j++) {
        		if (methodExceptions[i].isCompatibleWith(kosherExceptions[j], scope))
        			continue next;
        	}
        	scope.problemReporter().unhandledException(methodExceptions[i], this);
        }
        if (scope.compilerOptions().isAnnotationBasedNullAnalysisEnabled) {
        	int len = this.binding.parameters.length;
    		for (int i = 0; i < len; i++) {
    			long declared = this.descriptor.parameters[i+paramOffset].tagBits & TagBits.AnnotationNullMASK;
    			long implemented = this.binding.parameters[i].tagBits & TagBits.AnnotationNullMASK;
    			if (declared == TagBits.AnnotationNullable) { // promise to accept null
    				if (implemented != TagBits.AnnotationNullable) {
    					char[][] requiredAnnot = implemented == 0L ? null : scope.environment().getNonNullAnnotationName();
    					scope.problemReporter().parameterLackingNullableAnnotation(this, this.descriptor, i, paramOffset, 
    							scope.environment().getNullableAnnotationName(),
    							requiredAnnot, this.binding.parameters[i]);
    				}
    			} else if (declared == 0L) {
    				if (implemented == TagBits.AnnotationNonNull) {
    					scope.problemReporter().parameterRequiresNonnull(this, this.descriptor, i+paramOffset,
    							scope.environment().getNonNullAnnotationName(), this.binding.parameters[i]);
    				}
    			}
    		}
        	if ((this.descriptor.returnType.tagBits & TagBits.AnnotationNonNull) != 0) {
        		if ((this.binding.returnType.tagBits & TagBits.AnnotationNonNull) == 0) {
        			char[][] providedAnnotationName = ((this.binding.returnType.tagBits & TagBits.AnnotationNullable) != 0) ?
        					scope.environment().getNullableAnnotationName() : null;
        			scope.problemReporter().illegalReturnRedefinition(this, this.descriptor,
        					scope.environment().getNonNullAnnotationName(),
        					providedAnnotationName, this.binding.returnType);
        		}
        	}
        }
        
    	if (checkInvocationArguments(scope, null, this.receiverType, this.binding, null, descriptorParameters, false, this))
    		this.bits |= ASTNode.Unchecked;

    	if (this.descriptor.returnType.id != TypeIds.T_void) {
    		// from 1.5 source level on, array#clone() returns the array type (but binding still shows Object)
    		TypeBinding returnType = null;
    		if (this.binding == scope.environment().arrayClone || this.binding.isConstructor()) {
    			returnType = this.receiverType;
    		} else {
    			if ((this.bits & ASTNode.Unchecked) != 0 && this.resolvedTypeArguments == null) {
    				returnType = this.binding.returnType;
    				if (returnType != null) {
    					returnType = scope.environment().convertToRawType(returnType.erasure(), true);
    				}
    			} else {
    				returnType = this.binding.returnType;
    				if (returnType != null) {
    					returnType = returnType.capture(scope, this.sourceEnd);
    				}
    			}
    		}
    		if (!returnType.isCompatibleWith(this.descriptor.returnType, scope) && !isBoxingCompatible(returnType, this.descriptor.returnType, this, scope)) {
    			scope.problemReporter().incompatibleReturnType(this, this.binding, this.descriptor.returnType);
    			this.binding = null;
    			this.resolvedType = null;
    		}
    	}

    	return this.resolvedType; // Phew !
	}

	public boolean isConstructorReference() {
		return CharOperation.equals(this.selector,  ConstantPool.Init);
	}
	
	public boolean isExactMethodReference() {
		return this.exactMethodBinding != null;
	}
	
	public boolean isMethodReference() {
		return !CharOperation.equals(this.selector,  ConstantPool.Init);
	}
	
	public TypeBinding[] genericTypeArguments() {
		return this.resolvedTypeArguments;
	}

	public boolean isSuperAccess() {
		return false;
	}

	public boolean isTypeAccess() {
		return false;
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		return;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setFieldIndex(int depth) {
		return;
	}

	public StringBuffer printExpression(int tab, StringBuffer output) {
		
		this.lhs.print(0, output);
		output.append("::"); //$NON-NLS-1$
		if (this.typeArguments != null) {
			output.append('<');
			int max = this.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				this.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			this.typeArguments[max].print(0, output);
			output.append('>');
		}
		if (isConstructorReference())
			output.append("new"); //$NON-NLS-1$
		else 
			output.append(this.selector);
		
		return output;
	}
		
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			
			this.lhs.traverse(visitor, blockScope);
			
			int length = this.typeArguments == null ? 0 : this.typeArguments.length;
			for (int i = 0; i < length; i++) {
				this.typeArguments[i].traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}

	public boolean isCompatibleWith(TypeBinding left, Scope scope) {
		// 15.28.2
		final MethodBinding sam = left.getSingleAbstractMethod(this.enclosingScope);
		if (sam == null || !sam.isValidBinding())
			return false;
		boolean isCompatible;
		setExpectedType(left);
		IErrorHandlingPolicy oldPolicy = this.enclosingScope.problemReporter().switchErrorHandlingPolicy(silentErrorHandlingPolicy);
		try {
			this.binding = null;
			resolveType(this.enclosingScope);
		} finally {
			this.enclosingScope.problemReporter().switchErrorHandlingPolicy(oldPolicy);
			isCompatible = this.binding != null && this.binding.isValidBinding();
			this.binding = null;
			setExpectedType(null);
		}
		return isCompatible;
	}
	
	public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t) {
		
		if (TypeBinding.equalsEquals(s, t))
			return true;
		
		if (this.exactMethodBinding == null)
			return false;
		
		s = s.capture(this.enclosingScope, this.sourceEnd);
		MethodBinding sSam = s.getSingleAbstractMethod(this.enclosingScope);
		if (sSam == null || !sSam.isValidBinding())
			return false;
		TypeBinding r1 = sSam.returnType;
		
		MethodBinding tSam = t.getSingleAbstractMethod(this.enclosingScope);
		if (tSam == null || !tSam.isValidBinding())
			return false;
		TypeBinding r2 = tSam.returnType;
		
		if (r2.id == TypeIds.T_void)
			return true;
		
		if (r1.id == TypeIds.T_void)
			return false;
		
		// r1 <: r2
		if (r1.isCompatibleWith(r2))
			return true;
		
		return r1.isBaseType() != r2.isBaseType() && r1.isBaseType() == this.exactMethodBinding.returnType.isBaseType();
	}

	public org.eclipse.jdt.internal.compiler.lookup.MethodBinding getMethodBinding() {
		if (this.actualMethodBinding == null)  // array new/clone, no real binding.
			this.actualMethodBinding = this.binding;
		return this.actualMethodBinding;
	}
}
