/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fitlibrary.exception.FitLibraryException;

public abstract class ConfigureLogger {
	private static final String NAME = "storytest";
	private ShowAfterTableAppender appender;
	
	public ConfigureLogger(ShowAfterTableAppender appender) {
		this.appender = appender;
//		rootLogger().setLevel(Level.OFF);
	}
	public void showAfter(boolean show) {
		if (show)
			rootLogger().addAppender(appender);
		else
			rootLogger().removeAppender(appender);
	}
	public void level(String level) {
		rootLogger().setLevel(toLevel(level));
	}
	private Level toLevel(String level) {
	    if (level.equals("ALL"))   return Level.ALL; 
	    if (level.equals("DEBUG")) return Level.DEBUG; 
	    if (level.equals("INFO"))  return Level.INFO;
	    if (level.equals("WARN"))  return Level.WARN;  
	    if (level.equals("ERROR")) return Level.ERROR;
	    if (level.equals("FATAL")) return Level.FATAL;
	    if (level.equals("OFF"))   return Level.OFF;
	    if (level.equals("TRACE")) return Level.TRACE;
	    throw new FitLibraryException("Must be one of: ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF, TRACE");
	}
	public void levelFor(String level, String name) {
		getLogger(name).setLevel(toLevel(level));
	}
	public void debug(String s) {
		getLogger(NAME).debug(s);
	}
	public void trace(String s) {
		getLogger(NAME).trace(s);
	}
	public void error(String s) {
		getLogger(NAME).error(s);
	}
	protected abstract Logger rootLogger();
	protected abstract Logger getLogger(String name);
}
