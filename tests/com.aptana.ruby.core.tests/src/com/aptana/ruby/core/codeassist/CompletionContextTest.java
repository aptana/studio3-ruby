package com.aptana.ruby.core.codeassist;

import org.junit.Test;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.Node;

@SuppressWarnings("nls")
public class CompletionContextTest
{

	@Test
	public void testGetReceiverAfterDoubleColon() throws Exception
	{
		String src = "Namespace::";
		CompletionContext context = new CompletionContext(null, src, 11);

		assertEquals("Didn't generate expected parseable corrected source from common broken syntax state",
				"Namespace", context.getCorrectedSource());
		assertFalse(context.isNotParseable());

		Node receiver = context.getReceiver();
		assertNotNull("Context failed to give us a receiver", receiver);
		assertTrue("Receiver wasn't a constant node", receiver instanceof ConstNode);
		ConstNode constNode = (ConstNode) receiver;
		assertEquals("Receiver wasn't correct", "Namespace", constNode.getName());

		assertFalse("Context falsely reports an empty prefix", context.emptyPrefix());
		assertTrue("Context doesn't properly report that we're after a double colon", context.isDoubleColon());

		assertEquals("Context reports an incorrect full prefix", "Namespace::", context.getFullPrefix());
		assertEquals("Context reports an incorrect partial prefix", "", context.getPartialPrefix());
	}

	@Test
	public void testGetReceiverAfterDoubleColonInsideType() throws Exception
	{
		String src = "module Namespace\n" + //
				"  SubClass::\n" + //
				"end\n"; //
		CompletionContext context = new CompletionContext(null, src, 28);

		assertEquals("Didn't generate expected parseable corrected source from common broken syntax state",
				"module Namespace\n  SubClass\nend\n", context.getCorrectedSource());
		assertFalse(context.isNotParseable());

		Node receiver = context.getReceiver();
		assertNotNull("Context failed to give us a receiver", receiver);
		assertTrue("Receiver wasn't a constant node", receiver instanceof ConstNode);
		ConstNode constNode = (ConstNode) receiver;
		assertEquals("Receiver wasn't correct", "SubClass", constNode.getName());

		assertFalse("Context falsely reports an empty prefix", context.emptyPrefix());
		assertTrue("Context doesn't properly report that we're after a double colon", context.isDoubleColon());

		assertEquals("Context reports an incorrect full prefix", "SubClass::", context.getFullPrefix());
		assertEquals("Context reports an incorrect partial prefix", "", context.getPartialPrefix());

		assertEquals("Namespace at offset wasn't correct", "Namespace", context.getNamespace());
	}

}
