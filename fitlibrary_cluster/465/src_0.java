package fitlibrary.specify;

import fitlibrary.CalculateFixture;
import fitlibrary.utility.ExtendedCamelCase;

public class TestCamelCase extends CalculateFixture {
	public String identifierName(String name) {
		return ExtendedCamelCase.camel(name);
	}
}
