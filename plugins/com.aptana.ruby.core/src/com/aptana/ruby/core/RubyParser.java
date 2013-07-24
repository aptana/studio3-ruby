/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserResult;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.ParserSupport19;
import org.jrubyparser.parser.Ruby18Parser;
import org.jrubyparser.parser.Ruby19Parser;

import com.aptana.core.build.IProblem.Severity;
import com.aptana.core.logging.IdeLog;
import com.aptana.parsing.AbstractParser;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.WorkingParseResult;
import com.aptana.parsing.ast.IParseError;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseError;
import com.aptana.ruby.core.ast.SourceElementVisitor;
import com.aptana.ruby.internal.core.RubyComment;
import com.aptana.ruby.internal.core.RubyScript;

public class RubyParser extends AbstractParser
{

	public RubyParser()
	{
	}

	protected void parse(IParseState parseState, WorkingParseResult working)
	{
		String source = parseState.getSource();
		RubyScript root = new RubyScript(parseState.getStartingOffset(), parseState.getStartingOffset()
				+ source.length() - 1);

		CompatVersion compatVersion = CompatVersion.BOTH;
		int lineNumber = 0;
		String fileName = "<unnamed file>"; //$NON-NLS-1$		
		if (parseState instanceof RubyParseState)
		{
			RubyParseState rubyParseState = (RubyParseState) parseState;
			compatVersion = rubyParseState.getCompatVersion();
			lineNumber = rubyParseState.getStartingLineNumber();
			fileName = rubyParseState.getFilename();
		}

		CollectingRubyWarnings warnings = new CollectingRubyWarnings(fileName);

		org.jrubyparser.parser.RubyParser parser = null;
		ParserConfiguration config = new ParserConfiguration(lineNumber, compatVersion);
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
		parser.setWarnings(warnings);
		LexerSource lexerSource = LexerSource.getSource(fileName, new StringReader(source), config);
		lexerSource.setOffset(parseState.getStartingOffset());
		ParserResult result = new NullParserResult();
		try
		{
			result = parser.parse(config, lexerSource);

			RubyStructureBuilder builder = new RubyStructureBuilder(root);
			SourceElementVisitor visitor = new SourceElementVisitor(builder);
			visitor.acceptNode(result.getAST());
			List<IParseNode> commentParseNodes = new ArrayList<IParseNode>();
			for (CommentNode commentNode : result.getCommentNodes())
			{
				commentParseNodes.add(new RubyComment(commentNode, getText(source, commentNode.getPosition())));
			}
			root.setCommentNodes(commentParseNodes);
		}
		catch (SyntaxException se)
		{
			int start = se.getPosition().getStartOffset();
			working.addError(new ParseError(IRubyConstants.CONTENT_TYPE_RUBY, start, se.getPosition().getEndOffset()
					- start, se.getMessage(), Severity.ERROR));
		}
		catch (IOException e)
		{
			IdeLog.logError(RubyCorePlugin.getDefault(), "Failed to parse ruby code due to IOException", e); //$NON-NLS-1$
		}
		// Add warnings

		CollectingRubyWarnings collector = (CollectingRubyWarnings) warnings;
		for (IParseError warning : collector.getWarnings())
		{
			working.addError(warning);
		}

		working.setParseResult(root);
	}

	private String getText(String source, SourcePosition position)
	{
		return new String(source.substring(position.getStartOffset(), position.getEndOffset()));
	}
}
