/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import java.util.List;

import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.core.IImportContainer;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyScript;

public class RubyScript extends RubyElement implements IRubyScript
{

	// TODO Can we move this to some re-usable constant in Parsing plugin?
	private static final IParseNode[] NO_PARSE_NODES = new IParseNode[0];

	private RubyImportContainer fImportContainer;
	private List<IParseNode> commentNodes;

	public RubyScript(int start, int end)
	{
		super(start, end);
	}

	public IImportContainer getImportContainer()
	{
		if (fImportContainer == null)
		{
			fImportContainer = new RubyImportContainer();
			addChild(fImportContainer);
		}
		return fImportContainer;
	}

	@Override
	public short getNodeType()
	{
		return IRubyElement.SCRIPT;
	}

	public IParseNode[] getCommentNodes()
	{
		if (commentNodes == null || commentNodes.isEmpty())
		{
			return NO_PARSE_NODES;
		}
		return commentNodes.toArray(new IParseNode[commentNodes.size()]);
	}

	public void setCommentNodes(List<IParseNode> commentParseNodes)
	{
		this.commentNodes = commentParseNodes;
	}
}
