/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.batch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import fit.Counts;
import fitlibrary.batch.fitnesseIn.ParallelFitNesseRepository;
import fitlibrary.batch.resultsOut.ParallelSuiteResult;
import fitlibrary.batch.testRun.FitLibraryTestEngine;
import fitlibrary.batch.testRun.ParallelTestRunner;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.differences.FitLibraryRunnerDifference;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.traverse.Traverse;

public class FitLibraryRunner {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length != 3 && args.length != 4) {
			System.err.println("Usage: fitlibrary.batch.FitLibraryRunner suiteName fitNesseDiry resultsDiry [showPasses]");
			System.exit(-1);
		}
		System.out.println("FitLibraryRunner");
		String suiteName = args[0];
		String fitNesseDirectoryPath = args[1];
		String resultsDirectoryPath = args[2];
		boolean showPasses = args.length == 4;
		runParallel(suiteName, fitNesseDirectoryPath, resultsDirectoryPath, showPasses);
	}
	private static void runParallel(String suiteName, String fitNesseDirectoryPath, String resultsDirectoryPath, boolean showPasses) throws IOException, InterruptedException {
		verifyFitNesseDirectory(fitNesseDirectoryPath);
		DefineActionsOnPageSlowly.setFitNesseDiry(fitNesseDirectoryPath); // Hack this in for now.
		Traverse.setDifferenceStrategy(new FitLibraryRunnerDifference(fitNesseDirectoryPath));
		long start = System.currentTimeMillis();
		ParallelTestRunner runner = new ParallelTestRunner(new ParallelFitNesseRepository(fitNesseDirectoryPath), 
				new FitLibraryTestEngine(),resultsDirectoryPath,showPasses,suiteName);
		Counts counts = runner.runSuite(suiteName,new ParallelSuiteResult(suiteName,showPasses));
		report(start, counts);
	}
	private static void verifyFitNesseDirectory(String fitNesseDirectoryPath) {
		File fitNesseDiry = new File(fitNesseDirectoryPath);
		if (!fitNesseDiry.isDirectory())
			throw new FitLibraryException("Not a directory: "+fitNesseDirectoryPath);
		if (!Arrays.asList(fitNesseDiry.list()).contains("FitNesseRoot"))
			throw new FitLibraryException("Does not contain FitNesseRoot: "+fitNesseDirectoryPath);
	}
	private static void report(long start, Counts counts) {
		System.err.println("Total right="+counts.right+", wrong="+counts.wrong+", ignores="+counts.ignores+", exceptions="+counts.exceptions);
		System.err.println("Time to run = "+(System.currentTimeMillis()-start)+" milliseconds.");
		System.exit(counts.wrong+counts.exceptions);
	}
}
