package com.aptana.editor.ruby.parsing.ast;

import org.jrubyparser.ast.Node;

public interface INodeAcceptor
{

	public boolean accepts(Node node);
}
