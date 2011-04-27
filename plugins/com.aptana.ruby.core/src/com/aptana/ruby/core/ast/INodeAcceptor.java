package com.aptana.ruby.core.ast;

import org.jrubyparser.ast.Node;

public interface INodeAcceptor
{

	public boolean accepts(Node node);
}
