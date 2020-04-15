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
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.workflow.DoEvaluator;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;
import fitlibraryGeneric.typed.GenericTypedFactory;

public class SelectFixture extends DoFixture {
	private Map<String,Object> nametoFixture = new HashMap<String,Object>();
	
	public void addAs(String doFixtureClassName, String name) {
		try {
			Object instance = ClassUtility.newInstance(doFixtureClassName);
			if (instance instanceof DoEvaluator) {
				DoEvaluator doEval = (DoEvaluator) instance;
				doEval.setRuntimeContext(getRuntimeContext()); // includes timeouts
			} 
			add(instance,name);
			callSetUpSutChain(instance);
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
			add(result,row.text(1,this));
		else
			row.cell(0).failHtml(testResults, "Action did not return a DoFixture/DoTraverse");
	}
	private CalledMethodTarget findMethodFromRow(final Row row, int from, int less) throws Exception {
		return findMethodByActionName(row.rowFrom(from), row.size() - less);
	}
	private CalledMethodTarget findMethodByActionName(Row row, int allArgs) throws Exception {
		return PlugBoard.lookupTarget.findMethodInEverySecondCell(this, row, allArgs);
	}
	protected void add(Object sut, String name) {
		nametoFixture.put(name,sut);
	}
	public void tearDown() {
		List<Exception> errors = new ArrayList<Exception>();
		for (String key : nametoFixture.keySet()) {
			try {
				Object sut = nametoFixture.get(key);
				if (sut != getSystemUnderTest())
					callTearDownSutChain(sut);
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
	private void callSetUpSutChain(Object sut) throws Exception {
		Object object = sut;
		while (object instanceof DomainAdapter) {
			callMethod(object,"setUp");
			object = ((DomainAdapter)object).getSystemUnderTest();
		}
	}
	private void callTearDownSutChain(Object sut) throws Exception {
		Object object = sut;
		while (object instanceof DomainAdapter) {
			callMethod(object,"tearDown");
			object = ((DomainAdapter)object).getSystemUnderTest();
		}
	}
	private void callMethod(Object object, String methodName) throws Exception {
			CalledMethodTarget methodTarget = new GenericTypedFactory().asTypedObject(object).
				optionallyFindMethodOnTypedObject(methodName,0,this,false);
			if (methodTarget != null)
				methodTarget.invoke();
	}
}
