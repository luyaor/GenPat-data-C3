/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary;

import java.util.List;

import fit.Fixture;
import fit.Parse;
import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.ITableListener;
import fitlibrary.utility.TestResults;

/** An alternative to fit.ActionFixture
	@author rick mugridge, july 2003
  * 
  * See the specifications for examples
*/
public class DoFixture extends FitLibraryFixture implements DoEvaluator {
	private DoTraverse doTraverse = new DoTraverse(this);
	
	public DoFixture() {
    	setTraverse(doTraverse);
	}
	public DoFixture(Object sut) {
		this();
	    setSystemUnderTest(sut);
	}

	public void setTraverse(DoTraverse traverse) {
    	this.doTraverse = traverse;
    	super.setTraverse(traverse);
    }
    // Dispatched to from Fixture when a DoFixture is the first fixture in a storytest
    @Override
	final public void interpretTables(Parse parseTables) {
    	TableFactory.tables(parseTables).elementAt(0).error(createTestResults(),
    			new RuntimeException("Please use FitLibraryServer instead of FitServer."));
    }
    // Dispatched to from Fixture when Fixture is doTabling the tables one by one (not in flow)
	@Override
	final public Object interpretAfterFirstRow(Table table, TestResults testResults) {
    	return ((DoTraverse)traverse()).interpretInFlow(table,testResults);
    }
	/**
	 * if (stopOnError) then we don't continue intepreting a table
	 * as there's been a problem
	 */
	public void setStopOnError(boolean stopOnError) {
		doTraverse.setStopOnError(stopOnError);
	}
	protected void abandon() {
		doTraverse.abandonStorytest();
	}
	protected void showAfterTable(String s) {
		doTraverse.showAfterTable(s);
	}
	public void showAsAfterTable(String title,String s) {
		doTraverse.showAsAfterTable(title,s);
	}
	public Object getSymbolNamed(String fitSymbolName) {
		return Fixture.getSymbol(fitSymbolName);
	}
	protected void setExpandDefinedActions(boolean expandDefinedActions) {
		doTraverse.setExpandDefinedActions(expandDefinedActions);
	}
	public Object interpretInFlow(Table firstTable, TestResults testResults) {
		return doTraverse.interpretInFlow(firstTable,testResults);
	}
	final public Object interpretWholeTable(Table table, ITableListener tableListener) {
		return doTraverse.interpretWholeTable(table,tableListener);
	}
	// --------- Interpretation ---------------------------------------
	public List<String> methodsThatAreVisible() {
		return doTraverse.methodsThatAreVisible();
	}
}
