/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class StackMapFrameCodeStream extends CodeStream {
	public StackMapFrame currentFrame;
	public ArrayList frames;

	public Set framePositions;
	
	public ArrayList variablesModificationsPositions;
	
public StackMapFrameCodeStream(ClassFile givenClassFile) {
	super(givenClassFile);
}
public void aaload() {
	super.aaload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void aastore() {
	super.aastore();
	this.currentFrame.numberOfStackItems-=3;	
}
public void aconst_null() {
	super.aconst_null();
	this.currentFrame.addStackItem(TypeBinding.NULL);
}
public void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
	for (int i = 0; i < visibleLocalsCount; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null) {
			// Check if the local is definitely assigned
			if (isDefinitelyAssigned(scope, initStateIndex, localBinding)) {
				if ((localBinding.initializationCount == 0) || (localBinding.initializationPCs[((localBinding.initializationCount - 1) << 1) + 1] != -1)) {
					/* There are two cases:
					 * 1) there is no initialization interval opened ==> add an opened interval
					 * 2) there is already some initialization intervals but the last one is closed ==> add an opened interval
					 * An opened interval means that the value at localBinding.initializationPCs[localBinding.initializationCount - 1][1]
					 * is equals to -1.
					 * initializationPCs is a collection of pairs of int:
					 * 	first value is the startPC and second value is the endPC. -1 one for the last value means that the interval
					 * 	is not closed yet.
					 */
					currentFrame.putLocal(localBinding.resolvedPosition, new VerificationTypeInfo(localBinding.type));
				}
			}
		}
	}
	Integer newValue = new Integer(this.position);
	if (this.variablesModificationsPositions.size() == 0 || !this.variablesModificationsPositions.get(this.variablesModificationsPositions.size() - 1).equals(newValue)) {
		this.variablesModificationsPositions.add(newValue);
	}
	storeStackMapFrame();
	super.addDefinitelyAssignedVariables(scope, initStateIndex);
}
public void addVariable(LocalVariableBinding localBinding) {
	currentFrame.putLocal(localBinding.resolvedPosition, new VerificationTypeInfo(localBinding.type));	
	storeStackMapFrame();
	super.addVariable(localBinding);
}
public void aload(int iArg) {
	super.aload(iArg);
	this.currentFrame.addStackItem(getLocal(iArg, this.currentFrame));
}
public void aload_0() {
	super.aload_0();
	this.currentFrame.addStackItem(getLocal(0, this.currentFrame));
}
public void aload_1() {
	super.aload_1();
	this.currentFrame.addStackItem(getLocal(1, this.currentFrame));
}

