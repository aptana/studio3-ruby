/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import com.aptana.parsing.ast.INameNode;
import com.aptana.parsing.lexer.IRange;
import com.aptana.ruby.core.IRubyMember;

public abstract class NamedMember extends RubyElement implements IRubyMember
{
	private INameNode fNameNode;

	protected NamedMember(String name, int start, int nameStart, int nameEnd)
	{
		super(start, nameEnd);
		fNameNode = new NameNode(name, nameStart, nameEnd);
	}

	@Override
	public void addOffset(int offset)
	{
		IRange range = fNameNode.getNameRange();
		fNameNode = new NameNode(fNameNode.getName(), range.getStartingOffset() + offset, range.getEndingOffset()
				+ offset);
		super.addOffset(offset);
	}

	@Override
	public String getName()
	{
		return fNameNode.getName();
	}

	@Override
	public INameNode getNameNode()
	{
		return fNameNode;
	}

	@Override
	public String getText()
	{
		return fNameNode.getName();
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
