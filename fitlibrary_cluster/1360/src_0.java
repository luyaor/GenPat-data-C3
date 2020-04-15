/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.batch;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fit.Counts;
import fitlibrary.batch.fitnesseIn.ParallelFitNesseRepository;
import fitlibrary.batch.resultsOut.ParallelSuiteResult;
import fitlibrary.batch.testRun.FitLibraryTestEngine;
import fitlibrary.batch.testRun.ParallelTestRunner;
import fitlibrary.definedAction.DefineActionsOnPageSlowly;
import fitlibrary.differences.FitLibraryRunnerDifference;
import fitlibrary.exception.FitLibraryException;
import fitlibrary.traverse.Traverse;
import fitlibrary.utility.CollectionUtility;

public class FitLibraryRunner {
	private static final String SUITE_NAME = "suiteName";
	private static final String FIT_NESSE_DIRY = "fitNesseDiry";
	private static final String RESULTS_DIRY = "resultsDiry";
	private static final String SHOW_PASSES = "showPasses";
	private static final String PORT = "port";
	static int PORT_NO = 8980;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length > 0 && args[0].startsWith("-"))
			runWithNewArguments(args);
		else
			runWithOldArguments(args);
	}
	private static void runWithNewArguments(String[] args) throws IOException, InterruptedException {
		try {
			RunParameters runParameters = getRunParameters(args);
			String suiteName = runParameters.get(SUITE_NAME);
			String fitNesseDirectoryPath = runParameters.get(FIT_NESSE_DIRY);
			String resultsDirectoryPath = runParameters.get(RESULTS_DIRY);
			boolean showPasses = runParameters.get(SHOW_PASSES).equals("true");
			int port = runParameters.getInt(PORT);
			runParallel(suiteName, fitNesseDirectoryPath, resultsDirectoryPath, showPasses, port);
		} catch (InvalidParameterException e) {
			error();
		} catch (NumberFormatException e) {
			error();
		}
	}
	private static void error() {
		System.err.println("Usage: fitlibrary.batch.FitLibraryRunner "+"" +
				"-suiteName suiteName [-fitNesseDiry fitNesseDiry] [-resultsDiry resultsDiry] [-showPasses true] [-port port]");
		System.exit(-1);
	}
	private static void runWithOldArguments(String[] args) throws IOException,
			InterruptedException {
		if (args.length != 3 && args.length != 4)
			error();
		String suiteName = args[0];
		String fitNesseDirectoryPath = args[1];
		String resultsDirectoryPath = args[2];
		boolean showPasses = args.length == 4;
		runParallel(suiteName, fitNesseDirectoryPath, resultsDirectoryPath, showPasses, PORT_NO);
	}
	private static void runParallel(String suiteName, String fitNesseDirectoryPath, String resultsDirectoryPath, boolean showPasses, int port) throws IOException, InterruptedException {
		verifyFitNesseDirectory(fitNesseDirectoryPath);
		DefineActionsOnPageSlowly.setFitNesseDiry(fitNesseDirectoryPath); // Hack this in for now.
		Traverse.setDifferenceStrategy(new FitLibraryRunnerDifference(fitNesseDirectoryPath));
		long start = System.currentTimeMillis();
		ParallelTestRunner runner = new ParallelTestRunner(new ParallelFitNesseRepository(fitNesseDirectoryPath,port), 
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
	private static List<String> valids = list(SUITE_NAME,FIT_NESSE_DIRY,RESULTS_DIRY,SHOW_PASSES,PORT);
	public static RunParameters getRunParameters(String[] args) {
		RunParameters runParameters = new RunParameters();
		for (int i = 0; i < args.length; i++) {
			String tag = args[i];
			if (tag.startsWith("-")) {
				tag = tag.substring(1);
				i++;
				if (valids.contains(tag) && i < args.length)
					runParameters.put(tag,args[i]);
				else
					throw new InvalidParameterException(tag);
			}
		}
		if (runParameters.get("suiteName") == null)
			throw new InvalidParameterException("suiteName");
		return runParameters;
	}
	protected static List<String> list(String... ss) {
		return CollectionUtility.list(ss);
	}
	public static class RunParameters {
		private Map<String,String> parameterMap = new HashMap<String, String>();
		
		public RunParameters() {
			parameterMap.put(FIT_NESSE_DIRY, ".");
			parameterMap.put(RESULTS_DIRY, "runnerResults");
			parameterMap.put(SHOW_PASSES, "false");
			parameterMap.put(PORT, "80");
		}
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}
		public String get(String key) {
			return parameterMap .get(key);
		}
		public void put(String key, String value) {
			parameterMap.put(key,value);
		}
	}
}
