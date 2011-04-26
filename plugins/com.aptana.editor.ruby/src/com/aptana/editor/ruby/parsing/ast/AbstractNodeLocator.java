package com.aptana.editor.ruby.parsing.ast;

import java.util.Stack;

import org.jrubyparser.ast.Node;

public abstract class AbstractNodeLocator extends InOrderVisitor
{
	private Stack<String> typeNameStack;

	protected boolean spansOffset(Node node, int offset)
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
