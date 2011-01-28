/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

public class RubyRegexScannerTest extends TestCase
{
	protected ITokenScanner scanner;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		scanner = new RubyRegexpScanner()
		{
			@Override
			protected IToken getToken(String tokenName)
			{
				return new Token(tokenName);
			}
		};
	}

	@Override
	protected void tearDown() throws Exception
	{
		scanner = null;

		super.tearDown();
	}

	protected void assertToken(String scope, int offset, int length)
	{
		assertEquals("Token scope doesn't match", scope, scanner.nextToken().getData());
		assertEquals("Offsets don't match", offset, scanner.getTokenOffset());
		assertEquals("Lengths don't match", length, scanner.getTokenLength());
	}

	public void testBasicTokenizing()
	{
		String src = "[\\x20-\\x7F]+";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken("string.regexp.ruby", 0, 1);
		assertToken("constant.character.escape.ruby", 1, 4);
		assertToken("string.regexp.ruby", 5, 1);
		assertToken("constant.character.escape.ruby", 6, 4);
		assertToken("string.regexp.ruby", 10, 1);
		assertToken("string.regexp.ruby", 11, 1);
	}

}
