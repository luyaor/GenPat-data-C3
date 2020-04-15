/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.traverse.workflow.special;

import java.lang.reflect.InvocationTargetException;

import ognl.Ognl;
import fit.Fixture;
import fitlibrary.closure.ICalledMethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.NotRejectedException;
import fitlibrary.exception.parse.ParseException;
import fitlibrary.exception.table.ExtraCellsException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.caller.TwoStageSpecial;
import fitlibrary.utility.ExceptionHandler;
import fitlibrary.utility.TestResults;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;

public class PrefixSpecialAction {
	public enum ShowSyle { ORDINARY, ESCAPED, LOGGED }
	public enum NotSyle { PASSES_ON_EXCEPION, ERROR_ON_EXCEPION }
	protected final SpecialActionContext actionContext;
	
	public PrefixSpecialAction(SpecialActionContext actionContext) {
		this.actionContext = actionContext;
	}
	public TwoStageSpecial check(final Row row) throws Exception {
		if (row.size() <= 2)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,1);
		final Cell expectedCell = row.last();
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				target.invokeAndCheckForSpecial(row.rowFrom(2),expectedCell,testResults,row,row.cell(0));
			}
		};
	}
	public TwoStageSpecial show(final Row row) throws Exception {
		return show(row,PrefixSpecialAction.ShowSyle.ORDINARY);
	}
	public TwoStageSpecial showEscaped(final Row row) throws Exception {
		return show(row,PrefixSpecialAction.ShowSyle.ESCAPED);
	}
	public TwoStageSpecial log(final Row row) throws Exception {
		return show(row,PrefixSpecialAction.ShowSyle.LOGGED);
	}
	private TwoStageSpecial show(final Row row, final ShowSyle showStyle) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,true,row.cell(0));
					String text = target.getResultString(result);
					report(text);
				} catch (Exception e) {
					// No result, so ignore it
				}
			}
			private void report(String text) {
				switch (showStyle) {
				case ORDINARY:
					actionContext.show(row,text);
					break;
				case ESCAPED:
					actionContext.show(row,Fixture.escape(text));
					break;
				case LOGGED:
					actionContext.logMessage(text);
				}
			}
		};
	}
	// |show after|...action|
	public TwoStageSpecial showAfter(final Row row) throws Exception {
		if (row.size() < 2)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				try {
					Object result = target.invokeForSpecial(row.rowFrom(2),testResults,true,row.cell(0));
					actionContext.showAfterTable(target.getResultString(result));
				} catch (Exception e) {
					// No result, so ignore it
				}
			}
		};
	}
	// |show after as|folding title|...action|
	public TwoStageSpecial showAfterAs(final Row row) throws Exception {
		if (row.size() < 3)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,2,0);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				try {
					Object result = target.invokeForSpecial(row.rowFrom(3),testResults,true,row.cell(0));
					actionContext.showAsAfterTable(row.text(1,actionContext),target.getResultString(result));
				} catch (Exception e) {
					// No result, so ignore it
				}
			}
		};
	}
	public TwoStageSpecial ensure(final Row row) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				Cell firstCell = row.cell(0);
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
			}
		};
	}
	public TwoStageSpecial not(final Row row, final NotSyle notStyle) throws Exception {
		if (row.size() <= 1)
			throw new MissingCellsException("Do");
		final ICalledMethodTarget target = actionContext.findMethodFromRow(row,1,0);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				Cell notCell = row.cell(0);
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
			}
		};
	}
	private Option<ICalledMethodTarget> getTarget(final Row row) throws Exception {
		if (row.text(2,actionContext).equals("=")) {
			if (row.size() < 4)
				throw new MissingCellsException("Do");
			else if (row.size() > 4)
				throw new ExtraCellsException("");
			return None.none();
		}
		return new Some<ICalledMethodTarget>(actionContext.findMethodFromRow(row,2,0));
	}
	public TwoStageSpecial set(final Row row) throws Exception {
		if (row.size() <= 2)
			throw new MissingCellsException("Do");
		final Option<ICalledMethodTarget> optionalTarget = getTarget(row);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				try {
					String variableName = row.text(1,actionContext);
					if (optionalTarget.isSome()) {
						Object result = optionalTarget.get().invokeForSpecial(row.rowFrom(3),testResults,true,row.cell(0));
						actionContext.setDynamicVariable(variableName,result);
					} else
						actionContext.setDynamicVariable(variableName,Ognl.getValue(row.text(3,actionContext), null));
				} catch (IgnoredException e) {
					// No result, so ignore
				} catch (Exception e) {
					row.error(testResults, e);
				}
			}
		};
	}
	public TwoStageSpecial setSymbolNamed(final Row row) throws Exception {
		if (row.size() <= 2)
			throw new MissingCellsException("Do");
		final Option<ICalledMethodTarget> optionalTarget = getTarget(row);
		return new TwoStageSpecial() {
			@Override
			public void run(TestResults testResults) {
				try {
					String variableName = row.text(1,actionContext);
					if (optionalTarget.isSome()) {
						Object result = optionalTarget.get().invokeForSpecial(row.rowFrom(3),testResults,true,row.cell(0));
						actionContext.setFitVariable(variableName,result);
					} else
						actionContext.setFitVariable(variableName,Ognl.getValue(row.text(3,actionContext), null));
				} catch (IgnoredException e) {
					// No result, so ignore
				} catch (Exception e) {
					row.error(testResults, e);
				}
			}
		};
	}
}
