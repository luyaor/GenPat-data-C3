/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.ant.project;

import org.apache.log.LogEntry;
import org.apache.log.LogTarget;

/**
 * Adapter between Avalon LogKit and Project listener interfaces.
 * 
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 */
public class LogTargetToListenerAdapter
    implements LogTarget
{
    protected final ProjectListener    m_listener;

    /**
     * Constructor taking listener to convert to.
     *
     * @param listener the ProjectListener
     */
    public LogTargetToListenerAdapter( final ProjectListener listener )
    {
        m_listener = listener;
    }

    /**
     * Process a log entry.
     *
     * @param entry the entry
     */
    public void processEntry( final LogEntry entry )
    {
        if( null == entry.getThrowable() )
        {
            m_listener.log( entry.getMessage() );
        }
        else
        {
            m_listener.log( entry.getMessage(), entry.getThrowable() );
        }
    }
}
