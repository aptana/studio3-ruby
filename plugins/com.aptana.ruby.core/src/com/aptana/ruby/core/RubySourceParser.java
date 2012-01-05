/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.IRubyWarnings;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserResult;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.ParserSupport19;
import org.jrubyparser.parser.Ruby18Parser;
import org.jrubyparser.parser.Ruby19Parser;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.parsing.ParserPoolFactory;

/**
 * @author Chris Williams
 * @author Michael Xia
 * @deprecated Please use {@link ParserPoolFactory} to make use of the {@link RubyParser}. We may need to alter it and
 *             {@link RubyParseState} to get what we want.
 */
public class RubySourceParser
{

	private IRubyWarnings warnings;
	private org.jrubyparser.parser.RubyParser parser;
	private ParserConfiguration config;
	private CompatVersion compatVersion;

	public RubySourceParser(CompatVersion compatVersion, IRubyWarnings warnings)
	{
		this.compatVersion = compatVersion;
		this.warnings = warnings;
	}

	public RubySourceParser(CompatVersion compatVersion)
	{
		this(compatVersion, new org.jrubyparser.Parser.NullWarnings());
	}

	public ParserResult parse(String source)
	{
		return parse((String) null, source);
	}

	public ParserResult parse(String fileName, String source)
	{
		return parse(fileName, source, false);
	}

	/**
	 * @param fileName
	 *            the name of the file
	 * @param source
	 *            the source text
	 * @param bypassCache
	 *            boolean indicating if to force a parse and bypass any cached results
	 * @return the parse result
	 */
	private ParserResult parse(String fileName, String source, boolean bypassCache)
	{
		if (source == null)
		{
			return new NullParserResult();
		}

		ParserResult ast = null;
		Reader reader = new BufferedReader(new StringReader(source));
		try
		{
			ast = parse(fileName, reader);
		}
		catch (Exception e)
		{
			if (IdeLog.isInfoEnabled(RubyCorePlugin.getDefault(), null))
			{
				IdeLog.logInfo(RubyCorePlugin.getDefault(), "Unable to parse ruby file", e, null); //$NON-NLS-1$
			}
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		if (ast == null)
		{
			return new NullParserResult();
		}
		return ast;
	}

	private ParserResult parse(String fileName, Reader content) throws IOException
	{
		if (fileName == null)
		{
			fileName = StringUtil.EMPTY;
		}
		if (parser == null)
		{
			config = getParserConfig();
			if (compatVersion == CompatVersion.RUBY1_8)
			{
				ParserSupport support = new ParserSupport();
				support.setConfiguration(config);
				parser = new Ruby18Parser(support);
			}
			else
			{
				ParserSupport19 support = new ParserSupport19();
				support.setConfiguration(config);
				parser = new Ruby19Parser(support);
			}
		}
		parser.setWarnings(warnings);
		LexerSource lexerSource = LexerSource.getSource(fileName, content, config);
		ParserResult result = parser.parse(config, lexerSource);
		postProcessResult(result);
		return result;
	}

	/**
	 * Hook for subclasses to perform extra work on the resulting AST such as doing a pass through comments.
	 * 
	 * @param result
	 */
	protected void postProcessResult(ParserResult result)
	{
		// do nothing
	}

	protected ParserConfiguration getParserConfig()
	{
		return new ParserConfiguration(0, compatVersion);
	}
}
