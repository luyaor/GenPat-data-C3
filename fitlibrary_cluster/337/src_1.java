package fitlibrary.definedAction;

import fitlibrary.table.Table;
import fitlibrary.table.TableFactory;
import fitlibrary.table.Tables;
import fitlibrary.table.TablesOnParse;

public class DefinedActionBodyCollector {
	public void parseDefinitions(Tables tables, DefineActionBodyConsumer consumer) {
		if (tables instanceof TablesOnParse)
			parseDefinitionsWithParse(tables, consumer);
		else
			parseDefinitionsWithList(tables, consumer);
	}
	private void parseDefinitionsWithList(Tables tables, DefineActionBodyConsumer consumer) {
		TableFactory.useOnLists(true);
		try {
			Tables defineTables = TableFactory.tables();
			for (Table table : tables) {
				if (isHR(table.getLeader()) && !defineTables.isEmpty()) {
					consumer.addAction(defineTables);
					defineTables = TableFactory.tables();
				}
				if (isHR(table.getTrailer())) {
					defineTables.add(table);
					consumer.addAction(defineTables);
					defineTables = TableFactory.tables();
				} else
					defineTables.add(table);
			}
			if (!defineTables.isEmpty())
				consumer.addAction(defineTables);
		} finally {
			TableFactory.pop();
		}
	}
	// Warning: 'orrible code due to Parse!
	private void parseDefinitionsWithParse(Tables tables, DefineActionBodyConsumer consumer) {
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
