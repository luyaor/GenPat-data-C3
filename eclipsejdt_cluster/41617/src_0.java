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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 * Parser specialized for decoding javadoc comments
 */
public class JavadocParser {

	// recognized tags
	public static final char[] TAG_DEPRECATED = "deprecated".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_PARAM = "param".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_RETURN = "return".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_THROWS = "throws".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_EXCEPTION = "exception".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_SEE = "see".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINK = "link".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_LINKPLAIN = "linkplain".toCharArray(); //$NON-NLS-1$
	public static final char[] TAG_INHERITDOC = "inheritDoc".toCharArray(); //$NON-NLS-1$
	
	// tags expected positions
	public final static int ORDERED_TAGS_NUMBER = 3;
	public final static int PARAM_TAG_EXPECTED_ORDER = 0;
	public final static int THROWS_TAG_EXPECTED_ORDER = 1;
	public final static int SEE_TAG_EXPECTED_ORDER = 2;
	
	// Public fields
	public Javadoc javadoc;
	public boolean checkJavadoc = false;
	public Scanner scanner;
	
	// Private fields
	private int currentTokenType = -1;
	private Parser sourceParser;
	private int index, tagSourceStart, tagSourceEnd, lineEnd;
	private char[] source;
	private boolean lineStarted = false, inlineTagStarted = false;
	
	// Identifier stack
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	// Ast stack
	protected static int AstStackIncrement = 10;
	protected int astPtr;
	protected ASTNode[] astStack;
	protected int astLengthPtr;
	protected int[] astLengthStack;

	JavadocParser(Parser sourceParser) {
		this.sourceParser = sourceParser;
		this.checkJavadoc = (this.sourceParser.options.getSeverity(CompilerOptions.InvalidJavadoc) != ProblemSeverities.Ignore) ||
			(this.sourceParser.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore);
		this.scanner = new Scanner(false, false, false, ClassFileConstants.JDK1_3, null, null);
		this.identifierStack = new char[10][];
		this.identifierPositionStack = new long[10];
		this.identifierLengthStack = new int[20];
		this.astStack = new ASTNode[20];
		this.astLengthStack = new int[30];
	}

