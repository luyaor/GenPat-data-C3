/**
 * 
 */
package org.junit.experimental.interceptor;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class TestWatchman implements StatementInterceptor {
	public Statement intercept(final Statement base,
			final FrameworkMethod method) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
					succeeded(method);
				} catch (Throwable t) {
					failed(t, method);
					throw t;
				}
			}
		};
	}

	public void succeeded(FrameworkMethod method) {
		return;
	}

	// TODO (Apr 28, 2009 10:50:47 PM): is this right? Is
	// FrameworkMethod too powerful?
	public void failed(Throwable e, FrameworkMethod method) {
		return;
	}
}