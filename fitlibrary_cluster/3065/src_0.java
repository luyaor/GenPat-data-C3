package fitlibrary.specify.select;

import fitlibrary.DoFixture;

public class SelectWithTearDownFailing extends DoFixture {
	@Override
	public void tearDown() {
		throw new RuntimeException("Failed in tearDown()");
	}

}
