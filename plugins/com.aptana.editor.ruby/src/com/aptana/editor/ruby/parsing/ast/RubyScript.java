/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.parsing.ast;

import com.aptana.editor.ruby.core.IImportContainer;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.editor.ruby.core.IRubyScript;
import com.aptana.parsing.ast.IParseNode;

public class RubyScript extends RubyElement implements IRubyScript
{

	private RubyImportContainer fImportContainer;

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
		return new IParseNode[0];
	}
}