	/* (non-Javadoc)
	 * Returns true if tag @deprecated is present in javadoc comment.
	 * 
	 * If javadoc checking is enabled, will also construct an Javadoc node, which will be stored into Parser.javadoc
	 * slot for being consumed later on.
	 */
	public boolean checkDeprecation(int javadocStart, int javadocEnd) {

		boolean foundDeprecated = false;
		try {
			this.source = this.sourceParser.scanner.source;
			this.index = javadocStart +3;
			int endComment = javadocEnd - 2;
			if (this.checkJavadoc) {
				// Initialization
				this.javadoc = new Javadoc(javadocStart, javadocEnd);
				this.astLengthPtr = -1;
				this.astPtr = -1;
				this.currentTokenType = -1;
				this.scanner.startPosition = this.index;
				this.inlineTagStarted = false;
				this.lineStarted = false;
				int lineNumber = this.sourceParser.scanner.getLineNumber(javadocStart);
				int lastLineNumber = this.sourceParser.scanner.getLineNumber(javadocEnd);
				this.lineEnd = lineNumber == lastLineNumber ? javadocEnd - 2 : javadocStart + 3;
				char nextCharacter= 0, previousChar;
				int charPosition = -1, inlineStartPosition = 0;
				
				// Loop on each comment character
				while (this.index < endComment) {
					int previousPosition = this.index;
					previousChar = nextCharacter;
					
					// Calculate line end (cannot use this.scanner.linePtr as scanner does not parse line ends again)
					if (this.index > this.lineEnd) {
						if (lineNumber < lastLineNumber) {
							this.lineEnd = this.sourceParser.scanner.getLineEnd(++lineNumber) - 1;
						} else {
							this.lineEnd = javadocEnd - 2;
						}
						this.lineStarted = false;
					}
					
					// Read next char only if token was consumed
					if (this.currentTokenType < 0) {
						nextCharacter = readChar(); // consider unicodes
					} else {
						switch (this.currentTokenType) {
							case TerminalTokens.TokenNameRBRACE:
								nextCharacter = '}';
								break;
							case TerminalTokens.TokenNameMULTIPLY:
								nextCharacter = '*';
								break;
						default:
								nextCharacter = this.scanner.currentCharacter;
						}
						consumeToken();
					}
					
					switch (nextCharacter) {
						case '@' :
							boolean valid = false;
							// Start tag parsing only if we are on line beginning or at inline tag beginning
							if (!this.lineStarted || previousChar == '{') {
								this.lineStarted = true;
								if (this.inlineTagStarted) {
									this.inlineTagStarted = false;
									int start = this.astStack[this.astPtr].sourceStart;
									this.sourceParser.problemReporter().javadocInvalidTag(start, charPosition);
								} else {
									if (previousChar == '{') {
										this.inlineTagStarted = true;
									}
									this.scanner.resetTo(this.index, endComment);
									this.currentTokenType = -1; // flush token cache at line begin
									try {
										int tk = readTokenAndConsume();
										this.tagSourceStart = this.scanner.getCurrentTokenStartPosition();
										this.tagSourceEnd = this.scanner.getCurrentTokenEndPosition();
										switch (tk) {
											case TerminalTokens.TokenNameIdentifier :
												char[] tag = this.scanner.getCurrentIdentifierSource();
												if (CharOperation.equals(tag, TAG_DEPRECATED)) {
													foundDeprecated = true;
													valid = true;
												} else if (CharOperation.equals(tag, TAG_INHERITDOC)) {
													this.javadoc.inherited = true;
													valid = true;
												} else if (CharOperation.equals(tag, TAG_PARAM)) {
													valid = parseParam();
												} else if (CharOperation.equals(tag, TAG_EXCEPTION)) {
													valid = parseThrows();
												} else if (CharOperation.equals(tag, TAG_SEE) ||
														CharOperation.equals(tag, TAG_LINK) ||
														CharOperation.equals(tag, TAG_LINKPLAIN)) {
													valid = parseSee();
												} else {
													valid = parseTag();
												}
												break;
											case TerminalTokens.TokenNamereturn :
												valid = parseReturn();
												break;
											case TerminalTokens.TokenNamethrows :
												valid = parseThrows();
												break;
										}
										if (!valid && this.inlineTagStarted) {
											this.inlineTagStarted = false;
										}
									} catch (InvalidInputException e) {
										consumeToken();
									}
								}
							}
							break;
						case '\r':
						case '\n':
							this.lineStarted = false;
							break;
						case '}' :
							if (this.inlineTagStarted) this.inlineTagStarted = false;
							this.lineStarted = true;
							charPosition = previousPosition;
							break;
						case '{' :
							if (this.inlineTagStarted) {
								this.inlineTagStarted = false;
								this.sourceParser.problemReporter().javadocInvalidTag(inlineStartPosition, this.index);
							} else {
								inlineStartPosition = previousPosition;
							}
							break;
						case '*' :
							charPosition = previousPosition;
							break;
						default :
							charPosition = previousPosition;
							if (!this.lineStarted && !CharOperation.isWhitespace(nextCharacter)) {
								this.lineStarted = true;
							}
					}
				}
			} else {
				// Init javadoc if necessary
				if (this.sourceParser.options.getSeverity(CompilerOptions.MissingJavadocComments) != ProblemSeverities.Ignore) {
					this.javadoc = new Javadoc(javadocStart, javadocEnd);
				} else {
					this.javadoc = null;
				}
				
				// Parse comment
				int firstLineNumber = this.sourceParser.scanner.getLineNumber(javadocStart);
				int lastLineNumber = this.sourceParser.scanner.getLineNumber(javadocEnd);
	
				// scan line per line, since tags must be at beginning of lines only
				nextLine : for (int line = firstLineNumber; line <= lastLineNumber; line++) {
					int lineStart = line == firstLineNumber
							? javadocStart + 3 // skip leading /**
							: this.sourceParser.scanner.getLineStart(line);
					this.index = lineStart;
					this.lineEnd = line == lastLineNumber
							? javadocEnd - 2 // remove trailing * /
							: this.sourceParser.scanner.getLineEnd(line);
					while (this.index < this.lineEnd) {
						char nextCharacter = readChar(); // consider unicodes
						if  (nextCharacter == '@' &&
							(readChar() == 'd') &&
							(readChar() == 'e') &&
							(readChar() == 'p') &&
							(readChar() == 'r') &&
							(readChar() == 'e') &&
							(readChar() == 'c') &&
							(readChar() == 'a') &&
							(readChar() == 't') &&
							(readChar() == 'e') &&
							(readChar() == 'd'))
						{
							// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
							nextCharacter = readChar();
							if (Character.isWhitespace(nextCharacter) || nextCharacter == '*') {
								return true;
							}
						}
					}
				}
				return false;
			}
		} finally {
			if (this.checkJavadoc) {
				updateJavadoc();
			}
			this.source = null; // release source as soon as finished
		}
		return foundDeprecated;
	}

