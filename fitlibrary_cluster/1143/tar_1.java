/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
*/

package fitlibrary.table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import fit.Fixture;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.exception.IgnoredException;
import fitlibrary.traverse.Traverse;

public class ExceptionHandlingStandard implements ExceptionHandling {
	public void mustBeThreadSafe() {
		//
	}
	public String exceptionMessage(Throwable throwable) {
		Throwable exception = unwrapThrowable(throwable);
        if (exception instanceof IgnoredException)
            return "";
        if (exception instanceof FitLibraryExceptionInHtml)
        	return "<hr/>" + Fixture.label(exception.getMessage());
        if (exception instanceof FitLibraryException)
            return "<hr/>" + Fixture.label(Traverse.escapeHtml(exception.getMessage()));
        final StringWriter buf = new StringWriter();
        exception.printStackTrace(new PrintWriter(buf));
        return "<hr><pre><div class=\"fit_stacktrace\">"
            + (buf.toString()) + "</div></pre>";
    }
	public Throwable unwrapThrowable(Throwable throwable) {
		Throwable exception = throwable;
		while (true) {
			if (exception.getCause() != null)
				exception = exception.getCause();
			else if (exception.getClass().equals(InvocationTargetException.class))
				exception = ((InvocationTargetException) exception).getTargetException();
			else
				return exception;
		}
	}
}
