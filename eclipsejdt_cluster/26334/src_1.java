package org.eclipse.jdt.internal.compiler;

import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.Main;

public abstract class AbstractAnnotationProcessorManager {
	public abstract void configure(Main batchCompiler, String[] options);
	
	public abstract void setOut(PrintWriter out);
	
	public abstract void setErr(PrintWriter err);

	public abstract List getNewUnits();
	
	public abstract void reset();
	
	public abstract void processAnnotations(CompilationUnitDeclaration[] units, boolean isLastRound);
	
	public abstract void setProcessors(Object[] processors);
}
