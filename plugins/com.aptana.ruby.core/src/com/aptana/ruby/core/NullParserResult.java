/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.util.Collections;
import java.util.List;

import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserResult;
import org.jrubyparser.StaticScope;

public class NullParserResult extends ParserResult
{

	@Override
	public Node getAST()
	{
		return null;
	}

	@Override
	public List<Node> getBeginNodes()
	{
		return Collections.emptyList();
	}

	@Override
	public int getEndOffset()
	{
		return 0;
	}

	@Override
	public StaticScope getScope()
	{
		return null;
	}
}
