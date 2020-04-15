/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.UserLibraryClasspathContainer;
import org.eclipse.jdt.internal.core.UserLibraryManager;

import junit.framework.Test;

public class ClasspathInitializerTests extends ModifyingResourceTests {
	
public static class DefaultVariableInitializer implements VariablesInitializer.ITestInitializer {
	Map variableValues;
	
	/*
	 * values is [<var name>, <var value>]*
	 */
	public DefaultVariableInitializer(String[] values) {
		variableValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			variableValues.put(values[i], new Path(values[i+1]));
		}
	}
	
	public void initialize(String variable) throws JavaModelException {
		if (variableValues == null) return;
		JavaCore.setClasspathVariable(
			variable, 
			(IPath)variableValues.get(variable), 
			null);
	}
}

public static class DefaultContainerInitializer implements ContainerInitializer.ITestInitializer {
	
	public static class DefaultContainer implements IClasspathContainer {
		char[][] libPaths;
		public DefaultContainer(char[][] libPaths) {
			this.libPaths = libPaths;
		}
		public IClasspathEntry[] getClasspathEntries() {
			int length = this.libPaths.length;
			IClasspathEntry[] entries = new IClasspathEntry[length];
			for (int j = 0; j < length; j++) {
			    IPath path = new Path(new String(this.libPaths[j]));
			    if (path.segmentCount() == 1) {
			        entries[j] = JavaCore.newProjectEntry(path);
			    } else {
					entries[j] = JavaCore.newLibraryEntry(path, null, null);
			    }
			}
			return entries;
		}
		public String getDescription() {
			return "Test container";
		}
		public int getKind() {
			return IClasspathContainer.K_APPLICATION;
		}
		public IPath getPath() {
			return new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
		}
	}
	
	Map containerValues;
	CoreException exception;
	
	/*
	 * values is [<project name>, <lib path>[,<lib path>]* ]*
	 */
	public DefaultContainerInitializer(String[] values) {
		containerValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			final String projectName = values[i];
			final char[][] libPaths = CharOperation.splitOn(',', values[i+1].toCharArray());
			containerValues.put(
				projectName, 
				newContainer(libPaths)
			);
		}
	}
	protected DefaultContainer newContainer(final char[][] libPaths) {
		return new DefaultContainer(libPaths);
	}
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		if (containerValues == null) return;
		try {
			JavaCore.setClasspathContainer(
				containerPath, 
				new IJavaProject[] {project},
				new IClasspathContainer[] {(IClasspathContainer)containerValues.get(project.getElementName())}, 
				null);
		} catch (CoreException e) {
			this.exception = e;
			throw e;
		}
	}
}
// Simple container initializer, which keeps setting container to null
// (30920 - stackoverflow when setting container to null)
public class NullContainerInitializer implements ContainerInitializer.ITestInitializer {
	public boolean hasRun = false;
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
		hasRun = true;
		JavaCore.setClasspathContainer(
			containerPath, 
			new IJavaProject[] {project}, 
			new IClasspathContainer[] { null }, 
			null);
	}
}

