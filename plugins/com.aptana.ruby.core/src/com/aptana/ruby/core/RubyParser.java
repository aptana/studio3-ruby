/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.util.ArrayList;
import java.util.List;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.parser.ParserResult;

import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.ruby.core.ast.SourceElementVisitor;
import com.aptana.ruby.internal.core.RubyComment;
import com.aptana.ruby.internal.core.RubyScript;

public class RubyParser implements IParser
{

	public RubyParser()
	{

	}

	public IParseRootNode parse(IParseState parseState)
	{
		String source = new String(parseState.getSource());
		RubyScript root = new RubyScript(parseState.getStartingOffset(), parseState.getStartingOffset()
				+ source.length() - 1);
		RubyStructureBuilder builder = new RubyStructureBuilder(root);
		SourceElementVisitor visitor = new SourceElementVisitor(builder);

		CompatVersion compatVersion = CompatVersion.BOTH;
		if (parseState instanceof RubyParseState)
		{
			compatVersion = ((RubyParseState) parseState).getCompatVersion();
		}
		ParserResult result = getSourceParser(compatVersion).parse(source);
		visitor.acceptNode(result.getAST());
		List<IParseNode> commentParseNodes = new ArrayList<IParseNode>();
		for (CommentNode commentNode : result.getCommentNodes())
		{
			commentParseNodes.add(new RubyComment(commentNode));
		}
		root.setCommentNodes(commentParseNodes);
		parseState.setParseResult(root);

		return root;
	}

	public RubySourceParser getSourceParser(CompatVersion rubyVersion)
	{
		// TODO cache the parser by version here?
		return new RubySourceParser(rubyVersion);
	}
}
