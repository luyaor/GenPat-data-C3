/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.osgi.framework.BundleContext;

public class AptPlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	
	private static final String TOOLSJARNAME = "./tools.jar"; //$NON-NLS-1$
	
	/**
	 * Status IDs for system log entries.  Must be unique per plugin.
	 */
	public static final int STATUS_EXCEPTION = 1;
	public static final int STATUS_NOTOOLSJAR = 2;
	public static final String ERRTXT_NOTOOLSJAR = "Could not find tools.jar in plugin directory: org.eclipse.jdt.apt.core will not load";
	
	private static AptPlugin thePlugin = null; // singleton object
	
	public void start(BundleContext context) throws Exception {
		thePlugin = this;
		super.start(context);
		initDebugTracing();
		checkToolsJar();
		AptConfig.initialize();
	}

	/**
	 * Check for the Sun mirror interfaces.  If they aren't found,
	 * log an error.
	 */
	private void checkToolsJar() {
		boolean foundToolsJar = true;
		try {
			InputStream is = thePlugin.openStream(new Path(TOOLSJARNAME));
		}
		catch (IOException e) {
			foundToolsJar = false;
		}
		if (!foundToolsJar) {
			log(new Status(IStatus.ERROR, PLUGIN_ID, STATUS_NOTOOLSJAR, ERRTXT_NOTOOLSJAR, null));
		}
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	public static AptPlugin getPlugin() {
		return thePlugin;
	}

	/**
	 * Log a status message to the platform log.  Use this for reporting exceptions.
	 * @param status
	 */
	public static void log(IStatus status) {
		thePlugin.getLog().log(status);
	}
	
	/**
	 * Convenience wrapper around log(IStatus), to log an exception.
	 */
	public static void log(Throwable e, String message) {
		// TODO: before ship, remove this printing. Instead just log
		System.err.println(message);
		e.printStackTrace();
		
		log(new Status(IStatus.ERROR, PLUGIN_ID, STATUS_EXCEPTION, message, e)); 
	}
	
	private void initDebugTracing() {		
		String option = Platform.getDebugOption(APT_DEBUG_OPTION);
		if(option != null) DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$		
	}
	
	public static boolean DEBUG = false;
	public final static String APT_DEBUG_OPTION = AptPlugin.PLUGIN_ID + "/debug";
}