	private void consumeToken() {
		this.currentTokenType = -1; // flush token cache
	}

	private int getEndPosition() {
		if (this.scanner.getCurrentTokenEndPosition() > this.lineEnd) {
			return this.lineEnd;
		} else {
			return this.scanner.getCurrentTokenEndPosition();
		}
	}

	/*
	 * Parse argument in @see tag method reference
	 */
	private Expression parseArguments(TypeReference receiver) throws InvalidInputException {

		// Init
		int modulo = 0; // should be 2 for (Type,Type,...) or 3 for (Type arg,Type arg,...)
		int iToken = 0;
		char[] argName = null;
		int ptr = this.astPtr;
		int lptr = this.astLengthPtr;
		
		// Decide whether we have a constructor or not
		boolean isConstructor = true;
		if (receiver != null) {
			char[][] receiverTokens = receiver.getTypeName();
			char[] memberName = this.identifierStack[0];
			isConstructor = CharOperation.equals(memberName, receiverTokens[receiverTokens.length-1]);
		}

		// Parse arguments declaration if method reference
		nextArg : while (this.index < this.scanner.eofPosition) {

			// Read argument type reference
			TypeReference typeRef;
			try {
				typeRef = parseQualifiedName(false);
			} catch (InvalidInputException e) {
				break nextArg;
			}
			boolean firstArg = modulo == 0;
			if (firstArg) { // verify position
				if (iToken != 0)
					break nextArg;
			} else if ((iToken % modulo) != 0) {
					break nextArg;
			}
			if (typeRef == null) {
				if (firstArg && this.currentTokenType == TerminalTokens.TokenNameRPAREN) {
					if (isConstructor) {
						JavadocAllocationExpression expr = new JavadocAllocationExpression(this.identifierPositionStack[0]);
						expr.type = receiver;
						return expr;
					} else {
						JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0]);
						msg.receiver = receiver;
						return msg;
					}
				}
				break nextArg;
			}
			int argStart = typeRef.sourceStart;
			int argEnd = typeRef.sourceEnd;
			iToken++;

			// Read possible array declaration
			int dim = 0;
			if (readToken() == TerminalTokens.TokenNameLBRACKET) {
				while (readToken() == TerminalTokens.TokenNameLBRACKET) {
					consumeToken();
					if (readToken() != TerminalTokens.TokenNameRBRACKET) {
						break nextArg;
					}
					consumeToken();
					dim++;
				}
				long pos = ((long) typeRef.sourceStart) << 32 + typeRef.sourceEnd;
				if (typeRef instanceof JavadocSingleTypeReference) {
					JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
					typeRef = new JavadocArraySingleTypeReference(singleRef.token, dim, pos);
				} else {
					JavadocQualifiedTypeReference qualifRef = (JavadocQualifiedTypeReference) typeRef;
					typeRef = new JavadocArrayQualifiedTypeReference(qualifRef, dim);
				}
			}

			// Read argument name
			if (readToken() == TerminalTokens.TokenNameIdentifier) {
				consumeToken();
				if (firstArg) { // verify position
					if (iToken != 1)
						break nextArg;
				} else if ((iToken % modulo) != 1) {
						break nextArg;
				}
				if (argName == null) { // verify that all arguments name are declared
					if (!firstArg) {
						break nextArg;
					}
				}
				argName = this.scanner.getCurrentIdentifierSource();
				argEnd = this.scanner.getCurrentTokenEndPosition();
				iToken++;
			} else if (argName != null) { // verify that no argument name is declared
				break nextArg;
			}
			
			// Verify token position
			if (firstArg) {
				modulo = iToken + 1;
			} else {
				if ((iToken % modulo) != (modulo - 1)) {
					break nextArg;
				}
			}

