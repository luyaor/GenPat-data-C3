package fitlibrary.batch;

import java.io.IOException;

import fitlibrary.batch.FitLibraryRunner;

public class TestFitLibraryRunner {

	public static void main(String[] args) throws IOException, InterruptedException {
		String[] arguments = { 
				"FitLibrary.SpecifiCations.CoreFitSpecifications.PackageImportsAndDefaults",
				"fitnesse",
				"runnerResults",
				"showPasses"};
		FitLibraryRunner.main(arguments );
	}

}
