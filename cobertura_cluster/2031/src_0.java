/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2003 jcoverage ltd.
 * Copyright (C) 2005 Mark Doliner
 * Copyright (C) 2005 Joakim Erdfelt
 * Copyright (C) 2005 Grzegorz Lukasik
 * Contact information for the above is given in the COPYRIGHT file.
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * <p>
 * Add coverage instrumentation to existing classes.
 * </p>
 *
 * <h3>What does that mean, exactly?</h3>
 * <p>
 * It means Cobertura will look at each class you give it.  It
 * loads the bytecode into memory.  For each line of source,
 * Cobertura adds a few extra instructions.  These instructions 
 * do the following:
 * </p>
 * 
 * <ol>
 * <li>Get an instance of the ProjectData class.</li>
 * <li>Call a method in this ProjectData class that increments
 * a counter for this line of code.
 * </ol>
 *
 * <p>
 * After every line in a class has been "instrumented," Cobertura
 * edits the bytecode for the class one more time and adds
 * "implements net.sourceforge.cobertura.coveragedata.HasBeenInstrumented" 
 * This is basically just a flag used internally by Cobertura to
 * determine whether a class has been instrumented or not, so
 * as not to instrument the same class twice.
 * </p>
 */
public class Main
{

	private static final Logger logger = Logger.getLogger(Main.class);

	private File destinationDirectory = null;
	private File baseDir = null;
	private Pattern ignoreRegexp = null;
	private ProjectData projectData = null;

	/**
	 * @param file A file.
	 * @return True if the specified file has "class" as its extension,
	 * false otherwise.
	 */
	private static boolean isClass(File file)
	{
		return file.getName().endsWith(".class");
	}

	// TODO: Don't attempt to instrument a file if the outputFile already
	//       exists and is newer than the input file.
	private void addInstrumentation(File file)
	{
		if (file.isDirectory())
		{
			File[] contents = file.listFiles();
			for (int i = 0; i < contents.length; i++)
				addInstrumentation(contents[i]);
			return;
		}

		if (!isClass(file))
		{
			return;
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("instrumenting " + file.getAbsolutePath());
		}

		InputStream inputStream = null;
		ClassWriter cw;
		ClassInstrumenter cv;
		try
		{
			inputStream = new FileInputStream(file);
			ClassReader cr = new ClassReader(inputStream);
			cw = new ClassWriter(true);
			cv = new ClassInstrumenter(projectData, cw, ignoreRegexp);
			cr.accept(cv, false);
		}
		catch (IOException e)
		{
			logger.warn(
					"Unable to instrument file " + file.getAbsolutePath(), e);
			return;
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		OutputStream outputStream = null;
		try
		{
			if (cv.isInstrumented())
			{
				// If destinationDirectory is null, then overwrite
				// the original, uninstrumented file.
				File outputFile;
				if (destinationDirectory == null)
					outputFile = file;
				else
					outputFile = new File(destinationDirectory, cv
							.getClassName().replace('.', File.separatorChar)
							+ ".class");

				File parentFile = outputFile.getParentFile();
				if (parentFile != null)
				{
					parentFile.mkdirs();
				}

				byte[] instrumentedClass = cw.toByteArray();
				outputStream = new FileOutputStream(outputFile);
				outputStream.write(instrumentedClass);
			}
		}
		catch (IOException e)
		{
			logger.warn(
					"Unable to instrument file " + file.getAbsolutePath(), e);
			return;
		}
		finally
		{
			if (outputStream != null)
			{
				try
				{
					outputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	private void addInstrumentation(String filename)
	{
		if (logger.isDebugEnabled())
			logger.debug("filename: " + filename);

		if (baseDir == null)
			addInstrumentation(new File(filename));
		else
			addInstrumentation(new File(baseDir, filename));
	}

	private void parseArguments(String[] args)
	{
		// Parse our parameters
		Collection locations = new Vector();
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--basedir"))
				baseDir = new File(args[++i]);
			else if (args[i].equals("--datafile"))
				CoverageDataFileHandler.setDefaultDataFile(args[++i]);
			else if (args[i].equals("--destination"))
				destinationDirectory = new File(args[++i]);
			else if (args[i].equals("--ignore"))
			{
				String regex = args[++i];
				try
				{
					Perl5Compiler pc = new Perl5Compiler();
					this.ignoreRegexp = pc.compile(regex);
				}
				catch (MalformedPatternException e)
				{
					logger.warn("The regular expression " + regex
							+ " is invalid: " + e.getLocalizedMessage());
				}
			}
			else
				locations.add(args[i]);
		}

		// Load coverage data, instrument classes, save coverage data
		projectData = ProjectData.getGlobalProjectData();
		Iterator iter = locations.iterator();
		while (iter.hasNext())
			addInstrumentation((String)iter.next());
		ProjectData.saveGlobalProjectData();
	}

	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();

		Main main = new Main();

		boolean hasCommandsFile = false;
		String commandsFileName = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--commandsfile"))
			{
				hasCommandsFile = true;
				commandsFileName = args[++i];
			}
		}

		if (hasCommandsFile)
		{
			List arglist = new ArrayList();
			BufferedReader bufferedReader = null;

			try
			{
				bufferedReader = new BufferedReader(new FileReader(
						commandsFileName));
				String line;

				while ((line = bufferedReader.readLine()) != null)
					arglist.add(line);

			}
			catch (IOException e)
			{
				logger.fatal("Unable to read temporary commands file "
						+ commandsFileName + ".");
				logger.info(e);
			}
			finally
			{
				if (bufferedReader != null)
				{
					try
					{
						bufferedReader.close();
					}
					catch (IOException e)
					{
					}
				}
			}

			args = (String[])arglist.toArray(new String[arglist.size()]);
		}

		main.parseArguments(args);

		long stopTime = System.currentTimeMillis();
		System.out.println("Instrument time: " + (stopTime - startTime)
				+ "ms");
	}

}