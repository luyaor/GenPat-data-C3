package fitlibrary.batch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import fit.Counts;
import fitlibrary.batch.testRun.FitLibraryBatching;
import fitlibrary.batch.testRun.FitLibraryTestEngine;
import fitlibrary.batch.trinidad.InMemoryTestImpl;
import fitlibrary.batch.trinidad.SingleTestResult;
import fitlibrary.batch.trinidad.TestResult;
import fitlibrary.runResults.TableListener;
import fitlibrary.table.Tables;

@RunWith(JMock.class)
public class TestFitLibraryTestEngine {
	final Mockery context = new JUnit4Mockery();
	final FitLibraryBatching mockBatching = context.mock(FitLibraryBatching.class);

	@Test public void noTables() {
		String testName = "Test One";
		String html = "contents";
		FitLibraryTestEngine engine = new FitLibraryTestEngine(mockBatching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(),testName," contains no tables")));
	}
	@Test public void passWithNoOutput() {
		String testName = "Test Two";
		String html = "<table><tr><td>a</td></tr></table>";
		final FitLibraryBatching batching = new FitLibraryBatching() {
			public void doTables(Tables tables, TableListener listener) {
				listener.getTestResults().pass();
			}};
		FitLibraryTestEngine engine = new FitLibraryTestEngine(batching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(1,0,0,0),testName,html)));
	}
	@Test public void failWithOutOnly() {
		String testName = "Test Three";
		String html = "<table><tr><td>a</td></tr></table>";
		final FitLibraryBatching batching = new FitLibraryBatching() {
			public void doTables(Tables tables, TableListener listener) {
				listener.getTestResults().fail();
				System.out.print("Mess");
				System.out.println("age");
			}};
		FitLibraryTestEngine engine = new FitLibraryTestEngine(batching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html));
		assertThat(result.getContent(),equalTo(html+"\n<hr/><h1>out</h1>\n<pre>\nMessage\r\n\n</pre>\n"));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(0,1,0,0),testName,
				html+"\n<hr/><h1>out</h1>\n<pre>\nMessage\r\n\n</pre>\n")));
	}
	@Test public void exceptionWithErrOnly() {
		String testName = "Test Three";
		String html = "<table><tr><td>a</td></tr></table>";
		final FitLibraryBatching batching = new FitLibraryBatching() {
			public void doTables(Tables tables, TableListener listener) {
				listener.getTestResults().exception();
				System.err.println("Message");
			}};
		FitLibraryTestEngine engine = new FitLibraryTestEngine(batching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(0,0,0,1),testName,
				html+"\n<hr/><h1>err</h1>\n<pre>\nMessage\r\n\n</pre>\n")));
	}
	@Test public void ignoreWithOutAndErr() {
		String testName = "Test Three";
		String html = "<table><tr><td>a</td></tr></table>";
		final FitLibraryBatching batching = new FitLibraryBatching() {
			public void doTables(Tables tables, TableListener listener) {
				listener.getTestResults().ignore();
				// The following are a part of the test, as System.out/err should be redirected at this point
				System.out.println("Out Message");
				System.err.println("Err Message");
			}};
		FitLibraryTestEngine engine = new FitLibraryTestEngine(batching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(0,0,1,0),testName,
				html+"\n<hr/><h1>out</h1>\n<pre>\nOut Message\r\n\n</pre>\n"+
				     "\n<hr/><h1>err</h1>\n<pre>\nErr Message\r\n\n</pre>\n")));
	}
	@Test public void ignoreWithOutAndErrWithBody() {
		String testName = "Test Three";
		String html = "<body><table><tr><td>a</td></tr></table>";
		final FitLibraryBatching batching = new FitLibraryBatching() {
			public void doTables(Tables tables, TableListener listener) {
				listener.getTestResults().ignore();
				// The following are a part of the test, as System.out/err should be redirected at this point
				System.out.println("Out Message");
				System.err.println("Err Message");
			}};
		FitLibraryTestEngine engine = new FitLibraryTestEngine(batching);
		TestResult result = engine.runTest(new InMemoryTestImpl(testName,html+"</body>"));
		assertThat(result,matchesTestResult(new SingleTestResult(new Counts(0,0,1,0),testName,
				html+"\n<hr/><h1>out</h1>\n<pre>\nOut Message\r\n\n</pre>\n"+
				     "\n<hr/><h1>err</h1>\n<pre>\nErr Message\r\n\n</pre>\n</body>")));
	}

	
	
	private Matcher<TestResult> matchesTestResult(TestResult testResult) {
		return new TestResultMatcher(testResult);
	}
	static class TestResultMatcher extends TypeSafeMatcher<TestResult> {
		private TestResult expected;

		public TestResultMatcher(TestResult expected) {
			this.expected = expected;
		}
		@Override
		public boolean matchesSafely(TestResult other) {
			boolean namesMatch = expected.getName().equals(other.getName());
			if (!namesMatch)
				System.out.println("Names don't match: '"+other.getName()+"' not as expected: '"+expected.getName());
			boolean contentsMatch = expected.getContent().equals(other.getContent());
			if (!contentsMatch)
				System.out.println("Contents don't match: '\n"+other.getContent()+"\n' not as expected: '\n"+expected.getContent()+"\n'");
			boolean countsMatch = expected.getCounts().equals(other.getCounts());
			if (!countsMatch)
				System.out.println("Counts don't match: '"+other.getCounts()+"' not as expected: '"+expected.getCounts());
			return 	namesMatch && contentsMatch && countsMatch;
		}
		public void describeTo(Description description) {
			description.appendText(expected.getName());
		}
	}
}
