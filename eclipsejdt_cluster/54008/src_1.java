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
package org.eclipse.jdt.core.dom;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;

/**
 * A Java language parser for creating abstract syntax trees (ASTs).
 * <p>
 * Example: Create basic AST from source string
 * <pre>
 * char[] source = ...;
 * ASTParser parser = ASTParser.newParser3();  // handles JLS3 (J2SE 1.5)
 * parser.setSource(source);
 * CompilationUnit result = (CompilationUnit) parser.createAST(null);
 * </pre>
 * Once a configured parser instance has been used to create an AST,
 * the settings are automicatically returned to their defaults,
 * ready for the parser instance to be reused.
 * </p>
 * <p>
 * There are a number of configurable features:
 * <ul>
 * <li>Source string from {@link #setSource(char[]) char[]},
 * {@link #setSource(ICompilationUnit) ICompilationUnit},
 * or {@link #setSource(IClassFile) IClassFile}, and limited
 * to a specified {@linkplain #setSourceRange(int,int) subrange.</li>
 * <li>Whether {@linkplain #setResolveBindings(boolean) bindings} will be created.</li>
 * <li>Which {@linkplain #setWorkingCopyOwner(WorkingCopyOwner)
 * working set owner} to use when resolving bindings).</li>
 * <li>A hypothetical {@linkplain #setUnitName(String) compilation unit file name}
 * and {@linkplain #setProject(IJavaProject) Java project}
 * for locating a raw source string in the Java model (when
 * resolving bindings)</li>
 * <li>Which {@linkplain #setCompilerOptions(Map) compiler options}
 * to use.</li>
 * <li>Whether to parse just {@linkplain #setKind(int) an expression, statements,
 * or body declarations} rather than an entire compilation unit.</li>
 * <li>Whether to return a {@linkplain #setPartial(int) abridged AST}
 * focused on the declaration containing a given source position.</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public class ASTParser {

	/**
	 * Kind constant used to request that the source be parsed
     * as a single expression.
	 */
	public static final int K_EXPRESSION = 0x01;

	/**
	 * Kind constant used to request that the source be parsed
     * as a sequence of statements.
	 */
	public static final int K_STATEMENTS = 0x02;
	
	/**
	 * Kind constant used to request that the source be parsed
	 * as a sequence of class body declarations.
	 */
	public static final int K_CLASS_BODY_DECLARATIONS = 0x04;
	
	/**
	 * Kind constant used to request that the source be parsed
	 * as a compilation unit.
	 */
	public static final int K_COMPILATION_UNIT = 0x08;
	
	/**
	 * Creates a new object for creating a Java abstract syntax tree
     * (AST) following the 3.0 API rules. The 3.0 API is capable
     * of handling all constructs in the Java language as described
     * in the Java Language Specification, Third Edition (JLS3).
     * JLS3 is a superset of all earlier versions of the
     * Java language, and the 3.0 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 1.5.

     * @deprecated Do not use at this time. The 1.5 compiler support is missing.
     * TBD (jeem) - remove deprecation when 1.5 parser and compiler support available
	 */
	public static ASTParser newParser3() {
		return new ASTParser(AST.LEVEL_3_0);
	}

	/**
	 * Creates a new object for creating a Java abstract syntax tree
     * (AST) following the 2.0 API rules. The 2.0 API is capable
     * of handling all constructs in the Java language as described
     * in the Java Language Specification, Second Edition (JLS2).
     * JLS2 is a superset of all earlier versions of the
     * Java language, and the 2.0 API can be used to manipulate
     * programs written in all versions of the Java language
     * up to and including J2SE 1.4. 
     *
	 * TBD (jeem) deprecated Clients should port their code to
     * use the new 3.0 API and call {@link #newParser3()}
     * instead of this method.
	 */
	public static ASTParser newParser2() {
		return new ASTParser(AST.LEVEL_2_0);
	}

	/**
	 * Non-deprected version of <code>ASTParser newParser2()</code>
	 * for internal use. 
	 */
	static ASTParser internalNewParser2() {
		return new ASTParser(AST.LEVEL_2_0);
	}

	/**
	 * Level of AST API desired.
	 */
	private final int API_LEVEL;

	/**
	 * Kind of parse requested. Defaults to an entire compilation unit.
	 */
	private int astKind;
	
	/**
	 * Compiler options. Defaults to JavaCore.getOptions().
	 */
	private Map compilerOptions;
	
	/**
	 * Request for bindings. Defaults to <code>false</code>.
     */
	private boolean resolveBindings;

	/**
	 * Request for a partial AST. Defaults to <code>false</code>.
     */
	private boolean partial = false;

	/**
	 * The focal point for a partial AST request.
     * Only used when <code>partial</code> is <code>true</code>.
     */
	private int focalPointPosition;

    /**
     * Source string. 
     */
    private char[] rawSource = null;
    
    /**
     * Java mode compilation unit supplying the source.
     */
    private ICompilationUnit compilationUnitSource = null;
    
    /**
     * Java model class file supplying the source.
     */
    private IClassFile classFileSource = null;
    
    /**
     * Character-based offset into the source string where parsing is to
     * begin. Defaults to 0.
     */
	private int sourceOffset = 0;
	
    /**
     * Character-based length limit, or -1 if unlimited.
     * All characters in the source string between <code>offset</code>
     * and <code>offset+length-1</code> inclusive are parsed. Defaults to -1, 
     * which means the rest of the source string.
     */
	private int sourceLength = -1;

    /**
     * Working copy owner. Defaults to primary owner.
     */
	private WorkingCopyOwner workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	
    /**
	 * Java project used to resolve names, or <code>null</code> if none.
     * Defaults to none.
     */
	private IJavaProject project = null;
	
    /**
	 * Name of the compilation unit for resolving bindings, or 
	 * <code>null</code> if none. Defaults to none.
     */
	private String unitName = null; 

 	/**
	 * Creates a new AST parser for the given API level.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param level the API level; one of the LEVEL constants
     * declared on <code>AST</code>
	 */
	ASTParser(int level) {
		this.API_LEVEL = level;
	   	initializeDefaults();
	}

	/**
	 * Sets all the setting to their default values.
	 */
	private void initializeDefaults() {
	   this.astKind = K_COMPILATION_UNIT;
	   this.rawSource = null;
	   this.classFileSource = null;
	   this.compilationUnitSource = null;
	   this.resolveBindings = false;
	   this.sourceLength = -1;
	   this.sourceOffset = 0;
	   this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
	   this.unitName = null;
	   this.project = null;
	   this.partial = false;
	   this.compilerOptions = JavaCore.getOptions();
	}
	   
	/**
	 * Sets the compiler options to be used when parsing.
     * <p>
     * The compiler options default to {@link JavaCore#getOptions()}.
     * </p>
	 * 
	 * @param options the table of options (key type: <code>String</code>;
	 * value type: <code>String</code>), or <code>null</code>
     * to set it back to the default
	 */
	public void setCompilerOptions(Map options) {
	   if (options == null) {
	      this.compilerOptions = JavaCore.getOptions();
	   }
	   this.compilerOptions = options;
	}
	
	/**
	 * Requests that the compiler should provide binding information for
     * the AST nodes it creates.
     * <p>
     * Default to <code>false</code> (no bindings).
     * </p>
	 * <p>
	 * If <code>setResolveBindings(true)</code>, the various names
	 * and types appearing in the AST can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>setResolveBindings(false)</code> (the default), the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * When bindings are requested, instead of considering compilation units on disk only
	 * one can supply a <code>WorkingCopyOwner</code>. Working copies owned 
	 * by this owner take precedence over the underlying compilation units when looking
	 * up names and drawing the connections.
	 * </p>
	 * <p>
     * Binding information is obtained from the Java model.
     * This means that the compilation unit must be located
     * relative to the Java model via [TBD].
	 * Note that the compiler options that affect doc comment checking may also
	 * affect whether any bindings are resolved for nodes within doc comments.
	 * </p>
	 * 
	 * @param bindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 */
	public void setResolveBindings(boolean bindings) {
	  this.resolveBindings = bindings;
	}
	
	/**
     * Requests an abridged abstract syntax tree. 
     * By default, complete ASTs are returned.
     *
     * When <code>true</code> the resulting AST does not have nodes for
     * the entire compilation unit. Rather, the AST is only fleshed out
     * for the node that include the given source position. This kind of limited
     * AST is sufficient for certain purposes but totally unsuitable for others.
     * In places where it can be used, the limited AST offers the advantage of
     * being smaller and faster to faster to construct.
	 * </p>
	 * <p>
	 * The AST will include nodes for all of the compilation unit's
	 * package, import, and top-level type declarations. It will also always contain
	 * nodes for all the body declarations for those top-level types, as well
	 * as body declarations for any member types. However, some of the body
	 * declarations may be abridged. In particular, the statements ordinarily
	 * found in the body of a method declaration node will not be included
	 * (the block will be empty) unless the source position falls somewhere
	 * within the source range of that method declaration node. The same is true
	 * for initializer declarations; the statements ordinarily found in the body
	 * of initializer node will not be included unless the source position falls
	 * somewhere within the source range of that initializer declaration node.
	 * Field declarations are never abridged. Note that the AST for the body of
	 * that one unabridged method (or initializer) is 100% complete; it has all
	 * its statements, including any local or anonymous type declarations 
	 * embedded within them. When the the given position is not located within
	 * the source range of any body declaration of a top-level type, the AST
	 * returned will be a skeleton that includes nodes for all and only the major
	 * declarations; this kind of AST is still quite useful because it contains
	 * all the constructs that introduce names visible to the world outside the
	 * compilation unit.
	 * </p>
	 * 
	 * @param position a position into the corresponding body declaration
	 */
	public void setPartial(int position) {
		this.partial = true;
		this.focalPointPosition = position;
	}
	
	/**
	 * Sets the kind of constructs to be parsed from the source.
     * Defaults to an entire compilation unit.
	 * <p>
	 * When the parse is successful the result returned includes the ASTs for the
	 * requested source:
	 * <ul>
	 * <li>{@link #K_COMPILATION_UNIT}: The result node
	 * is a {@link CompilationUnit}.</li>
	 * <li>{@link #K_CLASS_BODY_DECLARATIONS}: The result node
	 * is a {@link TypeDeclaration} whose
	 * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
	 * are the new trees. Other aspects of the type declaration are unspecified.</li>
	 * <li>{@link #K_STATEMENTS}: The result node is a
	 * {@link Block Block} whose {@link Block#statements() statements}
	 * are the new trees. Other aspects of the block are unspecified.</li>
	 * <li>{@link #K_EXPRESSION}: The result node is a subclass of
	 * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
	 * </ul>
	 * The resulting AST node is rooted under (possibly contrived)
	 * {@link CompilationUnit CompilationUnit} node, to allow the
	 * client to retrieve the following pieces of information 
	 * available there:
	 * <ul>
	 * <li>{@linkplain CompilationUnit#lineNumber(int) Line number map}. Line
	 * numbers start at 1 and only cover the subrange scanned
	 * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
	 * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
	 * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
	 * Character positions are relative to the start of 
	 * <code>source</code>; line positions are for the subrange scanned.</li>
	 * <li>{@linkplain CompilationUnit#getCommentTable() Comment table}
	 * for the subrange scanned.</li>
	 * </ul>
	 * The contrived nodes do not have source positions. Other aspects of the
	 * {@link CompilationUnit CompilationUnit} node are unspecified, including
	 * the exact arrangment of intervening nodes.
	 * </p>
	 * <p>
	 * Lexical or syntax errors detected while parsing can result in
	 * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
	 * In more severe failure cases where the parser is unable to
	 * recognize the input, this method returns 
	 * a {@link CompilationUnit CompilationUnit} node with at least the
	 * compiler messages.
	 * </p>
	 * <p>Each node in the subtree (other than the contrived nodes) 
	 * carries source range(s) information relating back
	 * to positions in the given source (the given source itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * </p>
	 * <p>
	 * Binding information is only computed when <code>kind</code> is 
     * <code>K_COMPILATION_UNIT</code>.
	 * </p>
	 *  
	 * @param kind the kind of construct to parse: one of 
	 * {@link #K_COMPILATION_UNIT},
	 * {@link #K_CLASS_BODY_DECLARATIONS},
	 * {@link #K_EXPRESSION},
	 * {@link #K_STATEMENTS}
	 */
	public void setKind(int kind) {
	    if ((kind != K_COMPILATION_UNIT)
		    && (kind != K_CLASS_BODY_DECLARATIONS)
		    && (kind != K_EXPRESSION)
		    && (kind != K_STATEMENTS)) {
	    	throw new IllegalArgumentException();
	    }
		this.astKind = kind;
	}
	
	/**
     * Sets the source code to be parsed.
     *
	 * @param source the source string to be parsed,
     * or <code>null</code> if none
     */
	public void setSource(char[] source) {
		this.rawSource = source;
		// clear the others
		this.compilationUnitSource = null;
		this.classFileSource = null;
	}

	/**
     * Sets the source code to be parsed.
     *
	 * @param source the Java model compilation unit whose source code
     * is to be parsed, or <code>null</code> if none
     */
	public void setSource(ICompilationUnit source) {
		this.compilationUnitSource = source;
		// clear the others
		this.rawSource = null;
		this.classFileSource = null;
	}
	
	/**
     * Sets the source code to be parsed.
     *
	 * @param source the Java model class file whose corresponding source code
     * is to be parsed, or <code>null</code> if none
     */
	public void setSource(IClassFile source) {
		this.classFileSource = source;
		// clear the others
		this.rawSource = null;
		this.compilationUnitSource = null;
	}
	
	/**
     * Sets the subrange of the source code to be parsed.
     * By default, the entire source string will be parsed
     * (<code>offset</code> 0 and <code>length</code> -1).
     *
     * @param offset the index of the first character to parse
     * @param length the number of characters to parse, or -1 if
     * the remainder of the source string is 
     */
	public void setSourceRange(int offset, int length) {
		if (offset < 0 || length < -1) {
			throw new IllegalArgumentException();
		}
		this.sourceOffset = offset;
		this.sourceLength = length;
	}
	
    /**
     * Sets the working copy owner using when resolving bindings, where
     * <code>null</code> means the primary owner. Defaults to the primary owner.
     *
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
     */
	public void setWorkingCopyOwner(WorkingCopyOwner owner) {
	    if (owner == null) {
			this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
		} else {
			this.workingCopyOwner = owner;
	 	}
	}

	/**
     * Sets the name of the compilation unit that would hypothetically contains
     * the source string. This is used in conjunction with
     * <code>setSource(char[])</code> and <code>setProject</code> to locate the
     * compilation unit relative to a Java project.
     * Defaults to none (<code>null</code>).
	 * <p>
	 * The name of the compilation unit must be supplied for resolving bindings.
	 * This name should include the ".java" suffix and match the name of the main
	 * (public) class or interface declared in the source. For example, if the source
	 * declares a public class named "Foo", the name of the compilation should be
	 * "Foo.java".
	 * </p>
     *
	 * @param unitName the name of the compilation unit that would contain the source
	 *    string, or <code>null</code> if none
     */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	/**
     * Sets the Java project used when resolving bindings.
     * This setting is used in conjunction with <code>setSource(char[])</code>;
     * it is ignored if the source code comes from
     * <code>setSource(ICompilationUnit)</code>
     * or <code>setSource(IClassFile)</code>.
     * For the purposes of resolving bindings, types declared in the
	 * source string will hide types by the same name available
     * through the classpath of the given project.
     * Defaults to none (<code>null</code>).
     *
	 * @param project the Java project used to resolve names, or 
	 *    <code>null</code> if none
     */
	public void setProject(IJavaProject project) {
		this.project = project;
	}
	
	/**
     * Creates an abstract syntax tree.
     * <p>
     * Calling this method also returns all settings to their
     * default values so the object is ready to be reused.
     * </p>
     * 
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return an AST node whose type depends on the kind of parse
	 *  requested, with a fallback to a <code>CompilationUnit</code>
	 *  in the case of severe parsing errors
     */
	public ASTNode createAST(IProgressMonitor monitor) {
	   if ((this.rawSource == null)
	   	  && (this.compilationUnitSource == null)
	   	  && (this.classFileSource == null)) {
	   	  throw new IllegalStateException("source not specified"); //$NON-NLS-1$
	   }
	   ASTNode result;
	   if (this.API_LEVEL == AST.LEVEL_2_0) {
	   		result = temporaryCreateASTDispatch(monitor);
	   } else {
	   		throw new RuntimeException("J2SE 1.5 parser not implemented yet"); //$NON-NLS-1$
	   }
   	   // if successful, re-init defaults to allow reuse (and avoid leaking)
   	   initializeDefaults();
   	   return result;
	}
	
	private ASTNode temporaryCreateASTDispatch(IProgressMonitor monitor) {
		// old AST.parse(...)
		if (this.astKind != K_COMPILATION_UNIT) {
			return parse(this.astKind, this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions);
		}
		// old AST.parsePartialCompilationUnit(...)
		if (this.partial) {
			if (this.compilationUnitSource != null) {
				return parsePartialCompilationUnit(this.compilationUnitSource, this.focalPointPosition, this.resolveBindings, this.workingCopyOwner, monitor);
			}
			if (this.classFileSource != null) {
				return parsePartialCompilationUnit(this.classFileSource, this.focalPointPosition, this.resolveBindings, this.workingCopyOwner, monitor);
			}
			throw new RuntimeException("partial parses of raw sources not implemented yet"); //$NON-NLS-1$
		}
		// old AST.parseCompilationUnit(...)
		if (this.rawSource != null) {
			if (unitName != null || project != null) {
				return parseCompilationUnit(this.rawSource, this.unitName, this.project, this.workingCopyOwner, monitor);
			} else {
				return parseCompilationUnit(this.rawSource, this.compilerOptions);
			}
		}
		if (this.compilationUnitSource != null) {
			return parseCompilationUnit(this.compilationUnitSource, this.resolveBindings, this.workingCopyOwner, monitor);
		}
		if (this.classFileSource != null) {
			return parseCompilationUnit(this.classFileSource, this.resolveBindings, this.workingCopyOwner, monitor);
		}
		throw new IllegalStateException();
	}

	/**
	 * Parses the given source between the bounds specified by the given offset (inclusive)
	 * and the given length and creates and returns a corresponding abstract syntax tree.
	 * <p>
	 * When the parse is successful the result returned includes the ASTs for the
	 * requested source:
	 * <ul>
	 * <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node
	 * is a {@link TypeDeclaration TypeDeclaration} whose
	 * {@link TypeDeclaration#bodyDeclarations() bodyDeclarations}
	 * are the new trees. Other aspects of the type declaration are unspecified.</li>
	 * <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a
	 * {@link Block Block} whose {@link Block#statements() statements}
	 * are the new trees. Other aspects of the block are unspecified.</li>
	 * <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of
	 * {@link Expression Expression}. Other aspects of the expression are unspecified.</li>
	 * </ul>
	 * The resulting AST node is rooted under an contrived
	 * {@link CompilationUnit CompilationUnit} node, to allow the
	 * client to retrieve the following pieces of information 
	 * available there:
	 * <ul>
	 * <li>{@linkplain CompilationUnit#lineNumber(int) Line number map}. Line
	 * numbers start at 1 and only cover the subrange scanned
	 * (<code>source[offset]</code> through <code>source[offset+length-1]</code>).</li>
	 * <li>{@linkplain CompilationUnit#getMessages() Compiler messages}
	 * and {@linkplain CompilationUnit#getProblems() detailed problem reports}.
	 * Character positions are relative to the start of 
	 * <code>source</code>; line positions are for the subrange scanned.</li>
	 * <li>{@linkplain CompilationUnit#getCommentTable() Comment table}
	 * for the subrange scanned.</li>
	 * </ul>
	 * The contrived nodes do not have source positions. Other aspects of the
	 * {@link CompilationUnit CompilationUnit} node are unspecified, including
	 * the exact arrangment of intervening nodes.
	 * </p>
	 * <p>
	 * Lexical or syntax errors detected while parsing can result in
	 * a result node being marked as {@link ASTNode#MALFORMED MALFORMED}.
	 * In more severe failure cases where the parser is unable to
	 * recognize the input, this method returns 
	 * a {@link CompilationUnit CompilationUnit} node with at least the
	 * compiler messages.
	 * </p>
	 * <p>Each node in the subtree (other than the contrived nodes) 
	 * carries source range(s) information relating back
	 * to positions in the given source (the given source itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * </p>
	 * <p>
	 * This method does not compute binding information; all <code>resolveBinding</code>
	 * methods applied to nodes of the resulting AST return <code>null</code>.
	 * </p>
	 * 
	 * @param kind the kind of construct to parse: one of 
	 * {@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS},
	 * {@link #K_EXPRESSION K_EXPRESSION},
	 * {@link #K_STATEMENTS K_STATEMENTS}
	 * @param source the source to be parsed
	 * @param  offset  the index of the first byte to decode
	 * @param  length  the number of bytes to decode
	 * @param options the options; if null, <code>JavaCore.getOptions()</code> is used
	 * @return an AST node whose type depends on the kind of parse
	 *  requested, with a fallback to a <code>CompilationUnit</code>
	 *  in the case of severe parsing errors
	 * @throws IndexOutOfBoundsException
	 *         if the <code>offset</code> and the <code>length</code>
	 *         arguments index characters outside the bounds of
	 *         <code>source</code>
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @see JavaCore#getOptions()
	 */
	private static ASTNode parse(int kind, char[] source, int offset, int length, Map options) {
		if (kind != K_CLASS_BODY_DECLARATIONS
				&& kind != K_EXPRESSION
				&& kind != K_STATEMENTS) {
			throw new IllegalArgumentException();
		}
		if (source == null) {
			throw new IllegalArgumentException();
		}
		if (length < 0 || offset < 0 || offset > source.length - length) {
		    throw new IndexOutOfBoundsException();
		}
		if (options == null) {
			options = JavaCore.getOptions();
		}
		ASTConverter converter = new ASTConverter(options, false, null);
		converter.compilationUnitSource = source;
		converter.scanner.setSource(source);
		
		AST ast = AST.newAST2();
		ast.setBindingResolver(new BindingResolver());
		converter.setAST(ast);
		CodeSnippetParsingUtil codeSnippetParsingUtil = new CodeSnippetParsingUtil();
		CompilationUnit compilationUnit = ast.newCompilationUnit();
		switch(kind) {
			case K_STATEMENTS :
				ConstructorDeclaration constructorDeclaration = codeSnippetParsingUtil.parseStatements(source, offset, length, options, true);
				RecordedParsingInformation recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				int[][] comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (constructorDeclaration != null) {
					Block block = ast.newBlock();
					Statement[] statements = constructorDeclaration.statements;
					if (statements != null) {
						int statementsLength = statements.length;
						for (int i = 0; i < statementsLength; i++) {
							block.statements().add(converter.convert(statements[i]));
						}
					}
					rootNodeToCompilationUnit(ast, converter, compilationUnit, block, recordedParsingInformation);
					return block;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					return compilationUnit;
				}
			case K_EXPRESSION :
				org.eclipse.jdt.internal.compiler.ast.Expression expression = codeSnippetParsingUtil.parseExpression(source, offset, length, options, true);
				recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (expression != null) {
					Expression expression2 = converter.convert(expression);
					rootNodeToCompilationUnit(ast, converter, compilationUnit, expression2, codeSnippetParsingUtil.recordedParsingInformation);
					return expression2;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					return compilationUnit;
				}
			case K_CLASS_BODY_DECLARATIONS :
				final org.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes = codeSnippetParsingUtil.parseClassBodyDeclarations(source, offset, length, options, true);
				recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
				comments = recordedParsingInformation.commentPositions;
				if (comments != null) {
					converter.buildCommentsTable(compilationUnit, comments);
				}
				compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
				if (nodes != null) {
					TypeDeclaration typeDeclaration = converter.convert(nodes);
					rootNodeToCompilationUnit(ast, converter, compilationUnit, typeDeclaration, codeSnippetParsingUtil.recordedParsingInformation);
					return typeDeclaration;
				} else {
					IProblem[] problems = recordedParsingInformation.problems;
					if (problems != null) {
						compilationUnit.setProblems(problems);
					}
					return compilationUnit;
				}
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Parses the source string of the given Java model compilation unit element
	 * and creates and returns a corresponding abstract syntax tree. The source 
	 * string is obtained from the Java model element using
	 * <code>ICompilationUnit.getSource()</code>.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the source string (the source string is not remembered
	 * with the AST).
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If <code>resolveBindings</code> is <code>true</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>resolveBindings</code> is <code>false</code>, the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * When bindings are created, instead of considering compilation units on disk only
	 * one can supply a <code>WorkingCopyOwner</code>. Working copies owned 
	 * by this owner take precedence over the underlying compilation units when looking
	 * up names and drawing the connections.
	 * </p>
	 * <p>
	 * Note that the compiler options that affect doc comment checking may also
	 * affect whether any bindings are resolved for nodes within doc comments.
	 * </p>
	 * 
	 * @param unit the Java model compilation unit whose source code is to be parsed
	 * @param resolveBindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return the compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not 
	 * exist or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @see WorkingCopyOwner
	 */
	private static CompilationUnit parseCompilationUnit(
		ICompilationUnit unit,
		boolean resolveBindings,
		WorkingCopyOwner owner,
		IProgressMonitor monitor) {
		
		if (unit == null) {
			throw new IllegalArgumentException();
		}
		if (owner == null) {
			owner = DefaultWorkingCopyOwner.PRIMARY;
		}
		
		char[] source = null;
		try {
			source = unit.getSource().toCharArray();
		} catch(JavaModelException e) {
			// no source, then we cannot build anything
			throw new IllegalArgumentException();
		}
	
		if (resolveBindings) {
			CompilationUnitDeclaration compilationUnitDeclaration = null;
			try {
				// parse and resolve
				compilationUnitDeclaration = CompilationUnitResolver.resolve(unit, false/*don't cleanup*/, source, owner, monitor);
				ASTConverter converter = new ASTConverter(unit.getJavaProject().getOptions(true), true, monitor);
				AST ast = AST.newAST2();
				BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
			
				CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
				cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return cu;
			} catch(JavaModelException e) {
				/* if a JavaModelException is thrown trying to retrieve the name environment
				 * then we simply do a parsing without creating bindings.
				 * Therefore all binding resolution will return null.
				 */
				return parseCompilationUnit(source);			
			} finally {
				if (compilationUnitDeclaration != null) {
					compilationUnitDeclaration.cleanUp();
				}
			}
		} else {
			return parseCompilationUnit(source);
		}
	}

	/**
	 * Parses the source string corresponding to the given Java class file
	 * element and creates and returns a corresponding abstract syntax tree.
	 * The source string is obtained from the Java model element using
	 * <code>IClassFile.getSource()</code>, and is only available for a class
	 * files with attached source.
	 * In all other respects, this method works the same as
	 * {@link #parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner,IProgressMonitor)
	 * parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner,IProgressMonitor)}.
	 * <p>
	 * Note that the compiler options that affect doc comment checking may also
	 * affect whether any bindings are resolved for nodes within doc comments.
	 * </p>
	 * 
	 * @param classFile the Java model class file whose corresponding source code is to be parsed
	 * @param resolveBindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return the compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not 
	 * exist or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @see WorkingCopyOwner
	 */
	private static CompilationUnit parseCompilationUnit(
		IClassFile classFile,
		boolean resolveBindings,
		WorkingCopyOwner owner,
		IProgressMonitor monitor) {
			
		if (classFile == null) {
			throw new IllegalArgumentException();
		}
		if (owner == null) {
			owner = DefaultWorkingCopyOwner.PRIMARY;
		}
		char[] source = null;
		String sourceString = null;
		try {
			sourceString = classFile.getSource();
		} catch (JavaModelException e) {
			throw new IllegalArgumentException();
		}
		if (sourceString == null) {
			throw new IllegalArgumentException();
		}
		source = sourceString.toCharArray();
		if (!resolveBindings) {
			return parseCompilationUnit(source);
		}
		StringBuffer buffer = new StringBuffer(SuffixConstants.SUFFIX_STRING_java);
		
		String classFileName = classFile.getElementName(); // this includes the trailing .class
		buffer.insert(0, classFileName.toCharArray(), 0, classFileName.indexOf('.'));
		IJavaProject project = classFile.getJavaProject();
		CompilationUnitDeclaration compilationUnitDeclaration = null;
		try {
			// parse and resolve
			compilationUnitDeclaration =
				CompilationUnitResolver.resolve(
					source,
					CharOperation.splitOn('.', classFile.getType().getPackageFragment().getElementName().toCharArray()),
					buffer.toString(),
					project,
					null/*no node searcher*/,
					false/*don't cleanup*/,
					owner,
					monitor);
			ASTConverter converter = new ASTConverter(project.getOptions(true), true, monitor);
			AST ast = AST.newAST2();
			BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
		
			CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
			cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
			resolver.storeModificationCount(ast.modificationCount());
			return cu;
		} catch(JavaModelException e) {
			/* if a JavaModelException is thrown trying to retrieve the name environment
			 * then we simply do a parsing without creating bindings.
			 * Therefore all binding resolution will return null.
			 */
			return parseCompilationUnit(source);			
		} finally {
			if (compilationUnitDeclaration != null) {
				compilationUnitDeclaration.cleanUp();
			}
		}
	}

	/**
	 * Parses the given string as the hypothetical contents of the named
	 * compilation unit and creates and returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST).
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If the given project is not <code>null</code>, the various names
	 * and types appearing in the compilation unit can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If the given project is <code>null</code>, the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * When bindings are created, instead of considering compilation units on disk only
	 * one can supply a <code>WorkingCopyOwner</code>. Working copies owned 
	 * by this owner take precedence over the underlying compilation units when looking
	 * up names and drawing the connections.
	 * </p>
	 * <p>
	 * The name of the compilation unit must be supplied for resolving bindings.
	 * This name should include the ".java" suffix and match the name of the main
	 * (public) class or interface declared in the source. For example, if the source
	 * declares a public class named "Foo", the name of the compilation should be
	 * "Foo.java". For the purposes of resolving bindings, types declared in the
	 * source string hide types by the same name available through the classpath
	 * of the given project.
	 * </p>
	 * 
	 * @param source the string to be parsed as a Java compilation unit
	 * @param unitName the name of the compilation unit that would contain the source
	 *    string, or <code>null</code> if <code>javaProject</code> is also <code>null</code>
	 * @param project the Java project used to resolve names, or 
	 *    <code>null</code> if bindings are not resolved
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @see WorkingCopyOwner
	 */
	private static CompilationUnit parseCompilationUnit(
		char[] source,
		String unitName,
		IJavaProject project,
		WorkingCopyOwner owner,
		IProgressMonitor monitor) {
			
		if (source == null) {
			throw new IllegalArgumentException();
		}
		if (unitName == null && project != null) {
			throw new IllegalArgumentException();
		}
		if (project == null) {
			// this just reduces to the other simplest case
			return parseCompilationUnit(source);
		}
		if (owner == null) {
			owner = DefaultWorkingCopyOwner.PRIMARY;
		}
	
		CompilationUnitDeclaration compilationUnitDeclaration = null;
		try {
			// parse and resolve
			compilationUnitDeclaration =
				CompilationUnitResolver.resolve(
					source,
					unitName,
					project,
					false/*don't cleanup*/,
					owner,
					monitor);
			ASTConverter converter = new ASTConverter(project.getOptions(true), true, monitor);
			AST ast = AST.newAST2();
			BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
		
			CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
			cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
			resolver.storeModificationCount(ast.modificationCount());
			return cu;
		} catch(JavaModelException e) {
			/* if a JavaModelException is thrown trying to retrieve the name environment
			 * then we simply do a parsing without creating bindings.
			 * Therefore all binding resolution will return null.
			 */
			return parseCompilationUnit(source);			
		} finally {
			if (compilationUnitDeclaration != null) {
				compilationUnitDeclaration.cleanUp();
			}
		}
	}

	/**
	 * Parses the given string as a Java compilation unit and creates and 
	 * returns a corresponding abstract syntax tree.
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * This method does not compute binding information; all <code>resolveBinding</code>
	 * methods applied to nodes of the resulting AST return <code>null</code>.
	 * </p>
	 * 
	 * @param source the string to be parsed as a Java compilation unit
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	private static CompilationUnit parseCompilationUnit(char[] source) {
		return parseCompilationUnit(source, null);
	}

	/**
	 * Parses the given string as a Java compilation unit and creates and 
	 * returns a corresponding abstract syntax tree.
	 * <p>
	 * The given options are used to find out the compiler options to use while parsing.
	 * This could implies the settings for the assertion support. See the <code>JavaCore.getOptions()</code>
	 * methods for further details.
	 * </p>
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the given source string (the given source string itself
	 * is not remembered with the AST). 
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included. There are a handful of exceptions
	 * (including compilation units and the various body declarations); the
	 * specification for these node type spells out the details.
	 * Source ranges nest properly: the source range for a child is always
	 * within the source range of its parent, and the source ranges of sibling
	 * nodes never overlap.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * This method does not compute binding information; all <code>resolveBinding</code>
	 * methods applied to nodes of the resulting AST return <code>null</code>.
	 * </p>
	 * 
	 * @param source the string to be parsed as a Java compilation unit
	 * @param options options to use while parsing the file. If null, <code>JavaCore.getOptions()</code> is used.
	 * @return the compilation unit node
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 * @see JavaCore#getOptions()
	 */
	private static CompilationUnit parseCompilationUnit(char[] source, Map options) {
		if (options == null) {
			options = JavaCore.getOptions();
		}
		if (source == null) {
			throw new IllegalArgumentException();
		}
		CompilationUnitDeclaration compilationUnitDeclaration = 
			CompilationUnitResolver.parse(source, options);
	
		ASTConverter converter = new ASTConverter(options, false, null);
		AST ast = AST.newAST2();
		ast.setBindingResolver(new BindingResolver());
		converter.setAST(ast);
				
		CompilationUnit cu = converter.convert(compilationUnitDeclaration, source);
		
		// line end table should be extracted from scanner
		cu.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
		return cu;
	}

	/**
	 * Parses the source string of the given Java model compilation unit element
	 * and creates and returns an abridged abstract syntax tree. This method
	 * differs from
	 * {@link #parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner)
	 * parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner)} only in 
	 * that the resulting AST does not have nodes for the entire compilation
	 * unit. Rather, the AST is only fleshed out for the node that include
	 * the given source position. This kind of limited AST is sufficient for
	 * certain purposes but totally unsuitable for others. In places where it
	 * can be used, the limited AST offers the advantage of being smaller and
	 * faster to faster to construct.
	 * </p>
	 * <p>
	 * The resulting AST always includes nodes for all of the compilation unit's
	 * package, import, and top-level type declarations. It also always contains
	 * nodes for all the body declarations for those top-level types, as well
	 * as body declarations for any member types. However, some of the body
	 * declarations may be abridged. In particular, the statements ordinarily
	 * found in the body of a method declaration node will not be included
	 * (the block will be empty) unless the source position falls somewhere
	 * within the source range of that method declaration node. The same is true
	 * for initializer declarations; the statements ordinarily found in the body
	 * of initializer node will not be included unless the source position falls
	 * somewhere within the source range of that initializer declaration node.
	 * Field declarations are never abridged. Note that the AST for the body of
	 * that one unabridged method (or initializer) is 100% complete; it has all
	 * its statements, including any local or anonymous type declarations 
	 * embedded within them. When the the given position is not located within
	 * the source range of any body declaration of a top-level type, the AST
	 * returned is a skeleton that includes nodes for all and only the major
	 * declarations; this kind of AST is still quite useful because it contains
	 * all the constructs that introduce names visible to the world outside the
	 * compilation unit.
	 * </p>
	 * <p>
	 * In all other respects, this method works the same as
	 * {@link #parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner)
	 * parseCompilationUnit(ICompilationUnit,boolean,WorkingCopyOwner)}.
	 * The source string is obtained from the Java model element using
	 * <code>ICompilationUnit.getSource()</code>.
	 * </p>
	 * <p>
	 * The returned compilation unit node is the root node of a new AST.
	 * Each node in the subtree carries source range(s) information relating back
	 * to positions in the source string (the source string is not remembered
	 * with the AST).
	 * The source range usually begins at the first character of the first token 
	 * corresponding to the node; leading whitespace and comments are <b>not</b>
	 * included. The source range usually extends through the last character of
	 * the last token corresponding to the node; trailing whitespace and
	 * comments are <b>not</b> included.
	 * If a syntax error is detected while parsing, the relevant node(s) of the
	 * tree will be flagged as <code>MALFORMED</code>.
	 * </p>
	 * <p>
	 * If <code>resolveBindings</code> is <code>true</code>, the various names
	 * and types appearing in the method declaration can be resolved to "bindings"
	 * by calling the <code>resolveBinding</code> methods. These bindings 
	 * draw connections between the different parts of a program, and 
	 * generally afford a more powerful vantage point for clients who wish to
	 * analyze a program's structure more deeply. These bindings come at a 
	 * considerable cost in both time and space, however, and should not be
	 * requested frivolously. The additional space is not reclaimed until the 
	 * AST, all its nodes, and all its bindings become garbage. So it is very
	 * important to not retain any of these objects longer than absolutely
	 * necessary. Bindings are resolved at the time the AST is created. Subsequent
	 * modifications to the AST do not affect the bindings returned by
	 * <code>resolveBinding</code> methods in any way; these methods return the
	 * same binding as before the AST was modified (including modifications
	 * that rearrange subtrees by reparenting nodes).
	 * If <code>resolveBindings</code> is <code>false</code>, the analysis 
	 * does not go beyond parsing and building the tree, and all 
	 * <code>resolveBinding</code> methods return <code>null</code> from the 
	 * outset.
	 * </p>
	 * <p>
	 * When bindings are created, instead of considering compilation units on disk only
	 * one can supply a <code>WorkingCopyOwner</code>. Working copies owned 
	 * by this owner take precedence over the underlying compilation units when looking
	 * up names and drawing the connections.
	 * </p>
	 * <p>
	 * Note that the compiler options that affect doc comment checking may also
	 * affect whether any bindings are resolved for nodes within doc comments.
	 * </p>
	 * 
	 * @param unit the Java model compilation unit whose source code is to be parsed
	 * @param position a position into the corresponding body declaration
	 * @param resolveBindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return the abridged compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not 
	 * exist or the source range is null or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	private static CompilationUnit parsePartialCompilationUnit(
		ICompilationUnit unit,
		int position,
		boolean resolveBindings,
		WorkingCopyOwner owner,
		IProgressMonitor monitor) {
				
		if (unit == null) {
			throw new IllegalArgumentException();
		}
		if (owner == null) {
			owner = DefaultWorkingCopyOwner.PRIMARY;
		}
		
		char[] source = null;
		try {
			source = unit.getSource().toCharArray();
		} catch(JavaModelException e) {
			// no source, then we cannot build anything
			throw new IllegalArgumentException();
		}
	
		NodeSearcher searcher = new NodeSearcher(position);
	
		final Map options = unit.getJavaProject().getOptions(true);
		if (resolveBindings) {
			CompilationUnitDeclaration compilationUnitDeclaration = null;
			try {
				// parse and resolve
				compilationUnitDeclaration = CompilationUnitResolver.resolve(
					unit,
					searcher,
					false/*don't cleanup*/,
					source,
					owner,
					monitor);
				
				ASTConverter converter = new ASTConverter(options, true, monitor);
				AST ast = AST.newAST2();
				BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
			
				CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration, source);
				compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return compilationUnit;
			} catch(JavaModelException e) {
				/* if a JavaModelException is thrown trying to retrieve the name environment
				 * then we simply do a parsing without creating bindings.
				 * Therefore all binding resolution will return null.
				 */
				CompilationUnitDeclaration compilationUnitDeclaration2 = CompilationUnitResolver.parse(
					source,
					searcher,
					options);
				
				ASTConverter converter = new ASTConverter(options, false, monitor);
				AST ast = AST.newAST2();
				final BindingResolver resolver = new BindingResolver();
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
	
				CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration2, source);
				compilationUnit.setLineEndTable(compilationUnitDeclaration2.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return compilationUnit;
			} finally {
				if (compilationUnitDeclaration != null) {
					compilationUnitDeclaration.cleanUp();
				}
			}
		} else {
			CompilationUnitDeclaration compilationUnitDeclaration = CompilationUnitResolver.parse(
				source,
				searcher,
				options);
			
			ASTConverter converter = new ASTConverter(options, false, monitor);
			AST ast = AST.newAST2();
			final BindingResolver resolver = new BindingResolver();
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
	
			CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration, source);
			compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
			resolver.storeModificationCount(ast.modificationCount());
			return compilationUnit;
		}
	}

	/**
	 * Parses the source string corresponding to the given Java class file
	 * element and creates and returns a corresponding abstract syntax tree.
	 * The source string is obtained from the Java model element using
	 * <code>IClassFile.getSource()</code>, and is only available for a class
	 * files with attached source. 
	 * In all other respects, this method works the same as
	 * {@link #parsePartialCompilationUnit(ICompilationUnit,int,boolean,WorkingCopyOwner,IProgressMonitor)
	 * parsePartialCompilationUnit(ICompilationUnit,int,boolean,WorkingCopyOwner,IProgressMonitor)}.
	 * 
	 * @param classFile the Java model class file whose corresponding source code is to be parsed
	 * @param position a position into the corresponding body declaration
	 * @param resolveBindings <code>true</code> if bindings are wanted, 
	 *   and <code>false</code> if bindings are not of interest
	 * @param owner the owner of working copies that take precedence over underlying 
	 *   compilation units, or <code>null</code> if the primary owner should be used
	 * @param monitor the progress monitor used to report progress and request cancelation,
	 *   or <code>null</code> if none
	 * @return the abridged compilation unit node
	 * @exception IllegalArgumentException if the given Java element does not 
	 * exist or the source range is null or if its source string cannot be obtained
	 * @see ASTNode#getFlags()
	 * @see ASTNode#MALFORMED
	 * @see ASTNode#getStartPosition()
	 * @see ASTNode#getLength()
	 */
	private static CompilationUnit parsePartialCompilationUnit(
	    IClassFile classFile,
		int position,
		boolean resolveBindings,
		WorkingCopyOwner owner,
		IProgressMonitor monitor) {
				
		if (classFile == null) {
			throw new IllegalArgumentException();
		}
		if (owner == null) {
			owner = DefaultWorkingCopyOwner.PRIMARY;
		}
	
		char[] source = null;
		String sourceString = null;
		try {
			sourceString = classFile.getSource();
		} catch (JavaModelException e) {
			throw new IllegalArgumentException();
		}
	
		if (sourceString == null) {
			throw new IllegalArgumentException();
		}
		source = sourceString.toCharArray();
	
		NodeSearcher searcher = new NodeSearcher(position);
	
		final Map options = classFile.getJavaProject().getOptions(true);
		if (resolveBindings) {
			CompilationUnitDeclaration compilationUnitDeclaration = null;
			try {
				// parse and resolve
				compilationUnitDeclaration = CompilationUnitResolver.resolve(
					classFile,
					searcher,
					false/*don't cleanup*/,
					source,
					owner,
					monitor);
				
				ASTConverter converter = new ASTConverter(options, true, monitor);
				AST ast = AST.newAST2();
				BindingResolver resolver = new DefaultBindingResolver(compilationUnitDeclaration.scope);
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
			
				CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration, source);
				compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return compilationUnit;
			} catch(JavaModelException e) {
				/* if a JavaModelException is thrown trying to retrieve the name environment
				 * then we simply do a parsing without creating bindings.
				 * Therefore all binding resolution will return null.
				 */
				CompilationUnitDeclaration compilationUnitDeclaration2 = CompilationUnitResolver.parse(
					source,
					searcher,
					options);
				
				ASTConverter converter = new ASTConverter(options, false, monitor);
				AST ast = AST.newAST2();
				final BindingResolver resolver = new BindingResolver();
				ast.setBindingResolver(resolver);
				converter.setAST(ast);
	
				CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration2, source);
				compilationUnit.setLineEndTable(compilationUnitDeclaration2.compilationResult.lineSeparatorPositions);
				resolver.storeModificationCount(ast.modificationCount());
				return compilationUnit;
			} finally {
				if (compilationUnitDeclaration != null) {
					compilationUnitDeclaration.cleanUp();
				}
			}
		} else {
			CompilationUnitDeclaration compilationUnitDeclaration = CompilationUnitResolver.parse(
				source,
				searcher,
				options);
			
			ASTConverter converter = new ASTConverter(options, false, monitor);
			AST ast = AST.newAST2();
			final BindingResolver resolver = new BindingResolver();
			ast.setBindingResolver(resolver);
			converter.setAST(ast);
	
			CompilationUnit compilationUnit = converter.convert(compilationUnitDeclaration, source);
			compilationUnit.setLineEndTable(compilationUnitDeclaration.compilationResult.lineSeparatorPositions);
			resolver.storeModificationCount(ast.modificationCount());
			return compilationUnit;
		}
	}

	private static void rootNodeToCompilationUnit(AST ast, ASTConverter converter, CompilationUnit compilationUnit, ASTNode node, RecordedParsingInformation recordedParsingInformation) {
		final int problemsCount = recordedParsingInformation.problemsCount;
		switch(node.getNodeType()) {
			case ASTNode.BLOCK :
				{
					Block block = (Block) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						for (int i = 0, max = block.statements().size(); i < max; i++) {
							converter.propagateErrors((ASTNode) block.statements().get(i), problems);
						}
						compilationUnit.setProblems(problems);
					}
					TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
					Initializer initializer = ast.newInitializer();
					initializer.setBody(block);
					typeDeclaration.bodyDeclarations().add(initializer);
					compilationUnit.types().add(typeDeclaration);
				}
				break;
			case ASTNode.TYPE_DECLARATION :
				{
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						for (int i = 0, max = typeDeclaration.bodyDeclarations().size(); i < max; i++) {
							converter.propagateErrors((ASTNode) typeDeclaration.bodyDeclarations().get(i), problems);
						}
						compilationUnit.setProblems(problems);
					}
					compilationUnit.types().add(typeDeclaration);
				}
				break;
			default :
				if (node instanceof Expression) {
					Expression expression = (Expression) node;
					if (problemsCount != 0) {
						// propagate and record problems
						final IProblem[] problems = recordedParsingInformation.problems;
						converter.propagateErrors(expression, problems);
						compilationUnit.setProblems(problems);
					}
					ExpressionStatement expressionStatement = ast.newExpressionStatement(expression);
					Block block = ast.newBlock();
					block.statements().add(expressionStatement);
					Initializer initializer = ast.newInitializer();
					initializer.setBody(block);
					TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
					typeDeclaration.bodyDeclarations().add(initializer);
					compilationUnit.types().add(typeDeclaration);
				}
		}
	}
}
