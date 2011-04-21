package com.aptana.editor.ruby;

import org.jrubyparser.CompatVersion;

import com.aptana.parsing.ParseState;

public class RubyParseState extends ParseState
{
	private CompatVersion compatVersion = CompatVersion.BOTH;

	public void setVersion(CompatVersion compatVersion)
	{
		this.compatVersion = compatVersion;
	}

	public CompatVersion getCompatVersion()
	{
		return compatVersion;
	}

}
