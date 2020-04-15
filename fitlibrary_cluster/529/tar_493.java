/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fit.Parse;
import fitlibrary.dynamicVariable.VariableResolver;
import fitlibrary.exception.FitLibraryShowException;
import fitlibrary.exception.table.MissingCellsException;
import fitlibrary.global.PlugBoard;
import fitlibrary.utility.ExtendedCamelCase;
import fitlibrary.utility.TestResults;

public class RowOnParse extends ParseNode<Cell> implements Row {
    private boolean rowIsHidden = false;
    
	public RowOnParse(Parse parse) {
        super(parse);
    }
    public RowOnParse() {
        this(new Parse("tr","",null,null));
    }
    @Override
	public int size() {
    	if (parse.parts == null)
    		return 0;
        return parse.parts.size();
    }
    @Override
	public Cell elementAt(int i) {
        if (!elementExists(i))
            throw new MissingCellsException("");
        return new CellOnParse(parse.parts.at(i));
    }
    @Override
	public String toString() {
        return toString("Row",parse.parts);
    }
    @Override
	public void pass(TestResults testResults) {
    	if (rowIsHidden)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.pass(testResults);
    }
    @Override
	public void fail(TestResults testResults) {
    	if (rowIsHidden)
    		System.out.println("Bug: colouring a cell in a hidden table");
    	super.fail(testResults);
    }
    @Override
	public void error(TestResults testResults, Throwable throwable) {
		Throwable e = PlugBoard.exceptionHandling.unwrapThrowable(throwable);
		if (e instanceof FitLibraryShowException)
        	handleShow((FitLibraryShowException) e);
        else
        	elementAt(0).error(testResults,e);
    }
    private void handleShow(FitLibraryShowException exception) {
    	Cell cell = addCell();
    	cell.setText(exception.getResult().getHtmlString());
    	cell.shown();
    }
    public String text(int i, VariableResolver resolver) {
        return elementAt(i).text(resolver);
    }
   public void missing(TestResults testResults) {
        elementAt(0).expectedElementMissing(testResults);
    }
    public Cell addCell() {
    	Cell cell = TableFactory.cell("");
		add(cell);
		return cell;
    }
    public void add(Cell cell) {
    	if (rowIsHidden)
    		System.out.println("Bug: Adding a cell to a hidden row in a table");
        if (parse.parts == null)
            parse.parts = cell.parse();
        else
            parse.parts.last().more = cell.parse();
    }
	public Cell addCell(String text) {
        Cell cell = TableFactory.cell(text);
        add(cell);
        return cell;
	}
    public Cell addCell(String text, int cols) {
        Cell cell = new CellOnParse(text);
        cell.setColumnSpan(cols);
        add(cell);
        return cell;
    }
    @Override
	public boolean equals(Object object) {
        if (!(object instanceof RowOnParse))
            return false;
        RowOnParse other = (RowOnParse)object;
        if (other.size() != size())
            return false;
        for (int i = 0; i < size(); i++)
            if (!elementAt(i).equals(other.elementAt(i)))
                return false;
        return true;
    }
	public RowOnParse elementsFrom(int i) {
		// Can be an empty row
		return new RowOnParse(new Parse("tr","",parse.parts.at(i),null));
	}
	public void ignore(TestResults testResults) {
		for (int i = 0; i < size(); i++)
			elementAt(i).ignore(testResults);
	}
	public boolean isEmpty() {
		return size() == 0;
	}
	public void setIsHidden() {
		this.rowIsHidden  = true;
		for (int i = 0; i < size(); i++)
			elementAt(i).setIsHidden();
	}
	public Cell lastCell() {
		return elementAt(size()-1);
	}
	public void addCommentRow(CellOnParse cell) {
		RowOnParse commentRow = new RowOnParse();
		commentRow.addCell("note");
		commentRow.add(cell);
		Parse next = parse.more;
		parse.more = commentRow.parse;
		commentRow.parse.more = next;
	}
	public Row rowTo(int from, int upto) {
		Row row = TableFactory.row();
		for (int i = from; i < upto; i++)
			row.add(TableFactory.cell(elementAt(i)));
		return row;
	}
	public void passKeywords(TestResults testResults) {
		for (int i = 0; i < size(); i += 2)
			elementAt(i).pass(testResults);
	}
	public Row deepCopy() {
		Row rowCopy = TableFactory.row();
		for (int i = 0; i < size(); i++)
			rowCopy.add(TableFactory.cell(elementAt(i).fullText()));
		return rowCopy;
	}
	public void clear() {
		parse.parts = null;
	}
	public String methodNameForPlain(VariableResolver resolver) {
		String name = "";
		for (int i = 0; i < size(); i += 2) {
			name += text(i,resolver);
			if ((i+1) < size())
				name += "|";
		}
		return name;
	}
	public String methodNameForCamel(VariableResolver resolver) {
		String name = "";
		for (int i = 0; i < size(); i += 2)
			name += text(i,resolver)+" ";
		return ExtendedCamelCase.camel(name.trim());
	}
	public int argumentCount() {
		return size() / 2;
	}
	public boolean hasFurtherRows() {
		return parse.more != null;
	}
	public int getColumnSpan() {
		int col = 0;
		for (int i = 0; i < size(); i++)
			col += elementAt(i).getColumnSpan();
		return col;
	}
	public void setColumnSpan(int span) {
		if (isEmpty())
			addCell();
		lastCell().setColumnSpan(span - getColumnSpan() + lastCell().getColumnSpan());
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public void removeElementAt(int i) {
		if (i == 0)
			parse.parts = parse.parts.more;
		else {
			elementAt(i-1).parse().more = null;
			elementAt(i-1).parse().trailer = "";
		}
	}
	@Override
	public Iterator<Cell> iterator() {
		List<Cell> list = new ArrayList<Cell>();
		for (int i = 0; i < size(); i++)
			list.add(elementAt(i));
		return list.iterator();
	}
}
