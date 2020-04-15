/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibraryGeneric.list;

import java.util.ArrayList;
import java.util.List;

import fitlibrary.closure.CalledMethodTarget;
import fitlibrary.exception.IgnoredException;
import fitlibrary.exception.classes.NoNullaryConstructor;
import fitlibrary.exception.classes.NotSubclassFromClassFactoryMethod;
import fitlibrary.exception.classes.NullFromClassFactoryMethod;
import fitlibrary.global.PlugBoard;
import fitlibrary.object.DomainObjectSetUpTraverse;
import fitlibrary.table.Cell;
import fitlibrary.table.Row;
import fitlibrary.table.Table;
import fitlibrary.traverse.Traverse;
import fitlibrary.traverse.workflow.DoTraverse;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;

public class ListSetUpTraverse2 extends DoTraverse {
    private final Class<?> componentType;
    private CalledMethodTarget[] targets;
    private List<Object> list = new ArrayList<Object>();

    public ListSetUpTraverse2(Class<?> componentType) {
    	this.componentType = componentType;
    }
    @Override
	public Object interpretAfterFirstRow(Table table, TestResults testResults) {
        try {
            Row firstRow = table.elementAt(0);
            int classColumn = findClassColumn(firstRow);
            if (classColumn < 0) {
                Object element = ClassUtility.createElement(componentType,this); // Only used to bind setters
				bindFirstRowToTargetsForObject(element,firstRow,firstRow,testResults);
            }
            for (int rowNo = 1; rowNo < table.size(); rowNo++)
                processRow(firstRow,classColumn,table.elementAt(rowNo),testResults);
        } catch (IgnoredException e) {
        	//
        } catch (NoSuchMethodException e) {
        	table.error(testResults, new NoNullaryConstructor(componentType));
        } catch (Exception e) {
            table.error(testResults,e);
        }
        return list;
    }
	private int findClassColumn(Row row) {
        for (int cellNo = 0; cellNo < row.size(); cellNo++) {
            Cell cell = row.elementAt(cellNo);
            if (DomainObjectSetUpTraverse.givesClass(cell,this))
                return cellNo;
        }
        return -1;
    }
    private void bindFirstRowToTargetsForObject(Object element, Row firstRow, Row row, TestResults testResults) {
        setSystemUnderTest(element);
        targets = new CalledMethodTarget[firstRow.size()];
        for (int i = 0; i < firstRow.size(); i++) {
            Cell cell = firstRow.elementAt(i);
            if (!DomainObjectSetUpTraverse.givesClass(cell,this)) {
                try {
                	targets[i] = PlugBoard.lookupTarget.findSetterOnSut(cell.text(this), this);
                } catch (Exception e) {
                    row.elementAt(i).error(testResults,e);
                }
            }
        }
    }
    private void processRow(Row firstRow, int classColumn, Row row, TestResults testResults) throws Exception {
        Object element = createElement(firstRow,classColumn,row,testResults);
        setSystemUnderTest(element);
		callStartCreatingObjectMethod(element);
        list.add(element);
        for (int i = 0; i < row.size(); i++) {
            if (i != classColumn && targets[i] != null) {
                targets[i].setTypedSubject(Traverse.asTypedObject(element));
                Cell cell = row.elementAt(i);
                try {
                    targets[i].invoke(cell,testResults);
                } catch (Exception e) {
                    cell.error(testResults,e);
                }
            }
        }
		callEndCreatingObjectMethod(element);
    }
    private Object createElement(Row firstRow, int classColumn, Row row, TestResults testResults) throws Exception {
    	if (classColumn < 0)
			return ClassUtility.createElement(componentType,this);
    	String typeName = row.text(classColumn,this);
    	Class<?> findClass = PlugBoard.lookupTarget.findClassFromFactoryMethod(this, componentType, typeName);
    	if (findClass == null)
    		throw new NullFromClassFactoryMethod(typeName);
    	if (!componentType.isAssignableFrom(findClass))
    		throw new NotSubclassFromClassFactoryMethod(findClass,componentType);
    	try {
    		Object sut = ClassUtility.createElement(findClass,this);
    		bindFirstRowToTargetsForObject(sut,firstRow,row,testResults);
    		return sut;
    	} catch (NoSuchMethodException e) {
    		row.error(testResults, new NoNullaryConstructor(findClass));
    		throw new IgnoredException();
    	} catch (Exception e) {
    		row.elementAt(0).error(testResults, e);
    		throw new IgnoredException();
    	}
    }
    public List<Object> getResults() {
        return list;
    }
}
