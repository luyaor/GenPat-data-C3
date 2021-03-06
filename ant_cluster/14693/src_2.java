/*
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2002 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowlegement may appear in the software itself,
 *  if and wherever such third-party acknowlegements normally appear.
 *
 *  4. The names "The Jakarta Project", "Ant", and "Apache Software
 *  Foundation" must not be used to endorse or promote products derived
 *  from this software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache"
 *  nor may "Apache" appear in their names without prior written
 *  permission of the Apache Group.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.tools.ant.filters;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;

/**
 * Replace tokens with user supplied values
 *
 * Example Usage:
 * =============
 * &lt;filterreader classname="org.apache.tools.ant.filters.ReplaceTokens"&gt;
 *    &lt;param type="tokenchar" name="begintoken" value="#"/&gt;
 *    &lt;param type="tokenchar" name="endtoken" value="#"/&gt;
 *    &lt;param type="token" name="DATE" value="${DATE}"/&gt;
 * &lt;/filterreader&gt;
 *
 * @author <a href="mailto:umagesh@apache.org">Magesh Umasankar</a>
 */
public final class ReplaceTokens
    extends FilterReader
    implements Parameterizable
{
    private static final char DEFAULT_BEGIN_TOKEN = '@';

    private static final char DEFAULT_END_TOKEN = '@';

    private String queuedData = null;

    private Parameter[] parameters;

    private Hashtable hash = new Hashtable();

    private boolean initialized;

    private char beginToken = DEFAULT_BEGIN_TOKEN;

    private char endToken = DEFAULT_END_TOKEN;

    /**
     * Create a new filtered reader.
     *
     * @param in  a Reader object providing the underlying stream.
     */
    public ReplaceTokens(final Reader in) {
        super(in);
    }

    /**
     * Replace tokens with values.
     */
    public final int read() throws IOException {
        if (!initialized) {
            initialize();
            initialized = true;
        }

        if (queuedData != null && queuedData.length() > 0) {
            final int ch = queuedData.charAt(0);
            if (queuedData.length() > 1) {
                queuedData = queuedData.substring(1);
            } else {
                queuedData = null;
            }
            return ch;
        }

        int ch = in.read();
        if (ch == beginToken) {
            final StringBuffer key = new StringBuffer("");
            do  {
                ch = in.read();
                if (ch != -1) {
                    key.append((char) ch);
                } else {
                    break;
                }
            } while (ch != endToken);

            if (ch == -1) {
                queuedData = beginToken + key.toString();
                return read();
            } else {
                key.setLength(key.length() - 1);
                final String replaceWith = (String) hash.get(key.toString());
                if (replaceWith != null) {
                    queuedData = replaceWith;
                    return read();
                } else {
                    queuedData = beginToken + key.toString() + endToken;
                    return read();
                }
            }
        }
        return ch;
    }

    public final int read(final char cbuf[], final int off,
                          final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final int ch = read();
            if (ch == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            cbuf[off + i] = (char) ch;
        }
        return len;
    }

    public final long skip(long n) throws IOException {
        for (long i = 0; i < n; i++) {
            if (in.read() == -1) {
                return i;
            }
        }
        return n;
    }

    /**
     * Set Parameters
     */
    public final void setParameters(final Parameter[] parameters) {
        this.parameters = parameters;
        initialized = false;
    }

    /**
     * Initialize tokens and load the replacee-replacer hashtable.
     */
    private final void initialize() {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] != null) {
                    final String type = parameters[i].getType();
                    if ("tokenchar".equals(type)) {
                        final String name = parameters[i].getName();
                        if ("begintoken".equals(name)) {
                            beginToken = parameters[i].getValue().charAt(0);
                        } else if ("endtoken".equals(name)) {
                            endToken = parameters[i].getValue().charAt(0);
                        }
                    } else if ("token".equals(type)) {
                        final String name = parameters[i].getName();
                        final String value = parameters[i].getValue();
                        hash.put(name, value);
                    }
                }
            }
        }
    }
}
