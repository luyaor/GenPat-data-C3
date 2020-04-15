/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.exception.FitLibraryException;

public abstract class ConfigureLogger {
	private static final String NAME = "storytest";
	private ShowAfterTableAppender appender;
	
	public ConfigureLogger(ShowAfterTableAppender appender) {
		this.appender = appender;
//		rootLogger().setLevel(Level.OFF);
	}
	@AnAction(wiki="|''<i>show after</i>''|true or false|",actionType=ActionType.SIMPLE,
			tooltip="Specifies whether or not the log4j logs are to be shown after the table in which they occur.")
	public void showAfter(boolean show) {
		if (show)
			rootLogger().addAppender(appender);
		else
			rootLogger().removeAppender(appender);
	}
	@AnAction(wiki="|''<i>level</i>''|the level|",actionType=ActionType.SIMPLE,
			tooltip="Set the level of logging to one of ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF or TRACE")
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
	@AnAction(wiki="|''<i>level</i>''|the level|''<i>for</i>''|log name|",actionType=ActionType.SIMPLE,
			tooltip="Set the level of logging for the specified log to one of ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF or TRACE")
	public void levelFor(String level, String name) {
		getLogger(name).setLevel(toLevel(level));
	}
	@AnAction(wiki="|''<i>debug</i>''|message|",actionType=ActionType.SIMPLE,
			tooltip="Add the debug message to the logger")
	public void debug(String s) {
		getLogger(NAME).debug(s);
	}
	@AnAction(wiki="|''<i>trace</i>''|message|",actionType=ActionType.SIMPLE,
			tooltip="Add the trace message to the logger")
	public void trace(String s) {
		getLogger(NAME).trace(s);
	}
	@AnAction(wiki="|''<i>error</i>''|message|",actionType=ActionType.SIMPLE,
			tooltip="Add the error message to the logger")
	public void error(String s) {
		getLogger(NAME).error(s);
	}
	protected abstract Logger rootLogger();
	protected abstract Logger getLogger(String name);
}
