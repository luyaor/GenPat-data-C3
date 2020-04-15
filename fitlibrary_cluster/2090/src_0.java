/*
 * Copyright (c) 2009 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.definedAction;

import java.io.File;
import java.util.List;

import fit.Parse;
import fitlibrary.table.Table;
import fitlibrary.table.Tables;
import fitlibrary.utility.FileIO;
import fitlibrary.utility.SimpleWikiTranslator;
import fitlibrary.utility.TestResults;

public class DefineActionsOnPage extends DefineActionsOnPageSlowly {

	public DefineActionsOnPage(String topPageName) {
		super(topPageName);
	}
	@Override
	public Object interpretAfterFirstRow(Table tableWithPageName, TestResults testResults) {
		try {
			processPagesAsFiles(topPageName.substring(1));
		} catch (Exception e) {
			tableWithPageName.error(testResults, e);
//			e.printStackTrace();
		}
		return null;
	}
	private void processPagesAsFiles(String pageName) throws Exception {
		String fullPageName = "FitNesseRoot/"+pageName.replaceAll("\\.","/");
		File diry = new File(fitNesseDiry(),fullPageName);
		List<File> files = FileIO.filesWithSuffix(diry, "txt");
		for (File file : files) {
			String wiki = FileIO.read(file);
			String html = SimpleWikiTranslator.translate(wiki);
			String fileName = file.getAbsolutePath().replaceAll("/",".").replaceAll("\\\\",".");
			try
			{
				if (html.contains("<table"))
					parseDefinitions(new Tables(new Parse(html)),determineClassName("",fileName),file.getAbsolutePath());
			} catch (Exception e)
			{
//				System.err.println("\n\n----------------DefineActionsOnPage error with : "+fileName+"\n\n");
				throw e;
			}
		}
	}
}
