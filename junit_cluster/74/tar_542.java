/**
 * 
 */
package junit.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Plan;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class JUnit4TestAdapterCache extends HashMap<Description, Test> {
	private static final long serialVersionUID = 1L;
	private static final JUnit4TestAdapterCache fInstance = new JUnit4TestAdapterCache();

	public static JUnit4TestAdapterCache getDefault() {
		return fInstance;
	}
	
	public Test asTest(Plan plan) {
		Description description= plan.getDescription();
		if (description.isSuite())
			return createTestSuite(plan);
		else
			return asTestCase(description);
	}

	private Test asTestCase(Description description) {
		if (!containsKey(description))
			put(description, createTestCase(description));
		return get(description);
	}

	private Test createTestCase(Description description) {
		return new JUnit4TestCaseFacade(description);
	}

	Test createTestSuite(Plan plan) {
		// TODO: shouldn't have to test for suite
		Description description= plan.getDescription();
		if (description.isTest())
			return new JUnit4TestCaseFacade(description);
		else {
			TestSuite suite = new TestSuite(description.getDisplayName());
			for (Plan child : plan.getChildren())
				suite.addTest(asTest(child));
			return suite;
		}
	}

	public RunNotifier getNotifier(final TestResult result,
			final JUnit4TestAdapter adapter) {
		RunNotifier notifier = new RunNotifier();
		notifier.addListener(new RunListener() {
			@Override
			public void testFailure(Failure failure) throws Exception {
				result.addError(asTestCase(failure.getDescription()), failure.getException());
			}

			@Override
			public void testFinished(Description description)
					throws Exception {
				result.endTest(asTestCase(description));
			}

			@Override
			public void testStarted(Description description)
					throws Exception {
				result.startTest(asTestCase(description));
			}
		});
		return notifier;
	}

	public List<Test> asTestList(Plan plan) {
		Description description= plan.getDescription();
		if (description.isTest())
			return Arrays.asList(asTestCase(description));
		else {
			List<Test> returnThis = new ArrayList<Test>();
			for (Plan child : plan.getChildren()) {
				returnThis.add(asTest(child));
			}
			return returnThis;
		}
	}

}