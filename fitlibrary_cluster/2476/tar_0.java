/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.closure;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import fitlibrary.diff.Diff_match_patch;
import fitlibrary.diff.Diff_match_patch.Diff;
import fitlibrary.dynamicVariable.DynamicVariablesRecording;
import fitlibrary.exception.AbandonException;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.parse.NoValueProvidedException;
import fitlibrary.parser.Parser;
import fitlibrary.parser.lookup.GetterParser;
import fitlibrary.parser.lookup.ResultParser;
import fitlibrary.runResults.TestResults;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoTraverse.Comparison;
import fitlibrary.typed.TypedObject;

/**
 * Manages calling a method on row cells, and possibly checking the result against a cell.
 * It constructs Parsers to use for getting cell values, comparisons, etc.
 */
public class CalledMethodTarget implements ICalledMethodTarget {
	final private Closure closure;
	final private Evaluator evaluator;
	private Parser[] parameterParsers;
	protected ResultParser resultParser = null;
	final private Object[] args;
	private String repeatString = null;
    private String exceptionString = null;
	private boolean everySecond = false;

    public CalledMethodTarget(Closure closure, Evaluator evaluator) {
		this.closure = closure;
		this.evaluator = evaluator;
		args = new Object[getParameterTypes().length];
		parameterParsers = closure.parameterParsers(evaluator);
		resultParser = closure.resultParser(evaluator);
   }
	public CalledMethodTarget(Evaluator evaluator) {
		this.evaluator = evaluator;
		parameterParsers = new Parser[0];
		args = new Object[0];
		this.closure = null;
		resultParser = null;
	}
	public boolean isValid() {
		return closure != null;
	}
	public Class<?> getReturnType() {
		return closure.getReturnType();
	}
	public Class<?>[] getParameterTypes() {
		return closure.getParameterTypes();
	}
	public Object invoke(Object[] arguments) throws Exception {
		return closure.invoke(arguments);
	}
	public TypedObject invokeTyped(Object[] arguments) throws Exception {
		return closure.invokeTyped(arguments);
	}
    public Object invoke(Cell cell, TestResults testResults) throws Exception {
    	collectCell(cell,0,cell.text(evaluator),testResults,true);
    	return invoke(args);
    }
    public TypedObject invokeTyped(Row row, TestResults testResults) throws Exception {
		try {
			if (everySecond)
				collectCells(row,2,testResults,true);
			else
				collectCells(row,1,testResults,true);
		} catch (AbandonException e) {
			throw new IgnoredException(e); // no more to do
		} catch (Exception e) {
			throw new IgnoredException(e); // Unable to call
		}
		try {
			return invokeTyped(args);
		} catch (AbandonException e) {
			throw new IgnoredException(); // no more to do
		}
	}
    public Object invoke(Row row, TestResults testResults, boolean catchParseError) throws Exception {
		try {
			if (everySecond)
				collectCells(row,2,testResults,catchParseError);
			else
				collectCells(row,1,testResults,catchParseError);
		} catch (Exception e) {
			throw new IgnoredException(e); // Unable to call
		}
		return invoke(args);
	}
    public Object invokeForSpecial(Row row, TestResults testResults, boolean catchParseError, Cell operatorCell) throws Exception {
		try {
			if (everySecond)
				collectCells(row,2,testResults,catchParseError);
			else
				collectCells(row,1,testResults,catchParseError);
		} catch (Exception e) {
			throw new IgnoredException(e); // Unable to call
		}
		try {
			return invoke(args);
		} catch (InvocationTargetException e) {
			Throwable embedded = e.getTargetException();
			if (embedded instanceof FitLibraryShowException)
				operatorCell.error(testResults);
			throw e;
		}
	}
    private void collectCells(Row row, int step, TestResults testResults, boolean catchParseError) throws Exception {
		for (int argNo = 0; argNo < args.length; argNo++) {
			Cell cell = row.elementAt(argNo*step);
			collectCell(cell, argNo,cell.text(evaluator),testResults,catchParseError);
		}
	}
	private void collectCell(Cell cell, int argNo, String text, TestResults testResults, boolean catchParseError) throws Exception {
		try {
			if (!text.equals(repeatString))
				args[argNo] = parameterParsers[argNo].parseTyped(cell,testResults).getSubject();
		} catch (Exception e) {
			if (catchParseError) {
				cell.error(testResults,e);
				throw new IgnoredException();
			}
			throw e;
		}
	}
    public void invokeAndCheck(Row row, Cell expectedCell, TestResults testResults, boolean handleSubtype) {
        boolean exceptionExpected = exceptionIsExpected(expectedCell);
        try {
            Object result = invoke(row,testResults,true);
			if (exceptionExpected)
			    expectedCell.fail(testResults);
			else
				checkResult(expectedCell,result,true,handleSubtype,testResults);
        } catch (IgnoredException ex) {
            //
        } catch (Exception e) {
        	expectedCell.exceptionExpected(exceptionExpected, e, testResults);
        }
    }
    public void invokeAndCheckForSpecial(Row row, Cell expectedCell, TestResults testResults, Row fullRow, Cell specialCell) {
        boolean exceptionExpected = exceptionIsExpected(expectedCell);
        try {
            Object result = invoke(row,testResults,true);
			DynamicVariablesRecording recorder = evaluator.getRuntimeContext().getDynamicVariableRecorder();
			if (recorder.isRecording() && expectedCell.unresolved(evaluator)) {
            	String text = expectedCell.text();
            	String key = text.substring(2,text.length()-1);
            	String resultString = result.toString();
				if (!resultString.contains("@{"+key+"}")) { // Don't record a self-reference.
            		evaluator.setDynamicVariable(key, resultString);
            		recorder.record(key,resultString);
            	}
            	expectedCell.pass(testResults,resultString);
            	return;
            }
			if (exceptionExpected)
			    expectedCell.fail(testResults);
			else
				checkResult(expectedCell,result,true,false,testResults);
        } catch (IgnoredException e) {
            //
        } catch (InvocationTargetException e) {
        	Throwable embedded = e.getTargetException();
        	if (embedded instanceof FitLibraryShowException) {
        		specialCell.error(testResults);
        		fullRow.error(testResults, e);
        	} else
        		expectedCell.exceptionExpected(exceptionExpected, e, testResults);
        } catch (Exception e) {
        	expectedCell.exceptionExpected(exceptionExpected, e, testResults);
        }
    }
	private boolean exceptionIsExpected(Cell expectedCell) {
		return exceptionString != null && exceptionString.equals(expectedCell.text(evaluator));
	}
	public String getResult() throws Exception {
		return resultParser.show(invoke());
	}
	public boolean invokeAndCheckCell(Cell expectedCell, boolean matchedAlready, TestResults testResults) {
        try {
			return checkResult(expectedCell,invoke(),matchedAlready,false,testResults);
		} catch (Exception e) {
			expectedCell.error(testResults,e);
			return false;
		}
	}
	public Object invoke() throws Exception {
		return closure.invoke();
	}
	public boolean matches(Cell expectedCell, TestResults testResults) {
		try {
			return resultParser.matches(expectedCell,invoke(),testResults);
		} catch (Exception e) {
			return false;
		}
	}
	public boolean checkResult(Cell expectedCell, Object result, boolean showWrongs, boolean handleSubtype, TestResults testResults) {
		ResultParser valueParser = resultParser;
		if (handleSubtype && closure != null)
			valueParser = closure.specialisedResultParser(resultParser,result,evaluator);
		try {
			if (valueParser == null)
				throw new NoValueProvidedException();
			if (valueParser.isShowAsHtml()) {
				if (valueParser.matches(expectedCell,result,testResults)) {
					expectedCell.pass(testResults);
					return true;
				}
				expectedCell.wrongHtml(testResults,valueParser.show(result));
				return false;
			}
			if (valueParser.matches(expectedCell,result,testResults)) {
				expectedCell.passIfNotEmbedded(testResults);
				return true;
			}
			if (showWrongs && (result == null || !expectedCell.hasEmbeddedTables())) {
				if (result instanceof String)
					expectedCell.failWithStringEquals(testResults,valueParser.show(result),evaluator);
				else
					expectedCell.fail(testResults,valueParser.show(result),evaluator);
			}
			return false;
		} catch (Exception e) {
			expectedCell.error(testResults,e);
			return false;
		}
	}
	public static String matching(String expected, String actual) {
		LinkedList<Diff> diffs = new Diff_match_patch().diff_main(expected, actual, true);
		StringBuilder s = new StringBuilder();
		for (Diff diff : diffs) {
			switch (diff.operation) {
				case DELETE:
					s.append("<strike>"+diff.text+"</strike>");
					break;
				case EQUAL:
					s.append(diff.text);
					break;
				case INSERT:
					s.append("<b>"+diff.text+"</b>");
					break;
			}
		}
		return s.toString();
	}
	public void notResult(Cell expectedCell, Object result, TestResults testResults) {
		try {
			if (resultParser == null)
				throw new NoValueProvidedException();
			else if (!resultParser.matches(expectedCell,result,testResults))
				expectedCell.passIfNotEmbedded(testResults);
			else if (!expectedCell.hasEmbeddedTables()) {
				expectedCell.fail(testResults);
			}
		} catch (Exception e) {
			expectedCell.error(testResults,e);
		}
	}
	@SuppressWarnings("unchecked")
	public void compare(Cell expectedCell, Comparable actual, TestResults testResults, Comparison compare) {
		try {
			if (resultParser == null)
				throw new NoValueProvidedException();
			Object expected = resultParser.parseTyped(expectedCell, testResults).getSubject();
			if (expected instanceof Comparable) {
				if (compare.compares(actual,(Comparable)expected))
					expectedCell.passIfNotEmbedded(testResults);
				else if (!expectedCell.hasEmbeddedTables())
					expectedCell.fail(testResults,""+actual,evaluator);
			} else
				throw new FitLibraryException("Unable to compare, as expected value is not Comparable");
		} catch (Exception e) {
			expectedCell.error(testResults,e);
		}
	}
	public Object getResult(Cell expectedCell, TestResults testResults) {
		try {
			return resultParser.parseTyped(expectedCell,testResults).getSubject();
		} catch (Exception e) {
			return null;
		}
	}
	public void color(Row row, boolean right, TestResults testResults) throws Exception {
		if (!everySecond && row.elementExists(0))
			row.elementAt(0).passOrFail(testResults,right);
		else
			for (int i = 0; i < row.size(); i += 2)
				row.elementAt(i).passOrFail(testResults,right);
	}
	/** Defines the Strings that signifies that the value in the row above is
	 *  to be used again. Eg, it could be set to "" or to '"".
	 */
	public void setRepeatAndExceptionString(String repeatString, String exceptionString) {
		this.repeatString = repeatString;
		this.exceptionString = exceptionString;
	}
	public void setEverySecond(boolean everySecond) {
		this.everySecond = everySecond;
	}
	public String getResultString(Object result) throws Exception {
		if (getReturnType() == String.class)
			return (String)result;
		return resultParser.show(result);
	}
	@Override
	public String toString() {
		return "MethodTarget["+closure+"]";
	}
    public Parser getResultParser() { // TEMP while adding FitLibrary2
        return resultParser;
    }
    public void setResultParser(GetterParser resultAdapter) { // TEMP while adding FitLibrary2
        this.resultParser = resultAdapter;
    }
    public Parser[] getParameterParsers() {
        return parameterParsers;
    }
    public void setParameterParsers(Parser[] parameterAdapters) {
        this.parameterParsers = parameterAdapters;
    }
    public void setTypedSubject(TypedObject typedObject) {
    	closure.setTypedSubject(typedObject);
    }
	public boolean returnsVoid() {
		return getReturnType() == void.class;
	}
	public boolean returnsBoolean() {
		return getReturnType() == boolean.class;
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CalledMethodTarget))
			return false;
		CalledMethodTarget other = (CalledMethodTarget) obj;
		return closure == other.closure && evaluator == other.evaluator;
	}
	@Override
	public int hashCode() {
		return evaluator.hashCode();
	}
}
