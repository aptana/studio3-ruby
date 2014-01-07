/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

@SuppressWarnings("nls")
public class SassCodeScannerTest
{

	protected ITokenScanner scanner;

//	@Override
	@Before
	public void setUp() throws Exception
	{
//		super.setUp();

		scanner = new SassCodeScanner()
		{
			protected IToken createToken(String string)
			{
				return SassCodeScannerTest.this.getToken(string);
			};
		};
	}

//	@Override
	@After
	public void tearDown() throws Exception
	{
		scanner = null;

//		super.tearDown();
	}

	protected void assertToken(IToken token, int offset, int length)
	{
		assertToken(null, token, offset, length);
	}

	protected void assertToken(String msg, IToken token, int offset, int length)
	{
		assertEquals("Token scope doesn't match", token.getData(), scanner.nextToken().getData());
		assertEquals("Offsets don't match", offset, scanner.getTokenOffset());
		assertEquals("Lengths don't match", length, scanner.getTokenLength());
	}

	protected IToken getToken(String tokenName)
	{
		return new Token(tokenName);
	}

	@Test
	public void testH1Through6()
	{
		String src = "h1 h2 h3 h4 h5 h6 ";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		for (int i = 0; i < src.length(); i += 3)
		{
			assertToken(getToken("meta.selector.sass entity.name.tag.sass"), i, 2);
			assertToken(getToken("meta.selector.sass"), i + 2, 1);
		}
	}

