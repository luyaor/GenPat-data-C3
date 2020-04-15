package fitlibrary.batch.trinidad;

import fit.Counts;

public class SingleTestResult implements TestResult {
	private Counts counts;
	private String name;
	private String content;

	public SingleTestResult(Counts counts, String name, String content) {
		this.counts = counts;
		this.name = name;
		this.content = content;
	}
	public Counts getCounts() {
		return this.counts;
	}
	public String getName() {
		return this.name;
	}
	public String getContent() {
		return this.content;
	}
	@Override
	public String toString() {
		return "SingleTestResult['"+name+"', '"+content+"', '"+counts+"']";
	}
}