package fitlibrary.config;

import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.ShowSelectedActions;

@ShowSelectedActions
public interface Configuration {
	@AnAction(wiki="",actionType=ActionType.SIMPLE,
			tooltip="Retain sensible unicode characters when converting an action name to a Java method name. Set through fixturing code.")
	boolean keepingUniCode();
	
	boolean isAddTimings();
	
	@AnAction(wiki="|''<i>add timings</i>''|true or false|",actionType=ActionType.SIMPLE,
			tooltip="Specify whether timing information is to be added to reported tables. False by default.")
	void addTimings(boolean addTimings);
}
