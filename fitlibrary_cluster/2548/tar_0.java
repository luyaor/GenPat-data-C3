/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.traverse.workflow;

import org.apache.log4j.Logger;

import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.flow.DoAutoWrapper;
import fitlibrary.flow.IDoAutoWrapper;
import fitlibrary.global.PlugBoard;
import fitlibrary.log.FitLibraryLogger;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.special.SpecialActionContext;
import fitlibrary.typed.TypedObject;

public abstract class DoTraverseInterpreter extends Traverse implements FlowEvaluator, SpecialActionContext {
	@SuppressWarnings("unused")
	private static Logger logger = FitLibraryLogger.getLogger(DoTraverseInterpreter.class);
	private IDoAutoWrapper doAutoWrapper = new DoAutoWrapper(this);
	private final DispatchRowInFlow dispatchRowInFlow;
	protected final boolean sequencing;

	public DoTraverseInterpreter() {
		this.sequencing = false;
		this.dispatchRowInFlow = new DispatchRowInFlow(this, sequencing);
	}
	public DoTraverseInterpreter(Object sut) {
		super(sut);
		this.sequencing = false;
		this.dispatchRowInFlow = new DispatchRowInFlow(this, sequencing);
	}
	public DoTraverseInterpreter(TypedObject typedObject) {
		super(typedObject);
		this.sequencing = false;
		this.dispatchRowInFlow = new DispatchRowInFlow(this, sequencing);
	}
	public DoTraverseInterpreter(Object sut, boolean sequencing) {
		super(sut);
		this.sequencing = sequencing;
		this.dispatchRowInFlow = new DispatchRowInFlow(this, sequencing);
	}
	@Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
		// Now handled by DoFlow
		return null;
	}
    // @Overridden in CollectionSetUpTraverse
    public Object interpretInFlow(Table table, TestResults testResults) {
    	return null; // Leave it here, as override it.
    }
    public TypedObject interpretRow(Row row, TestResults testResults) {
    	return doAutoWrapper.wrap(interpretRowBeforeWrapping(row,testResults));
    }
    final public TypedObject interpretRowBeforeWrapping(Row row, TestResults testResults) {
    	return dispatchRowInFlow.interpretRowBeforeWrapping(row, testResults);
    }
	// The following is needed for its obligation to the interface SpecialActionContext, which is called by specials
	public ICalledMethodTarget findMethodFromRow(Row row, int from, int extrasCellsOnEnd) throws Exception {
		int upTo = row.size() - extrasCellsOnEnd;
		return PlugBoard.lookupTarget.findMethodByArity(row, from, upTo, !dispatchRowInFlow.isDynamicSequencing(), this);
	}
	public ICalledMethodTarget findMethodFromRow222(Row row, int from, int less) throws Exception {
		int extrasCellsOnEnd = less-from-1;
		int upTo = row.size() - extrasCellsOnEnd;
		return PlugBoard.lookupTarget.findMethodByArity(row, from, upTo, !dispatchRowInFlow.isDynamicSequencing(), this);
	}
//	public DoTraverseInterpreter switchSetUp() {
//		return this;
//	}
	protected Object callMethodInRow(Row row, TestResults testResults, boolean catchError, Cell operatorCell) throws Exception {
		return findMethodFromRow222(row,1, 2).invokeForSpecial(row.fromAt(2),testResults,catchError,operatorCell);
	}
}
