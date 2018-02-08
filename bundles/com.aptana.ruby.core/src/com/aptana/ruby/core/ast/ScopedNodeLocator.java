/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jrubyparser.ast.Node;

// Visitor to find all nodes within a specific scope adhering to a certain condition.
// 
// @author Jason Morrison
public class ScopedNodeLocator extends AbstractNodeLocator
{
	private List<Node> locatedNodes;
	private INodeAcceptor acceptor;

	// Finds all nodes within the scoping node that is accepted by the acceptor.
	//
	// +scoping_node+
	// Root Node that contains all nodes to search.
	// +acceptor+
	// INodeAcceptor defining the condition which the desired node fulfills.
	// @return List of located nodes.
	public List<Node> find(Node scopingNode, INodeAcceptor acceptor)
	{
		if (scopingNode == null)
		{
			return Collections.emptyList();
		}

		this.locatedNodes = new ArrayList<Node>();
		this.acceptor = acceptor;

		// Traverse to find all matches
		scopingNode.accept(this);

		// Return the matches
		return this.locatedNodes;
	}

	@Override
	protected Object handleNode(Node visited)
	{
		if (acceptor.accepts(visited))
		{
			locatedNodes.add(visited);
		}
		return super.handleNode(visited);
	}
}
