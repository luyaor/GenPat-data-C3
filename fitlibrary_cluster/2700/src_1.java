/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import junit.framework.TestCase;
import fit.Counts;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.DoFixture;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.TestResults;

public class TestParseTables extends TestCase {
    private Tables tables;
    private Counts counts;
    private TestResults testResults;
    private DoFixture doFixture = new DoFixture();
    
    @Override
	public void setUp() throws FitParseException {
        tables = new Tables(new Parse("<table><tr><td>1</td></tr><tr><td>2</td><td>3</td></tr></table>\n"+
                "<table><tr><td>1</td></tr><tr><td>2</td><td>3</td></tr></table>\n"+
                "<table><tr><td>1</td></tr><tr><td>2</td><td>3</td></tr></table>\n"));
        counts = new Counts();
        testResults = TestResults.create(counts);
    }
    public void testTables() {
        assertEquals(3,tables.size());
    }
    public void testTable0() {
        Table table = tables.table(0);
        assertEquals(2,table.size());
        assertTrue(!table.rowExists(-1));
        assertTrue(table.rowExists(0));
        assertTrue(table.rowExists(1));
        assertTrue(!table.rowExists(2));
        assertEquals(table.row(1),table.lastRow());
        try {
            table.row(2);
            fail("Exception expected");
        } catch (Exception e) {
        	//
        }
    }
    public void testTable0Right() {
        Table table0 = tables.table(0);
        table0.pass(testResults);
        assertTrue(table0.row(0).didPass());
        assertEquals("1 right, 0 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testTable0Wrong() {
        Table table0 = tables.table(0);
        table0.fail(testResults);
        assertTrue(table0.didFail());
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testTable0Missing() {
        Table table0 = tables.table(0);
        table0.missing(testResults);
        assertTrue(table0.row(0).cell(0).didFail());
        assertEquals("1 missing",table0.row(0).text(0,doFixture));
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testTable0Ignored() {
        Table table0 = tables.table(0);
        table0.ignore(testResults);
        assertTrue(table0.row(0).cell(0).wasIgnored());
        assertEquals("0 right, 0 wrong, 1 ignored, 0 exceptions",counts.toString());
    }
    public void testTable0Exception() {
        Table table0 = tables.table(0);
        table0.error(testResults,new RuntimeException("Forced"));
        assertTrue(table0.row(0).cell(0).hadError());
        assertTrue(table0.row(0).text(0,doFixture).startsWith("1java.lang.RuntimeException: Forced"));
        assertEquals("0 right, 0 wrong, 0 ignored, 1 exceptions",counts.toString());
    }

    public void testRow0() {
        Row row = getRow(0,0);
        assertEquals(1,row.size());
        assertTrue(!row.cellExists(-1));
        assertTrue(row.cellExists(0));
        assertTrue(!row.cellExists(1));
        try {
            row.cell(1);
            fail("Exception expected");
        } catch (Exception e) {
        	//
        }
        assertEquals("1",row.text(0,doFixture));
    }
    public void testRow0Right() {
        Row row0 = getRow(0,0);
        row0.pass(testResults);
        assertTrue(row0.didPass());
        assertEquals("1 right, 0 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testRow0Wrong() {
        Row row0 = getRow(0,0);
        row0.fail(testResults);
        assertTrue(row0.didFail());
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testRow0Missing() {
        Row row0 = getRow(0,0);
        row0.missing(testResults);
        assertTrue(row0.cell(0).didFail());
        assertEquals("1 missing",row0.text(0,doFixture));
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testRow0Ignored() {
        Row row0 = getRow(0,0);
        row0.ignore(testResults);
        assertTrue(row0.cell(0).wasIgnored());
        assertEquals("0 right, 0 wrong, 1 ignored, 0 exceptions",counts.toString());
    }
    public void testRow0Exception() {
        Row row0 = getRow(0,0);
        DoFixture doFixture2 = new DoFixture();
        doFixture2.counts = counts;
        row0.error(testResults,new RuntimeException("Forced"));
        assertTrue(row0.cell(0).hadError());
        assertTrue(row0.text(0,doFixture2).startsWith("1java.lang.RuntimeException: Forced"));
        assertEquals("0 right, 0 wrong, 0 ignored, 1 exceptions",counts.toString());
    }

    public void testCell0Text() {
        assertEquals("1",getCell(0,0,0).text(new DoFixture()));
        assertEquals("0 right, 0 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testCell0Right() {
        Cell cell0 = getCell(0,0,0);
        cell0.pass(testResults);
        assertTrue(cell0.didPass());
        assertEquals("1 right, 0 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testCell0Wrong() {
        Cell cell0 = getCell(0,0,0);
        cell0.fail(testResults);
        assertTrue(cell0.didFail());
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testCell0Missing() {
        Cell cell0 = getCell(0,0,0);
        cell0.expectedElementMissing(testResults);
        assertTrue(cell0.didFail());
        assertEquals("1 missing",cell0.text(new DoFixture()));
        assertEquals("0 right, 1 wrong, 0 ignored, 0 exceptions",counts.toString());
    }
    public void testCell0Ignored() {
        Cell cell0 = getCell(0,0,0);
        cell0.ignore(testResults);
        assertTrue(cell0.wasIgnored());
        assertEquals("0 right, 0 wrong, 1 ignored, 0 exceptions",counts.toString());
    }
    public void testCell0Exception() {
        Cell cell0 = getCell(0,0,0);
        DoFixture doFixture2 = new DoFixture();
        doFixture2.counts = counts;
        cell0.error(testResults,new RuntimeException("Forced"));
        assertTrue(cell0.hadError());
        assertTrue(cell0.text(doFixture2).startsWith("1java.lang.RuntimeException: Forced"));
        assertEquals("0 right, 0 wrong, 0 ignored, 1 exceptions",counts.toString());
    }

    private Cell getCell(int tableNo, int rowNo, int cellNo) {
        return getRow(tableNo, rowNo).cell(cellNo);
    }
    private Row getRow(int tableNo, int rowNo) {
        return tables.table(tableNo).row(rowNo);
    }
}
