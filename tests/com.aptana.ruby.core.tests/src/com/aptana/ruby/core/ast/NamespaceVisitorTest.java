package com.aptana.ruby.core.ast;

import junit.framework.TestCase;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.parser.ParserResult;

import com.aptana.ruby.core.RubySourceParser;

public class NamespaceVisitorTest extends TestCase
{

	public void testNamespaceIsCorrectWhenNoNodesFollowOffset() throws Exception
	{
		String src = "module Namespace\n" + //
				"  SubClass\n" + //
				"end\n"; //

		NamespaceVisitor visitor = new NamespaceVisitor();
		RubySourceParser parser = new RubySourceParser(CompatVersion.BOTH);
		ParserResult result = parser.parse(src);
		assertEquals("Namespace at offset wasn't correct", "Namespace", visitor.getNamespace(result.getAST(), 27));
	}

}
