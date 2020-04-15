/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.batch;

import java.security.InvalidParameterException;

import org.junit.Test;

import fitlibrary.batch.FitLibraryRunner.RunParameters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestFitLibraryRunner {
	@Test
	public void validRunParametersWithDefaults() {
		String[] args = {"-suiteName", "Suite.Name"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("."));
		assertThat(runParameters.get("resultsDiry"),is("runnerResults"));
		assertThat(runParameters.get("showPasses"),is("false"));
		assertThat(runParameters.get("port"),is("80"));
	}
	@Test(expected=InvalidParameterException.class)
	public void missingSuiteName() {
		String[] args = {};
		FitLibraryRunner.getRunParameters(args);
	}
	@Test
	public void fitNesseDiryIsAlsoGiven() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-fitNesseDiry", "../.."};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("../.."));
		assertThat(runParameters.get("resultsDiry"),is("runnerResults"));
		assertThat(runParameters.get("showPasses"),is("false"));
		assertThat(runParameters.get("port"),is("80"));
	}
	@Test
	public void resultsDiryIsAlsoGiven() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-resultsDiry", "TheResults"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("."));
		assertThat(runParameters.get("resultsDiry"),is("TheResults"));
		assertThat(runParameters.get("showPasses"),is("false"));
		assertThat(runParameters.get("port"),is("80"));
	}
	@Test
	public void showPassesIsAlsoGiven() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-showPasses", "true"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("."));
		assertThat(runParameters.get("resultsDiry"),is("runnerResults"));
		assertThat(runParameters.get("showPasses"),is("true"));
		assertThat(runParameters.getInt("port"),is(80));
	}
	@Test
	public void portIsAlsoGiven() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-port", "8990"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("."));
		assertThat(runParameters.get("resultsDiry"),is("runnerResults"));
		assertThat(runParameters.get("showPasses"),is("false"));
		assertThat(runParameters.get("port"),is("8990"));
	}
	@Test(expected=NumberFormatException.class)
	public void portIsGivenIncorrectly() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-port", "abc"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		runParameters.getInt("port");
	}
	@Test
	public void allAreGiven() {
		String[] args = {
				"-suiteName", "Suite.Name",
				"-fitNesseDiry", "../..",
				"-resultsDiry", "TheResults",
				"-showPasses", "true",
				"-port", "8990"};
		RunParameters runParameters = FitLibraryRunner.getRunParameters(args);
		assertThat(runParameters.get("suiteName"),is("Suite.Name"));
		assertThat(runParameters.get("fitNesseDiry"),is("../.."));
		assertThat(runParameters.get("resultsDiry"),is("TheResults"));
		assertThat(runParameters.get("showPasses"),is("true"));
		assertThat(runParameters.get("port"),is("8990"));
	}
}
