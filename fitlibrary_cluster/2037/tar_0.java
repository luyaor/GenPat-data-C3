/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.spec;

import java.util.Iterator;

import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.table.Cell;
import fitlibrary.table.TableElement;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class TablesCompare {
	private SpecifyErrorReport errorReport;

	public TablesCompare(SpecifyErrorReport errorReport) {
		this.errorReport = errorReport;
	}
	public boolean tablesEqual(String path, TableElement actual, TableElement expected) {
		boolean actualContainsHtmlDueToShow = false;
		if (actual instanceof Cell) {
			Cell actualCell = (Cell) actual;
			Cell expectedCell = (Cell) expected;
			if (!actualCell.fullText().isEmpty() && expectedCell.fullText().isEmpty() && expectedCell.hasEmbeddedTables()) {
				expectedCell.setText(expectedCell.last().getTrailer());
				expectedCell.last().setTrailer("");
			}
			if (!equals(actualCell.fullText(),expectedCell.fullText())) {
				if (!actualCell.hasEmbeddedTables() && expectedCell.hasEmbeddedTables()) {
					try {
						TableFactory.useOnLists(false);
						Tables actualTables = TableFactory.tables(new Parse(actualCell.fullText()));
						TableFactory.pop();
						if (!tablesEqual(path,actualTables,expectedCell.getEmbeddedTables()))
							return false;
						actualContainsHtmlDueToShow = true;
					} catch (FitParseException e) {
						errorReport.cellTextWrong(path,actualCell.fullText(),expectedCell.fullText());
						return false;
					}
				} else {
					errorReport.cellTextWrong(path,actualCell.fullText(),expectedCell.fullText());
					return false;
				}
			}
			actual = actualCell.getEmbeddedTables();
			expected = expectedCell.getEmbeddedTables();
		}
		if (!actualContainsHtmlDueToShow) {
			if (expected.getLeader().isEmpty() && actual.getLeader().equals("<html>"))
				;
			else if (!equals(actual.getLeader(),expected.getLeader())) {
				errorReport.leaderWrong(path, actual.getLeader(), expected.getLeader());
				return false;
			}
			if (expected.getTrailer().isEmpty() && actual.getTrailer().equals("</html>"))
				;
			else if (!equals(actual.getTrailer(),expected.getTrailer())) {
				errorReport.trailerWrong(path, actual.getTrailer(), expected.getTrailer());
				return false;
			}
			if (expected.getTagLine().isEmpty() && actual.getTagLine().equals("border=\"1\" cellspacing=\"0\""))
				;
			else if (!actual.getTagLine().equals(expected.getTagLine())) {
				errorReport.tagLineWrong(path, actual.getTagLine(), expected.getTagLine());
				return false;
			}
			if (actual.size() != expected.size()) {
				errorReport.sizeWrong(path,actual.size(),expected.size());
				return false;
			}
		}
		Iterator<TableElement> actuals = actual.iterator();
		Iterator<TableElement> expecteds = expected.iterator();
		int count = 0;
		while (actuals.hasNext()) {
			TableElement act = actuals.next();
			String nameOfElement = act.getType()+"["+count+"]";
			String pathFurther = path.isEmpty() ? nameOfElement : path + "." + nameOfElement;
			if (!tablesEqual(pathFurther,act,expecteds.next()))
				return false;
			count++;
		}
		return true;
	}
	private boolean equals(String actual, String expected) {
		String canonicalActual = canonical(actual);
		String canonicalExpected = canonical(expected);
		
		if ("IGNORE".equals(canonicalExpected))
			return true;
		String stackTrace = "class=\"fit_stacktrace\">";
		int start = canonicalExpected.indexOf(stackTrace);
		if (start >= 0)
			return canonicalActual.startsWith(canonicalExpected.substring(0,start+stackTrace.length()));
		String fitLabel = "<span class=\"fit_label\">";
		start = canonicalExpected.indexOf(fitLabel);
		if (start >= 0)
			return canonicalActual.startsWith(canonicalExpected.substring(0,start+fitLabel.length()));
		return canonicalActual.equals(canonicalExpected);
	}
	private String canonical(String s) {
		return ignoreFold(s).replaceAll("\t"," ").replaceAll("\r","").replaceAll("<hr>","").replaceAll("<hr/>","").replaceAll("<br>","").replaceAll("<br/>","").trim();
	}
	private String ignoreFold(String text) {
		String s = text;
		while (true) {
			int include = s.indexOf("<div class=\"included\">");
			if (include < 0)
				return s;
			int endDiv = s.indexOf("</div></div>");
			if (endDiv < 0)
				return s;
			s = s.substring(0,include)+s.substring(endDiv+"</div></div>".length());
		}
	}
}
