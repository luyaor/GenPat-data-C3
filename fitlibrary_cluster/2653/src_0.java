/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.runResults.TestResults;
import fitlibrary.suite.SuiteEvaluator;
import fitlibrary.table.Row;
import fitlibrary.traverse.DomainAdapter;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.option.None;
import fitlibrary.utility.option.Option;
import fitlibrary.utility.option.Some;
import fitlibraryGeneric.typed.GenericTypedObject;

public class ScopeStack implements IScopeStack {
	private final TypedObject flowEvaluator;
	protected final Stack<TypedObject> stack = new Stack<TypedObject>();
	protected Option<TypedObject> suiteOption = None.none();
	protected List<TypedObject> selectObjects = new ArrayList<TypedObject>();
	protected List<TypedObject> globals = new ArrayList<TypedObject>();
	protected final TypedObject global;
	private Map<String,TypedObject> selectNames = new HashMap<String, TypedObject>();
	private boolean abandon = false;
	private boolean stopOnError = false;

	public ScopeStack(FlowEvaluator flowEvaluator, TypedObject global) {
		this.flowEvaluator = new GenericTypedObject(flowEvaluator);
		this.global = global;
		globals.add(global);
	}
	public void clearAllButSuite() {
		stack.clear();
	}
	// Tracks the first SuiteEvaluator, which is not popped during a storytest
	public void push(TypedObject typedObject) {
		if (typedObject.getSubject() instanceof SuiteEvaluator && suiteOption.isNone())
			suiteOption = new Some<TypedObject>(typedObject);
		else if (stack.isEmpty() && selectObjects.isEmpty())
			selectObjects.add(typedObject);
		else
			stack.push(typedObject);
	}
	public List<TypedObject> poppedAtEndOfTable() {
		ArrayList<TypedObject> results = new ArrayList<TypedObject>();
		while (!stack.isEmpty())
			results.add(stack.pop());
		return results;
	}
	public List<TypedObject> poppedAtEndOfStorytest() {
		ArrayList<TypedObject> results = new ArrayList<TypedObject>();
		while (!stack.isEmpty())
			results.add(stack.pop());
		for (TypedObject typedObject : selectObjects)
			results.add(typedObject);
		selectObjects.clear();
		return results;
	}
	public TypedObject pop() {
		return stack.pop();
	}
	public List<TypedObject> objectsForLookup() {
		List<TypedObject> objects = new ArrayList<TypedObject>();
		for (int i = stack.size() - 1; i >= 0; i--)
			addObject(stack.elementAt(i),objects);
		for (TypedObject typedObject : selectObjects)
			addObject(typedObject,objects);
		if (suiteOption.isSome())
			addObject(suiteOption.get(),objects);
		for (TypedObject typedObject : globals)
			addObject(typedObject,objects);
		addObject(flowEvaluator,objects);
		return objects;
	}
	private void addObject(TypedObject typedObject, List<TypedObject> accumulatingObjects) {
		if (typedObject == null)
			return;
		if (typedObject.isNull())
			return;
		if (accumulatingObjects.contains(typedObject))
			return;
		accumulatingObjects.add(typedObject);
		Object subject = typedObject.getSubject();
		if (subject instanceof Evaluator)
			addObject(((Evaluator)subject).getTypedSystemUnderTest(),accumulatingObjects);
		else if (subject instanceof DomainAdapter) {
			Object sut = ((DomainAdapter)subject).getSystemUnderTest();
			if (sut != null)
				addObject(new GenericTypedObject(sut),accumulatingObjects);
		}
	}
	public List<Class<?>> possibleClasses() {
		ArrayList<Class<?>> results = new ArrayList<Class<?>>();
		for (int i = stack.size() - 1; i >= 0; i--)
			addClass(stack.elementAt(i).getSubject(), results);
		for (TypedObject t : selectObjects)
			addClass(t.getSubject(),results);
		if (suiteOption.isSome())
			addClass(suiteOption.get().getSubject(),results);
		for (TypedObject t : globals)
			if (t != global)
				addClass(t.getSubject(),results);
		return results;
	}
	private void addClass(Object subject, ArrayList<Class<?>> results) {
		if (subject == null)
			return;
		if (toBeVisible(subject) && !results.contains(subject.getClass()))
			results.add(subject.getClass());
		if (subject instanceof DomainAdapter)
			addClass(((DomainAdapter)subject).getSystemUnderTest(), results);
	}
	private boolean toBeVisible(Object subject) {
		Package aPackage = subject.getClass().getPackage();
		if (aPackage == null)
			return true;
		String packageName = aPackage.getName();
		if (!packageName.startsWith("fitlibrary"))
			return true;
		return packageName.startsWith("fitlibrary.specify") || packageName.startsWith("fitlibraryGeneric.specify");
	}
	@Override
	public void temporarilyAdd(Evaluator evaluator) {
		stack.push(new GenericTypedObject(evaluator));
	}
	@Override
	public void removeTemporary(Evaluator evaluator) {
		TypedObject top = stack.pop();
		if (top.getSubject() != evaluator)
			throw new RuntimeException("Whoops, temporary was not on the top of the stack!");
	}
	@Override
	public IScopeState currentState() {
		final int size = stack.size();
		return new IScopeState() {
			@Override
			public void restore() {
				while (stack.size() > size)
					stack.pop();
			}
		};
	}
	@Override
	public void addNamedObject(String name, TypedObject typedObject, Row row, TestResults testResults) {
		selectObjects.add(typedObject);
		selectNames.put(name,typedObject);
	}
	@Override
	public void select(String name) {
		TypedObject typedObject = selectNames.get(name);
		if (typedObject == null)
			throw new FitLibraryException("Unknown name");
		selectObjects.remove(typedObject);
		selectObjects.add(0,typedObject);
	}
	@Override
	public void addGlobal(TypedObject typedObject) {
		globals.add(typedObject);
	}
	@Override
	public void setAbandon(boolean abandon) {
		this.abandon  = abandon;
	}
	@Override
	public boolean isAbandon() {
		return abandon;
	}
	@Override
	public boolean isStopOnError() {
		return stopOnError;
	}
	@Override
	public void setStopOnError(boolean stop) {
		this.stopOnError = stop;
	}
}
