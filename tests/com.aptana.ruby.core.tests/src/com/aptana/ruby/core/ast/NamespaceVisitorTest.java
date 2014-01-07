/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.parser.ParserResult;

import com.aptana.ruby.core.RubySourceParser;

@SuppressWarnings("nls")
public class NamespaceVisitorTest
{

	@Test
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