public ClasspathInitializerTests(String name) {
	super(name);
}
public static Test suite() {
	return buildTestSuite(ClasspathInitializerTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testContainerInitializer11" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 2, 12 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
}
protected void tearDown() throws Exception {
	// Cleanup caches
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	manager.containers = new HashMap(5);
	manager.variables = new HashMap(5);

	super.tearDown();
}
public void testContainerInitializer01() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer02() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testContainerInitializer03() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
		createJavaProject(
				"P2", 
				new String[] {}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// change value of TEST_CONTAINER
		createFile("/P1/lib2.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib2.jar"}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		getJavaProject("P2").getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/* Ensure that initializer is not callled when resource tree is locked.
 * (regression test for bug 29585 Core Exception as resource tree is locked initializing classpath container)
 */
public void testContainerInitializer04() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"});
		ContainerInitializer.setInitializer(initializer);
		createJavaProject(
				"P2", 
				new String[] {""}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		createFile("/P2/X.java", "public class X {}");
		
		assertEquals("Should not get exception", null, initializer.exception);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/* 
 * 30920 - Stack overflow when container resolved to null
 */
public void testContainerInitializer05() throws CoreException {
	try {
		NullContainerInitializer nullInitializer = new NullContainerInitializer();
		ContainerInitializer.setInitializer(nullInitializer);
		createJavaProject(
				"P1", 
				new String[] {""}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"");
				
		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();

		// will trigger classpath resolution (with null container value)
		createFile("/P1/X.java", "public class X {}");
		assertDeltas(
			"Unexpected delta on startup", 
			"P1[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}"
		);
		assertTrue("initializer did not run", nullInitializer.hasRun);
		
		// next cp resolution request will rerun the initializer
		waitForAutoBuild();
		nullInitializer.hasRun = false; // reset		
		getJavaProject("P1").getResolvedClasspath(true);
		assertTrue("initializer did not run", nullInitializer.hasRun); // initializer should have run again (since keep setting to null)

		// assigning new (non-null) value to container
		waitForAutoBuild();
		createFile("/P1/lib.jar", "");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}));
		getJavaProject("P1").getResolvedClasspath(true);
		assertDeltas(
			"Unexpected delta after setting container", 
			"P1[*]: {CHILDREN}\n" + 
			"	<project root>[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			X.java[+]: {}\n" + 
			"\n" + 
			"P1[*]: {CONTENT}\n" + 
			"	ResourceDelta(/P1/lib.jar)[+]"
		);

	} catch (StackOverflowError e) {
		e.printStackTrace();
		assertTrue("stack overflow assigning container", false);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensures that running the initializer during a reconcile operation just after workspace startup
 * doesn't throw a NPE
 * (regression test for bug 48818 NPE in delta processor)
  */
public void testContainerInitializer06() throws CoreException {
    ICompilationUnit workingCopy = null;
	try {
		createProject("P1");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", ""}));
		createJavaProject(
				"P2", 
				new String[] {"src"}, 
				new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
				"bin");
		createFile(
			"/P2/src/X,java",
			"public class X {\n" +
			"}"
		);
				
		// change value of TEST_CONTAINER
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		workingCopy = getCompilationUnit("/P2/src/X.java");
		workingCopy.becomeWorkingCopy(null, null);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	src[*]: {CHILDREN}\n" + 
			"		<default>[*]: {CHILDREN}\n" + 
			"			[Working copy] X.java[+]: {PRIMARY WORKING COPY}"
		);
	} finally {
		stopDeltas();
		if (workingCopy != null) workingCopy.discardWorkingCopy();
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */
public void testContainerInitializer07() throws CoreException {
	try {
		boolean gotException = false;
		try {
			ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
				public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
					throw new OperationCanceledException("test");
				}});
			IJavaProject p1 = createJavaProject(
					"P1", 
					new String[] {}, 
					new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
					"");
			p1.getResolvedClasspath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		stopDeltas();
		deleteProject("P1");
	}
}
/*
 * Ensure that the stack doesn't blow up if initializer is missbehaving
 * (regression test for bug 61052 Flatten cp container initialization)
 */
public void testContainerInitializer08() throws CoreException {
	final int projectLength = 10;
	final String[] projects = new String[projectLength];
	for (int i = 0; i < projectLength; i++) {
		projects[i] = "P" + i;
	}
	try {
		String[] projectRefs = new String[(projectLength-1) * 2];
		for (int i = 0; i < projectLength-1; i++) {
			projectRefs[i*2] = "P" + i;
			projectRefs[(i*2)+1] = "/P" + i + "/test.jar";
		}
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(projectRefs) {
			void foo(int n) {
				if (n > 0) {
					foo(n-1);
					return;
				}
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				for (int i = 0; i < projectLength-1; i++) {
					try {
						JavaCore.create(root.getProject(projects[i])).getResolvedClasspath(true);
					} catch (JavaModelException e) {
						// project doesn't exist: ignore
					}
				}
			}
			public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
				foo(500);
				super.initialize(containerPath, project);
			}
		});
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < projectLength; i++) {
					createProject(projects[i]);
					editFile(
						"/" + projects[i] + "/.project",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
						"<projectDescription>\n" + 
						"	<name>" + projects[i] + "</name>\n" + 
						"	<comment></comment>\n" + 
						"	<projects>\n" + 
						(i == 0 ? "" : "<project>" + projects[i-1] + "</project>\n") +
						"	</projects>\n" + 
						"	<buildSpec>\n" + 
						"		<buildCommand>\n" + 
						"			<name>org.eclipse.jdt.core.javabuilder</name>\n" + 
						"			<arguments>\n" + 
						"			</arguments>\n" + 
						"		</buildCommand>\n" + 
						"	</buildSpec>\n" + 
						"	<natures>\n" + 
						"		<nature>org.eclipse.jdt.core.javanature</nature>\n" + 
						"	</natures>\n" + 
						"</projectDescription>"
					);
					createFile(
						"/" + projects[i] + "/.classpath",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						(i == 0 ? "" : "<classpathentry kind=\"src\" path=\"/" + projects[i-1] + "\"/>\n") +					 
						"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
						"	<classpathentry kind=\"output\" path=\"\"/>\n" +
						"</classpath>"
					);
				}
			}
		}, null);
		getJavaProject("P0").getResolvedClasspath(true);
	} finally {
		stopDeltas();
		deleteProjects(projects);
	}
}
/*
 * Ensure that a StackOverFlowError is not thrown if the initializer asks for the resolved classpath
 * that is being resolved.
 * (regression test for bug 61040 Should add protect for reentrance to #getResolvedClasspath)
 */
