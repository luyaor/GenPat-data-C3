/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fit;

import fitlibrary.exception.classes.ConstructorNotVisible;
import fitlibrary.exception.classes.NoNullaryConstructor;
import fitlibrary.exception.classes.UnknownClassException;
import fitlibrary.table.Cell;
import fitlibrary.table.RowOnParse;
import fitlibrary.table.TableOnParse;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;

/** Needed to get at the fixture of the first table of the storytest page.
 */
public class FixtureBridge extends Fixture {
	public Object firstObject(Parse tables, TestResults results) {
		if (tables != null) {
			return getFixture(new TableOnParse(tables.at(0)),results);
		}
		return null;
	}
	public Object getFixture(TableOnParse table, TestResults results) {
		Cell headingCell = table.row(0).cell(0);
		try {
			String className = headingCell.text().replaceAll(" ","");
			try {
		        Fixture fixture = loadFixture(className);
		        fixture.counts = counts;
		        fixture.summary = summary;
		        fixture.args = getArgsForTable(table.row(0));
		        return fixture;
			} catch (Exception e) {
				try {
					return ClassUtility.newInstance(className);
				} catch (NoSuchMethodException ex) {
					throw new NoNullaryConstructor(className);
				} catch (ClassNotFoundException ex) {
					if (ex.getCause() != null)
						throw new UnknownClassException(className+": "+ex.getCause().getLocalizedMessage());
					throw new UnknownClassException(className);
				} catch (InstantiationException ex) {
					throw new NoNullaryConstructor(className);
				} catch (IllegalAccessException ex) {
					throw new ConstructorNotVisible(className);
				}
			}
		}
		catch (Throwable e) {
			headingCell.error(results, e);
			return null;
		}
	}

    String[] getArgsForTable(RowOnParse row) {
        String[] arguments = new String[row.size()-1];
        for (int i = 1; i < row.size(); i++)
        	arguments[i-1] = row.text(i,null);
        return arguments;
    }
}
