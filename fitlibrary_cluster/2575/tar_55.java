package fitlibrary.domainAdapter;

import fitlibrary.annotation.ActionType;
import fitlibrary.annotation.AnAction;
import fitlibrary.annotation.ShowSelectedActions;
import fitlibrary.traverse.DomainAdapter;

@ShowSelectedActions
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
	
	@AnAction(wiki="|''<i>index of</i>''|text|",actionType=ActionType.SIMPLE,
			tooltip="What is the offset of the text in the string?")
	public int indexOf(String pattern) { // As the underlying method is overloaded
		return subject.indexOf(pattern);
	}
	
	@AnAction(wiki="|''<i>index of</i>''|text|''<i>from</i>''|start|",actionType=ActionType.SIMPLE,
			tooltip="What is the offset of the text in the string, starting at start?")
	public int indexOf(String pattern, int from) { // As the underlying method is overloaded
		return subject.indexOf(pattern,from);
	}

	@AnAction(wiki="|''<i>last index of</i>''|text|",actionType=ActionType.SIMPLE,
			tooltip="What is the last offset of the text in the string?")
	public int lastIndexOf(String pattern) { // As the underlying method is overloaded
		return subject.lastIndexOf(pattern);
	}
	
	@AnAction(wiki="|''<i>compare to</i>''|text|",actionType=ActionType.SIMPLE,
			tooltip="What is the result of comparing the text to the string?")
	public int compareTo(String pattern) { // As the underlying method is overloaded
		return subject.compareTo(pattern);
	}
	
	@AnAction(wiki="|''<i>is equals</i>''|text|",actionType=ActionType.SIMPLE,
			tooltip="Returns whether the text is the same as the string.")
	public boolean isEquals(String pattern) { // As the underlying method takes an Object
		return subject.equals(pattern);
	}
	
	@AnAction(wiki="|''<i>contains</i>''|text|",actionType=ActionType.SIMPLE,
			tooltip="Returns whether the string contains the text.")
	public boolean contains(String pattern) { // As the underlying method takes a CharSequence
		return subject.contains(pattern);
	}
	
	@AnAction(wiki="|''<i>replace</i>''|pattern|replacement|",actionType=ActionType.SIMPLE,
			tooltip="Returns the result of replacing the pattern by the replacement in the string.")
	public String replace(String pattern, String replacement) { // As the underlying method takes a CharSequence
		return subject.replace(pattern,replacement);
	}
}
