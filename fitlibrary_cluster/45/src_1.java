/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.suite;

import java.util.Date;

import fit.FitServerBridge;
import fit.Parse;
import fit.exception.FitParseException;
import fitlibrary.table.TablesOnParse;
import fitlibrary.utility.TableListener;
import fitlibrary.utility.TestResults;

public class FitLibraryServer extends FitServerBridge implements Reportage {
	private BatchFitLibrary batching = new BatchFitLibrary();

    public FitLibraryServer(String host, int port, boolean verbose) {
        super(host,port,verbose);
    }
    public FitLibraryServer() {
    	//
    }
	@Override
	public void doTables(String html) {
		try {
			String translated = html; //ParseUtility.tabulize(html); //ParseUtility.translate(html);
			fixture.counts = doTables(new TablesOnParse(new Parse(translated))).getCounts();
		} catch (FitParseException e) {
			e.printStackTrace();
		}
	}
	public TestResults doTables(TablesOnParse theTables) {
		TableListener tableListener = new TableListener(fixtureListener);
		batching.doTables(theTables,tableListener);
		return tableListener.getTestResults();
	}
	@Override
	public void exit() throws Exception {
		batching.exit();
		super.exit();
	}
    @Override
	protected void usage() {
        print("usage: java fitlibrary.suite.FitLibraryServer [-v] host port socketTicket");
        System.exit(-1);
    }
	public static void main(String[] args) {
		FitServerBridge fitServer = new FitLibraryServer();
		try {
			fitServer.print("\n-----------\n"+new Date()+"\n");
			fitServer.run(args);
			fitServer.print("exit: "+fitServer.exitCode());
			if (fitServer.isExit())
				System.exit(fitServer.exitCode());
		} catch (Exception e) {
			fitServer.printExceptionDetails(e);
		}
    }
}