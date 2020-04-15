/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.flow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.runResults.TestResults;
import fitlibrary.runResults.TestResultsFactory;
import fitlibrary.suite.SuiteFixture;
import fitlibrary.table.TableFactory;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.traverse.workflow.FlowEvaluator;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.CollectionUtility;
import fitlibraryGeneric.typed.GenericTypedObject;

public class TestScopeStack {
	final Object something = "something";
	final TypedObject someTypedObject = new GenericTypedObject(something);
	final Object other = 12;
	final TypedObject otherTypedObject = new GenericTypedObject(other);
	final DoTraverse doTraverse = new DoTraverse(something);
	final TypedObject doTypedObjectWithSomeSut = new GenericTypedObject(doTraverse);
	final TypedObject suiteTypedObject = new GenericTypedObject(new SuiteFixture());
	final List<TypedObject> emptyList = new ArrayList<TypedObject>();
	final Object global = "global";
	final TypedObject globalTypedObject = new GenericTypedObject(global);
	final FlowEvaluator flowEvaluator = new DoTraverse();
	final ScopeStack scopeStack = new ScopeStack(flowEvaluator,globalTypedObject);

	@Test
	public void initially() {
		assertThat(scopeStack.objectsForLookup(),hasSubjects(global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList()));
		assertThat(scopeStack.poppedAtEndOfTable(),is(emptyList));
	}
	@Test
	public void onePush() {
		scopeStack.push(someTypedObject);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
		assertThat(scopeStack.poppedAtEndOfTable(),is(emptyList));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(list(someTypedObject)));
	}
	@Test
	public void onePushWithSut() {
		scopeStack.push(doTypedObjectWithSomeSut);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(doTraverse,something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
		assertThat(scopeStack.poppedAtEndOfTable(),is(emptyList));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(list(doTypedObjectWithSomeSut)));
	}
	@Test
	public void sameOnePushedTwice() {
		scopeStack.push(someTypedObject);
		scopeStack.push(someTypedObject);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
		assertThat(scopeStack.poppedAtEndOfTable(),is(list(someTypedObject)));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(list(someTypedObject)));
	}
	@Test
	public void onePushAndPopAtEndOfStorytest() {
		scopeStack.push(someTypedObject);
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(list(someTypedObject)));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(emptyList));
		assertThat(scopeStack.objectsForLookup(),hasSubjects(global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList()));
	}
	@Test
	public void twoPushes() {
		scopeStack.push(someTypedObject);
		scopeStack.push(otherTypedObject);
		assertThat(scopeStack.objectsForLookup(),
				hasSubjects(other,something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(other.getClass(),something.getClass())));
	}
	@Test
	public void twoPushesAndTwoPops() {
		scopeStack.push(someTypedObject);
		scopeStack.push(otherTypedObject);
		assertThat(scopeStack.poppedAtEndOfTable(),is(list(otherTypedObject)));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(list(someTypedObject)));
	}
	@Test
	public void pushSuiteEvaluator() {
		scopeStack.push(suiteTypedObject);
		assertThat(scopeStack.objectsForLookup(),
				hasSubjects(suiteTypedObject.getSubject(),global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList()));
		assertThat(scopeStack.poppedAtEndOfTable(),is(emptyList));
		assertThat(scopeStack.poppedAtEndOfStorytest(),is(emptyList));
	}
	@Test
	public void restoreStateAfterNoChange() {
		scopeStack.push(someTypedObject);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		IScopeState currentState = scopeStack.currentState();
		assertThat(currentState.restore().isEmpty(),is(true));
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
	}
	@Test
	public void restoreStateAfterChange() {
		scopeStack.push(someTypedObject);
		IScopeState currentState = scopeStack.currentState();
		scopeStack.push(otherTypedObject);
		assertThat(currentState.restore(),is(list(otherTypedObject)));
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
	}
	@Test(expected=FitLibraryException.class)
	public void selectUnknown() {
		scopeStack.select("unknown");
	}
	@Test
	public void addNamedObjectAndSelect() {
		final TestResults testResults = TestResultsFactory.testResults();
		scopeStack.addNamedObject("x",someTypedObject,TableFactory.row(),testResults);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));

		scopeStack.select("x");
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass())));
	}
	@Test
	public void addTwoNamedObjectAndSelectBetweenThem() {
		final TestResults testResults = TestResultsFactory.testResults();
		scopeStack.addNamedObject("x",someTypedObject,TableFactory.row(),testResults);
		scopeStack.addNamedObject("y",otherTypedObject,TableFactory.row(),testResults);
		assertThat(scopeStack.objectsForLookup(),hasSubjects(something,other,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(something.getClass(),other.getClass())));

		scopeStack.select("y");
		assertThat(scopeStack.objectsForLookup(),hasSubjects(other,something,global,flowEvaluator));
		assertThat(scopeStack.possibleClasses(),is(classList(other.getClass(),something.getClass())));
	}

	static Matcher<List<TypedObject>> hasSubjects(Object... expected) {
		return new HasTypedSubjectsMatcher(expected);
	}
	private <T> List<T> list(T... ss) {
		return CollectionUtility.list(ss);
	}
	private List<Class<?>> classList(Class<?>... classes) {
		ArrayList<Class<?>> results = new ArrayList<Class<?>>();
		for (Class<?> type : classes)
			results.add(type);
		return results;
	}
}