			// Read separator or end arguments declaration
			int token = readToken();
			char[] name = argName == null ? new char[0] : argName;
			if (token == TerminalTokens.TokenNameCOMMA) {
				// Create new argument
				JavadocArgumentExpression expr = new JavadocArgumentExpression(name, argStart, argEnd, typeRef);
				pushOnAstStack(expr, firstArg);
				consumeToken();
				iToken++;
			} else if (token == TerminalTokens.TokenNameRPAREN) {
				// Create new argument
				JavadocArgumentExpression expr = new JavadocArgumentExpression(name, argStart, argEnd, typeRef);
				pushOnAstStack(expr, firstArg);
				int size = this.astLengthStack[this.astLengthPtr--];
				// Build arguments array
				JavadocArgumentExpression[] arguments = new JavadocArgumentExpression[size];
				for (int i = (size - 1); i >= 0; i--) {
					arguments[i] = (JavadocArgumentExpression) this.astStack[this.astPtr--];
				}
				// Create message send
				if (isConstructor) {
					JavadocAllocationExpression alloc = new JavadocAllocationExpression(this.identifierPositionStack[0]);
					alloc.arguments = arguments;
					alloc.type = receiver;
					return alloc;
				} else {
					JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[0], this.identifierPositionStack[0], arguments);
					msg.receiver = receiver;
					return msg;
				}
			} else {
				break nextArg;
			}
		}

		// Invalid input: reset ast stacks pointers
		consumeToken();
		if (iToken > 0) {
			this.astPtr = ptr;
			this.astLengthPtr = lptr;
		}
		throw new InvalidInputException();
	}

	/*
	 * Parse an URL link reference in @see tag
	 */
	private boolean parseHref() throws InvalidInputException {
		int start = this.scanner.getCurrentTokenStartPosition();
		if (readToken() == TerminalTokens.TokenNameIdentifier) {
			consumeToken();
			if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'a'}, false)
					&& readToken() == TerminalTokens.TokenNameIdentifier) {
				consumeToken();
				try {
					if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'h', 'r', 'e', 'f'}, false) &&
						readToken() == TerminalTokens.TokenNameEQUAL) {
						consumeToken();
						if (readToken() == TerminalTokens.TokenNameStringLiteral) {
							consumeToken();
							if (readToken() == TerminalTokens.TokenNameGREATER) {
								consumeToken();
								while (readToken() != TerminalTokens.TokenNameLESS) {
									if (this.source[this.index] == '\r' || this.source[this.index] == '\n' || this.scanner.getCurrentTokenStartPosition() > this.lineEnd) {
										this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
										return false;
									}
									consumeToken();
								}
								consumeToken();
								if (readToken() == TerminalTokens.TokenNameDIVIDE) {
									consumeToken();
									if (readToken() == TerminalTokens.TokenNameIdentifier) {
										consumeToken();
										if (CharOperation.equals(this.scanner.getCurrentIdentifierSource(), new char[]{'a'}, false)	&&
											readToken() == TerminalTokens.TokenNameGREATER) {
											consumeToken();
											// Valid href
											return true;
										}
									}
								}
							}
						}
					}
				} catch (InvalidInputException ex) {
					// Do nothing as we want to keep positions for error message
				}
			}
		}
		this.sourceParser.problemReporter().javadocInvalidSeeUrlReference(start, this.lineEnd);
		return false;
	}

	/*
	 * Parse a method reference in @see tag
	 */
	private Expression parseMember(TypeReference receiver) throws InvalidInputException {
		// Init
		this.identifierPtr = -1;
		this.identifierLengthPtr = -1;
		int start = this.scanner.getCurrentTokenStartPosition();
		
		// Get type ref
		TypeReference typeRef = receiver;
		if (typeRef == null) {
			char[] name = this.sourceParser.compilationUnit.compilationResult.compilationUnit.getMainTypeName();
			if (name == null) {
				throw new InvalidInputException();
			}
			typeRef = new JavadocSingleTypeReference(name, 0, 0, 0);
		}
		
		// Get member identifier
		if (readToken() == TerminalTokens.TokenNameIdentifier) {
			consumeToken();
			pushIdentifier(true);
			if (readToken() == TerminalTokens.TokenNameLPAREN) {
				consumeToken();
				start = this.scanner.getCurrentTokenStartPosition();
				try {
					return parseArguments(typeRef);
				} catch (InvalidInputException e) {
					int end = this.scanner.getCurrentTokenEndPosition() < this.lineEnd ?
							this.scanner.getCurrentTokenEndPosition() :
							this.scanner.getCurrentTokenStartPosition();
					end = end < this.lineEnd ? end : this.lineEnd;
					this.sourceParser.problemReporter().javadocInvalidSeeReferenceArgs(start, end);
				}
				return null;
			}
			JavadocFieldReference field = new JavadocFieldReference(this.identifierStack[0], this.identifierPositionStack[0]);
			field.receiver = typeRef;
			field.tagSourceStart = this.tagSourceStart;
			field.tagSourceEnd = this.tagSourceEnd;
			return field;
		}
		int end = getEndPosition() - 1;
		end = start > end ? getEndPosition() : end;
		this.sourceParser.problemReporter().javadocInvalidSeeReference(start, end);
		return null;
	}

	/*
	 * Parse @param tag declaration
	 */
	private boolean parseParam() {

		// Store current token state
		int start = this.tagSourceStart;
		int end = this.tagSourceEnd;

		try {
			// Push identifier next
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					consumeToken();
					JavadocSingleNameReference argument = new JavadocSingleNameReference(this.scanner.getCurrentIdentifierSource(),
							this.scanner.getCurrentTokenStartPosition(),
							this.scanner.getCurrentTokenEndPosition());
					argument.tagSourceStart = this.tagSourceStart;
					argument.tagSourceEnd = this.tagSourceEnd;
					return pushParamName(argument);
				case TerminalTokens.TokenNameEOF :
					break;
				default :
					start = this.scanner.getCurrentTokenStartPosition();
					end = getEndPosition();
					if (end < start) start = this.tagSourceStart;
					break;
			}
		} catch (InvalidInputException e) {
			end = getEndPosition();
		}

		// Report problem
		this.sourceParser.problemReporter().javadocMissingParamName(start, end);
		return false;
	}

	/*
	 * Parse a qualified name and built a type reference if the syntax is valid.
	 */
	private TypeReference parseQualifiedName(boolean reset) throws InvalidInputException {

		// Reset identifier stack if requested
		if (reset) {
			this.identifierPtr = -1;
			this.identifierLengthPtr = -1;
		}

		// Scan tokens
		nextToken : for (int iToken = 0; ; iToken++) {
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameIdentifier :
					if (((iToken % 2) > 0)) { // identifiers must be odd tokens
						break nextToken;
					}
					pushIdentifier(iToken == 0);
					consumeToken();
					break;

				case TerminalTokens.TokenNameDOT :
					if ((iToken % 2) == 0) { // dots must be even tokens
						throw new InvalidInputException();
					}
					consumeToken();
					break;

				case TerminalTokens.TokenNamevoid :
				case TerminalTokens.TokenNameboolean :
				case TerminalTokens.TokenNamebyte :
				case TerminalTokens.TokenNamechar :
				case TerminalTokens.TokenNamedouble :
				case TerminalTokens.TokenNamefloat :
				case TerminalTokens.TokenNameint :
				case TerminalTokens.TokenNamelong :
				case TerminalTokens.TokenNameshort :
					if (iToken > 0) {
						throw new InvalidInputException();
					}
					pushIdentifier(true);
					consumeToken();
					break nextToken;

				default :
					if (iToken == 0) {
						return null;
					}
					if ((iToken % 2) == 0) { // cannot leave on a dot
						throw new InvalidInputException();
					}
					break nextToken;
			}
		}

		// Build type reference from read tokens
		TypeReference typeRef = null;
		int size = this.identifierLengthStack[this.identifierLengthPtr--];
		if (size == 1) { // Single Type ref
			typeRef = new JavadocSingleTypeReference(
						this.identifierStack[this.identifierPtr],
						this.identifierPositionStack[this.identifierPtr],
						this.tagSourceStart,
						this.tagSourceEnd);
		} else if (size > 1) { // Qualified Type ref
			char[][] tokens = new char[size][];
			System.arraycopy(this.identifierStack, this.identifierPtr - size + 1, tokens, 0, size);
			long[] positions = new long[size];
			System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
			typeRef = new JavadocQualifiedTypeReference(tokens, positions, this.tagSourceStart, this.tagSourceEnd);
		}
		this.identifierPtr -= size;
		return typeRef;
	}

	/*
	 * Parse a reference in @see tag
	 */
	private boolean parseReference() throws InvalidInputException {
		TypeReference typeRef = null;
		Expression reference = null;
		nextToken : while (this.index < this.scanner.eofPosition) {
			int token = readToken();
			switch (token) {
				case TerminalTokens.TokenNameStringLiteral :
					// @see "string"
					int start = this.scanner.getCurrentTokenStartPosition();
					if (typeRef == null) {
						consumeToken();
						if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
							return true;
						}
					}
					this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
					return false;
				case TerminalTokens.TokenNameLESS :
					// @see "<a href="URL#Value">label</a>
					consumeToken();
					start = this.scanner.getCurrentTokenStartPosition();
					if (parseHref()) {
						if (typeRef == null) {
							consumeToken();
							if (this.source[this.index] == '\r' || this.source[this.index] == '\n') {
								return true;
							}
						}
						this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
					}
					return false;
				case TerminalTokens.TokenNameERROR :
					if (this.scanner.currentCharacter == '#') { // @see ...#member
						consumeToken();
						reference = parseMember(typeRef);
						if (reference != null) {
							pushSeeRef(reference);
						}
						return false;
					}
					break nextToken;
				case TerminalTokens.TokenNameIdentifier :
					if (typeRef == null) {
						typeRef = parseQualifiedName(true);
						break;
					}
					break nextToken;
				default :
					break nextToken;
			}
		}
		
		// Verify that we got a reference
		if (reference == null) reference = typeRef;
		if (reference == null) {
			this.sourceParser.problemReporter().javadocMissingSeeReference(this.tagSourceStart, this.tagSourceEnd);
			return false;
		}

		// Verify that line end does not start with an open parenthese (which could be a constructor reference wrongly written...)
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=47215
		int start = this.scanner.getCurrentTokenStartPosition();
		try {
			int token = readToken();
			if (token != TerminalTokens.TokenNameLPAREN) {
				return pushSeeRef(reference);
			}
		} catch (InvalidInputException e) {
			// Do nothing as we report an error after
		}
		this.sourceParser.problemReporter().javadocInvalidSeeReference(start, this.lineEnd);
		return false;
	}
	
	/*
	 * Parse @return tag declaration
	 */
	private boolean parseReturn() {
		if (this.javadoc.returnStatement == null) {
			this.javadoc.returnStatement = new JavadocReturnStatement(this.scanner.getCurrentTokenStartPosition(),
					this.scanner.getCurrentTokenEndPosition(),
					this.scanner.getRawTokenSourceEnd());
			return true;
		} else {
			this.sourceParser.problemReporter().javadocDuplicatedReturnTag(
					this.scanner.getCurrentTokenStartPosition(),
					this.scanner.getCurrentTokenEndPosition());
			return false;
		}
	}

	/*
	 * Parse @see tag declaration
	 */
	private boolean parseSee() {
		int start = this.scanner.currentPosition;
		try {
			return parseReference();
		} catch (InvalidInputException ex) {
				this.sourceParser.problemReporter().javadocInvalidSeeReference(start, getEndPosition());
		}
		return false;
	}
	
	/*
	 * Parse @return tag declaration
	 */
	private boolean parseTag() {
		return true;
	}

	/*
	 * Parse @throws tag declaration
	 */
	private boolean parseThrows() {
		int start = this.scanner.currentPosition;
		try {
			TypeReference typeRef = parseQualifiedName(true);
			if (typeRef == null) {
				this.sourceParser.problemReporter().javadocMissingThrowsClassName(this.tagSourceStart, this.tagSourceEnd);
			} else {
				return pushThrowName(typeRef);
			}
		} catch (InvalidInputException ex) {
			this.sourceParser.problemReporter().javadocInvalidThrowsClass(start, getEndPosition());
		}
		return false;
	}
	
	/*
	 * push the consumeToken on the identifier stack. Increase the total number of identifier in the stack.
	 */
	private void pushIdentifier(boolean newLength) {

		try {
			this.identifierStack[++this.identifierPtr] = this.scanner.getCurrentIdentifierSource();
			this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32)
					+ (this.scanner.currentPosition - 1);
		} catch (IndexOutOfBoundsException e) {
			//---stack reallaocation (identifierPtr is correct)---
			int oldStackLength = this.identifierStack.length;
			char[][] oldStack = this.identifierStack;
			this.identifierStack = new char[oldStackLength + 10][];
			System.arraycopy(oldStack, 0, this.identifierStack, 0, oldStackLength);
			this.identifierStack[this.identifierPtr] = this.scanner.getCurrentTokenSource();
			// identifier position stack
			long[] oldPos = this.identifierPositionStack;
			this.identifierPositionStack = new long[oldStackLength + 10];
			System.arraycopy(oldPos, 0, this.identifierPositionStack, 0, oldStackLength);
			this.identifierPositionStack[this.identifierPtr] = (((long) this.scanner.startPosition) << 32)
					+ (this.scanner.currentPosition - 1);
		}

		if (newLength) {
			try {
				this.identifierLengthStack[++this.identifierLengthPtr] = 1;
			} catch (IndexOutOfBoundsException e) {
				/* ---stack reallocation (identifierLengthPtr is correct)--- */
				int oldStackLength = this.identifierLengthStack.length;
				int oldStack[] = this.identifierLengthStack;
				this.identifierLengthStack = new int[oldStackLength + 10];
				System.arraycopy(oldStack, 0, this.identifierLengthStack, 0, oldStackLength);
				this.identifierLengthStack[this.identifierLengthPtr] = 1;
			}
		} else {
			this.identifierLengthStack[this.identifierLengthPtr]++;
		}
	}

	/*
	 * Add a new obj on top of the ast stack.
	 * If new length is required, then add also a new length in length stack.
	 */
	private void pushOnAstStack(ASTNode node, boolean newLength) {

		if (node == null) {
			this.astLengthStack[++this.astLengthPtr] = 0;
			return;
		}

		try {
			this.astStack[++this.astPtr] = node;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = this.astStack.length;
			ASTNode[] oldStack = this.astStack;
			this.astStack = new ASTNode[oldStackLength + AstStackIncrement];
			System.arraycopy(oldStack, 0, this.astStack, 0, oldStackLength);
			this.astPtr = oldStackLength;
			this.astStack[this.astPtr] = node;
		}

		if (newLength) {
			try {
				this.astLengthStack[++this.astLengthPtr] = 1;
			} catch (IndexOutOfBoundsException e) {
				int oldStackLength = this.astLengthStack.length;
				int[] oldPos = this.astLengthStack;
				this.astLengthStack = new int[oldStackLength + AstStackIncrement];
				System.arraycopy(oldPos, 0, this.astLengthStack, 0, oldStackLength);
				this.astLengthStack[this.astLengthPtr] = 1;
			}
		} else {
			this.astLengthStack[this.astLengthPtr]++;
		}
	}

	/*
	 * Push a param name in ast node stack.
	 */
	private boolean pushParamName(JavadocSingleNameReference arg) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(arg, true);
		} else {
			// Verify that no @throws has been declared before
			for (int i=THROWS_TAG_EXPECTED_ORDER; i<=this.astLengthPtr; i+=ORDERED_TAGS_NUMBER) {
				if (this.astLengthStack[i] != 0) {
					this.sourceParser.problemReporter().javadocUnexpectedTag(arg.tagSourceStart, arg.tagSourceEnd);
					return false;
				}
			}
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push another param name
					pushOnAstStack(arg, false);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push new param name
					pushOnAstStack(arg, true);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	/*
	 * Push a reference statement in ast node stack.
	 */
	private boolean pushSeeRef(Statement statement) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(null, true);
			pushOnAstStack(null, true);
			pushOnAstStack(statement, true);
		} else {
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push empty @throws tag and new @see tag
					pushOnAstStack(null, true);
					pushOnAstStack(statement, true);
					break;
				case THROWS_TAG_EXPECTED_ORDER :
					// previous push was a @throws tag => push new @see tag
					pushOnAstStack(statement, true);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push another @see tag
					pushOnAstStack(statement, false);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	private boolean pushThrowName(TypeReference typeRef) {
		if (this.astLengthPtr == -1) { // First push
			pushOnAstStack(null, true);
			pushOnAstStack(typeRef, true);
		} else {
			switch (this.astLengthPtr % ORDERED_TAGS_NUMBER) {
				case PARAM_TAG_EXPECTED_ORDER :
					// previous push was a @param tag => push new @throws tag
					pushOnAstStack(typeRef, true);
					break;
				case THROWS_TAG_EXPECTED_ORDER :
					// previous push was a @throws tag => push another @throws tag
					pushOnAstStack(typeRef, false);
					break;
				case SEE_TAG_EXPECTED_ORDER :
					// previous push was a @see tag => push empty @param and new @throws tags
					pushOnAstStack(null, true);
					pushOnAstStack(typeRef, true);
					break;
				default:
					return false;
			}
		}
		return true;
	}

	private char readChar() {

		char c = this.source[this.index++];
		if (c == '\\') {
			int c1, c2, c3, c4;
			this.index++;
			while (this.source[this.index] == 'u')
				this.index++;
			if (!(((c1 = Character.getNumericValue(this.source[this.index++])) > 15 || c1 < 0)
					|| ((c2 = Character.getNumericValue(this.source[this.index++])) > 15 || c2 < 0)
					|| ((c3 = Character.getNumericValue(this.source[this.index++])) > 15 || c3 < 0) || ((c4 = Character.getNumericValue(this.source[this.index++])) > 15 || c4 < 0))) {
				c = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			}
		}
		return c;
	}

	/*
	 * Read token only if previous was consumed
	 */
	private int readToken() throws InvalidInputException {
		if (this.currentTokenType < 0) {
			this.currentTokenType = this.scanner.getNextToken();
			if (this.scanner.currentPosition > (this.lineEnd+1) && this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
				while (this.currentTokenType == TerminalTokens.TokenNameMULTIPLY) {
					this.currentTokenType = this.scanner.getNextToken();
				}
			}
			this.index = this.scanner.currentPosition;
		}
		return this.currentTokenType;
	}

	private int readTokenAndConsume() throws InvalidInputException {
		int token = readToken();
		consumeToken();
		return token;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("check javadoc: ").append(this.checkJavadoc).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("javadoc: ").append(this.javadoc).append("\n");	//$NON-NLS-1$ //$NON-NLS-2$
//		buffer.append("scanner: ").append(this.scanner.toString());	//$NON-NLS-1$
		int startPos = this.scanner.currentPosition<this.index ? this.scanner.currentPosition : this.index;
		int endPos = this.scanner.currentPosition<this.index ? this.index : this.scanner.currentPosition;
		if (startPos == this.source.length)
			return "EOF\n\n" + new String(this.source); //$NON-NLS-1$
		if (endPos > this.source.length)
			return "behind the EOF\n\n" + new String(this.source); //$NON-NLS-1$
	
		char front[] = new char[startPos];
		System.arraycopy(this.source, 0, front, 0, startPos);
	
		int middleLength = (endPos - 1) - startPos + 1;
		char middle[];
		if (middleLength > -1) {
			middle = new char[middleLength];
			System.arraycopy(
				this.source, 
				startPos, 
				middle, 
				0, 
				middleLength);
		} else {
			middle = CharOperation.NO_CHAR;
		}
		
		char end[] = new char[this.source.length - (endPos - 1)];
		System.arraycopy(
			this.source, 
			(endPos - 1) + 1, 
			end, 
			0, 
			this.source.length - (endPos - 1) - 1);
		
		buffer.append(front);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("\n===============================\nScanner current position here -->"); //$NON-NLS-1$
		} else {
			buffer.append("\n===============================\nParser index here -->"); //$NON-NLS-1$
		}
		buffer.append(middle);
		if (this.scanner.currentPosition<this.index) {
			buffer.append("<-- Parser index here\n===============================\n"); //$NON-NLS-1$
		} else {
			buffer.append("<-- Scanner current position here\n===============================\n"); //$NON-NLS-1$
		}
		//	+ "" //$NON-NLS-1$
		buffer.append(end);

		return buffer.toString();
	}
	/*
	 * Fill javadoc fields with information in ast nodes stack.
	 */
	private void updateJavadoc() {
		if (this.astLengthPtr == -1) {
			return;
		}

		// Initialize arrays
		int[] sizes = new int[ORDERED_TAGS_NUMBER];
		for (int i=0; i<=this.astLengthPtr; i++) {
			sizes[i%ORDERED_TAGS_NUMBER] += this.astLengthStack[i];
		}
		this.javadoc.references = new Expression[sizes[SEE_TAG_EXPECTED_ORDER]];
		this.javadoc.thrownExceptions = new TypeReference[sizes[THROWS_TAG_EXPECTED_ORDER]];
		this.javadoc.parameters = new JavadocSingleNameReference[sizes[PARAM_TAG_EXPECTED_ORDER]];
		
		// Store nodes in arrays
		while (this.astLengthPtr >= 0) {
			int ptr = this.astLengthPtr % ORDERED_TAGS_NUMBER;
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			if (ptr == SEE_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.javadoc.references[--sizes[ptr]] = (Expression) this.astStack[this.astPtr--];
				}
			}

			// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
			else if (ptr == THROWS_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.javadoc.thrownExceptions[--sizes[ptr]] = (TypeReference) this.astStack[this.astPtr--];
				}
			}

			// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
			else if (ptr == PARAM_TAG_EXPECTED_ORDER) {
				int size = this.astLengthStack[this.astLengthPtr--];
				for (int i=0; i<size; i++) {
					this.javadoc.parameters[--sizes[ptr]] = (JavadocSingleNameReference) this.astStack[this.astPtr--];
				}
			}
		}
	}
}
