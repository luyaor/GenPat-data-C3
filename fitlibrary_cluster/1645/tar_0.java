/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.dynamicVariable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class RecordDynamicVariables {
	private static ThreadLocal<Recording> recordings = new ThreadLocal<Recording>();
	
	public static void recordToFile(String fileName) {
		recordings.set(new Recording(fileName));
	}
	static class Recording {
		private String fileName;
		private Properties props = new Properties();

		public Recording(String fileName) {
			this.fileName = fileName;
		}
		public void record(String key, String value) {
			props.setProperty(key, value);
		}
		public void write() throws IOException {
			if (fileName.equals("pleaseThrowAnExceptionOnThisFile"))
				throw new IOException("Some file exception");
			Properties original = new Properties();
			collectOriginalProperties(original);
			for (Object key : props.keySet())
				original.setProperty(key.toString(), props.getProperty(key.toString()));
			FileOutputStream fileWriter = new FileOutputStream(new File(fileName));
			original.store(fileWriter, "Recorded on "+new Date());
			fileWriter.close();
		}
		private void collectOriginalProperties(Properties original) {
			try {
				FileInputStream fileReader = new FileInputStream(new File(fileName));
				original.load(fileReader);
				fileReader.close();
			} catch (Exception e) {
				//
			}
		}
	}
	public static boolean recording() {
		return recordings.get() != null;
	}
	public static void record(String key, String value) {
 		recordings.get().record(key,value);
	}
	public static void write() throws IOException {
		Recording recording = recordings.get();
		recordings.remove();
		recording.write();
	}
}
