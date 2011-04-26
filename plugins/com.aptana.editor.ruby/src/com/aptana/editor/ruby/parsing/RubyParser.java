/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.parsing;

import org.jrubyparser.CompatVersion;

import com.aptana.editor.ruby.parsing.ast.RubyScript;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseRootNode;

public class RubyParser implements IParser
{

	public RubyParser()
	{

	}

	public IParseRootNode parse(IParseState parseState) throws Exception
	{
		String source = new String(parseState.getSource());
		RubyScript root = new RubyScript(parseState.getStartingOffset(), parseState.getStartingOffset()
				+ source.length());
		RubyStructureBuilder builder = new RubyStructureBuilder(root);
		SourceElementVisitor visitor = new SourceElementVisitor(builder);

		CompatVersion compatVersion = CompatVersion.BOTH;
		if (parseState instanceof RubyParseState)
		{
			compatVersion = ((RubyParseState) parseState).getCompatVersion();
		}
		visitor.acceptNode(getSourceParser(compatVersion).parse(source).getAST());
		parseState.setParseResult(root);

		return root;
	}

	public RubySourceParser getSourceParser(CompatVersion rubyVersion)
	{
		// TODO cache the parser by version here?
		return new RubySourceParser(rubyVersion);
	}
}
