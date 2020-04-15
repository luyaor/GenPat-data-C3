/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.batch.resultsOut;

import java.util.Date;
import fit.Counts;
import fitlibrary.batch.trinidad.TestResult;

public class ParallelSuiteResult implements SuiteResult {
	private String name;
	private Counts counts=new Counts();
	private StringBuffer content;
	private boolean showPasses;
	
	public ParallelSuiteResult(String name, boolean showPasses){
		this.name = name;
		this.showPasses = showPasses;
		content = new StringBuffer(
				"<head><title>").append(name).append(
				"</title><link rel='stylesheet' type='text/css' href='fitnesse.css' media='screen'/>" +
				"<link rel='stylesheet' type='text/css' href='fitnesse_print.css' media='print'/>"+
				"</head><body><h2>").append(name).
				append("</h2><p>"+new Date()).
				append("<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>");
	}
	
	public String getContent() {
		appendRow(this,counts);
		return content+"</table></body></html>";
	}	
	public Counts getCounts() {
		return counts;
	}
	public String getName() {
		return name;
	}
	public void append(TestResult result){
			Counts resultCounts = result.getCounts();
			counts.tally(resultCounts);
			appendRow(result, resultCounts);							
	}

	private void appendRow(TestResult result, Counts resultCounts) {
		content.append("<tr class='").append(getCssClass(resultCounts)).append("'><td>");
		appendNameOrLink(result);
		content.append("</td><td>").append(resultCounts.right)
			.append("</td><td>").append(resultCounts.wrong)
			.append("</td><td>").append(resultCounts.exceptions)
			.append("</td></tr>");
	}

	private void appendNameOrLink(TestResult result) {
		if (showPasses || (result.getCounts().exceptions+result.getCounts().wrong) > 0)
			content.append("<a href='").append(result.getName()).append(".html'>").append(result.getName()).append("</a>");
		else
			content.append(result.getName());
	}
	private String getCssClass(Counts c){
		if (c.exceptions>0) return "error";
		if (c.wrong>0) return "fail";
		if (c.right>0) return "pass";
		return "plain";
	}
}