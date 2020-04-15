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
	public boolean equals(String actualString, String expectedString) {
		String actual = canonical(actualString);
		String expected = canonical(expectedString);
		
		if ("IGNORE".equals(expected))
			return true;
		String stackTrace = "class=\"fit_stacktrace\">";
		int startExpected = expected.indexOf(stackTrace);
		int startActual = actual.indexOf(stackTrace);
		if (startExpected != startActual)
			return false;
		if (startExpected >= 0)
			return actual.startsWith(expected.substring(0,startExpected));
		String fitLabel = "<span class=\"fit_label\">";
		String endFitLabel = "</span>";
		while (true) {
			startExpected = expected.indexOf(fitLabel);
			startActual = actual.indexOf(fitLabel);
			if (startExpected != startActual)
				return false;
			if (startExpected < 0)
				return actual.equals(expected);
			
			String expectedPrefix = expected.substring(0,startExpected);
			if (!actual.substring(0,startActual).equals(expectedPrefix))
				return false;
			int endExpected = expected.indexOf(endFitLabel,startExpected);
			int endActual = actual.indexOf(endFitLabel,startActual);
			if (endExpected < 0 || endActual < 0)
				return false;
			String actualLabel = actual.substring(startActual+fitLabel.length(),endActual);
			String expectedLabel = expected.substring(startExpected+fitLabel.length(),endExpected);
			if (!actualLabel.startsWith(expectedLabel))
				return false;
			actual = actual.substring(endActual+endFitLabel.length());
			expected = expected.substring(endExpected+endFitLabel.length());
		}
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
