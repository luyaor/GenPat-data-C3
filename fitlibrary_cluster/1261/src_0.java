/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.closure;

import org.junit.Test;

import fitlibrary.utility.CollectionUtility;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestActionSignature {
	@Test
	public void equality() {
		assertThat(new ActionSignature("act",0),is(new ActionSignature("act",0)));
		assertThat(new ActionSignature("act",0),not(new ActionSignature("act",1)));
		assertThat(new ActionSignature("act",0),not(new ActionSignature("actIve",0)));
	}
	@Test
	public void hash() {
		assertThat(new ActionSignature("act",0).hashCode(),is(new ActionSignature("act",0).hashCode()));
		assertThat(new ActionSignature("act",0).hashCode(),not(new ActionSignature("act",1).hashCode()));
		assertThat(new ActionSignature("act",0).hashCode(),not(new ActionSignature("actIve",0).hashCode()));
	}
	@Test
	public void display() {
		assertThat(new ActionSignature("act",0).toString(),is("act/0"));
		assertThat(new ActionSignature("act",1).toString(),is("act/1"));
		assertThat(new ActionSignature("actIve",2).toString(),is("actIve/2"));
	}
	@Test
	public void doStyle() {
		assertThat(ActionSignature.doStyle("act"),is(new ActionSignature("act",0)));
		assertThat(ActionSignature.doStyle("act","1"),is(new ActionSignature("act",1)));
		assertThat(ActionSignature.doStyle("act","1","ive"),is(new ActionSignature("actIve",1)));
		assertThat(ActionSignature.doStyle("act","1","ive","2"),is(new ActionSignature("actIve",2)));
		assertThat(ActionSignature.doStyle("act","1","ive","2","ly"),is(new ActionSignature("actIveLy",2)));
		assertThat(ActionSignature.doStyle(CollectionUtility.list("act","1","ive","2","ly")),is(new ActionSignature("actIveLy",2)));
	}
	@Test
	public void seqStyle() {
		assertThat(ActionSignature.seqStyle("act"),is(new ActionSignature("act",0)));
		assertThat(ActionSignature.seqStyle("act","1"),is(new ActionSignature("act",1)));
		assertThat(ActionSignature.seqStyle("act","1","ive"),is(new ActionSignature("act",2)));
		assertThat(ActionSignature.seqStyle("act","1","ive","2"),is(new ActionSignature("act",3)));
		assertThat(ActionSignature.seqStyle("act","1","ive","2","ly"),is(new ActionSignature("act",4)));
		assertThat(ActionSignature.seqStyle(CollectionUtility.list("act","1","ive","2","ly")),is(new ActionSignature("act",4)));
	}
}
