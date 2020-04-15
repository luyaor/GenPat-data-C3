package fitlibrary.log;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

public class ConfigureLoggingThroughFiles {
	public static void configure() {
		configure("");
	}

	public static void configure(String diry) {
		PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
		if (new File(diry + "FitLibraryLogger.properties").exists())
			propertyConfigurator.doConfigure(diry + "FitLibraryLogger.properties",
					FitLibraryLogger.getOwnHierarchy());
		if (new File(diry + "FixturingLogger.properties").exists())
			propertyConfigurator.doConfigure(diry + "FixturingLogger.properties",
					FixturingLogger.getOwnHierarchy());
		if (new File(diry + "log4j.properties").exists())
			PropertyConfigurator.configure(diry + "log4j.properties");
	}
}