public void testContainerInitializer09() throws CoreException {
	try {
		ClasspathInitializerTests.DefaultContainerInitializer initializer = new ClasspathInitializerTests.DefaultContainerInitializer(new String[] {"P1", "/P1/lib.jar"}) {
			protected DefaultContainer newContainer(char[][] libPaths) {
				return new DefaultContainer(libPaths) {
					public IClasspathEntry[] getClasspathEntries() {
						try {
							getJavaProject("P1").getResolvedClasspath(true);
						} catch (JavaModelException e) {
							// project doesn't exist: ignore
						}
						return super.getClasspathEntries();
					}
				};
			}
		};
		ContainerInitializer.setInitializer(initializer);
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createProject("P1");
				editFile(
					"/P1/.project",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
					"<projectDescription>\n" + 
					"	<name>P1</name>\n" + 
					"	<comment></comment>\n" + 
					"	<projects>\n" + 
					"	</projects>\n" + 
					"	<buildSpec>\n" + 
					"		<buildCommand>\n" + 
					"			<name>org.eclipse.jdt.core.javabuilder</name>\n" + 
					"			<arguments>\n" + 
					"			</arguments>\n" + 
					"		</buildCommand>\n" + 
					"	</buildSpec>\n" + 
					"	<natures>\n" + 
					"		<nature>org.eclipse.jdt.core.javanature</nature>\n" + 
					"	</natures>\n" + 
					"</projectDescription>"
				);
				createFile(
					"/P1/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<classpath>\n" +
					"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
					"	<classpathentry kind=\"output\" path=\"\"/>\n" +
					"</classpath>"
				);
			}
		}, null);
		getJavaProject("P1").getResolvedClasspath(true);
	} finally {
		stopDeltas();
		ContainerInitializer.setInitializer(null);
		deleteProject("P1");
	}
}
/*
 * Ensure that creating a Java project initializes a container and refreshes the external jar at the same time
 * without throwing a ConcurrentModificationException
 * (regression test for bug 63534 ConcurrentModificationException after "catching up")
 */
public void testContainerInitializer10() throws CoreException {
	class LogListener implements ILogListener {
    	IStatus log;
        public void logging(IStatus status, String plugin) {
            this.log = status;
        }
	}
	LogListener listener = new LogListener();
	try {
		Platform.addLogListener(listener);
		final IJavaProject p1 = createJavaProject("P1");
		final IJavaProject p2 = createJavaProject("P2");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P3", "/P1"}) {
	        public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
	            super.initialize(containerPath, project);
	            getJavaModel().refreshExternalArchives(new IJavaElement[] {p1}, null);
	        }
		});
		getWorkspace().run(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                p2.setRawClasspath(new IClasspathEntry[] {JavaCore.newSourceEntry(new Path("/P2/src"))}, new Path("/P2/bin"), null);
				createProject("P3");
                editFile(
                	"/P3/.project",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                	"<projectDescription>\n" + 
                	"	<name>P3</name>\n" + 
                	"	<comment></comment>\n" + 
                	"	<projects>\n" + 
                	"	</projects>\n" + 
                	"	<buildSpec>\n" + 
                	"		<buildCommand>\n" + 
                	"			<name>org.eclipse.jdt.core.javabuilder</name>\n" + 
                	"			<arguments>\n" + 
                	"			</arguments>\n" + 
                	"		</buildCommand>\n" + 
                	"	</buildSpec>\n" + 
                	"	<natures>\n" + 
                	"		<nature>org.eclipse.jdt.core.javanature</nature>\n" + 
                	"	</natures>\n" + 
                	"</projectDescription>\n"
                );
                createFile(
                	"/P3/.classpath",
                	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                	"<classpath>\n" + 
                	"	<classpathentry kind=\"src\" path=\"\"/>\n" + 
                	"	<classpathentry kind=\"var\" path=\"JCL_LIB\"/>\n" + 
                	"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" + 
                	"	<classpathentry kind=\"output\" path=\"\"/>\n" + 
                	"</classpath>"
                );
             }
        }, null);
		
		assertEquals("Should not get any exception in log", null, listener.log);
	} finally {
	    Platform.removeLogListener(listener);
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}
/*
 * Ensure that a classpath initializer is not run on shutdown
 * (regression test for bug 93941 Classpath initialization on shutdown)
 */
