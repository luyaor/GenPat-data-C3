/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import java.lang.reflect.InvocationTargetException;

import fit.Fixture;
import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.exception.parse.ParseException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.table.ICell;
import fitlibrary.table.IRow;
import fitlibrary.traverse.workflow.caller.CallManager;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.utility.ExceptionHandler;
import fitlibrary.utility.TestResults;

public class SpecialAction {
	public enum ShowSyle { ORDINARY, ESCAPED }
	public enum NotSyle { PASSES_ON_EXCEPION, ERROR_ON_EXCEPION }
	protected final SpecialActionContext actionContext;
	
	public SpecialAction(SpecialActionContext actionContext) {
		this.actionContext = actionContext;
	}
	public TwoStageSpecial check(final IRow row) throws Exception {
		if (row.size() <= 2)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,1);
		final ICell expectedCell = row.last();
		return new TwoStageSpecial() {
			@Override
			public Object run(TestResults testResults) {
				if (actionContext.isGatherExpectedForGeneration()) // This needs to use a copy of the row, otherwise duplicates error messages
					actionContext.setExpectedResult(target.getResult(expectedCell,testResults));
				target.invokeAndCheckForSpecial(row.rowFrom(2),expectedCell,testResults,row,row.cell(0));
				return null;
			}
		};
	}
	public TwoStageSpecial show(final IRow row) throws Exception {
		return show(row,SpecialAction.ShowSyle.ORDINARY);
	}
	public TwoStageSpecial showEscaped(final IRow row) throws Exception {
		return show(row,SpecialAction.ShowSyle.ESCAPED);
	}
	private TwoStageSpecial show(final IRow row, final ShowSyle showStyle) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public Object run(TestResults testResults) {
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,true,row.cell(0));
					String text = target.getResultString(result);
					if (showStyle == ShowSyle.ESCAPED)
						text = Fixture.escape(text);
					row.addCell(text).shown();
					CallManager.addShow(row);
				} catch (Exception e) {
					// No result, so ignore it
				}
				return null;
			}
		};
	}
	public TwoStageSpecial showAfter(final IRow row) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public Object run(TestResults testResults) {
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,true,row.cell(0));
					actionContext.showAfterTable(target.getResultString(result));
				} catch (Exception e) {
					// No result, so ignore it
				}
				return null;
			}
		};
	}
	public TwoStageSpecial ensure(final IRow row) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public Object run(TestResults testResults) {
				actionContext.setExpectedResult(true); // Has to be in 2nd stage
				ICell firstCell = row.cell(0);
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,true,firstCell);
				    Boolean resultBoolean = result == null ? Boolean.TRUE : (Boolean) result;
				    firstCell.passOrFail(testResults,resultBoolean.booleanValue());
				} catch (IgnoredException e) {
					// No result, so ignore
				} catch (InvocationTargetException e) {
					Throwable embedded = ExceptionHandler.unwrap(e);
					row.error(testResults, embedded);
				} catch (Exception e) {
					row.error(testResults, e);
				}
				return null;
			}
		};
	}
	public TwoStageSpecial not(final IRow row, final NotSyle notStyle) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public Object run(TestResults testResults) {
				actionContext.setExpectedResult(false); // Has to be in 2nd stage
				ICell notCell = row.cell(0);
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,false,row.cell(0));
				    if (!(result instanceof Boolean))
				        notCell.error(testResults,new NotRejectedException());
				    else if (((Boolean)result).booleanValue())
				        notCell.fail(testResults);
				    else
				        notCell.pass(testResults);
				} catch (IgnoredException e) {
					if (e.getIgnoredException() instanceof ParseException)
						if (notStyle == NotSyle.PASSES_ON_EXCEPION)
							notCell.pass(testResults);
						else
							row.error(testResults,e.getIgnoredException());
				} catch (FitLibraryException e) {
					if (notStyle == NotSyle.PASSES_ON_EXCEPION && e instanceof ParseException)
						notCell.pass(testResults);
					else
						row.error(testResults,e);
				} catch (InvocationTargetException e) {
					Throwable embedded = ExceptionHandler.unwrap(e);
					if (notStyle == NotSyle.PASSES_ON_EXCEPION && embedded instanceof ParseException)
						notCell.pass(testResults);
					else if (notStyle == NotSyle.ERROR_ON_EXCEPION || embedded instanceof FitLibraryException)
						row.error(testResults, embedded);
					else
						notCell.pass(testResults);
				} catch (Exception e) {
					if (notStyle == NotSyle.PASSES_ON_EXCEPION)
						notCell.pass(testResults);
					else
						row.error(testResults,e);
				}
				return null;
			}
		};
	}
}
