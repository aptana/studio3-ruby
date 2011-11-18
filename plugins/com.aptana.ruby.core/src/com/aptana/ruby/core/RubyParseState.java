/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.IRubyWarnings;
import org.jrubyparser.Parser;

import com.aptana.parsing.ParseState;

public class RubyParseState extends ParseState
{
	private static final String DEFAULT_FILENAME = "<unnamed file>"; //$NON-NLS-1$
	private CompatVersion compatVersion = CompatVersion.BOTH;
	private IRubyWarnings warnings = new Parser.NullWarnings();
	private int lineNumber = 0;
	private String filename = DEFAULT_FILENAME;

	public void setVersion(CompatVersion compatVersion)
	{
		this.compatVersion = compatVersion;
	}

	public CompatVersion getCompatVersion()
	{
		return compatVersion;
	}

	public IRubyWarnings getWarnings()
	{
		return this.warnings;
	}

	public void setWarnings(IRubyWarnings warnings)
	{
		if (warnings == null)
		{
			this.warnings = new Parser.NullWarnings();
		}
		else
		{
			this.warnings = warnings;
		}
	}

	public int getStartingLineNumber()
	{
		return this.lineNumber;
	}

	public void setStartingLineNumber(int lineNumber)
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

	public void setFilename(String filename)
	{
		if (filename == null)
		{
			filename = DEFAULT_FILENAME;
		}
		this.filename = filename;
	}

}