public void testContainerInitializer11() throws CoreException {
	boolean hasExited = false;
	try {
		ContainerInitializer.setInitializer(null);
		createJavaProject(
			"P", 
			new String[] {}, 
			new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, 
			"");
		simulateExitRestart();
		ClasspathInitializerTests.DefaultContainerInitializer initializer = new ClasspathInitializerTests.DefaultContainerInitializer(new String[] {}) {
			public void initialize(IPath containerPath,IJavaProject project) throws CoreException {
				assertTrue("Should not initialize container on shutdown", false);
			}
		};
		ContainerInitializer.setInitializer(initializer);
		simulateExit();
		hasExited = true;
	} finally {
		ContainerInitializer.setInitializer(null);
		if (hasExited)
			simulateRestart();
		deleteProject("P");
	}
}
public void testVariableInitializer1() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}));
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer2() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(getFile("/P1/lib.jar"));
		assertEquals("Unexpected source attachment path", "/P1/src.zip", root.getSourceAttachmentPath().toString());
		assertEquals("Unexpected source attachment root path", "src", root.getSourceAttachmentRootPath().toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer3() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		getJavaProject("P2").getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			""
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer4() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer5() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				JavaCore.getClasspathVariable("TEST_SRC");
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Initializing TEST_SRC\n" +
			"Setting variable TEST_SRC to test_src\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that if the initializer doesn't initialize a variable, it can be
 * initialized later on.
 */
public void testVariableInitializer6() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) {
				// do nothing
				buffer.append("Ignoring request to initialize");
			}
		});
		IPath path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after initializer was called",
			null,
			path);
		IPath varValue = new Path("src.zip");
		JavaCore.setClasspathVariable("TEST_SRC", varValue, null);
		path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after setting it",
			varValue,
			path);
	} finally {
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer7() throws CoreException {
	try {
		createProject("P1");
		createFile("/P1/lib.jar", "");
		createFile("/P1/src.zip", "");
		String[] variableValues = new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		};
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(variableValues));
		createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// change value of TEST_LIB
		createFile("/P1/lib2.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib2.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));

		// simulate state on startup
		simulateExitRestart();
		
		startDeltas();
		//JavaModelManager.CP_RESOLVE_VERBOSE=true;		
		getJavaProject("P2").getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			"P2[*]: {CHILDREN}\n" + 
			"	/P1/lib.jar[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	/P1/lib2.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		//JavaModelManager.CP_RESOLVE_VERBOSE=false;		
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
		VariablesInitializer.reset();
	}
}
/*
 * Ensure that an OperationCanceledException goes through
 * (regression test for bug 59363 Should surface cancellation exceptions)
 */

public void testVariableInitializer8() throws CoreException {
	try {
		boolean gotException = false;
		try {
			VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}) {
				public void initialize(String variable) throws JavaModelException {
					throw new OperationCanceledException("test");
				}
			});
			IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {"TEST_LIB"}, "");
			p1.getResolvedClasspath(true);
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException", gotException);
	} finally {
		deleteProject("P1");
		VariablesInitializer.reset();
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=61872
 */
public void testUserLibraryInitializer1() throws CoreException {
	try {
		// Create new user library "SWT"
		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "SWT";
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
		initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		// Create java project
		createJavaProject("p61872");
		IFile jarFile = createFile("/p61872/swt.jar", "");
		IFile srcFile = createFile("/p61872/swtsrc.zip", "");

		// Modify user library
		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
		String propertyName = UserLibraryManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"SWT";
		StringBuffer propertyValue = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String jarFullPath = getWorkspaceRoot().getLocation().append(jarFile.getFullPath()).toString();
		propertyValue.append(" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.setValue(propertyName, propertyValue.toString());
		JavaCore.getPlugin().savePluginPreferences();

		// Modify project classpath
		editFile(
			"/p61872/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.USER_LIBRARY/SWT\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);

		// Verify 
		IClasspathEntry[] entries = getJavaProject("p61872").getResolvedClasspath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertNull("Project 61872 classpath entry should not have any source attached!", entries[0].getSourceAttachmentPath());

		// Modify user library
		propertyValue = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		String srcFullPath = getWorkspaceRoot().getLocation().append(srcFile.getFullPath()).toString();
		propertyValue.append(" sourceattachment=\""+srcFullPath);
		propertyValue.append("\" path=\""+jarFullPath);
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.setValue(propertyName, propertyValue.toString());
		JavaCore.getPlugin().savePluginPreferences();

		// Verify 
		entries = getJavaProject("p61872").getResolvedClasspath(true);
		assertEquals("Invalid entries number in resolved classpath for project p61872!", 1, entries.length);
		assertEquals("Invalid path for project 61872 classpath entry!", jarFullPath.toLowerCase(), entries[0].getPath().toString().toLowerCase());
		assertEquals("Invalid source attachement path for project 61872 classpath entry!", srcFullPath.toLowerCase(), entries[0].getSourceAttachmentPath().toString().toLowerCase());
	} finally {
		deleteProject("p61872");
	}
}
}
