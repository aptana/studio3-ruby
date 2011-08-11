/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import org.jrubyparser.ast.Node;

public class OffsetNodeLocator extends ClosestSpanningNodeLocator
{

	// Gets the most closely spanning node of the requested offset.
	//
	// +root_node+
	// Node which should span or have children spanning the offset.
	// +offset+
	// Offset to locate the node of.
	// @return Node most closely spanning the requested offset.
	public Node find(Node root_node, int offset)
	{
		return super.find(root_node, offset, new INodeAcceptor()
		{
			public boolean accepts(Node node)
			{
				return node.getNodeType() != org.jrubyparser.ast.NodeType.NEWLINENODE;
			}
		});
	}
}
