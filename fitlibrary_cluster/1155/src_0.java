/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileHandler {
	private String fileName = "(file name not given)";
	
	public FileHandler(String fileName) {
		fileNameIs(fileName);
	}
	public void fileNameIs(String fileNameGiven) {
		this.fileName = fileNameGiven;
	}
	public void write(String content) throws IOException {
		FileWriter fileWriter = new FileWriter(fileName);
		fileWriter.write(content.replace("\\n","\n"));
		fileWriter.close();
	}
	public void writeUnicode(String content) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName),"UTF8");
		out.write(content.replace("\\n","\n"));
		out.close();
	}
	public String readFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		String result = "";
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			result += line;
		}
		reader.close();
		return result;
	}
	public void makeFolders(String folderPath) {
		new File(folderPath).mkdirs();
	}
	public boolean delete() {
		return new File(fileName).delete();
	}
	public boolean exists() {
		return new File(fileName).exists();
	}
	@Override
	public String toString() {
		return "FileHandler["+fileName+"]";
	}
}