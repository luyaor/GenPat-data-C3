////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2004  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.checks.javadoc;

import java.util.Stack;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.ScopeUtils;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * <p>Custom Checkstyle Check to validate Javadoc.
 * The following checks are performed:
 * <ul>
 * <li>Ensures the first sentence ends with proper punctuation (That is
 * a period, question mark, or exclaimation mark).  Javadoc  automatically
 * places the first sentence in the method summary table and index.  With out
 * proper punctuation the Javadoc may be malformed.
 * <li> Check text for Javadoc statements that do not have any description.
 * This includes both completely empty Javadoc, and Javadoc with only
 * tags such as @param and @return.
 * <li>Check text for incomplete html tags.  Verifies that HTML tags have
 * corresponding end tags and issues an UNCLOSED_HTML error if not.
 * An EXTRA_HTML error is issued if an end tag is found without a previous
 * open tag.
 * </ul>
 * <p>These checks were patterned after the checks made by the doclet
 * <code>com.sun.tools.doclets.doccheck.DocCheck</code>
 *
 * @author Chris Stillwell
 * @author Daniel Grenner
 * @version 1.2
 */
public class JavadocStyleCheck
    extends Check
{
    /** Message property key for the Unclosed HTML message. */
    private static final String UNCLOSED_HTML = "javadoc.unclosedhtml";

    /** Message property key for the Extra HTML message. */
    private static final String EXTRA_HTML = "javadoc.extrahtml";

    /** HTML tags that do not require a close tag. */
    private static final String[] SINGLE_TAG =
    {"p", "br", "li", "dt", "dd", "td", "hr", "img", "tr", "th", "td"};

    /** The scope to check. */
    private Scope mScope = Scope.PRIVATE;

    /** Regular expression for matching the end of a sentence. */
    private RE mEndOfSentenceRE;

    /**
     * Indicates if the first sentence should be checked for proper end of
     * sentence punctuation.
     */
    private boolean mCheckFirstSentence = true;

    /**
     * Indicates if the HTML within the comment should be checked.
     */
    private boolean mCheckHtml = true;

    /**
     * Indicates if empty javadoc statements should be checked.
     */
    private boolean mCheckEmptyJavadoc;

    /**
     * The default tokens this Check is used for.
     * @see com.puppycrawl.tools.checkstyle.api.Check#getDefaultTokens()
     */
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.INTERFACE_DEF,
            TokenTypes.CLASS_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.VARIABLE_DEF,
        };
    }

    /**
     * Called to process a token.
     * @see com.puppycrawl.tools.checkstyle.api.Check
     */
    public void visitToken(DetailAST aAST)
    {
        if (!ScopeUtils.inCodeBlock(aAST)) {
            final DetailAST mods =
                aAST.findFirstToken(TokenTypes.MODIFIERS);
            final Scope declaredScope = ScopeUtils.getScopeFromMods(mods);
            final Scope variableScope =
                ScopeUtils.inInterfaceBlock(aAST)
                    ? Scope.PUBLIC
                    : declaredScope;

            if (variableScope.isIn(mScope)) {
                final Scope surroundingScope =
                    ScopeUtils.getSurroundingScope(aAST);

                if ((surroundingScope == null)
                    || surroundingScope.isIn(mScope))
                {
                    final FileContents contents = getFileContents();
                    final TextBlock cmt =
                        contents.getJavadocBefore(aAST.getLineNo());

                    checkComment(cmt);
                }
            }
        }
    }

    /**
     * Performs the various checks agains the Javadoc comment.
     *
     * @param aComment the source lines that make up the Javadoc comment.
     *
     * @see #checkFirstSentence(TextBlock)
     * @see #checkHtml(TextBlock)
     */
    private void checkComment(TextBlock aComment)
    {
        if (aComment == null) {
            return;
        }

        if (mCheckFirstSentence) {
            checkFirstSentence(aComment);
        }

        if (mCheckHtml) {
            checkHtml(aComment);
        }

        if (mCheckEmptyJavadoc) {
            checkEmptyJavadoc(aComment);
        }
    }

    /**
     * Checks that the first sentence ends with proper puctuation.  This method
     * uses a regular expression that checks for the presence of a period,
     * question mark, or exclaimation mark followed either by whitespace, an
     * HTML element, or the end of string. This method ignores {@inheritDoc}
     * comments.
     *
     * @param aComment the source lines that make up the Javadoc comment.
     */
    private void checkFirstSentence(TextBlock aComment)
    {
        final String commentText = getCommentText(aComment.getText());

        if ((commentText.length() != 0)
            && !getEndOfSentenceRE().match(commentText)
            && !"{@inheritDoc}".equals(commentText))
        {
            log(aComment.getStartLineNo(), "javadoc.noperiod");
        }
    }

    /**
     * Checks that the Javadoc is not empty.
     *
     * @param aComment the source lines that make up the Javadoc comment.
     */
    private void checkEmptyJavadoc(TextBlock aComment)
    {
        final String commentText = getCommentText(aComment.getText());

        if (commentText.length() == 0) {
            log(aComment.getStartLineNo(), "javadoc.empty");
        }
    }

    /**
     * Returns the comment text from the Javadoc.
     * @param aComments the lines of Javadoc.
     * @return a comment text String.
     */
    private String getCommentText(String[] aComments)
    {
        final StringBuffer buffer = new StringBuffer();
        boolean foundTag = false;

        for (int i = 0; i < aComments.length; i++) {
            String line = aComments[i];
            final int textStart = findTextStart(line);

            if (textStart != -1) {
                // Look for Javadoc tag that's not an inline tag
                // it can appear within the comment text.
                // Inline tags are denoted by curly braces: {@tag}
                final int ndx = line.indexOf('@');
                if ((ndx != -1)
                    && (ndx == 0 || line.charAt(ndx - 1) != '{'))
                {
                    foundTag = true;
                    line = line.substring(0, ndx);
                }

                buffer.append(line.substring(textStart));
                trimTail(buffer);
                buffer.append('\n');

                if (foundTag) {
                    break;
                }
            }
        }

        return buffer.toString().trim();
    }

    /**
     * Finds the index of the first non-whitespace character ignoring the
     * Javadoc comment start and end strings (&#47** and *&#47) as well as any
     * leading asterisk.
     * @param aLine the Javadoc comment line of text to scan.
     * @return the int index relative to 0 for the start of text
     *         or -1 if not found.
     */
    private int findTextStart(String aLine)
    {
        int textStart = -1;
        for (int i = 0; i < aLine.length(); i++) {
            if (!Character.isWhitespace(aLine.charAt(i))) {
                if (aLine.regionMatches(i, "/**", 0, "/**".length())) {
                    i += 2;
                }
                else if (aLine.regionMatches(i, "*/", 0, 2)) {
                    i++;
                }
                else if (aLine.charAt(i) != '*') {
                    textStart = i;
                    break;
                }
            }
        }
        return textStart;
    }

    /**
     * Trims any trailing whitespace or the end of Javadoc comment string.
     * @param aBuffer the StringBuffer to trim.
     */
    private void trimTail(StringBuffer aBuffer)
    {
        for (int i = aBuffer.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(aBuffer.charAt(i))) {
                aBuffer.deleteCharAt(i);
            }
            else if ((i > 0)
                     && (aBuffer.charAt(i - 1) == '*')
                     && (aBuffer.charAt(i) == '/'))
            {
                aBuffer.deleteCharAt(i);
                aBuffer.deleteCharAt(i - 1);
                i--;
            }
            else {
                break;
            }
        }
    }

    /**
     * Checks the comment for HTML tags that do not have a corresponding close
     * tag or a close tage that has no previous open tag.  This code was
     * primarily copied from the DocCheck checkHtml method.
     *
     * @param aComment the <code>TextBlock</code> which represents
     *                 the Javadoc comment.
     */
    private void checkHtml(TextBlock aComment)
    {
        final int lineno = aComment.getStartLineNo();
        final Stack htmlStack = new Stack();
        final String[] text = aComment.getText();

        TagParser parser = null;
        parser = new TagParser(text, lineno);

        while (parser.hasNextTag()) {
            final HtmlTag tag = parser.nextTag();

            if (tag.isIncompleteTag()) {
                log(tag.getLineno(), "javadoc.incompleteTag",
                    new Object[] {text[tag.getLineno() - lineno]});
                return;
            }
            if (tag.isClosedTag()) {
                //do nothing
                continue;
            }
            if (!tag.isCloseTag()) {
                htmlStack.push(tag);
            }
            else {
                // We have found a close tag.
                if (isExtraHtml(tag.getId(), htmlStack)) {
                    // No corresponding open tag was found on the stack.
                    log(tag.getLineno(),
                        tag.getPosition(),
                        EXTRA_HTML,
                        tag);
                }
                else {
                    // See if there are any unclosed tags that were opened
                    // after this one.
                    checkUnclosedTags(htmlStack, tag.getId());
                }
            }
        }

        // Identify any tags left on the stack.
        String lastFound = ""; // Skip multiples, like <b>...<b>
        for (int i = 0; i < htmlStack.size(); i++) {
            final HtmlTag htag = (HtmlTag) htmlStack.elementAt(i);
            if (!isSingleTag(htag) && !htag.getId().equals(lastFound)) {
                log(htag.getLineno(), htag.getPosition(), UNCLOSED_HTML, htag);
                lastFound = htag.getId();
            }
        }
    }

    /**
     * Checks to see if there are any unclosed tags on the stack.  The token
     * represents a html tag that has been closed and has a corresponding open
     * tag on the stack.  Any tags, except single tags, that were opened
     * (pushed on the stack) after the token are missing a close.
     *
     * @param aHtmlStack the stack of opened HTML tags.
     * @param aToken the current HTML tag name that has been closed.
     */
    private void checkUnclosedTags(Stack aHtmlStack, String aToken)
    {
        final Stack unclosedTags = new Stack();
        HtmlTag lastOpenTag = (HtmlTag) aHtmlStack.pop();
        while (!aToken.equalsIgnoreCase(lastOpenTag.getId())) {
            // Find unclosed elements. Put them on a stack so the
            // output order won't be back-to-front.
            if (isSingleTag(lastOpenTag)) {
                lastOpenTag = (HtmlTag) aHtmlStack.pop();
            }
            else {
                unclosedTags.push(lastOpenTag);
                lastOpenTag = (HtmlTag) aHtmlStack.pop();
            }
        }

        // Output the unterminated tags, if any
        String lastFound = ""; // Skip multiples, like <b>..<b>
        for (int i = 0; i < unclosedTags.size(); i++) {
            lastOpenTag = (HtmlTag) unclosedTags.get(i);
            if (lastOpenTag.getId().equals(lastFound)) {
                continue;
            }
            lastFound = lastOpenTag.getId();
            log(lastOpenTag.getLineno(),
                lastOpenTag.getPosition(),
                UNCLOSED_HTML,
                lastOpenTag);
        }
    }

    /**
     * Determines if the HtmlTag is one which does not require a close tag.
     *
     * @param aTag the HtmlTag to check.
     * @return <code>true</code> if the HtmlTag is a single tag.
     */
    private boolean isSingleTag(HtmlTag aTag)
    {
        boolean isSingleTag = false;
        for (int i = 0; i < SINGLE_TAG.length; i++) {
            // If its a singleton tag (<p>, <br>, etc.), ignore it
            // Can't simply not put them on the stack, since singletons
            // like <dt> and <dd> (unhappily) may either be terminated
            // or not terminated. Both options are legal.
            if (aTag.getId().equalsIgnoreCase(SINGLE_TAG[i])) {
                isSingleTag = true;
            }
        }
        return isSingleTag;
    }

    /**
     * Determines if the given token is an extra HTML tag. This indicates that
     * a close tag was found that does not have a corresponding open tag.
     *
     * @param aToken an HTML tag id for which a close was found.
     * @param aHtmlStack a Stack of previous open HTML tags.
     * @return <code>false</code> if a previous open tag was found
     *         for the token.
     */
    private boolean isExtraHtml(String aToken, Stack aHtmlStack)
    {
        boolean isExtra = true;
        for (int i = 0; i < aHtmlStack.size(); i++) {
            // Loop, looking for tags that are closed.
            // The loop is needed in case there are unclosed
            // tags on the stack. In that case, the stack would
            // not be empty, but this tag would still be extra.
            HtmlTag td = (HtmlTag) aHtmlStack.elementAt(i);
            if (aToken.equalsIgnoreCase(td.getId())) {
                isExtra = false;
                break;
            }
        }

        return isExtra;
    }

    /**
     * Sets the scope to check.
     * @param aFrom string to get the scope from
     */
    public void setScope(String aFrom)
    {
        mScope = Scope.getInstance(aFrom);
    }

    /**
     * Returns a regular expression for matching the end of a sentence.
     *
     * @return a regular expression for matching the end of a sentence.
     */
    private RE getEndOfSentenceRE()
    {
        if (mEndOfSentenceRE == null) {
            try {
                mEndOfSentenceRE = new RE("([.?!][ \t\n\r\f<])|([.?!]$)");
            }
            catch (RESyntaxException e) {
                // This should never occur.
                e.printStackTrace();
            }
        }
        return mEndOfSentenceRE;
    }

    /**
     * Sets the flag that determines if the first sentence is checked for
     * proper end of sentence punctuation.
     * @param aFlag <code>true</code> if the first sentence is to be checked
     */
    public void setCheckFirstSentence(boolean aFlag)
    {
        mCheckFirstSentence = aFlag;
    }

    /**
     * Sets the flag that determines if HTML checking is to be performed.
     * @param aFlag <code>true</code> if HTML checking is to be performed.
     */
    public void setCheckHtml(boolean aFlag)
    {
        mCheckHtml = aFlag;
    }

    /**
     * Sets the flag that determines if empty JavaDoc checking should be done.
     * @param aFlag <code>true</code> if empty JavaDoc checking should be done.
     */
    public void setCheckEmptyJavadoc(boolean aFlag)
    {
        mCheckEmptyJavadoc = aFlag;
    }
}