public void aload_2() {
	super.aload_2();
	this.currentFrame.addStackItem(getLocal(2, this.currentFrame));
}
public void aload_3() {
	super.aload_3();
	this.currentFrame.addStackItem(getLocal(3, this.currentFrame));
}
public void anewarray(TypeBinding typeBinding) {
	super.anewarray(typeBinding);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(typeBinding);
}
public void areturn() {
	super.areturn();
	this.currentFrame.numberOfStackItems--;
	framePositions.add(new Integer(this.position));
}
public void arraylength() {
	super.arraylength();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
public void astore(int iArg) {
	super.astore(iArg);
	this.currentFrame.putLocal(iArg, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void astore_0() {
	super.astore_0();
	this.currentFrame.putLocal(0, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void astore_1() {
	super.astore_1();
	this.currentFrame.putLocal(1, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void astore_2() {
	super.astore_2();
	this.currentFrame.putLocal(2, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void astore_3() {
	super.astore_3();
	this.currentFrame.putLocal(3, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void athrow() {
	super.athrow();
	this.currentFrame.numberOfStackItems--;
	framePositions.add(new Integer(this.position));
}
public void baload() {
	super.baload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void bastore() {
	super.bastore();
	this.currentFrame.numberOfStackItems-=3;	
}
public void bipush(byte b) {
	super.bipush(b);
	this.currentFrame.addStackItem(TypeBinding.INT);	
}
public void caload() {
	super.caload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void castore() {
	super.castore();
	this.currentFrame.numberOfStackItems-=3;	
}
public void checkcast(int baseId) {
	super.checkcast(baseId);
	VerificationTypeInfo info = null;
	switch (baseId) {
		case TypeIds.T_byte :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangByte, ConstantPool.JavaLangByteConstantPoolName);
			break;
		case TypeIds.T_short :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangShort, ConstantPool.JavaLangShortConstantPoolName);
			break;
		case TypeIds.T_char :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangCharacter, ConstantPool.JavaLangCharacterConstantPoolName);
			break;
		case TypeIds.T_int :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangInteger, ConstantPool.JavaLangIntegerConstantPoolName);
			break;
		case TypeIds.T_long :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangLong, ConstantPool.JavaLangLongConstantPoolName);
			break;
		case TypeIds.T_float :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangFloat, ConstantPool.JavaLangFloatConstantPoolName);
			break;
		case TypeIds.T_double :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangDouble, ConstantPool.JavaLangDoubleConstantPoolName);
			break;
		case TypeIds.T_boolean :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangBoolean, ConstantPool.JavaLangBooleanConstantPoolName);
	}
	if (info != null) {
		this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info;
	}
}
public void checkcast(TypeBinding typeBinding) {
	super.checkcast(typeBinding);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(typeBinding);
}
public void d2f() {
	super.d2f();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.FLOAT);
}
public void d2i() {
	super.d2i();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
public void d2l() {
	super.d2l();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.LONG);
}
public void dadd() {
	super.dadd();
	this.currentFrame.numberOfStackItems--;
}
public void daload() {
	super.daload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void dastore() {
	super.dastore();
	this.currentFrame.numberOfStackItems-=3;	
}
public void dcmpg() {
	super.dcmpg();
	this.currentFrame.numberOfStackItems-=2;
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void dcmpl() {
	super.dcmpl();
	this.currentFrame.numberOfStackItems-=2;
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void dconst_0() {
	super.dconst_0();
	this.currentFrame.addStackItem(TypeBinding.DOUBLE);	
}
public void dconst_1() {
	super.dconst_1();
	this.currentFrame.addStackItem(TypeBinding.DOUBLE);	
}
public void ddiv() {
	super.ddiv();
	this.currentFrame.numberOfStackItems--;	
}
public void decrStackSize(int offset) {
	super.decrStackSize(offset);
	this.currentFrame.numberOfStackItems --;
}
public void dload(int iArg) {
	super.dload(iArg);
	this.currentFrame.addStackItem(getLocal(iArg, this.currentFrame));	
}
public void dload_0() {
	super.dload_0();
	this.currentFrame.addStackItem(getLocal(0, this.currentFrame));	
}
public void dload_1() {
	super.dload_1();
	this.currentFrame.addStackItem(getLocal(1, this.currentFrame));
}
public void dload_2() {
	super.dload_2();
	this.currentFrame.addStackItem(getLocal(2, this.currentFrame));
}
public void dload_3() {
	super.dload_3();
	this.currentFrame.addStackItem(getLocal(3, this.currentFrame));
}
public void dmul() {
	super.dmul();
	this.currentFrame.numberOfStackItems--;	
}
public void drem() {
	super.drem();
	this.currentFrame.numberOfStackItems--;	
}
public void dreturn() {
	super.dreturn();
	this.currentFrame.numberOfStackItems--;	
	this.framePositions.add(new Integer(this.position));
}
public void dstore(int iArg) {
	super.dstore(iArg);
	this.currentFrame.putLocal(iArg, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void dstore_0() {
	super.dstore_0();
	this.currentFrame.putLocal(0, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void dstore_1() {
	super.dstore_1();
	this.currentFrame.putLocal(1, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void dstore_2() {
	super.dstore_2();
	this.currentFrame.putLocal(2, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void dstore_3() {
	super.dstore_3();
	this.currentFrame.putLocal(3, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void dsub() {
	super.dsub();
	this.currentFrame.numberOfStackItems--;
}
public void dup() {
	super.dup();
	this.currentFrame.addStackItem(this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
}
public void dup_x1() {
	super.dup_x1();
	VerificationTypeInfo info = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	VerificationTypeInfo info2 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.addStackItem(info);
	this.currentFrame.addStackItem(info2);
	this.currentFrame.addStackItem(info);
}
public void dup_x2() {
	super.dup_x2();
	VerificationTypeInfo info = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	VerificationTypeInfo info2 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	switch(info2.id()) {
		case TypeIds.T_long :
		case TypeIds.T_double :
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
			break;
		default:
			VerificationTypeInfo info3 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
			this.currentFrame.numberOfStackItems--;
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info3);
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
	}
}
public void dup2() {
	super.dup2();
	VerificationTypeInfo info = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	switch(info.id()) {
		case TypeIds.T_double :
		case TypeIds.T_long :
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info);
			break;
		default:
			VerificationTypeInfo info2 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
			this.currentFrame.numberOfStackItems--;
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
	}
}
public void dup2_x1() {
	super.dup2_x1();
	VerificationTypeInfo info = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	VerificationTypeInfo info2 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	switch(info.id()) {
		case TypeIds.T_double :
		case TypeIds.T_long :
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
			break;
		default:
			VerificationTypeInfo info3 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
			this.currentFrame.numberOfStackItems--;
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
			this.currentFrame.addStackItem(info3);
			this.currentFrame.addStackItem(info2);
			this.currentFrame.addStackItem(info);
	}
}
public void dup2_x2() {
	super.dup2_x2();
	VerificationTypeInfo info = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	VerificationTypeInfo info2 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
	this.currentFrame.numberOfStackItems--;
	switch(info.id()) {
		case TypeIds.T_long :
		case TypeIds.T_double :
			switch(info2.id()) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					// form 4
					this.currentFrame.addStackItem(info);
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
					break;
				default:
					// form 2
					VerificationTypeInfo info3 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
					this.currentFrame.numberOfStackItems--;
					this.currentFrame.addStackItem(info);
					this.currentFrame.addStackItem(info3);
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
			}
			break;
		default:
			VerificationTypeInfo info3 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
			this.currentFrame.numberOfStackItems--;
			switch(info3.id()) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					// form 3
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
					this.currentFrame.addStackItem(info3);
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
					break;
				default:
					// form 1
					VerificationTypeInfo info4 = this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1];
					this.currentFrame.numberOfStackItems--;
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
					this.currentFrame.addStackItem(info4);
					this.currentFrame.addStackItem(info3);
					this.currentFrame.addStackItem(info2);
					this.currentFrame.addStackItem(info);
			}
	}
}
public void exitUserScope(BlockScope currentScope) {
	int index = this.visibleLocalsCount - 1;
	while (index >= 0) {
		LocalVariableBinding visibleLocal = visibleLocals[index];
		if (visibleLocal == null) {
			index--;
			continue;
		}
		if (visibleLocal.declaringScope != currentScope) // left currentScope
			break;

		// there may be some preserved locals never initialized
		if (visibleLocal.initializationCount > 0){
			this.currentFrame.removeLocals(visibleLocal.resolvedPosition);
		}
		index--;
	}
	if (currentScope != null) {
		int localIndex = currentScope.localIndex;
		if (localIndex != 0) {
			for (int i = 0; i < localIndex; i++) {
				LocalVariableBinding variableBinding = currentScope.locals[i];
				if (variableBinding != null && variableBinding.useFlag == LocalVariableBinding.USED && variableBinding.resolvedPosition != -1) {
					this.currentFrame.removeLocals(variableBinding.resolvedPosition);
				}
			}
		}
	}
	super.exitUserScope(currentScope);
}
public void exitUserScope(BlockScope currentScope, LocalVariableBinding binding) {
	int index = this.visibleLocalsCount - 1;
	while (index >= 0) {
		LocalVariableBinding visibleLocal = visibleLocals[index];
		if (visibleLocal == null || visibleLocal == binding) {
			index--;
			continue;
		}
		if (visibleLocal.declaringScope != currentScope) // left currentScope
			break;

		// there may be some preserved locals never initialized
		if (visibleLocal.initializationCount > 0){
			this.currentFrame.removeLocals(visibleLocal.resolvedPosition);
		}
		index--;
	}
	if (currentScope != null) {
		int localIndex = currentScope.localIndex;
		if (localIndex != 0) {
			for (int i = 0; i < localIndex; i++) {
				LocalVariableBinding variableBinding = currentScope.locals[i];
				if (variableBinding != null && variableBinding != binding && variableBinding.useFlag == LocalVariableBinding.USED && variableBinding.resolvedPosition != -1) {
					this.currentFrame.removeLocals(variableBinding.resolvedPosition);
				}
			}
		}
	}
	this.storeStackMapFrame();	
	super.exitUserScope(currentScope, binding);
}
public void f2d() {
	super.f2d();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.DOUBLE);
}
public void f2i() {
	super.f2i();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
public void f2l() {
	super.f2l();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.LONG);
}
public void fadd() {
	super.fadd();
	this.currentFrame.numberOfStackItems--;
}
public void faload() {
	super.faload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void fastore() {
	super.fastore();
	this.currentFrame.numberOfStackItems-=3;	
}
public void fcmpg() {
	super.fcmpg();
	this.currentFrame.numberOfStackItems-=2;
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void fcmpl() {
	super.fcmpl();
	this.currentFrame.numberOfStackItems-=2;
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void fconst_0() {
	super.fconst_0();
	this.currentFrame.addStackItem(TypeBinding.FLOAT);	
}
public void fconst_1() {
	super.fconst_1();
	this.currentFrame.addStackItem(TypeBinding.FLOAT);	
}
public void fconst_2() {
	super.fconst_2();
	this.currentFrame.addStackItem(TypeBinding.FLOAT);	
}
public void fdiv() {
	super.fdiv();
	this.currentFrame.numberOfStackItems--;	
}
public void fload(int iArg) {
	super.fload(iArg);
	this.currentFrame.addStackItem(getLocal(iArg, this.currentFrame));
}
public void fload_0() {
	super.fload_0();
	this.currentFrame.addStackItem(getLocal(0, this.currentFrame));
}
public void fload_1() {
	super.fload_1();
	this.currentFrame.addStackItem(getLocal(1, this.currentFrame));
}
public void fload_2() {
	super.fload_2();
	this.currentFrame.addStackItem(getLocal(2, this.currentFrame));
}
public void fload_3() {
	super.fload_3();
	this.currentFrame.addStackItem(getLocal(3, this.currentFrame));
}
public void fmul() {
	super.fmul();
	this.currentFrame.numberOfStackItems--;	
}
public void frem() {
	super.frem();
	this.currentFrame.numberOfStackItems--;	
}
public void freturn() {
	super.freturn();
	this.currentFrame.numberOfStackItems--;
	this.framePositions.add(new Integer(this.position));
}
public void fstore(int iArg) {
	super.fstore(iArg);
	this.currentFrame.putLocal(iArg, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void fstore_0() {
	super.fstore_0();
	this.currentFrame.putLocal(0, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void fstore_1() {
	super.fstore_1();
	this.currentFrame.putLocal(1, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void fstore_2() {
	super.fstore_2();
	this.currentFrame.putLocal(2, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void fstore_3() {
	super.fstore_3();
	this.currentFrame.putLocal(3, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void fsub() {
	super.fsub();
	this.currentFrame.numberOfStackItems--;	
}
public void generateBoxingConversion(int unboxedTypeID) {
	super.generateBoxingConversion(unboxedTypeID);
	VerificationTypeInfo info = null;
    switch (unboxedTypeID) {
        case TypeIds.T_byte :
        	info = new VerificationTypeInfo(TypeIds.T_JavaLangByte, ConstantPool.JavaLangByteConstantPoolName);
            break;
        case TypeIds.T_short :
        	info = new VerificationTypeInfo(TypeIds.T_JavaLangShort, ConstantPool.JavaLangShortConstantPoolName);
            break;
        case TypeIds.T_char :
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangCharacter, ConstantPool.JavaLangCharacterConstantPoolName);
           break;
        case TypeIds.T_int :             
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangInteger, ConstantPool.JavaLangIntegerConstantPoolName);
            break;
        case TypeIds.T_long :
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangLong, ConstantPool.JavaLangLongConstantPoolName);
            break;
        case TypeIds.T_float :
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangFloat, ConstantPool.JavaLangFloatConstantPoolName);
            break;
        case TypeIds.T_double :
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangDouble, ConstantPool.JavaLangDoubleConstantPoolName);
            break;  
        case TypeIds.T_boolean :
           	info = new VerificationTypeInfo(TypeIds.T_JavaLangBoolean, ConstantPool.JavaLangBooleanConstantPoolName);
    }
    if (info != null) {
    	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info;
    }
}
public void generateOuterAccess(Object[] mappingSequence, ASTNode invocationSite, Binding target, Scope scope) {
	if (mappingSequence == null) {
		if (target instanceof LocalVariableBinding) {
			scope.problemReporter().needImplementation(); //TODO (philippe) should improve local emulation failure reporting
		} else {
			scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
			this.currentFrame.addStackItem((ReferenceBinding)target);
		}
		return;
	}
	if (mappingSequence == BlockScope.NoEnclosingInstanceInConstructorCall) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, true);
		this.currentFrame.addStackItem((ReferenceBinding)target);
		return;
	} else if (mappingSequence == BlockScope.NoEnclosingInstanceInStaticContext) {
		scope.problemReporter().noSuchEnclosingInstance((ReferenceBinding)target, invocationSite, false);
		this.currentFrame.addStackItem((ReferenceBinding)target);
		return;
	}
	
	if (mappingSequence == BlockScope.EmulationPathToImplicitThis) {
		this.aload_0();
		return;
	} else if (mappingSequence[0] instanceof FieldBinding) {
		FieldBinding fieldBinding = (FieldBinding) mappingSequence[0];
		this.aload_0();
		this.getfield(fieldBinding);
	} else {
		load((LocalVariableBinding) mappingSequence[0]);
	}
	for (int i = 1, length = mappingSequence.length; i < length; i++) {
		if (mappingSequence[i] instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) mappingSequence[i];
			this.getfield(fieldBinding);
		} else {
			this.invokestatic((MethodBinding) mappingSequence[i]);
		}
	}
}
public void generateUnboxingConversion(int unboxedTypeID) {
	super.generateUnboxingConversion(unboxedTypeID);
	VerificationTypeInfo info = null;
	switch (unboxedTypeID) {
		case TypeIds.T_byte :
			info = new VerificationTypeInfo(TypeBinding.BYTE);
			break;
		case TypeIds.T_short :
			info = new VerificationTypeInfo(TypeBinding.SHORT);
			break;
		case TypeIds.T_char :
			info = new VerificationTypeInfo(TypeBinding.CHAR);
			break;
		case TypeIds.T_int :
			info = new VerificationTypeInfo(TypeBinding.INT);
			break;
		case TypeIds.T_long :
			info = new VerificationTypeInfo(TypeBinding.LONG);
			break;
		case TypeIds.T_float :
			info = new VerificationTypeInfo(TypeBinding.FLOAT);
			break;
		case TypeIds.T_double :
			info = new VerificationTypeInfo(TypeBinding.DOUBLE);
			break;
		case TypeIds.T_boolean :
			info = new VerificationTypeInfo(TypeBinding.BOOLEAN);
	}
	if (info != null) {
    	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info;
	}
}
public void getBaseTypeValue(int baseTypeID) {
	super.getBaseTypeValue(baseTypeID);
	VerificationTypeInfo info = null;
	switch (baseTypeID) {
		case TypeIds.T_byte :
			info = new VerificationTypeInfo(TypeBinding.BYTE);
			break;
		case TypeIds.T_short :
			info = new VerificationTypeInfo(TypeBinding.SHORT);
			break;
		case TypeIds.T_char :
			info = new VerificationTypeInfo(TypeBinding.CHAR);
			break;
		case TypeIds.T_int :
			info = new VerificationTypeInfo(TypeBinding.INT);
			break;
		case TypeIds.T_long :
			info = new VerificationTypeInfo(TypeBinding.LONG);
			break;
		case TypeIds.T_float :
			info = new VerificationTypeInfo(TypeBinding.FLOAT);
			break;
		case TypeIds.T_double :
			info = new VerificationTypeInfo(TypeBinding.DOUBLE);
			break;
		case TypeIds.T_boolean :
			info = new VerificationTypeInfo(TypeBinding.BOOLEAN);
	}
	if (info != null) {
		this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info;	
	}
}
public void getfield(FieldBinding fieldBinding) {
	super.getfield(fieldBinding);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(fieldBinding.type);
}
private VerificationTypeInfo getLocal(int resolvedPosition, StackMapFrame frame) {
	final VerificationTypeInfo verificationTypeInfo = frame.locals[resolvedPosition];
	if (verificationTypeInfo == null) {
		return null;
	}
	try {
		if (verificationTypeInfo.tag == VerificationTypeInfo.ITEM_UNINITIALIZED_THIS
				|| verificationTypeInfo.tag == VerificationTypeInfo.ITEM_UNINITIALIZED) {
			return verificationTypeInfo;
		}
		return (VerificationTypeInfo) verificationTypeInfo.clone();
	} catch (CloneNotSupportedException e) {
		return verificationTypeInfo;
	}
}
protected int getPosition() {
	// need to record a new stack frame at this position
	int pos = super.getPosition();
	this.framePositions.add(new Integer(this.position));
	storeStackMapFrame();
	return pos;
}
public void getstatic(FieldBinding fieldBinding) {
	super.getstatic(fieldBinding);
	this.currentFrame.addStackItem(fieldBinding.type);
}
public void getTYPE(int baseTypeID) {
	super.getTYPE(baseTypeID);
	this.currentFrame.addStackItem(new VerificationTypeInfo(TypeIds.T_JavaLangClass, ConstantPool.JavaLangClassConstantPoolName));
}
/**
 * We didn't call it goto, because there is a conflit with the goto keyword
 */
public void goto_(BranchLabel label) {
	super.goto_(label);
	this.framePositions.add(new Integer(this.position));
}
public void goto_w(BranchLabel label) {
	super.goto_w(label);
	this.framePositions.add(new Integer(this.position));
}
public void i2b() {
	super.i2b();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.BYTE);
}
public void i2c() {
	super.i2c();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.CHAR);
}
public void i2d() {
	super.i2d();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.DOUBLE);
}
public void i2f() {
	super.i2f();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.FLOAT);
}
public void i2l() {
	super.i2l();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.LONG);
}
public void i2s() {
	super.i2s();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.SHORT);
}
public void iadd() {
	super.iadd();
	this.currentFrame.numberOfStackItems--;
}
public void iaload() {
	super.iaload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void iand() {
	super.iand();
	this.currentFrame.numberOfStackItems--;
}
public void iastore() {
	super.iastore();
	this.currentFrame.numberOfStackItems-=3;
}
public void iconst_0() {
	super.iconst_0();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_1() {
	super.iconst_1();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_2() {
	super.iconst_2();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_3() {
	super.iconst_3();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_4() {
	super.iconst_4();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_5() {
	super.iconst_5();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void iconst_m1() {
	super.iconst_m1();
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void idiv() {
	super.idiv();
	this.currentFrame.numberOfStackItems--;
}
public void if_acmpeq(BranchLabel lbl) {
	super.if_acmpeq(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_acmpne(BranchLabel lbl) {
	super.if_acmpne(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmpeq(BranchLabel lbl) {
	super.if_icmpeq(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmpge(BranchLabel lbl) {
	super.if_icmpge(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmpgt(BranchLabel lbl) {
	super.if_icmpgt(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmple(BranchLabel lbl) {
	super.if_icmple(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmplt(BranchLabel lbl) {
	super.if_icmplt(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void if_icmpne(BranchLabel lbl) {
	super.if_icmpne(lbl);
	this.currentFrame.numberOfStackItems-=2;
}
public void ifeq(BranchLabel lbl) {
	super.ifeq(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifge(BranchLabel lbl) {
	super.ifge(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifgt(BranchLabel lbl) {
	super.ifgt(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifle(BranchLabel lbl) {
	super.ifle(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void iflt(BranchLabel lbl) {
	super.iflt(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifne(BranchLabel lbl) {
	super.ifne(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifnonnull(BranchLabel lbl) {
	super.ifnonnull(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void ifnull(BranchLabel lbl) {
	super.ifnull(lbl);
	this.currentFrame.numberOfStackItems--;
}
public void iload(int iArg) {
	super.iload(iArg);
	this.currentFrame.addStackItem(getLocal(iArg, this.currentFrame));
}
public void iload_0() {
	super.iload_0();
	this.currentFrame.addStackItem(getLocal(0, this.currentFrame));
}
public void iload_1() {
	super.iload_1();
	this.currentFrame.addStackItem(getLocal(1, this.currentFrame));
}
public void iload_2() {
	super.iload_2();
	this.currentFrame.addStackItem(getLocal(2, this.currentFrame));
}
public void iload_3() {
	super.iload_3();
	this.currentFrame.addStackItem(getLocal(3, this.currentFrame));
}
public void imul() {
	super.imul();
	this.currentFrame.numberOfStackItems--;
}
public boolean inlineForwardReferencesFromLabelsTargeting(BranchLabel targetLabel, int gotoLocation) {
	if (targetLabel.delegate != null) return false; // already inlined
	int chaining = L_UNKNOWN;

	boolean removeFrame = true;
	for (int i = this.countLabels - 1; i >= 0; i--) {
		BranchLabel currentLabel = labels[i];
		if (currentLabel.position != gotoLocation) break;
		if (currentLabel == targetLabel) {
			chaining |= L_CANNOT_OPTIMIZE;
			continue;
		}
		if (currentLabel.isStandardLabel()) {
			if (currentLabel.delegate != null) continue;			
			chaining |= L_OPTIMIZABLE;
			if (currentLabel.forwardReferenceCount() == 0 && ((currentLabel.tagBits & BranchLabel.USED) != 0)) {
				removeFrame = false;
			}
			continue;
		}
		// case label
		removeFrame = false;
		chaining |= L_CANNOT_OPTIMIZE;
	}
	if ((chaining & L_OPTIMIZABLE) != 0) {
		for (int i = this.countLabels - 1; i >= 0; i--) {
			BranchLabel currentLabel = labels[i];
			if (currentLabel.position != gotoLocation) break;
			if (currentLabel == targetLabel) continue;
			if (currentLabel.isStandardLabel()) {
				if (currentLabel.delegate != null) continue;
				targetLabel.becomeDelegateFor(currentLabel);
				// we should remove the frame corresponding to otherLabel position in order to prevent unused stack frame
				if (removeFrame) {
					currentLabel.tagBits &= ~BranchLabel.USED;
					this.removeStackFrameFor(gotoLocation);
				}
			}
		}
	}
	return (chaining & (L_OPTIMIZABLE|L_CANNOT_OPTIMIZE)) == L_OPTIMIZABLE; // check was some standards, and no case/recursive	
}

public void init(ClassFile targetClassFile) {
	super.init(targetClassFile);
	this.frames = null;
	this.currentFrame = null;
}
public void initializeMaxLocals(MethodBinding methodBinding) {
	super.initializeMaxLocals(methodBinding);
	StackMapFrame frame = new StackMapFrame();
	frame.pc = -1;
	
	if (this.maxLocals != 0) {		
		int resolvedPosition = 0;
		// take into account enum constructor synthetic name+ordinal
		final boolean isConstructor = methodBinding.isConstructor();
		if (isConstructor) {
			frame.putLocal(resolvedPosition, new VerificationTypeInfo(VerificationTypeInfo.ITEM_UNINITIALIZED_THIS, methodBinding.declaringClass));
			resolvedPosition++;
		} else if (!methodBinding.isStatic()) {
			frame.putLocal(resolvedPosition, new VerificationTypeInfo(VerificationTypeInfo.ITEM_OBJECT, methodBinding.declaringClass));
			resolvedPosition++;			
		}
			
		if (isConstructor) {
			if (methodBinding.declaringClass.isEnum()) {
				frame.putLocal(resolvedPosition, new VerificationTypeInfo(TypeIds.T_JavaLangString, ConstantPool.JavaLangStringConstantPoolName));
				resolvedPosition++;
				frame.putLocal(resolvedPosition, new VerificationTypeInfo(TypeBinding.INT));
				resolvedPosition++;
			}
			
			// take into account the synthetic parameters
			if (methodBinding.declaringClass.isNestedType()) {
				ReferenceBinding enclosingInstanceTypes[];
				if ((enclosingInstanceTypes = methodBinding.declaringClass.syntheticEnclosingInstanceTypes()) != null) {
					for (int i = 0, max = enclosingInstanceTypes.length; i < max; i++) {
						 // an enclosingInstanceType can only be a reference binding. It cannot be
						// LongBinding or DoubleBinding
						frame.putLocal(resolvedPosition, new VerificationTypeInfo(enclosingInstanceTypes[i]));
						resolvedPosition++;
					}
				}
				SyntheticArgumentBinding syntheticArguments[];
				if ((syntheticArguments = methodBinding.declaringClass.syntheticOuterLocalVariables()) != null) {
					for (int i = 0, max = syntheticArguments.length; i < max; i++) {
						final TypeBinding typeBinding = syntheticArguments[i].type;
						frame.putLocal(resolvedPosition, new VerificationTypeInfo(typeBinding));
						switch(typeBinding.id) {
							case TypeIds.T_double :
							case TypeIds.T_long :
								resolvedPosition+=2;
								break;
							default:
								resolvedPosition++;
						}
					}
				}
			}
		}

		TypeBinding[] arguments;
		if ((arguments = methodBinding.parameters) != null) {
			for (int i = 0, max = arguments.length; i < max; i++) {
				final TypeBinding typeBinding = arguments[i];
				frame.putLocal(resolvedPosition, new VerificationTypeInfo(typeBinding));
				switch(typeBinding.id) {
					case TypeIds.T_double :
					case TypeIds.T_long :
						resolvedPosition += 2;
						break;
					default:
						resolvedPosition++;
				}
			}
		}
	}
	try {
		this.frames = new ArrayList();
		this.frames.add(frame.clone());
	} catch (CloneNotSupportedException e) {
		e.printStackTrace();
	}
	this.currentFrame = frame;
	this.framePositions = new HashSet();
	this.variablesModificationsPositions = new ArrayList();
}
public void instance_of(TypeBinding typeBinding) {
	super.instance_of(typeBinding);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
protected void invokeAccessibleObjectSetAccessible() {
	super.invokeAccessibleObjectSetAccessible();
	this.currentFrame.numberOfStackItems-=2;
}
protected void invokeArrayNewInstance() {
	super.invokeArrayNewInstance();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangObject, ConstantPool.JavaLangObjectConstantPoolName);
}
public void invokeClassForName() {
	super.invokeClassForName();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangClass, ConstantPool.JavaLangClassConstantPoolName);	
}
protected void invokeClassGetDeclaredConstructor() {
	super.invokeClassGetDeclaredConstructor();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangReflectConstructor, ConstantPool.JavaLangReflectConstructorConstantPoolName);
}
protected void invokeClassGetDeclaredField() {
	super.invokeClassGetDeclaredField();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangReflectField, ConstantPool.JAVALANGREFLECTFIELD_CONSTANTPOOLNAME);	
}
protected void invokeClassGetDeclaredMethod() {
	super.invokeClassGetDeclaredMethod();
	this.currentFrame.numberOfStackItems-=2;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangReflectMethod, ConstantPool.JAVALANGREFLECTMETHOD_CONSTANTPOOLNAME);	
}
public void invokeEnumOrdinal(char[] enumTypeConstantPoolName) {
	super.invokeEnumOrdinal(enumTypeConstantPoolName);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);	
}
public void invokeinterface(MethodBinding methodBinding) {
	super.invokeinterface(methodBinding);
	int argCount = 1;
	argCount += methodBinding.parameters.length;
	this.currentFrame.numberOfStackItems -= argCount;
	if (methodBinding.returnType != TypeBinding.VOID) {
		this.currentFrame.addStackItem(methodBinding.returnType);
	}
}
public void invokeJavaLangAssertionErrorConstructor(int typeBindingID) {
	// invokespecial: java.lang.AssertionError.<init>(typeBindingID)V
	super.invokeJavaLangAssertionErrorConstructor(typeBindingID);
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeJavaLangAssertionErrorDefaultConstructor() {
	super.invokeJavaLangAssertionErrorDefaultConstructor();
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeJavaLangClassDesiredAssertionStatus() {
	// invokevirtual: java.lang.Class.desiredAssertionStatus()Z;
	super.invokeJavaLangClassDesiredAssertionStatus();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.BOOLEAN);	
}
public void invokeJavaLangEnumvalueOf(ReferenceBinding binding) {
	// invokestatic: java.lang.Enum.valueOf(Class,String)
	super.invokeJavaLangEnumvalueOf(binding);
	this.currentFrame.numberOfStackItems -= 2;
	this.currentFrame.addStackItem(binding);
}
public void invokeJavaLangEnumValues(TypeBinding enumBinding, ArrayBinding arrayBinding) {
	super.invokeJavaLangEnumValues(enumBinding, arrayBinding);
	this.currentFrame.addStackItem(arrayBinding);
}
public void invokeJavaLangErrorConstructor() {
	super.invokeJavaLangErrorConstructor();
	this.currentFrame.numberOfStackItems --;
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeJavaLangReflectConstructorNewInstance() {
	super.invokeJavaLangReflectConstructorNewInstance();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangObject, ConstantPool.JavaLangObjectConstantPoolName);	
}
protected void invokeJavaLangReflectFieldGetter(int typeID) {
	super.invokeJavaLangReflectFieldGetter(typeID);
	VerificationTypeInfo info = null;
	switch (typeID) {
		case TypeIds.T_int :
			info = new VerificationTypeInfo(TypeBinding.INT);
			break;
		case TypeIds.T_byte :
			info = new VerificationTypeInfo(TypeBinding.BYTE);
			break;
		case TypeIds.T_short :
			info = new VerificationTypeInfo(TypeBinding.SHORT);
			break;
		case TypeIds.T_long :
			info = new VerificationTypeInfo(TypeBinding.LONG);
			break;
		case TypeIds.T_float :
			info = new VerificationTypeInfo(TypeBinding.FLOAT);
			break;
		case TypeIds.T_double :
			info = new VerificationTypeInfo(TypeBinding.DOUBLE);
			break;
		case TypeIds.T_char :
			info = new VerificationTypeInfo(TypeBinding.CHAR);
			break;
		case TypeIds.T_boolean :
			info = new VerificationTypeInfo(TypeBinding.BOOLEAN);
			break;
		default :
			info = new VerificationTypeInfo(TypeIds.T_JavaLangObject, ConstantPool.JavaLangObjectConstantPoolName);
			break;
	}
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info;	
}
protected void invokeJavaLangReflectFieldSetter(int typeID) {
	super.invokeJavaLangReflectFieldSetter(typeID);
	this.currentFrame.numberOfStackItems -= 2;
}
public void invokeJavaLangReflectMethodInvoke() {
	super.invokeJavaLangReflectMethodInvoke();
	this.currentFrame.numberOfStackItems -= 3;
}
public void invokeJavaUtilIteratorHasNext() {
	super.invokeJavaUtilIteratorHasNext();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.BOOLEAN);		
}
public void invokeJavaUtilIteratorNext() {
	// invokeinterface java.util.Iterator.next()java.lang.Object
	super.invokeJavaUtilIteratorNext();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangObject, ConstantPool.JavaLangObjectConstantPoolName);		
}
public void invokeNoClassDefFoundErrorStringConstructor() {
	super.invokeNoClassDefFoundErrorStringConstructor();
	this.currentFrame.numberOfStackItems --;
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeObjectGetClass() {
	super.invokeObjectGetClass();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangClass, ConstantPool.JavaLangClassConstantPoolName);		
}
public void invokespecial(MethodBinding methodBinding) {
	super.invokespecial(methodBinding);
	int argCount = 0;
	if (methodBinding.isConstructor()) {
		final ReferenceBinding declaringClass = methodBinding.declaringClass;
		if (declaringClass.isNestedType()) {
			// enclosing instances
			TypeBinding[] syntheticArgumentTypes = declaringClass.syntheticEnclosingInstanceTypes();
			if (syntheticArgumentTypes != null) {
				argCount += syntheticArgumentTypes.length;
			}
			// outer local variables
			SyntheticArgumentBinding[] syntheticArguments = declaringClass.syntheticOuterLocalVariables();
			if (syntheticArguments != null) {
				argCount += syntheticArguments.length;
			}
		} else if (declaringClass.isEnum()) {
			argCount += 2;
		}
		argCount += methodBinding.parameters.length;
		this.currentFrame.numberOfStackItems -= argCount;
		this.currentFrame.initializeReceiver();
		this.currentFrame.numberOfStackItems--; // remove the top of stack
	} else {
		argCount = 1;
		argCount += methodBinding.parameters.length;
		this.currentFrame.numberOfStackItems -= argCount;
		if (methodBinding.returnType != TypeBinding.VOID) {
			this.currentFrame.addStackItem(methodBinding.returnType);
		}
	}
}
public void invokestatic(MethodBinding methodBinding) {
	super.invokestatic(methodBinding);
	this.currentFrame.numberOfStackItems -= methodBinding.parameters.length;
	if (methodBinding.returnType != TypeBinding.VOID) {
		this.currentFrame.addStackItem(methodBinding.returnType);
	}	
}
public void invokeStringConcatenationAppendForType(int typeID) {
	super.invokeStringConcatenationAppendForType(typeID);
	this.currentFrame.numberOfStackItems--;
}
public void invokeStringConcatenationDefaultConstructor() {
	// invokespecial: java.lang.StringBuffer.<init>()V
	super.invokeStringConcatenationDefaultConstructor();
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeStringConcatenationStringConstructor() {
	super.invokeStringConcatenationStringConstructor();
	this.currentFrame.numberOfStackItems--; // remove argument
	this.currentFrame.initializeReceiver();
	this.currentFrame.numberOfStackItems--; // remove the top of stack
}
public void invokeStringConcatenationToString() {
	super.invokeStringConcatenationToString();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangString, ConstantPool.JavaLangStringConstantPoolName);		
}
public void invokeStringValueOf(int typeID) {
	super.invokeStringValueOf(typeID);
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangString, ConstantPool.JavaLangStringConstantPoolName);		
}
public void invokeSystemArraycopy() {
	super.invokeSystemArraycopy();
	this.currentFrame.numberOfStackItems -= 5;
}
public void invokeThrowableGetMessage() {
	super.invokeThrowableGetMessage();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangString, ConstantPool.JavaLangStringConstantPoolName);		
}
public void invokevirtual(MethodBinding methodBinding) {
	super.invokevirtual(methodBinding);
	int argCount = 1;
	argCount += methodBinding.parameters.length;
	this.currentFrame.numberOfStackItems -= argCount;
	if (methodBinding.returnType != TypeBinding.VOID) {
		this.currentFrame.addStackItem(methodBinding.returnType);
	}
}
public void ior() {
	super.ior();
	this.currentFrame.numberOfStackItems--;
}
public void irem() {
	super.irem();
	this.currentFrame.numberOfStackItems--;
}
public void ireturn() {
	super.ireturn();
	this.currentFrame.numberOfStackItems--;
	this.framePositions.add(new Integer(this.position));
}
public void ishl() {
	super.ishl();
	this.currentFrame.numberOfStackItems--;
}
public void ishr() {
	super.ishr();
	this.currentFrame.numberOfStackItems--;
}
public void istore(int iArg) {
	super.istore(iArg);
	this.currentFrame.putLocal(iArg, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void istore_0() {
	super.istore_0();
	this.currentFrame.putLocal(0, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void istore_1() {
	super.istore_1();
	this.currentFrame.putLocal(1, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void istore_2() {
	super.istore_2();
	this.currentFrame.putLocal(2, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void istore_3() {
	super.istore_3();
	this.currentFrame.putLocal(3, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void isub() {
	super.isub();
	this.currentFrame.numberOfStackItems--;
}
public void iushr() {
	super.iushr();
	this.currentFrame.numberOfStackItems--;
}
public void ixor() {
	super.ixor();
	this.currentFrame.numberOfStackItems--;
}
public void l2d() {
	super.l2d();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.DOUBLE);
}
public void l2f() {
	super.l2f();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.FLOAT);
}
public void l2i() {
	super.l2i();
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
public void ladd() {
	super.ladd();
	this.currentFrame.numberOfStackItems--;
}
public void laload() {
	super.laload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void land() {
	super.land();
	this.currentFrame.numberOfStackItems--;
}
public void lastore() {
	super.lastore();
	this.currentFrame.numberOfStackItems -= 3;
}
public void lcmp() {
	super.lcmp();
	this.currentFrame.numberOfStackItems--;	
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeBinding.INT);
}
public void lconst_0() {
	super.lconst_0();
	this.currentFrame.addStackItem(TypeBinding.LONG);
}
public void lconst_1() {
	super.lconst_1();
	this.currentFrame.addStackItem(TypeBinding.LONG);
}
public void ldc(float constant) {
	super.ldc(constant);
	this.currentFrame.addStackItem(TypeBinding.FLOAT);
}
public void ldc(int constant) {
	super.ldc(constant);
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void ldc(TypeBinding typeBinding) {
	super.ldc(typeBinding);
	this.currentFrame.addStackItem(typeBinding);
}
public void ldc2_w(double constant) {
	super.ldc2_w(constant);
	this.currentFrame.addStackItem(TypeBinding.DOUBLE);
}
public void ldc2_w(long constant) {
	super.ldc2_w(constant);
	this.currentFrame.addStackItem(TypeBinding.LONG);
}
public void ldcForIndex(int index, char[] constant) {
	super.ldcForIndex(index, constant);
	this.currentFrame.addStackItem(new VerificationTypeInfo(TypeIds.T_JavaLangString, ConstantPool.JavaLangStringConstantPoolName));
}
public void ldiv() {
	super.ldiv();
	this.currentFrame.numberOfStackItems--;
}
public void lload(int iArg) {
	super.lload(iArg);
	this.currentFrame.addStackItem(getLocal(iArg, this.currentFrame));
}
public void lload_0() {
	super.lload_0();
	this.currentFrame.addStackItem(getLocal(0, this.currentFrame));
}
public void lload_1() {
	super.lload_1();
	this.currentFrame.addStackItem(getLocal(1, this.currentFrame));
}
public void lload_2() {
	super.lload_2();
	this.currentFrame.addStackItem(getLocal(2, this.currentFrame));
}
public void lload_3() {
	super.lload_3();
	this.currentFrame.addStackItem(getLocal(3, this.currentFrame));
}
public void lmul() {
	super.lmul();
	this.currentFrame.numberOfStackItems--;
}
public void lookupswitch(CaseLabel defaultLabel, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	super.lookupswitch(defaultLabel, keys, sortedIndexes, casesLabel);
	this.currentFrame.numberOfStackItems--;
}
public void lor() {
	super.lor();
	this.currentFrame.numberOfStackItems--;
}
public void lrem() {
	super.lrem();
	this.currentFrame.numberOfStackItems--;
}
public void lreturn() {
	super.lreturn();
	this.currentFrame.numberOfStackItems--;
	this.framePositions.add(new Integer(this.position));
}
public void lshl() {
	super.lshl();
	this.currentFrame.numberOfStackItems--;
}
public void lshr() {
	super.lshr();
	this.currentFrame.numberOfStackItems--;
}
public void lstore(int iArg) {
	super.lstore(iArg);
	this.currentFrame.putLocal(iArg, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void lstore_0() {
	super.lstore_0();
	this.currentFrame.putLocal(0, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void lstore_1() {
	super.lstore_1();
	this.currentFrame.putLocal(1, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void lstore_2() {
	super.lstore_2();
	this.currentFrame.putLocal(2, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void lstore_3() {
	super.lstore_3();
	this.currentFrame.putLocal(3, this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1]);
	this.currentFrame.numberOfStackItems--;
}
public void lsub() {
	super.lsub();
	this.currentFrame.numberOfStackItems--;
}
public void lushr() {
	super.lushr();
	this.currentFrame.numberOfStackItems--;
}
public void lxor() {
	super.lxor();
	this.currentFrame.numberOfStackItems--;
}
public void monitorenter() {
	super.monitorenter();
	this.currentFrame.numberOfStackItems--;
}
public void monitorexit() {
	super.monitorexit();
	this.currentFrame.numberOfStackItems--;
}
public void multianewarray(TypeBinding typeBinding, int dimensions) {
	super.multianewarray(typeBinding, dimensions);
	this.currentFrame.numberOfStackItems -= dimensions;
	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
	char[] constantPoolName = CharOperation.concat(brackets, typeBinding.constantPoolName());
	this.currentFrame.addStackItem(new VerificationTypeInfo(typeBinding.id, constantPoolName));	
}
// We didn't call it new, because there is a conflit with the new keyword
public void new_(TypeBinding typeBinding) {
	int pc = this.position;
	super.new_(typeBinding);
	final VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(VerificationTypeInfo.ITEM_UNINITIALIZED, typeBinding);
	verificationTypeInfo.offset = pc;
	this.currentFrame.addStackItem(verificationTypeInfo);
}
public void newarray(int array_Type) {
	super.newarray(array_Type);
	char[] constantPoolName = null;
	switch (array_Type) {
		case ClassFileConstants.INT_ARRAY :
			constantPoolName = new char[] { '[', 'I' };
			break;
		case ClassFileConstants.BYTE_ARRAY :
			constantPoolName = new char[] { '[', 'B' };
			break;
		case ClassFileConstants.BOOLEAN_ARRAY :
			constantPoolName = new char[] { '[', 'Z' };
			break;
		case ClassFileConstants.SHORT_ARRAY :
			constantPoolName = new char[] { '[', 'S' };
			break;
		case ClassFileConstants.CHAR_ARRAY :
			constantPoolName = new char[] { '[', 'C' };
			break;
		case ClassFileConstants.LONG_ARRAY :
			constantPoolName = new char[] { '[', 'J' };
			break;
		case ClassFileConstants.FLOAT_ARRAY :
			constantPoolName = new char[] { '[', 'F' };
			break;
		case ClassFileConstants.DOUBLE_ARRAY :
			constantPoolName = new char[] { '[', 'D' };
			break;
	}
	this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = new VerificationTypeInfo(TypeIds.T_JavaLangObject, constantPoolName);	
}
public void newJavaLangAssertionError() {
	int pc = this.position;
	super.newJavaLangAssertionError();
	final VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangAssertionError, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangAssertionErrorConstantPoolName);
	verificationTypeInfo.offset = pc;
	this.currentFrame.addStackItem(verificationTypeInfo);
}
public void newJavaLangError() {
	int pc = this.position;
	super.newJavaLangError();
	final VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangError, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangErrorConstantPoolName);
	verificationTypeInfo.offset = pc;
	this.currentFrame.addStackItem(verificationTypeInfo);
}
public void newNoClassDefFoundError() {
	int pc = this.position;
	super.newNoClassDefFoundError();
	final VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangNoClassDefError, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangNoClassDefFoundErrorConstantPoolName);
	verificationTypeInfo.offset = pc;
	this.currentFrame.addStackItem(verificationTypeInfo);
}
public void newStringContatenation() {
	int pc = this.position;
	super.newStringContatenation();
	// in 1.6, string concatenation uses StringBuilder
	final VerificationTypeInfo verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangStringBuilder, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangStringBuilderConstantPoolName);
	verificationTypeInfo.offset = pc;
	this.currentFrame.addStackItem(verificationTypeInfo);
}
public void newWrapperFor(int typeID) {
	int pc = this.position;
	super.newWrapperFor(typeID);
	VerificationTypeInfo verificationTypeInfo = null;
	switch (typeID) {
		case TypeIds.T_int : // new: java.lang.Integer
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangInteger, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangIntegerConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_boolean : // new: java.lang.Boolean
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangBoolean, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangBooleanConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_byte : // new: java.lang.Byte
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangByte, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangByteConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_char : // new: java.lang.Character
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangCharacter, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangCharacterConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_float : // new: java.lang.Float
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangFloat, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangFloatConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_double : // new: java.lang.Double
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangDouble, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangDoubleConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_short : // new: java.lang.Short
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangShort, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangShortConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_long : // new: java.lang.Long
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangLong, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangLongConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
			break;
		case TypeIds.T_void : // new: java.lang.Void
			verificationTypeInfo = new VerificationTypeInfo(TypeIds.T_JavaLangVoid, VerificationTypeInfo.ITEM_UNINITIALIZED, ConstantPool.JavaLangVoidConstantPoolName);
			this.currentFrame.addStackItem(verificationTypeInfo);
	}
	if (verificationTypeInfo != null) {
		verificationTypeInfo.offset = pc;
	}
}
public void optimizeBranch(int oldPosition, BranchLabel lbl) {
	super.optimizeBranch(oldPosition, lbl);
	int frameIndex = this.frames.size() - 1;
	loop: while(frameIndex > 0) {
		StackMapFrame frame = (StackMapFrame) this.frames.get(frameIndex);
		if (frame.pc == oldPosition) {
			if (this.framePositions.remove(new Integer(oldPosition))) {
				this.framePositions.add(new Integer(this.position));
			}
			if (this.variablesModificationsPositions.remove(new Integer(oldPosition))) {
				this.variablesModificationsPositions.add(new Integer(this.position));
			}
			frame.pc = this.position;
			StackMapFrame previousFrame = (StackMapFrame) this.frames.get(frameIndex - 1);
			if (previousFrame.pc == this.position) {
				// remove the current frame
				this.frames.set(frameIndex - 1, frame);
				this.frames.remove(frameIndex);
			}
			break loop;
		} else if (frame.pc > oldPosition) {
			return;
		}
		frameIndex--;
	}
}
public void pop() {
	super.pop();
	this.currentFrame.numberOfStackItems--;
}
public void pop2() {
	super.pop2();
	switch(this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1].id()) {
		case TypeIds.T_long :
		case TypeIds.T_double :
			this.currentFrame.numberOfStackItems--;
			break;
		default:
			this.currentFrame.numberOfStackItems -= 2;
	}
}
public void pushOnStack(TypeBinding binding) {
	super.pushOnStack(binding);
	this.currentFrame.addStackItem(binding);
}
public void putfield(FieldBinding fieldBinding) {
	super.putfield(fieldBinding);
	this.currentFrame.numberOfStackItems -= 2;	
}
public void putstatic(FieldBinding fieldBinding) {
	super.putstatic(fieldBinding);
	this.currentFrame.numberOfStackItems--;	
}
public void recordExpressionType(TypeBinding typeBinding) {
	super.recordExpressionType(typeBinding);
	this.currentFrame.setTopOfStack(typeBinding);
}
public void removeVariable(LocalVariableBinding localBinding) {
	this.currentFrame.removeLocals(localBinding.resolvedPosition);
	super.removeVariable(localBinding);
}
public void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
	int index = this.visibleLocalsCount;
	for (int i = 0; i < index; i++) {
		LocalVariableBinding localBinding = visibleLocals[i];
		if (localBinding != null && !isDefinitelyAssigned(scope, initStateIndex, localBinding)
				&& localBinding.initializationCount > 0) {
			this.currentFrame.removeLocals(localBinding.resolvedPosition);
		}
	}
	Integer newValue = new Integer(this.position);
	if (this.variablesModificationsPositions.size() == 0 || !this.variablesModificationsPositions.get(this.variablesModificationsPositions.size() - 1).equals(newValue)) {
		this.variablesModificationsPositions.add(newValue);
	}
	storeStackMapFrame();
	super.removeNotDefinitelyAssignedVariables(scope, initStateIndex);
}
public void storeStackMapFrame() {
	int frameSize = this.frames.size();
	StackMapFrame mapFrame = null;
	try {
		mapFrame = (StackMapFrame) this.currentFrame.clone();
		mapFrame.pc = this.position;
	} catch(CloneNotSupportedException e) {
		// ignore
	}
	if (frameSize == 0) {
			this.frames.add(mapFrame);
	} else {
		StackMapFrame lastFrame = (StackMapFrame) this.frames.get(frameSize - 1);
		if (lastFrame.pc == this.position) {
			this.frames.set(frameSize - 1, mapFrame);
		} else {
			this.frames.add(mapFrame);
		}
	}
}
public void return_() {
	super.return_();
	this.framePositions.add(new Integer(this.position));
}
public void saload() {
	super.saload();
	this.currentFrame.numberOfStackItems--;
	this.currentFrame.replaceWithElementType();
}
public void sastore() {
	super.sastore();
	this.currentFrame.numberOfStackItems -= 3;
}
public void sipush(int s) {
	super.sipush(s);
	this.currentFrame.addStackItem(TypeBinding.INT);
}
public void store(LocalVariableBinding localBinding, boolean valueRequired) {
	super.store(localBinding, valueRequired);
	final TypeBinding typeBinding = localBinding.type;
	switch(typeBinding.id) {
		default:
			// Reference object
			this.currentFrame.locals[localBinding.resolvedPosition] = new VerificationTypeInfo(typeBinding);
	}
}
public void swap() {
	super.swap();
	try {
		VerificationTypeInfo info = (VerificationTypeInfo) this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1].clone();
		VerificationTypeInfo info2 = (VerificationTypeInfo) this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 2].clone();
		this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 1] = info2;
		this.currentFrame.stackItems[this.currentFrame.numberOfStackItems - 2] = info;
	} catch (CloneNotSupportedException e) {
		// ignore
	}
}
public void tableswitch(CaseLabel defaultLabel, int low, int high, int[] keys, int[] sortedIndexes, CaseLabel[] casesLabel) {
	super.tableswitch(defaultLabel, low, high, keys, sortedIndexes, casesLabel);
	this.currentFrame.numberOfStackItems--;
}
public void throwAnyException(LocalVariableBinding anyExceptionVariable) {
	this.currentFrame.putLocal(anyExceptionVariable.resolvedPosition, new VerificationTypeInfo(VerificationTypeInfo.ITEM_OBJECT, anyExceptionVariable.type));
	super.throwAnyException(anyExceptionVariable);
	this.currentFrame.removeLocals(anyExceptionVariable.resolvedPosition);
}
public void removeStackFrameFor(int pos) {
	// TODO (olivier) need to see how to get rid of some unnecessary frames
}
public void reset(ClassFile givenClassFile) {
	super.reset(givenClassFile);
	this.frames = null;
	this.currentFrame = null;
	this.framePositions = null;
	this.variablesModificationsPositions = null;
}
protected void writePosition(BranchLabel label) {
	super.writePosition(label);
	framePositions.add(new Integer(label.position));
}
protected void writeWidePosition(BranchLabel label) {
	super.writeWidePosition(label);
	framePositions.add(new Integer(label.position));
}
protected void writePosition(BranchLabel label, int forwardReference) {
	super.writePosition(label, forwardReference);
	framePositions.add(new Integer(label.position));
}
}
