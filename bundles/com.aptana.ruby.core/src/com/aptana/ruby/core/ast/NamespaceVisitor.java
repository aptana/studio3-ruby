/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import org.jrubyparser.ast.Colon3Node;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;

public class NamespaceVisitor extends AbstractNodeLocator
{

	private static final String NAMESPACE_DELIMITER = "::"; //$NON-NLS-1$

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
			// Don't pop if this module ends after offset!
			if (iVisited.getPosition().getEndOffset() < offset)
			{
				popType();
			}
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
		while ((type = popType()) != null) // $codepro.audit.disable assignmentInCondition
		{
			builder.insert(0, type).insert(0, NAMESPACE_DELIMITER);
		}
		if (builder.length() > 0)
		{
			builder.delete(0, 2);
		}

		return builder.toString();
	}
}
