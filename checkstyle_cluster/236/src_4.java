////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2002  Oliver Burn
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
package com.puppycrawl.tools.checkstyle;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.InvalidObjectException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Represents the configuration that checkstyle uses when checking.
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 **/
public class Configuration
    implements Defn, Serializable
{
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /** the pattern to match against todo lines **/
    private static final String TODO_PATTERN = "TODO:";
    /** the pattern to match against parameter names **/
    private static final String PARAMETER_PATTERN = "^[a-z][a-zA-Z0-9]*$";
    /** the pattern to match against static names **/
    private static final String STATIC_PATTERN = "^[a-z][a-zA-Z0-9]*$";
    /** the pattern to match against constant names **/
    private static final String CONST_PATTERN = "^[A-Z](_?[A-Z0-9]+)*$";
    /** the pattern to match against member names **/
    private static final String MEMBER_PATTERN = "^[a-z][a-zA-Z0-9]*$";
    /** the pattern to match against public member names **/
    private static final String PUBLIC_MEMBER_PATTERN = "^f[A-Z][a-zA-Z0-9]*$";
    /** the pattern to match against type names **/
    private static final String TYPE_PATTERN = "^[A-Z][a-zA-Z0-9]*$";
    /** the pattern to match against method local variables **/
    private static final String LOCAL_VAR_PATTERN = "^[a-z][a-zA-Z0-9]*$";
    /** the pattern to match against method names **/
    private static final String METHOD_PATTERN = "^[a-z][a-zA-Z0-9]*$";
    /** the pattern to exclude from line length checks **/
    private static final String IGNORE_LINE_LENGTH_PATTERN = "^$";
    /** the maximum line length **/
    private static final int MAX_LINE_LENGTH = 80;
    /** the maximum method length **/
    private static final int MAX_METHOD_LENGTH = 150;
    /** the maximum constructor length **/
    private static final int MAX_CONSTRUCTOR_LENGTH = 150;
    /** the maximum file length **/
    private static final int MAX_FILE_LENGTH = 2000;
    /** the set of illegal imports (comma separated package prefixes) **/
    private static final String ILLEGAL_IMPORTS = "sun";
    /** the set of illegal instantiations (comma separated class names) **/
    private static final String ILLEGAL_INSTANTIATIONS = "";
    /** the number of spaces that are represented by one tab character **/
    private static final int TAB_WIDTH = 8;

    ////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////

    /** pattern to match todo lines **/
    private String mTodoPat;
    /** regexp to match todo lines **/
    private transient RE mTodoRegexp;

    /** pattern to match parameters **/
    private String mParamPat;
    /** regexp to match parameters **/
    private transient RE mParamRegexp;

    /** pattern to match static variables **/
    private String mStaticPat;
    /** regexp to match static variables **/
    private transient RE mStaticRegexp;

    /** pattern to match static final variables **/
    private String mStaticFinalPat;
    /** regexp to match static final variables **/
    private transient RE mStaticFinalRegexp;

    /** pattern to match member variables **/
    private String mMemberPat;
    /** regexp to match member variables **/
    private transient RE mMemberRegexp;

    /** pattern to match public member variables **/
    private String mPublicMemberPat;
    /** regexp to match public member variables **/
    private transient RE mPublicMemberRegexp;

    /** pattern to match type names **/
    private String mTypePat;
    /** regexp to match type names **/
    private transient RE mTypeRegexp;

    /** pattern to match local variables **/
    private String mLocalVarPat;
    /** regexp to match local variables **/
    private transient RE mLocalVarRegexp;

    /** pattern to match method names **/
    private String mMethodPat;
    /** regexp to match method names **/
    private transient RE mMethodRegexp;

    /** the maximum line length **/
    private int mMaxLineLength = MAX_LINE_LENGTH;
    /** the maximum method length **/
    private int mMaxMethodLength = MAX_METHOD_LENGTH;
    /** the maximum constructor length **/
    private int mMaxConstructorLength = MAX_CONSTRUCTOR_LENGTH;
    /** the maximum file length **/
    private int mMaxFileLength = MAX_FILE_LENGTH;
    /** the distance between tab stops **/
    private int mTabWidth = TAB_WIDTH;
    /** visibility scope where Javadoc is checked **/
    private Scope mJavadocScope = Scope.PRIVATE;
    /** illegal imports **/
    private final HashSet mIllegalImports = new HashSet();
    /** illegal instantiations **/
    private final HashSet mIllegalInstantiations = new HashSet();
    /** name of the cache file **/
    private String mCacheFile = null;
    /** pattern to exclude from line lengh checking **/
    private String mIgnoreLineLengthPat;
    /** regexp to exclude from line length checking **/
    private transient RE mIgnoreLineLengthRegexp;

    /** the header lines to check for **/
    private String[] mHeaderLines = {};
    /** line numbers to ignore in header **/
    private TreeSet mHeaderIgnoreLineNo = new TreeSet();

    /** where to place left curlies on methods **/
    private LeftCurlyOption mLCurlyMethod = LeftCurlyOption.EOL;
    /** where to place left curlies on types **/
    private LeftCurlyOption mLCurlyType = LeftCurlyOption.EOL;
    /** where to place left curlies on others **/
    private LeftCurlyOption mLCurlyOther = LeftCurlyOption.EOL;
    /** where to place right curlies  **/
    private RightCurlyOption mRCurly = RightCurlyOption.SAME;
    /** how to process try blocks **/
    private BlockOption mTryBlock = BlockOption.STMT;
    /** how to process catch blocks **/
    private BlockOption mCatchBlock = BlockOption.TEXT;
    /** how to process finally blocks **/
    private BlockOption mFinallyBlock = BlockOption.STMT;

    /** how to pad parenthesis **/
    private PadOption mParenPadOption = PadOption.NOSPACE;

    /** set of boolean flags **/
    private final Set mBooleanFlags = new HashSet();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new <code>Configuration</code> instance.
     *
     * @param aProps where to extract configuration parameters from
     * @param aLog where to log errors to
     * @throws RESyntaxException if an error occurs
     * @throws FileNotFoundException if an error occurs
     * @throws IOException if an error occurs
     */
    public Configuration(Properties aProps, PrintStream aLog)
        throws RESyntaxException, FileNotFoundException, IOException
    {
        setTodoPat(aProps.getProperty(TODO_PATTERN_PROP, TODO_PATTERN));
        setParamPat(aProps.getProperty(PARAMETER_PATTERN_PROP,
                                       PARAMETER_PATTERN));
        setStaticPat(aProps.getProperty(STATIC_PATTERN_PROP, STATIC_PATTERN));
        setStaticFinalPat(aProps.getProperty(CONST_PATTERN_PROP,
                                             CONST_PATTERN));
        setMemberPat(aProps.getProperty(MEMBER_PATTERN_PROP, MEMBER_PATTERN));
        setPublicMemberPat(aProps.getProperty(PUBLIC_MEMBER_PATTERN_PROP,
                                              PUBLIC_MEMBER_PATTERN));
        setTypePat(aProps.getProperty(TYPE_PATTERN_PROP, TYPE_PATTERN));
        setLocalVarPat(aProps.getProperty(LOCAL_VAR_PATTERN_PROP,
                                          LOCAL_VAR_PATTERN));
        setMethodPat(aProps.getProperty(METHOD_PATTERN_PROP, METHOD_PATTERN));
        setIgnoreLineLengthPat(aProps.getProperty(
            IGNORE_LINE_LENGTH_PATTERN_PROP, IGNORE_LINE_LENGTH_PATTERN));
        setMaxLineLength(getIntProperty(
            aProps, aLog, MAX_LINE_LENGTH_PROP, MAX_LINE_LENGTH));
        setMaxMethodLength(getIntProperty(
            aProps, aLog, MAX_METHOD_LENGTH_PROP, MAX_METHOD_LENGTH));
        setMaxConstructorLength(getIntProperty(
            aProps, aLog, MAX_CONSTRUCTOR_LENGTH_PROP, MAX_CONSTRUCTOR_LENGTH));
        setMaxFileLength(getIntProperty(
            aProps, aLog, MAX_FILE_LENGTH_PROP, MAX_FILE_LENGTH));

        setBooleanFlag(aProps, ALLOW_TABS_PROP);
        setTabWidth(getIntProperty(aProps, aLog, TAB_WIDTH_PROP, TAB_WIDTH));
        setBooleanFlag(aProps, ALLOW_PROTECTED_PROP);
        setBooleanFlag(aProps, ALLOW_PACKAGE_PROP);
        setBooleanFlag(aProps, ALLOW_NO_AUTHOR_PROP);
        setJavadocScope(
            Scope.getInstance(aProps.getProperty(JAVADOC_CHECKSCOPE_PROP,
                                                 Scope.PRIVATE.getName())));
        setBooleanFlag(aProps, REQUIRE_PACKAGE_HTML_PROP);
        setBooleanFlag(aProps, IGNORE_IMPORTS_PROP);
        setIllegalImports(
            aProps.getProperty(ILLEGAL_IMPORTS_PROP, ILLEGAL_IMPORTS));
        setIllegalInstantiations(aProps.getProperty(ILLEGAL_INSTANTIATIONS_PROP,
                                                   ILLEGAL_INSTANTIATIONS));
        setBooleanFlag(aProps, IGNORE_WHITESPACE_PROP);
        setBooleanFlag(aProps, IGNORE_CAST_WHITESPACE_PROP);
        setBooleanFlag(aProps, IGNORE_OP_WRAP_PROP);
        setBooleanFlag(aProps, IGNORE_BRACES_PROP);
        setBooleanFlag(aProps, IGNORE_LONG_ELL_PROP);
        setBooleanFlag(aProps, IGNORE_PUBLIC_IN_INTERFACE_PROP);
        setCacheFile(aProps.getProperty(CACHE_FILE_PROP));
        setBooleanFlag(aProps, IGNORE_IMPORT_LENGTH_PROP);
        setHeaderIgnoreLines(aProps.getProperty(HEADER_IGNORE_LINE_PROP));
        setBooleanFlag(aProps, HEADER_LINES_REGEXP_PROP);

        final String fname = aProps.getProperty(HEADER_FILE_PROP);
        if (fname != null) {
            setHeaderFile(fname);
        }

        setLCurlyMethod(getLeftCurlyOptionProperty(
                            aProps, LCURLY_METHOD_PROP,
                            LeftCurlyOption.EOL, aLog));
        setLCurlyType(getLeftCurlyOptionProperty(
                          aProps, LCURLY_TYPE_PROP,
                          LeftCurlyOption.EOL, aLog));
        setLCurlyOther(getLeftCurlyOptionProperty(
                           aProps, LCURLY_OTHER_PROP,
                           LeftCurlyOption.EOL, aLog));
        setRCurly(getRightCurlyOptionProperty(
                      aProps, RCURLY_PROP, RightCurlyOption.SAME, aLog));
        setTryBlock(
            getBlockOptionProperty(
                aProps, TRY_BLOCK_PROP, BlockOption.TEXT, aLog));
        setCatchBlock(
            getBlockOptionProperty(
                aProps, CATCH_BLOCK_PROP, BlockOption.TEXT, aLog));
        setFinallyBlock(
            getBlockOptionProperty(
                aProps, FINALLY_BLOCK_PROP, BlockOption.TEXT, aLog));
        setParenPadOption(getPadOptionProperty(aProps,
                                               PAREN_PAD_PROP,
                                               PadOption.NOSPACE,
                                               aLog));
    }

    /**
     * Creates a new <code>Configuration</code> instance.
     *
     * @throws IllegalStateException if an error occurs
     */
    public Configuration()
        throws IllegalStateException
    {
        setIllegalImports(ILLEGAL_IMPORTS);
        setIllegalInstantiations(ILLEGAL_INSTANTIATIONS);
        try {
            setTodoPat(TODO_PATTERN);
            setParamPat(PARAMETER_PATTERN);
            setStaticPat(STATIC_PATTERN);
            setStaticFinalPat(CONST_PATTERN);
            setMemberPat(MEMBER_PATTERN);
            setPublicMemberPat(PUBLIC_MEMBER_PATTERN);
            setTypePat(TYPE_PATTERN);
            setLocalVarPat(LOCAL_VAR_PATTERN);
            setMethodPat(METHOD_PATTERN);
            setIgnoreLineLengthPat(IGNORE_LINE_LENGTH_PATTERN);
        }
        catch (RESyntaxException ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex.getMessage());
        }
    }

    /**
     * Extend default deserialization to initialize the RE member variables.
     *
     * @param aStream the ObjectInputStream that contains the serialized data
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object
     *     could not be found
     */
    private void readObject(ObjectInputStream aStream)
        throws IOException, ClassNotFoundException
    {
        // initialize the serialized fields
        aStream.defaultReadObject();

        // initialize the transient fields
        try {
            setTodoPat(getTodoPat());
            setParamPat(getParamPat());
            setStaticPat(getStaticPat());
            setStaticFinalPat(getStaticFinalPat());
            setMemberPat(getMemberPat());
            setPublicMemberPat(getPublicMemberPat());
            setTypePat(getTypePat());
            setLocalVarPat(getLocalVarPat());
            setMethodPat(getMethodPat());
            setIgnoreLineLengthPat(getIgnoreLineLengthPat());
        }
        catch (RESyntaxException ex) {
            // This should never happen, as the serialized regexp patterns
            // somehow must have passed a setPattern() method.
            throw new InvalidObjectException(
                "invalid regular expression syntax");
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////////////////////////

    /** @return pattern to match todo lines **/
    public String getTodoPat()
    {
        return mTodoPat;
    }

    /** @return regexp to match todo lines **/
    public RE getTodoRegexp()
    {
        return mTodoRegexp;
    }

    /** @return pattern to match parameters **/
    public String getParamPat()
    {
        return mParamPat;
    }

    /** @return regexp to match parameters **/
    public RE getParamRegexp()
    {
        return mParamRegexp;
    }

    /** @return pattern to match static variables **/
    public String getStaticPat()
    {
        return mStaticPat;
    }

    /** @return regexp to match static variables **/
    public RE getStaticRegexp()
    {
        return mStaticRegexp;
    }

    /** @return pattern to match static final variables **/
    public String getStaticFinalPat()
    {
        return mStaticFinalPat;
    }

    /** @return regexp to match static final variables **/
    public RE getStaticFinalRegexp()
    {
        return mStaticFinalRegexp;
    }

    /** @return pattern to match member variables **/
    public String getMemberPat()
    {
        return mMemberPat;
    }

    /** @return regexp to match member variables **/
    public RE getMemberRegexp()
    {
        return mMemberRegexp;
    }

    /** @return pattern to match public member variables **/
    public String getPublicMemberPat()
    {
        return mPublicMemberPat;
    }

    /** @return regexp to match public member variables **/
    public RE getPublicMemberRegexp()
    {
        return mPublicMemberRegexp;
    }

    /** @return pattern to match type names **/
    public String getTypePat()
    {
        return mTypePat;
    }

    /** @return regexp to match type names **/
    public RE getTypeRegexp()
    {
        return mTypeRegexp;
    }

    /** @return pattern to match local variables **/
    public String getLocalVarPat()
    {
        return mLocalVarPat;
    }

    /** @return regexp to match local variables **/
    public RE getLocalVarRegexp()
    {
        return mLocalVarRegexp;
    }

    /** @return pattern to match method names **/
    public String getMethodPat()
    {
        return mMethodPat;
    }

    /** @return regexp to match method names **/
    public RE getMethodRegexp()
    {
        return mMethodRegexp;
    }

    /** @return the maximum line length **/
    public int getMaxLineLength()
    {
        return mMaxLineLength;
    }

    /** @return the maximum method length **/
    public int getMaxMethodLength()
    {
        return mMaxMethodLength;
    }

    /** @return the maximum constructor length **/
    public int getMaxConstructorLength()
    {
        return mMaxConstructorLength;
    }

    /** @return the maximum file length **/
    public int getMaxFileLength()
    {
        return mMaxFileLength;
    }

    /** @return whether to allow tabs **/
    public boolean isAllowTabs()
    {
        return isFlagSet(ALLOW_TABS_PROP);
    }

    /** @return distance between tab stops */
    public int getTabWidth()
    {
        return mTabWidth;
    }

    /** @return whether to allow protected data **/
    public boolean isAllowProtected()
    {
        return isFlagSet(ALLOW_PROTECTED_PROP);
    }

    /** @return whether to allow package data **/
    public boolean isAllowPackage()
    {
        return isFlagSet(ALLOW_PACKAGE_PROP);
    }

    /** @return whether to allow having no author tag **/
    public boolean isAllowNoAuthor()
    {
        return isFlagSet(ALLOW_NO_AUTHOR_PROP);
    }

    /** @return visibility scope where Javadoc is checked **/
    public Scope getJavadocScope()
    {
        return mJavadocScope;
    }

    /** @return whether javadoc package documentation is required */
    public boolean isRequirePackageHtml()
    {
        return isFlagSet(REQUIRE_PACKAGE_HTML_PROP);
    }

    /** @return whether to process imports **/
    public boolean isIgnoreImports()
    {
        return isFlagSet(IGNORE_IMPORTS_PROP);
    }

    /** @return Set of pkg prefixes that are illegal in import statements */
    public Set getIllegalImports()
    {
        return mIllegalImports;
    }

    /** @return Set of classes where calling a constructor is illegal */
    public Set getIllegalInstantiations()
    {
        return mIllegalInstantiations;
    }

    /** @return pattern to exclude from line lengh checking **/
    public String getIgnoreLineLengthPat()
    {
        return mIgnoreLineLengthPat;
    }

    /** @return regexp to exclude from line lengh checking **/
    public RE getIgnoreLineLengthRegexp()
    {
        return mIgnoreLineLengthRegexp;
    }

    /** @return whether to ignore checks for whitespace **/
    public boolean isIgnoreWhitespace()
    {
        return isFlagSet(IGNORE_WHITESPACE_PROP);
    }

    /** @return whether to ignore checks for whitespace after casts **/
    public boolean isIgnoreCastWhitespace()
    {
        return isFlagSet(IGNORE_CAST_WHITESPACE_PROP);
    }

    /** @return whether to ignore checks for operator wrapping **/
    public boolean isIgnoreOpWrap()
    {
        return isFlagSet(IGNORE_OP_WRAP_PROP);
    }

    /** @return whether to ignore checks for braces **/
    public boolean isIgnoreBraces()
    {
        return isFlagSet(IGNORE_BRACES_PROP);
    }

    /** @return whether to ignore long 'L' **/
    public boolean isIgnoreLongEll()
    {
        return isFlagSet(IGNORE_LONG_ELL_PROP);
    }

    /** @return whether to ignore 'public' keyword in interface definitions **/
    public boolean isIgnorePublicInInterface()
    {
        return isFlagSet(IGNORE_PUBLIC_IN_INTERFACE_PROP);
    }

    /** @return whether to ignore max line length for import statements **/
    public boolean isIgnoreImportLength()
    {
        return isFlagSet(IGNORE_IMPORT_LENGTH_PROP);
    }

    /** @return the header lines to check for **/
    public String[] getHeaderLines()
    {
        return mHeaderLines;
    }


    /** @return if lines in header file are regular expressions */
    public boolean getHeaderLinesRegexp()
    {
        return isFlagSet(HEADER_LINES_REGEXP_PROP);
    }

    /**
     * @param aLineNo a line number
     * @return if <code>aLineNo</code> is one of the ignored header lines.
     */
    public boolean isHeaderIgnoreLineNo(int aLineNo)
    {
        return mHeaderIgnoreLineNo.contains(new Integer(aLineNo));
    }

    /** @return the name of the cache file **/
    public String getCacheFile()
    {
        return mCacheFile;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param aTodoPat pattern to match todos
     * @throws RESyntaxException if an error occurs
     */
    public void setTodoPat(String aTodoPat)
        throws RESyntaxException
    {
        mTodoRegexp = Utils.getRE(aTodoPat);
        mTodoPat = aTodoPat;
    }

    /**
     * @param aParamPat pattern to match parameters
     * @throws RESyntaxException if an error occurs
     */
    public void setParamPat(String aParamPat)
        throws RESyntaxException
    {
        mParamRegexp = Utils.getRE(aParamPat);
        mParamPat = aParamPat;
    }

    /**
     * @param aStaticPat pattern to match static variables
     * @throws RESyntaxException if an error occurs
     */
    public void setStaticPat(String aStaticPat)
        throws RESyntaxException
    {
        mStaticRegexp = Utils.getRE(aStaticPat);
        mStaticPat = aStaticPat;
    }

    /**
     * @param aStaticFinalPat pattern to match static final variables
     * @throws RESyntaxException if an error occurs
     */
    public void setStaticFinalPat(String aStaticFinalPat)
        throws RESyntaxException
    {
        mStaticFinalRegexp = Utils.getRE(aStaticFinalPat);
        mStaticFinalPat = aStaticFinalPat;
    }

    /**
     * @param aMemberPat pattern to match member variables
     * @throws RESyntaxException if an error occurs
     */
    public void setMemberPat(String aMemberPat)
        throws RESyntaxException
    {
        mMemberRegexp = Utils.getRE(aMemberPat);
        mMemberPat = aMemberPat;
    }

    /**
     * @param aPublicMemberPat pattern to match public member variables
     * @throws RESyntaxException if an error occurs
     */
    public void setPublicMemberPat(String aPublicMemberPat)
        throws RESyntaxException
    {
        mPublicMemberRegexp = Utils.getRE(aPublicMemberPat);
        mPublicMemberPat = aPublicMemberPat;
    }

    /**
     * @param aTypePat pattern to match type names
     * @throws RESyntaxException if an error occurs
     */
    public void setTypePat(String aTypePat)
        throws RESyntaxException
    {
        mTypeRegexp = Utils.getRE(aTypePat);
        mTypePat = aTypePat;
    }

    /**
     * @param aLocalVarPat pattern to match member variables
     * @throws RESyntaxException if an error occurs
     */
    public void setLocalVarPat(String aLocalVarPat)
        throws RESyntaxException
    {
        mLocalVarRegexp = Utils.getRE(aLocalVarPat);
        mLocalVarPat = aLocalVarPat;
    }

    /**
     * @param aMethodPat pattern to match method names
     * @throws RESyntaxException if an error occurs
     */
    public void setMethodPat(String aMethodPat)
        throws RESyntaxException
    {
        mMethodRegexp = Utils.getRE(aMethodPat);
        mMethodPat = aMethodPat;
    }

    /**
     * @param aIgnoreLineLengthPat pattern to exclude from line legth checking
     * @throws RESyntaxException if an error occurs
     */
    public void setIgnoreLineLengthPat(String aIgnoreLineLengthPat)
        throws RESyntaxException
    {
        mIgnoreLineLengthRegexp = Utils.getRE(aIgnoreLineLengthPat);
        mIgnoreLineLengthPat = aIgnoreLineLengthPat;
    }

    /**
     * @param aMaxLineLength the maximum line length
     */
    public void setMaxLineLength(int aMaxLineLength)
    {
        mMaxLineLength = aMaxLineLength;
    }

    /**
     * @param aMaxMethodLength the maximum method length
     */
    public void setMaxMethodLength(int aMaxMethodLength)
    {
        mMaxMethodLength = aMaxMethodLength;
    }

    /**
     * @param aMaxConstructorLength the maximum constructor length
     */
    public void setMaxConstructorLength(int aMaxConstructorLength)
    {
        mMaxConstructorLength = aMaxConstructorLength;
    }

    /**
     * @param aMaxFileLength the maximum file length
     */
    public void setMaxFileLength(int aMaxFileLength)
    {
        mMaxFileLength = aMaxFileLength;
    }

    /**
     * @param aPkgPrefixList comma separated list of package prefixes
     */
    public void setIllegalImports(String aPkgPrefixList)
    {
        mIllegalImports.clear();
        final StringTokenizer st = new StringTokenizer(aPkgPrefixList, ",");
        while (st.hasMoreTokens()) {
            mIllegalImports.add(st.nextToken());
        }
    }

    /**
     * @param aClassList comma separated list of fully qualified class names
     */
    public void setIllegalInstantiations(String aClassList)
    {
        mIllegalInstantiations.clear();
        final StringTokenizer st = new StringTokenizer(aClassList, ",");
        while (st.hasMoreTokens()) {
            mIllegalInstantiations.add(st.nextToken());
        }
    }

    /** @param aTabWidth distance between tab stops */
    public void setTabWidth(int aTabWidth)
    {
        mTabWidth = aTabWidth;
    }

    /**
     * @param aJavadocScope visibility scope where Javadoc is checked
     */
    public void setJavadocScope(Scope aJavadocScope)
    {
        mJavadocScope = aJavadocScope;
    }

    /**
     * Set the boolean flag.
     * @param aName name of the flag. Should be defined in Defn.
     * @param aTo the value to set
     */
    public void setBooleanFlag(String aName, boolean aTo)
    {
        if (aTo) {
            mBooleanFlags.add(aName);
        }
        else {
            mBooleanFlags.remove(aName);
        }
    }

    /**
     * @param aFileName the header lines to check for
     * @throws FileNotFoundException if an error occurs
     * @throws IOException if an error occurs
     */
    public void setHeaderFile(String aFileName)
        throws FileNotFoundException, IOException
    {
        final LineNumberReader lnr =
            new LineNumberReader(new FileReader(aFileName));
        final ArrayList lines = new ArrayList();
        while (true) {
            final String l = lnr.readLine();
            if (l == null) {
                break;
            }
            lines.add(l);
        }
        mHeaderLines = (String[]) lines.toArray(new String[0]);
    }

    /**
     * @param aList comma separated list of line numbers to ignore in header.
     */
    public void setHeaderIgnoreLines(String aList)
    {
        mHeaderIgnoreLineNo.clear();

        if (aList == null) {
            return;
        }

        final StringTokenizer tokens = new StringTokenizer(aList, ",");
        while (tokens.hasMoreTokens()) {
            final String ignoreLine = tokens.nextToken();
            mHeaderIgnoreLineNo.add(new Integer(ignoreLine));
        }
    }

    /**
     * @param aCacheFile name of cache file
     */
    public void setCacheFile(String aCacheFile)
    {
        mCacheFile = aCacheFile;
    }

    /** @return the left curly placement option for methods **/
    public LeftCurlyOption getLCurlyMethod()
    {
        return mLCurlyMethod;
    }

    /** @param aTo set the left curly placement option for methods **/
    public void setLCurlyMethod(LeftCurlyOption aTo)
    {
        mLCurlyMethod = aTo;
    }

    /** @return the left curly placement option for types **/
    public LeftCurlyOption getLCurlyType()
    {
        return mLCurlyType;
    }

    /** @param aTo set the left curly placement option for types **/
    public void setLCurlyType(LeftCurlyOption aTo)
    {
        mLCurlyType = aTo;
    }

    /** @return the left curly placement option for others **/
    public LeftCurlyOption getLCurlyOther()
    {
        return mLCurlyOther;
    }

    /** @param aTo set the left curly placement option for others **/
    public void setLCurlyOther(LeftCurlyOption aTo)
    {
        mLCurlyOther = aTo;
    }

    /** @return the right curly placement option **/
    public RightCurlyOption getRCurly()
    {
        return mRCurly;
    }

    /** @param aTo set the right curly placement option **/
    public void setRCurly(RightCurlyOption aTo)
    {
        mRCurly = aTo;
    }

    /** @return the try block option **/
    public BlockOption getTryBlock()
    {
        return mTryBlock;
    }

    /** @param aTo set the try block option **/
    public void setTryBlock(BlockOption aTo)
    {
        mTryBlock = aTo;
    }

    /** @return the catch block option **/
    public BlockOption getCatchBlock()
    {
        return mCatchBlock;
    }

    /** @param aTo set the catch block option **/
    public void setCatchBlock(BlockOption aTo)
    {
        mCatchBlock = aTo;
    }

    /** @return the finally block option **/
    public BlockOption getFinallyBlock()
    {
        return mFinallyBlock;
    }

    /** @param aTo set the finally block option **/
    public void setFinallyBlock(BlockOption aTo)
    {
        mFinallyBlock = aTo;
    }

    /** @return the parenthesis padding option **/
    public PadOption getParenPadOption()
    {
        return mParenPadOption;
    }

    /** @param aTo set the parenthesis option **/
    public void setParenPadOption(PadOption aTo)
    {
        mParenPadOption = aTo;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param aProps the properties set to use
     * @param aLog where to log errors to
     * @param aName the name of the property to parse
     * @param aDefault the default value to use.
     *
     * @return the value of an integer property. If the property is not defined
     *    or cannot be parsed, then a default value is returned.
     */
    private static int getIntProperty(Properties aProps, PrintStream aLog,
                                      String aName, int aDefault)
    {
        int retVal = aDefault;
        final String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            try {
                retVal = Integer.parseInt(strRep);
            }
            catch (NumberFormatException nfe) {
                aLog.println("Unable to parse " + aName
                             + " property with value " + strRep
                             + ", defaulting to " + aDefault + ".");
            }
        }
        return retVal;
    }

    /**
     * @param aProps the properties set to use
     * @param aLog where to log errors to
     * @param aName the name of the property to parse
     * @param aDefault the default value to use.
     *
     * @return the value of a LeftCurlyOption property. If the property is not
     *    defined or cannot be decoded, then a default value is returned.
     */
    private static LeftCurlyOption getLeftCurlyOptionProperty(
        Properties aProps,
        String aName,
        LeftCurlyOption aDefault,
        PrintStream aLog)
    {
        LeftCurlyOption retVal = aDefault;
        final String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            retVal = LeftCurlyOption.decode(strRep);
            if (retVal == null) {
                aLog.println("Unable to parse " + aName
                             + " property with value " + strRep
                             + ", defaulting to " + aDefault + ".");
            }
        }
        return retVal;
    }

    /**
     * @param aProps the properties set to use
     * @param aLog where to log errors to
     * @param aName the name of the property to parse
     * @param aDefault the default value to use.
     *
     * @return the value of a RightCurlyOption property. If the property is not
     *    defined or cannot be decoded, then a default value is returned.
     */
    private static RightCurlyOption getRightCurlyOptionProperty(
        Properties aProps,
        String aName,
        RightCurlyOption aDefault,
        PrintStream aLog)
    {
        RightCurlyOption retVal = aDefault;
        final String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            retVal = RightCurlyOption.decode(strRep);
            if (retVal == null) {
                aLog.println("Unable to parse " + aName
                             + " property with value " + strRep
                             + ", defaulting to " + aDefault + ".");
            }
        }
        return retVal;
    }

    /**
     * @param aProps the properties set to use
     * @param aLog where to log errors to
     * @param aName the name of the property to parse
     * @param aDefault the default value to use.
     *
     * @return the value of a BlockOption property. If the property is not
     *    defined or cannot be decoded, then a default value is returned.
     */
    private static BlockOption getBlockOptionProperty(Properties aProps,
                                                      String aName,
                                                      BlockOption aDefault,
                                                      PrintStream aLog)
    {
        BlockOption retVal = aDefault;
        final String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            retVal = BlockOption.decode(strRep);
            if (retVal == null) {
                aLog.println("Unable to parse " + aName
                             + " property with value " + strRep
                             + ", defaulting to " + aDefault + ".");
            }
        }
        return retVal;
    }

    /**
     * @param aProps the properties set to use
     * @param aLog where to log errors to
     * @param aName the name of the property to parse
     * @param aDefault the default value to use.
     *
     * @return the value of a PadOption property. If the property is not
     *    defined or cannot be decoded, then a default value is returned.
     */
    private static PadOption getPadOptionProperty(
        Properties aProps,
        String aName,
        PadOption aDefault,
        PrintStream aLog)
    {
        PadOption retVal = aDefault;
        final String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            retVal = PadOption.decode(strRep);
            if (retVal == null) {
                aLog.println("Unable to parse " + aName
                             + " property with value " + strRep
                             + ", defaulting to " + aDefault + ".");
            }
        }
        return retVal;
    }

    /**
     * @param aName name of flag to return
     * @return return whether a specified flag is set
     */
    private boolean isFlagSet(String aName)
    {
        return mBooleanFlags.contains(aName);
    }

    /**
     * Set a boolean flag from a property set.
     * @param aProps the properties set to extract flag from
     * @param aName name of the flag to extract
     */
    private void setBooleanFlag(Properties aProps, String aName)
    {
        String strRep = aProps.getProperty(aName);
        if (strRep != null) {
            strRep = strRep.toLowerCase().trim();
            if (strRep.equals("true") || strRep.equals("yes")
                || strRep.equals("on"))
            {
                setBooleanFlag(aName, true);
            }
        }
    }
}
