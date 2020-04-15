package fitlibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.global.PlugBoard;
import fitlibrary.table.Row;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;

public class SelectFixture extends DoFixture {
	private Map<String,DoEvaluator> nametoFixture = new HashMap<String,DoEvaluator>();
	
	public void addAs(String doFixtureClassName, String name) {
		try {
			Object instance = ClassUtility.newInstance(doFixtureClassName);
			if (instance instanceof DoEvaluator) {
				DoEvaluator doEval = (DoEvaluator) instance;
				doEval.setRuntimeContext(runtime()); // includes timeouts
				doEval.setUp();
				add(doEval,name);
			} else
				throw new FitLibraryException("Class must be a DoFixture or a DoTraverse");
		} catch (FitLibraryException e) {
			throw e;
		} catch (Exception e) {
			throw new FitLibraryException(e);
		}
	}
	public void select(String name) {
		Object fixture = nametoFixture.get(name);
		if (fixture == null)
			throw new FitLibraryException("Unknown name");
		setSystemUnderTest(fixture);
	}
	/*
	 * |''add named fixture''|name|...|
	 */
	public void addNamedFixture(final Row row, TestResults testResults) throws Exception {
		int less = 3;
		if (row.size() < less)
			throw new MissingCellsException("addNamedFixture");
		Object result = findMethodFromRow(row,2,less).invokeForSpecial(row.rowFrom(3),testResults,true,row.cell(0));
		if (result instanceof DoEvaluator)
			add((DoEvaluator) result,row.text(1,this));
		else
			row.cell(0).failHtml(testResults, "Action did not return a DoFixture/DoTraverse");
	}
	private CalledMethodTarget findMethodFromRow(final Row row, int from, int less) throws Exception {
		return findMethodByActionName(row.rowFrom(from), row.size() - less);
	}
	private CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		return PlugBoard.lookupTarget.findMethodInEverySecondCell(this, row, allArgs);
	}
	protected void add(DoEvaluator eval, String name) {
		nametoFixture.put(name,eval);
	}
	@Override
	public void tearDown() {
		List<Exception> errors = new ArrayList<Exception>();
		for (String key : nametoFixture.keySet()) {
			try {
				DoEvaluator eval = nametoFixture.get(key);
				eval.tearDown();
			} catch (Exception e) {
				errors.add(e);
			}
		}
		if (!errors.isEmpty())
			throw new FitLibraryException("tearDown errors: "+errors);
	}
	@Override
	public List<String> methodsThatAreVisible() {
		List<String> list = new ArrayList<String>(super.methodsThatAreVisible());
		list.add("addAs/2");
		list.add("select/1");
		list.add("tearDown/0");
		return list;
	}
}
