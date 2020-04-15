package fitlibrary.log;

import org.apache.log4j.PropertyConfigurator;

public class ConfigureLoggingThroughFiles {
	public static void configure() {
		configure("");
	}
	public static void configure(String diry) {
		PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
		propertyConfigurator.doConfigure(diry+"FitLibraryLogger.properties", FitLibraryLogger.getOwnHierarchy());
		propertyConfigurator.doConfigure(diry+"FixturingLogger.properties", FixturingLogger.getOwnHierarchy());
	}
}
