/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * 20/10/2009
*/

package fitlibrary.table;

import java.io.PrintWriter;
import java.io.StringWriter;

import fit.Fixture;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.exception.FitLibraryExceptionInHtml;
import fitlibrary.exception.IgnoredException;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.ExceptionHandler;
import fitlibrary.utility.HtmlUtils;

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
            return "<hr/>" + Fixture.label(HtmlUtils.escapeHtml(exception.getMessage()));
        final StringWriter buf = new StringWriter();
        exception.printStackTrace(new PrintWriter(buf));
        return "<hr><pre><div class=\"fit_stacktrace\">"
            + (buf.toString()) + "</div></pre>";
    }
	public Throwable unwrapThrowable(Throwable throwable) {
		return ExceptionHandler.unwrap(throwable);
	}
}
