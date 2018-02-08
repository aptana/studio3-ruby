/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import org.jrubyparser.CompatVersion;

import com.aptana.parsing.ParseState;

public class RubyParseState extends ParseState
{

	private static final String DEFAULT_FILENAME = "<unnamed file>"; //$NON-NLS-1$
	private CompatVersion compatVersion = CompatVersion.BOTH;
	private int lineNumber = 0;
	private String filename = DEFAULT_FILENAME;

	public RubyParseState(String source, String filename, int startingLineNumber, CompatVersion version)
	{
		super(source, 0);
		setFilename(filename);
		setStartingLineNumber(startingLineNumber);
		setVersion(version);
	}

	private void setVersion(CompatVersion compatVersion)
	{
		this.compatVersion = compatVersion;
	}

	public CompatVersion getCompatVersion()
	{
		return compatVersion;
	}

	public int getStartingLineNumber()
	{
		return this.lineNumber;
	}

	private void setStartingLineNumber(int lineNumber)
	{
		if (lineNumber < 0)
		{
			lineNumber = 0;
		}
		this.lineNumber = lineNumber;
	}

	public String getFilename()
	{
		return this.filename;
	}

	private void setFilename(String filename)
	{
		if (filename == null)
		{
			filename = DEFAULT_FILENAME;
		}
		this.filename = filename;
	}

}