	@Test
	public void testCSS3PropertyNames()
	{
		String src = "border-radius: 1px\n" + "border-image-width: 1px\n" + "box-decoration-break: clone";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("meta.property-name.sass support.type.property-name.sass"), 0, 13);
		assertToken(getToken("meta.property-value.sass punctuation.separator.key-value.sass"), 13, 1);
		assertToken(getToken("meta.property-value.sass"), 14, 1);
		assertToken(getToken("meta.property-value.sass constant.numeric.sass"), 15, 1);
		assertToken(getToken("meta.property-value.sass keyword.other.unit.sass"), 16, 2);
		assertToken(Token.WHITESPACE, 18, 1);
		assertToken(getToken("meta.property-name.sass support.type.property-name.sass"), 19, 18);
		assertToken(getToken("meta.property-value.sass punctuation.separator.key-value.sass"), 37, 1);
		assertToken(getToken("meta.property-value.sass"), 38, 1);
		assertToken(getToken("meta.property-value.sass constant.numeric.sass"), 39, 1);
		assertToken(getToken("meta.property-value.sass keyword.other.unit.sass"), 40, 2);
		assertToken(Token.WHITESPACE, 42, 1);
		assertToken(getToken("meta.property-name.sass support.type.property-name.sass"), 43, 20);
		assertToken(getToken("meta.property-value.sass punctuation.separator.key-value.sass"), 63, 1);
		assertToken(getToken("meta.property-value.sass"), 64, 1);
	}

	// FIXME Test actual Sass, not CSS!

	@Test
	public void testSmallCaps()
	{
		String src = "small { font: small-caps; }";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("meta.selector.sass entity.name.tag.sass"), 0, 5);
		assertToken(getToken("meta.selector.sass"), 5, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 6, 1);
		assertToken(getToken("meta.property-list.sass"), 7, 1);
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 8, 4);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				12, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 13, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				14, 10);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 24,
				1);
		assertToken(getToken("meta.property-list.sass"), 25, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 26, 1);
	}

	@Test
	public void testVariableDefinition()
	{
		String src = "!blue = #3bbfce";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("variable.other.sass"), 0, 5);
		assertToken(Token.WHITESPACE, 5, 1);
		assertToken(getToken("punctuation.definition.entity.sass"), 6, 1);
		assertToken(Token.WHITESPACE, 7, 1);
		assertToken(getToken("constant.other.color.rgb-value.sass"), 8, 7);
	}

	@Test
	public void testVariableUsage()
	{
		String src = ".content_navigation\n  border-color = !blue";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("meta.selector.sass entity.other.attribute-name.class.sass"), 0, 19);
		assertToken(Token.WHITESPACE, 19, 3);
		assertToken(getToken("meta.property-name.sass support.type.property-name.sass"), 22, 12);
		assertToken(Token.WHITESPACE, 34, 1);
		assertToken(getToken("punctuation.definition.entity.sass"), 35, 1);
		assertToken(Token.WHITESPACE, 36, 1);
		assertToken(getToken("variable.other.sass"), 37, 5);
	}

	@Test
	public void testDeprecatedMixinDefinition()
	{
		String src = "=table-scaffolding\n  th\n    text-align: center";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("variable.other.sass"), 0, 18);
		assertToken(Token.WHITESPACE, 18, 3);
		assertToken(getToken("meta.selector.sass entity.name.tag.sass"), 21, 2);
		assertToken(Token.WHITESPACE, 23, 5);
		assertToken(getToken("meta.property-name.sass support.type.property-name.sass"), 28, 10);
		assertToken(getToken("meta.property-value.sass punctuation.separator.key-value.sass"), 38, 1);
		assertToken(getToken("meta.property-value.sass"), 39, 1);
		assertToken(getToken("meta.property-value.sass support.constant.property-value.sass"), 40, 6);
	}

	@Test
	public void testMixinDefinition()
	{
		String src = "@mixin silly-links {\n" + //
				"  a {\n" + //
				"    color: blue;\n" + //
				"    background-color: red;\n" + //
				"  }\n" + //
				"}";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());
		// @mixin
		assertToken(getToken("keyword.control.at-rule.mixin.sass"), 0, 6);
		assertToken(Token.WHITESPACE, 6, 1);
		// silly-links
		assertToken(getToken("entity.name.function.sass"), 7, 11);
		assertToken(Token.WHITESPACE, 18, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 19, 1);
		assertToken(getToken("meta.property-list.sass"), 20, 3);
		// a
		assertToken(getToken("meta.property-list.sass entity.name.tag.sass"), 23, 1);
		assertToken(getToken("meta.property-list.sass"), 24, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 25, 1);
		assertToken(getToken("meta.property-list.sass"), 26, 5);
		// color:
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 31, 5);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				36, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 37, 1);
		// blue;\n
		assertToken(
				getToken("meta.property-list.sass meta.property-value.sass support.constant.color.w3c-standard-color-name.sass"),
				38, 4);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 42,
				1);
		assertToken(getToken("meta.property-list.sass"), 43, 5);
	}

	@Test
	public void testMixinInclusion()
	{
		String src = "@include silly-links;";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("keyword.control.at-rule.include.sass"), 0, 8);
		assertToken(Token.WHITESPACE, 8, 1);
		// silly-links
		assertToken(getToken("entity.name.function.sass"), 9, 11);
		assertToken(getToken("punctuation.terminator.rule.sass"), 20, 1);
	}

	@Test
	public void testDeprecatedMixinUsage()
	{
		String src = "#data\n  +table-scaffolding";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("meta.selector.sass entity.other.attribute-name.id.sass"), 0, 5);
		assertToken(Token.WHITESPACE, 5, 3);
		assertToken(getToken("variable.other.sass"), 8, 18);
	}

	@Test
	public void testBasicTokenizing()
	{
		String src = "html { color: red; background-color: #333; }";
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());

		assertToken(getToken("meta.selector.sass entity.name.tag.sass"), 0, 4);
		assertToken(getToken("meta.selector.sass"), 4, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 5, 1);
		assertToken(getToken("meta.property-list.sass"), 6, 1);
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 7, 5);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				12, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 13, 1);
		assertToken(
				getToken("meta.property-list.sass meta.property-value.sass support.constant.color.w3c-standard-color-name.sass"),
				14, 3);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 17,
				1);
		assertToken(getToken("meta.property-list.sass"), 18, 1);
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 19, 16);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				35, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 36, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.other.color.rgb-value.sass"),
				37, 4);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 41,
				1);
		assertToken(getToken("meta.property-list.sass"), 42, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 43, 1);
	}

	@Test
	public void testBasicTokenizing2()
	{
		String src = "body {\n" + // 1
				"  background-image: url();\n" + // 2
				"  background-position-x: left;\n" + // 3
				"  background-position-y: top;\n" + // 4
				"  background-repeat: repeat-x;\n" + // 5
				"  font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;\n" + // 6
				"}\n" + // 7
				"\n" + // 8
				".main {\n" + // 9
				"  border: 1px dotted #222222;\n" + // 10
				"  margin: 5px;\n" + // 11
				"}\n" + // 12
				"\n" + // 13
				".header {\n" + // 14
				"  background-color: #FFFFFF;\n" + // 15
				"  color: #444444;\n" + // 16
				"  font-size: xx-large;\n" + // 17
				"}\n" + // 18
				"\n" + // 19
				".menu {\n" + // 20
				"  border-top: 2px solid #FC7F22;\n" + // 21
				"  background-color: #3B3B3B;\n" + // 22
				"  color: #FFFFFF;\n" + // 23
				"  text-align: right;\n" + // 24
				"  vertical-align: right;\n" + // 25
				"  font-size: small;\n" + // 26
				"}\n" + // 27
				"\n" + // 28
				".menu a {\n" + // 29
				"  color: #DDDDDD;\n" + // 30
				"  text-decoration: none;\n" + // 31
				"}\n"; // 32
		IDocument document = new Document(src);
		scanner.setRange(document, 0, src.length());
		// line 1
		assertToken(getToken("meta.selector.sass entity.name.tag.sass"), 0, 4);
		assertToken(getToken("meta.selector.sass"), 4, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 5, 1);
		assertToken(getToken("meta.property-list.sass"), 6, 3);
		// line 2
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 9, 16);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				25, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 26, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.function.misc.sass"), 27, 3);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.section.function.sass"), 30,
				1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.section.function.sass"), 31,
				1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 32,
				1);
		assertToken(getToken("meta.property-list.sass"), 33, 3);
		// line 3
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 36, 21);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				57, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 58, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				59, 4);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 63,
				1);
		assertToken(getToken("meta.property-list.sass"), 64, 3);
		// line 4
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 67, 21);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				88, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 89, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				90, 3);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 93,
				1);
		assertToken(getToken("meta.property-list.sass"), 94, 3);
		// line 5
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 97, 17);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				114, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 115, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				116, 8);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 124,
				1);
		assertToken(getToken("meta.property-list.sass"), 125, 3);
		// line 6
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 128,
				11);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				139, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 140, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.font-name.sass"), 141,
				7);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.sass"), 148, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 149, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.font-name.sass"), 150,
				6);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.sass"), 156, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 157, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.font-name.sass"), 158,
				5);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.sass"), 163, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 164, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.font-name.sass"), 165,
				9);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.sass"), 174, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 175, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.font-name.sass"), 176,
				10);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 186,
				1);
		assertToken(getToken("meta.property-list.sass"), 187, 1);
		// line 7
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 188, 1);
		assertToken(Token.WHITESPACE, 189, 2);
		// line 9
		assertToken(getToken("meta.selector.sass entity.other.attribute-name.class.sass"), 191, 5);
		assertToken(getToken("meta.selector.sass"), 196, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 197, 1);
		assertToken(getToken("meta.property-list.sass"), 198, 3);
		// line 10 border: 1px dotted #222222;
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 201, 6);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				207, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 208, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.numeric.sass"), 209, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass keyword.other.unit.sass"), 210, 2);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 212, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				213, 6);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 219, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.other.color.rgb-value.sass"),
				220, 7);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 227,
				1);
		assertToken(getToken("meta.property-list.sass"), 228, 3);
		// line 11 margin: 5px;
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 231, 6);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				237, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 238, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.numeric.sass"), 239, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass keyword.other.unit.sass"), 240, 2);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 242,
				1);
		assertToken(getToken("meta.property-list.sass"), 243, 1);
		// line 12
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 244, 1);
		assertToken(Token.WHITESPACE, 245, 2);
		// line 13 .header {
		assertToken(getToken("meta.selector.sass entity.other.attribute-name.class.sass"), 247, 7);
		assertToken(getToken("meta.selector.sass"), 254, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 255, 1);
		assertToken(getToken("meta.property-list.sass"), 256, 3);
		// line 14 background-color: #FFFFFF;
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 259,
				16);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				275, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 276, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.other.color.rgb-value.sass"),
				277, 7);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 284,
				1);
		assertToken(getToken("meta.property-list.sass"), 285, 3);
		// line 15 color: #444444;
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 288, 5);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				293, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 294, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass constant.other.color.rgb-value.sass"),
				295, 7);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 302,
				1);
		assertToken(getToken("meta.property-list.sass"), 303, 3);
		// line 16 font-size: xx-large;
		assertToken(getToken("meta.property-list.sass meta.property-name.sass support.type.property-name.sass"), 306, 9);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.separator.key-value.sass"),
				315, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass"), 316, 1);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass support.constant.property-value.sass"),
				317, 8);
		assertToken(getToken("meta.property-list.sass meta.property-value.sass punctuation.terminator.rule.sass"), 325,
				1);
		assertToken(getToken("meta.property-list.sass"), 326, 1);
		// line 17 }
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 327, 1);
		assertToken(Token.WHITESPACE, 328, 2);
		// line 19 .menu {
		assertToken(getToken("meta.selector.sass entity.other.attribute-name.class.sass"), 330, 5);
		assertToken(getToken("meta.selector.sass"), 335, 1);
		assertToken(getToken("meta.property-list.sass punctuation.section.property-list.sass"), 336, 1);
		assertToken(getToken("meta.property-list.sass"), 337, 3);
		// line 20
	}
}
