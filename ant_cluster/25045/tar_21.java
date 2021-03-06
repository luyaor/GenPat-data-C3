/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.tools.ant.util.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.myrmidon.api.TaskException;

/**
 * Regular expression implementation using the JDK 1.4 regular expression
 * package
 *
 * @author Matthew Inger <a href="mailto:mattinger@mindless.com">
 *      mattinger@mindless.com</a>
 */
public class Jdk14RegexpRegexp extends Jdk14RegexpMatcher implements Regexp
{

    public Jdk14RegexpRegexp()
    {
        super();
    }

    public String substitute( String input, String argument, int options )
        throws TaskException
    {
        // translate \1 to $(1) so that the Matcher will work
        StringBuffer subst = new StringBuffer();
        for( int i = 0; i < argument.length(); i++ )
        {
            char c = argument.charAt( i );
            if( c == '\\' )
            {
                if( ++i < argument.length() )
                {
                    c = argument.charAt( i );
                    int value = Character.digit( c, 10 );
                    if( value > -1 )
                    {
                        subst.append( "$" ).append( value );
                    }
                    else
                    {
                        subst.append( c );
                    }
                }
                else
                {
                    // XXX - should throw an exception instead?
                    subst.append( '\\' );
                }
            }
            else
            {
                subst.append( c );
            }
        }
        argument = subst.toString();

        int sOptions = getSubsOptions( options );
        Pattern p = getCompiledPattern( options );
        StringBuffer sb = new StringBuffer();

        Matcher m = p.matcher( input );
        if( RegexpUtil.hasFlag( sOptions, REPLACE_ALL ) )
        {
            sb.append( m.replaceAll( argument ) );
        }
        else
        {
            boolean res = m.find();
            if( res )
            {
                m.appendReplacement( sb, argument );
                m.appendTail( sb );
            }
            else
            {
                sb.append( input );
            }
        }

        return sb.toString();
    }

    protected int getSubsOptions( int options )
    {
        int subsOptions = REPLACE_FIRST;
        if( RegexpUtil.hasFlag( options, REPLACE_ALL ) )
            subsOptions = REPLACE_ALL;
        return subsOptions;
    }
}
