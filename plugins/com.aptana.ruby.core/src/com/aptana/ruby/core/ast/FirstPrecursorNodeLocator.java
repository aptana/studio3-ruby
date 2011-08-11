/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import org.jrubyparser.ast.Node;

public class FirstPrecursorNodeLocator extends AbstractNodeLocator
{
	private int offset;
	private INodeAcceptor acceptor;
	private Node locatedNode;

	// Finds the first node preceding the given offset that is accepted by the acceptor.
	// +param+ rootNode
	// Root Node that contains all nodes to search.
	// +param+ offset
	// Offset to search backwards from; returned node must occur strictly before this (i.e. end before offset.)
	// +param+ acceptor
	// block defining the condition which the desired node fulfills.
	// @return First precursor or nil.
	public Node find(Node root_node, int offset, INodeAcceptor acceptor)
	{
		this.locatedNode = null;
		this.offset = offset;
		this.acceptor = acceptor;

		// Traverse to find closest precursor
		root_node.accept(this);

		// Return the match
		return this.locatedNode;
	}

	@Override
	protected Object handleNode(Node node)
	{
		// TODO This will include nodes that envelop nodeStart, not only those starting strictly before it.
		// If this behavior is unwanted, remove the || (node.position.start_offset <= offset)
		// in the conditional
		if ((node.getPosition().getEndOffset() <= this.offset) || (node.getPosition().getStartOffset() <= this.offset))
		{
			if (acceptor.accepts(node))
			{
				this.locatedNode = node;
			}
		}

		return super.handleNode(node);
	}
}
