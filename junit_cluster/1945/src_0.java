/**
 * 
 */
package org.junit.internal.runners.model;


import org.junit.Ignore;
import org.junit.Assume.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class EachTestNotifier {
	private RunNotifier fNotifier;

	private Description fDescription;

	public EachTestNotifier(RunNotifier notifier, Description description) {
		fNotifier= notifier;
		fDescription= description;
	}
	
	public void addFailure(Throwable targetException) {
		if (targetException instanceof MultipleFailureException) {
			MultipleFailureException mfe= (MultipleFailureException) targetException;
			for (Throwable each : mfe.getFailures()) {
				addFailure(each);
			}
			return;
		}
		fNotifier.fireTestFailure(new Failure(fDescription, targetException));
	}

	public void fireTestFinished() {
		fNotifier.fireTestFinished(fDescription);
	}

	public void fireTestStarted() {
		fNotifier.fireTestStarted(fDescription);
	}

	public void fireTestIgnored() {
		fNotifier.fireTestIgnored(fDescription);
		fNotifier.fireTestAssumptionFailed(fDescription, makeIgnoredException(fDescription));
	}

	// TODO: (Dec 9, 2007 9:25:42 PM) Static ignores should not have backtraces
	
	private AssumptionViolatedException makeIgnoredException(
			Description description) {
		String reason= description.getAnnotation(Ignore.class).value();
		return new AssumptionViolatedException(reason);
	}

	public void addFailedAssumption(AssumptionViolatedException e) {
		fNotifier.fireTestAssumptionFailed(fDescription, e);
	}
}