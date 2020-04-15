package fitlibrary.batch;

import java.io.IOException;

import fitlibrary.batch.FitLibraryRunner;

public class TestFitLibraryRunner {

	public static void main(String[] args) throws IOException, InterruptedException {
		String[] arguments = { 
				"FitLibrary.SpecifiCations",
				"C:\\Documents and Settings\\RimuResearch\\My Documents\\work\\fitnesse",
				"C:/result"};
		FitLibraryRunner.main(arguments );
	}

}
