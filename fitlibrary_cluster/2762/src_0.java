package fitlibrary.specify.global;

import fitlibrary.exception.FitLibraryException;
import fitlibrary.listener.OnError;

public class OnErrorHandler implements OnError {
	public boolean stopOnError(int fails, int errors) {
		return errors >= 2;
	}
	
	public Object listener() {
		return this;
	}
	
	public boolean fails() {
		return false;
	}

	public boolean exceptions() {
		throw new FitLibraryException("error");
	}
}
