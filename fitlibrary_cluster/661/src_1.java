package fitlibrary.domainAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import fitlibrary.traverse.DomainAdapter;

public class AbstractFileHandler implements DomainAdapter {
	protected File file = new File(".");

	public void append(String content) throws IOException {
		writeToFile(content, true);
	}
	public void write(String content) throws IOException {
		writeToFile(content, false);
	}
	public void appendUnicode(String content) throws IOException {
		writeUnicodeToFile(content, true);
	}
	public void writeUnicode(String content) throws IOException {
		writeUnicodeToFile(content, false);
	}
	public String read() throws IOException {
		return readFile();
	}
	public String readFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
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
	public void makeFolders(String folderPath) { // Don't need this action, but leave it for backwards compatibility
		new File(folderPath).mkdirs();
	}
	@Override
	public String toString() {
		return "FileHandler["+file.getName()+"]";
	}
	private void writeToFile(String content, boolean append) throws IOException {
		FileWriter fileWriter = new FileWriter(file,append);
		fileWriter.write(content.replace("\\n","\n"));
		fileWriter.close();
	}
	private void writeUnicodeToFile(String content, boolean append)
			throws IOException {
				OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file, append),"UTF8");
				out.write(content.replace("\\n","\n"));
				out.close();
			}
	@Override
	public Object getSystemUnderTest() {
		return file;
	}
}