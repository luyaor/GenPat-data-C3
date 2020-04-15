package fitlibrary.definedAction;

import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;

public class DefinedActionBodyCollector {
	// Warning: 'orrible code due to Parse!
	public void parseDefinitions(Tables tables, DefineActionBodyConsumer consumer) {
		Tables innerTables = tables;
		// Process the first and last tables differently
		// (Ignore the first & (also) handle last outside loop)
		for (int i = 1; i < tables.size(); i++) {
			Table table = tables.at(i);
			Table previousTable = tables.at(i-1);
			if (isHR(table.getLeader()) || isHR(previousTable.getTrailer())) {
				table.parse().leader = "";
				previousTable.parse().more = null;
				previousTable.parse().trailer = "";
				consumer.addAction(innerTables);
				previousTable.parse().more = table.parse();
				innerTables = TableFactory.tables(table);
			}
		}
		consumer.addAction(innerTables);
	}
	private boolean isHR(String s) {
		return s != null && (s.contains("<hr>") || s.contains("<hr/>"));
	}
	public interface DefineActionBodyConsumer {
		void addAction(Tables innerTables);
	}
}
