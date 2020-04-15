/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 */

package fitlibrary.traverse.workflow.caller;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import fit.Fixture;
import fitlibrary.exception.classes.ConstructorNotVisible;
import fitlibrary.exception.classes.NoNullaryConstructor;
import fitlibrary.flow.DoFlow;
import fitlibrary.table.Row;
import fitlibrary.table.TableOnParse;
import fitlibrary.traverse.Evaluator;
import fitlibrary.traverse.workflow.DoCaller;
import fitlibrary.typed.TypedObject;
import fitlibrary.utility.ClassUtility;
import fitlibrary.utility.TestResults;
import fitlibraryGeneric.typed.GenericTypedObject;

public class CreateFromClassNameCaller extends DoCaller {
	private static final ThreadLocal<Set<String>> packages = // Put into Runtime
		new ThreadLocal<Set<String>> () {
		@Override
		protected Set<String> initialValue() {
			HashSet<String> hashSet = new HashSet<String>();
			hashSet.add("fit.");
			return hashSet;
		}
	};
	private final String className;
	private Object object = null;
	private Exception exceptionToThrow = null;

	public CreateFromClassNameCaller(Row row, Evaluator doTraverse) {
		String name = row.text(0,doTraverse).trim();
		this.className = substituteName(name);
		// Later, the following will handle constructor arguments, and etc
		if (DoFlow.IS_ACTIVE && validClassName())
			try {
				object = ClassUtility.newInstance(determineFullClass());
				if (object instanceof Fixture && row.size() > 1)
					handleArgs((Fixture)object,row);
			} catch (NoSuchMethodException ex) {
				exceptionToThrow = new NoNullaryConstructor(className);
			} catch (InstantiationException ex) {
				exceptionToThrow = new NoNullaryConstructor(className);
			} catch (IllegalAccessException ex) {
				exceptionToThrow = new ConstructorNotVisible(className);
			} catch (InvocationTargetException ex) {
				exceptionToThrow = ex;
			} catch (Throwable e) {
				// Nothing to do
			}
	}
	private boolean validClassName() {
		return !className.isEmpty() && !className.contains(" ") &&
			(className.contains(".") || Character.isUpperCase(className.charAt(0)));
	}
	private String substituteName(String name) {
		if ("Import".equals(name) || "fit.Import".equals(name) || "ImportFixture".equals(name) || "fit.ImportFixture".equals(name))
			return "fitlibrary.DefaultPackages";
		return name;
	}
	private Class<?> determineFullClass() throws ClassNotFoundException {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			try {
				return Class.forName(className+"Fixture");
			} catch (Exception e1) {
				for (String s : packages.get()) {
					try {
						return Class.forName(s+className);
					} catch (Exception e2) {
						try {
							return Class.forName(s+className+"Fixture");
						} catch (ClassNotFoundException e3) {
							// Do nothing
						}
					}
				}
			}
		}
		throw new ClassNotFoundException(className);
	}
	private void handleArgs(Fixture fixture, Row row) {
		fixture.getArgsForTable(new TableOnParse(row).parse());
	}
	@Override
	public boolean isValid() {
		return object != null || exceptionToThrow != null;
	}
	@Override
	public String ambiguityErrorMessage() {
		return "class " + className;
	}
	@Override
	public TypedObject run(Row row, TestResults testResults) throws Exception {
		if (exceptionToThrow != null)
			throw exceptionToThrow;
		return new GenericTypedObject(object);
	}
	public static void addDefaultPackage(String name) {
		packages.get().add(name+".");
	}
}
