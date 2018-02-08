/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import java.util.Stack;

import org.jrubyparser.ast.Node;

public abstract class AbstractNodeLocator extends InOrderVisitor
{
	private Stack<String> typeNameStack;

	public boolean spansOffset(Node node, int offset)
	{
		if (node == null || node.getPosition() == null)
		{
			return false;
		}
		return node.getPosition().getStartOffset() <= offset && node.getPosition().getEndOffset() > offset;
	}

	protected int spanLength(Node node)
	{
		if (node == null || node.getPosition() == null)
		{
			return 0;
		}
		return node.getPosition().getEndOffset() - node.getPosition().getStartOffset();
	}

	protected synchronized void pushType(String type_name)
	{
		if (typeNameStack == null)
		{
			typeNameStack = new Stack<String>();
		}
		typeNameStack.push(type_name);
	}

	protected String popType()
	{
		if (typeNameStack == null || typeNameStack.isEmpty())
		{
			return null;
		}
		return typeNameStack.pop();
	}

	protected String peekType()
	{
		if (typeNameStack == null || typeNameStack.isEmpty())
		{
			return null;
		}
		return typeNameStack.peek();
	}
}
