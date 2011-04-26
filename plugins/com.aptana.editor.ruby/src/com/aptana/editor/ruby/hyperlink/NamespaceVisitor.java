package com.aptana.editor.ruby.hyperlink;

import org.jrubyparser.ast.Colon3Node;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;

import com.aptana.editor.ruby.parsing.ast.AbstractNodeLocator;

public class NamespaceVisitor extends AbstractNodeLocator
{

	private int offset;
	private boolean done;

	@Override
	public Object visitModuleNode(ModuleNode iVisited)
	{
		if (!done)
		{
			Colon3Node node = iVisited.getCPath();
			pushType(node.getName());
		}
		Object obj = super.visitModuleNode(iVisited);
		if (!done)
		{
			popType();
		}
		return obj;
	}

	@Override
	protected Object handleNode(Node visited)
	{
		if (done || visited.getPosition().getStartOffset() > offset)
		{
			done = true;
			return null;
		}
		return super.handleNode(visited);
	}

	public String getNamespace(Node root_node, int offset)
	{
		if (root_node == null)
		{
			return null;
		}
		this.done = false;
		this.offset = offset;

		root_node.accept(this);

		StringBuilder builder = new StringBuilder();
		String type = null;
		while ((type = popType()) != null)
		{
			builder.insert(0, type).insert(0, "::");
		}
		if (builder.length() > 0)
		{
			builder.delete(0, 2);
		}

		return builder.toString();
	}
}
