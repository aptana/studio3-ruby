/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.parsing.ast;

import java.util.ArrayList;
import java.util.List;

import com.aptana.editor.ruby.IRubyConstants;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.ParseNode;

public abstract class RubyElement extends ParseNode implements IRubyElement
{

	private static final String EMPTY = ""; //$NON-NLS-1$
	private int occurrenceCount = 1;

	protected RubyElement()
	{
		super(IRubyConstants.CONTENT_TYPE_RUBY);
	}

	public RubyElement(int start, int end)
	{
		super(IRubyConstants.CONTENT_TYPE_RUBY);
		this.setLocation(start, end);
	}

	public String getName()
	{
		return EMPTY;
	}

	public IRubyElement[] getChildrenOfType(int type)
	{
		List<IRubyElement> list = new ArrayList<IRubyElement>();
		IParseNode[] children = getChildren();
		for (IParseNode child : children)
		{
			if (child.getNodeType() == type)
			{
				list.add((IRubyElement) child);
			}
		}
		return list.toArray(new IRubyElement[list.size()]);
	}

	public List<IRubyElement> getChildrenOfTypeRecursive(int type)
	{
		List<IRubyElement> list = new ArrayList<IRubyElement>();
		IParseNode[] children = getChildren();
		for (IParseNode child : children)
		{
			if (child.getNodeType() == type)
			{
				list.add((IRubyElement) child);
			}
			list.addAll(((RubyElement) child).getChildrenOfTypeRecursive(type));
		}
		return list;
	}

	public int getOccurrenceCount()
	{
		return occurrenceCount;
	}

	public void incrementOccurrence()
	{
		occurrenceCount++;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RubyElement))
			return false;

		return getName().equals(((RubyElement) obj).getName());
	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + getName().hashCode();
	}
}
