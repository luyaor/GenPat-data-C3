package fitlibrary.domainAdapter;

import fitlibrary.traverse.DomainAdapter;

public class StringAdapter implements DomainAdapter {
	private final String subject;

	public StringAdapter(String subject) {
		this.subject = subject;
	}
	public String[] split(String separator) {
		if (separator.isEmpty())
			return subject.split(" ");
		if (separator.equals("\\n"))
			return subject.split("\n");
		return subject.split(separator);
	}
	@Override
	public String toString() {
		return subject.toString();
	}
	@Override
	public Object getSystemUnderTest() {
		return subject;
	}
	public int indexOf(String pattern) { // As the underlying method is overloaded
		return subject.indexOf(pattern);
	}
	public int indexOf(String pattern, int from) { // As the underlying method is overloaded
		return subject.indexOf(pattern,from);
	}
	public int lastIndexOf(String pattern) { // As the underlying method is overloaded
		return subject.lastIndexOf(pattern);
	}
	public int compareTo(String pattern) { // As the underlying method is overloaded
		return subject.compareTo(pattern);
	}
	public boolean isEquals(String pattern) { // As the underlying method takes an Object
		return subject.equals(pattern);
	}
	public boolean contains(String pattern) { // As the underlying method takes a CharSequence
		return subject.contains(pattern);
	}
	public String replace(String pattern, String replacement) { // As the underlying method takes a CharSequence
		return subject.replace(pattern,replacement);
	}
}
