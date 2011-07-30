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
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.IPartitioningConfiguration;

public class RubyCodeScannerTest extends TestCase
{
	protected ITokenScanner scanner;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		scanner = new RubyCodeScanner()
		{
			protected IToken getToken(String tokenName)
			{
				return new Token(tokenName);
			};
		};
	}

	@Override
	protected void tearDown() throws Exception
	{
		scanner = null;

		super.tearDown();
	}

	private void setUpScanner(String code)
	{
		setUpScanner(code, 0, code.length());
	}

	private void setUpScanner(String code, int offset, int length)
	{
		Document doc = new Document(code);
		scanner.setRange(doc, offset, length);
	}

	private void assertToken(String scope, int offset, int length)
	{
		assertToken(getToken(scope), offset, length);
	}

	private void assertToken(IToken expectedToken, int offset, int length)
	{
		// FIXME MErge with AbstractTokenScannerTestCase.assertToken
		IToken token = scanner.nextToken();
		assertEquals("Offsets don't match", offset, scanner.getTokenOffset());
		assertEquals("Lengths don't match", length, scanner.getTokenLength());
		assertEquals("Token scope/data doesn't match", expectedToken.getData(), token.getData());
		assertEquals(expectedToken.isWhitespace(), token.isWhitespace());
		assertEquals(expectedToken.isOther(), token.isOther());
	}

	private IToken getToken(String scope)
	{
		return new Token(scope);
	}

	public void testNoParensNextIdentifierIsntParameter()
	{
		String code = "def denominator\nmethod_call\nend";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 11); // 'denominator'
		assertToken(Token.WHITESPACE, 15, 1); // '\n'
		assertToken("", 16, 11); // 'method_call'
		assertToken(Token.WHITESPACE, 27, 1); // '\n'
		assertToken("keyword.control.ruby", 28, 3); // 'end'
	}

	public void testMethodDefinition()
	{
		String code = "def denominator() 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 11); // 'denominator'
		assertToken("punctuation.definition.parameters.ruby", 15, 1); // '('
		assertToken("punctuation.definition.parameters.ruby", 16, 1); // ')'
		assertToken(Token.WHITESPACE, 17, 1); // ' '
		assertToken("constant.numeric.ruby", 18, 1); // '0'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("keyword.control.ruby", 20, 3); // 'end'
	}

	public void testSpecialCompareMethodDefinition()
	{
		String code = "def <=>(other) 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 3); // '<=>'
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // '('
		assertToken("variable.parameter.function.ruby", 8, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 13, 1); // ')'
		assertToken(Token.WHITESPACE, 14, 1); // ' '
		assertToken("constant.numeric.ruby", 15, 1); // '0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.control.ruby", 17, 3); // 'end'
	}

	public void testPercentMethodDefinition()
	{
		String code = "def %(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '%'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 3); // '0.0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 17, 2); // '||'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("support.class.ruby", 20, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 28, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 29, 3); // 'new'
		assertToken(Token.WHITESPACE, 32, 1); // ' '
		assertToken("keyword.control.ruby", 33, 3); // 'end'
	}

	public void testMultiplyMethodDefinition()
	{
		String code = "def *(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '*'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 3); // '0.0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 17, 2); // '||'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("support.class.ruby", 20, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 28, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 29, 3); // 'new'
		assertToken(Token.WHITESPACE, 32, 1); // ' '
		assertToken("keyword.control.ruby", 33, 3); // 'end'
	}

	public void testPowerMethodDefinition()
	{
		String code = "def **(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '**'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 12, 1); // ')'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("constant.numeric.ruby", 14, 3); // '0.0'
		assertToken(Token.WHITESPACE, 17, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 18, 2); // '||'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("support.class.ruby", 21, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 29, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 30, 3); // 'new'
		assertToken(Token.WHITESPACE, 33, 1); // ' '
		assertToken("keyword.control.ruby", 34, 3); // 'end'
	}

	public void testPlusMethodDefinition()
	{
		String code = "def +(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '+'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 3); // '0.0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 17, 2); // '||'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("support.class.ruby", 20, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 28, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 29, 3); // 'new'
		assertToken(Token.WHITESPACE, 32, 1); // ' '
		assertToken("keyword.control.ruby", 33, 3); // 'end'
	}

	public void testMinusMethodDefinition()
	{
		String code = "def *(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '-'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 3); // '0.0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 17, 2); // '||'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("support.class.ruby", 20, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 28, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 29, 3); // 'new'
		assertToken(Token.WHITESPACE, 32, 1); // ' '
		assertToken("keyword.control.ruby", 33, 3); // 'end'
	}

	public void testDivideMethodDefinition()
	{
		String code = "def /(other) 0.0 || Rational.new end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '/'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 3); // '0.0'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 17, 2); // '||'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("support.class.ruby", 20, 8); // 'Rational'
		assertToken("punctuation.separator.method.ruby", 28, 1); // '.'
		assertToken("keyword.other.special-method.ruby", 29, 3); // 'new'
		assertToken(Token.WHITESPACE, 32, 1); // ' '
		assertToken("keyword.control.ruby", 33, 3); // 'end'
	}

	public void testEqualMethodDefinition()
	{
		String code = "def ==(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '=='
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 12, 1); // ')'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("variable.other.constant.ruby", 14, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 21, 1); // ' '
		assertToken("keyword.control.ruby", 22, 3); // 'end'
	}

	public void testTripleEqualMethodDefinition()
	{
		String code = "def ===(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 3); // '==='
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // '('
		assertToken("variable.parameter.function.ruby", 8, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 13, 1); // ')'
		assertToken(Token.WHITESPACE, 14, 1); // ' '
		assertToken("variable.other.constant.ruby", 15, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 22, 1); // ' '
		assertToken("keyword.control.ruby", 23, 3); // 'end'
	}

	public void testGreaterThanOrEqualMethodDefinition()
	{
		String code = "def >=(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '>='
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 12, 1); // ')'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("variable.other.constant.ruby", 14, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 21, 1); // ' '
		assertToken("keyword.control.ruby", 22, 3); // 'end'
	}

	public void testLessThanOrEqualMethodDefinition()
	{
		String code = "def <=(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '<='
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 12, 1); // ')'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("variable.other.constant.ruby", 14, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 21, 1); // ' '
		assertToken("keyword.control.ruby", 22, 3); // 'end'
	}

	public void testLessThanMethodDefinition()
	{
		String code = "def <(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '<'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.other.constant.ruby", 13, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("keyword.control.ruby", 21, 3); // 'end'
	}

	public void testGreaterThanMethodDefinition()
	{
		String code = "def >(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '>'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.other.constant.ruby", 13, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("keyword.control.ruby", 21, 3); // 'end'
	}

	public void testBitwiseOrMethodDefinition()
	{
		String code = "def |(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '|'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.other.constant.ruby", 13, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("keyword.control.ruby", 21, 3); // 'end'
	}

	public void testBitwiseAndMethodDefinition()
	{
		String code = "def &(other) self || other end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '&'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.language.ruby", 13, 4); // 'self'
		assertToken(Token.WHITESPACE, 17, 1); // ' '
		assertToken("keyword.operator.logical.ruby", 18, 2); // '||'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("", 21, 5); // 'other'
		assertToken(Token.WHITESPACE, 26, 1); // ' '
		assertToken("keyword.control.ruby", 27, 3); // 'end'
	}

	public void testShiftMethodDefinition()
	{
		String code = "def <<(obj) self end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '<<'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 3); // 'obj'
		assertToken("punctuation.definition.parameters.ruby", 10, 1); // ')'
		assertToken(Token.WHITESPACE, 11, 1); // ' '
		assertToken("variable.language.ruby", 12, 4); // 'self'
		assertToken(Token.WHITESPACE, 16, 1); // ' '
		assertToken("keyword.control.ruby", 17, 3); // 'end'
	}

	public void testOverridePlusMethodDefinition()
	{
		String code = "def +@() self end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '+@'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // ')'
		assertToken(Token.WHITESPACE, 8, 1); // ' '
		assertToken("variable.language.ruby", 9, 4); // 'self'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("keyword.control.ruby", 14, 3); // 'end'
	}

	public void testOverrideMinusMethodDefinition()
	{
		String code = "def -@() 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '-@'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // ')'
		assertToken(Token.WHITESPACE, 8, 1); // ' '
		assertToken("constant.numeric.ruby", 9, 1); // '0'
		assertToken(Token.WHITESPACE, 10, 1); // ' '
		assertToken("keyword.control.ruby", 11, 3); // 'end'
	}

	public void testBitwiseComplementMethodDefinition()
	{
		String code = "def ~() 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '~'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // ')'
		assertToken(Token.WHITESPACE, 7, 1); // ' '
		assertToken("constant.numeric.ruby", 8, 1); // '0'
		assertToken(Token.WHITESPACE, 9, 1); // ' '
		assertToken("keyword.control.ruby", 10, 3); // 'end'
	}

	public void testHatMethodDefinition()
	{
		String code = "def ^(other) BOOLEAN end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 1); // '^'
		assertToken("punctuation.definition.parameters.ruby", 5, 1); // '('
		assertToken("variable.parameter.function.ruby", 6, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.other.constant.ruby", 13, 7); // 'BOOLEAN'
		assertToken(Token.WHITESPACE, 20, 1); // ' '
		assertToken("keyword.control.ruby", 21, 3); // 'end'
	}

	public void testArrayIndexMethodDefinition()
	{
		String code = "def [](*) at(0) end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '[]'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 1); // '*'
		assertToken("punctuation.definition.parameters.ruby", 8, 1); // ')'
		assertToken(Token.WHITESPACE, 9, 1); // ' '
		assertToken("", 10, 2); // 'at'
		assertToken("punctuation.section.function.ruby", 12, 1); // '('
		assertToken("constant.numeric.ruby", 13, 1); // '0'
		assertToken("punctuation.section.function.ruby", 14, 1); // ')'
		assertToken(Token.WHITESPACE, 15, 1); // ' '
		assertToken("keyword.control.ruby", 16, 3); // 'end'
	}

	public void testArraySetMethodDefinition()
	{
		String code = "def []=(key, value) value end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 3); // '[]='
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // '('
		assertToken("variable.parameter.function.ruby", 8, 3); // 'key'
		assertToken("punctuation.separator.object.ruby", 11, 1); // ','
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("variable.parameter.function.ruby", 13, 5); // 'value'
		assertToken("punctuation.definition.parameters.ruby", 18, 1); // ')'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("", 20, 5); // 'value'
		assertToken(Token.WHITESPACE, 25, 1); // ' '
		assertToken("keyword.control.ruby", 26, 3); // 'end'
	}

	public void testNextMethodDefinition()
	{
		String code = "def next() 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 4); // 'next'
		assertToken("punctuation.definition.parameters.ruby", 8, 1); // '('
		assertToken("punctuation.definition.parameters.ruby", 9, 1); // ')'
		assertToken(Token.WHITESPACE, 10, 1); // ' '
		assertToken("constant.numeric.ruby", 11, 1); // '0'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("keyword.control.ruby", 13, 3); // 'end'
	}

	public void testBeginMethodDefinition()
	{
		String code = "def begin(n) 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 5); // 'begin'
		assertToken("punctuation.definition.parameters.ruby", 9, 1); // '('
		assertToken("variable.parameter.function.ruby", 10, 1); // 'n'
		assertToken("punctuation.definition.parameters.ruby", 11, 1); // ')'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("constant.numeric.ruby", 13, 1); // '0'
		assertToken(Token.WHITESPACE, 14, 1); // ' '
		assertToken("keyword.control.ruby", 15, 3); // 'end'
	}

	public void testEndMethodDefinition()
	{
		String code = "def end(n) 0 end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 3); // 'end'
		assertToken("punctuation.definition.parameters.ruby", 7, 1); // '('
		assertToken("variable.parameter.function.ruby", 8, 1); // 'n'
		assertToken("punctuation.definition.parameters.ruby", 9, 1); // ')'
		assertToken(Token.WHITESPACE, 10, 1); // ' '
		assertToken("constant.numeric.ruby", 11, 1); // '0'
		assertToken(Token.WHITESPACE, 12, 1); // ' '
		assertToken("keyword.control.ruby", 13, 3); // 'end'
	}

	public void testMatchMethodDefinition()
	{
		String code = "def =~(other) FALSE end";
		setUpScanner(code);
		assertToken("keyword.control.def.ruby", 0, 3); // 'def'
		assertToken(Token.WHITESPACE, 3, 1); // ' '
		assertToken("entity.name.function.ruby", 4, 2); // '=~'
		assertToken("punctuation.definition.parameters.ruby", 6, 1); // '('
		assertToken("variable.parameter.function.ruby", 7, 5); // 'other'
		assertToken("punctuation.definition.parameters.ruby", 12, 1); // ')'
		assertToken(Token.WHITESPACE, 13, 1); // ' '
		assertToken("variable.other.constant.ruby", 14, 5); // 'FALSE'
		assertToken(Token.WHITESPACE, 19, 1); // ' '
		assertToken("keyword.control.ruby", 20, 3); // 'end'
	}

	public void testTwoAliasLines()
	{
		String code = "alias :include? :===\nalias :member? :===";
		setUpScanner(code);
		assertToken("keyword.control.ruby", 0, 5); // 'alias'
		assertToken(Token.WHITESPACE, 5, 1); // ' '
		assertToken("constant.other.symbol.ruby", 6, 1); // ':'
		assertToken("constant.other.symbol.ruby", 7, 8); // 'include?'
		assertToken(Token.WHITESPACE, 15, 1); // ' '
		assertToken("constant.other.symbol.ruby", 16, 1); // ':'
		assertToken("constant.other.symbol.ruby", 17, 3); // '==='
		assertToken(Token.WHITESPACE, 20, 1); // '\n'
		assertToken("keyword.control.ruby", 21, 5); // 'alias'
		assertToken(Token.WHITESPACE, 26, 1); // ' '
		assertToken("constant.other.symbol.ruby", 27, 1); // ':'
		assertToken("constant.other.symbol.ruby", 28, 7); // 'member?'
		assertToken(Token.WHITESPACE, 35, 1); // ' '
		assertToken("constant.other.symbol.ruby", 36, 1); // ':'
		assertToken("constant.other.symbol.ruby", 37, 3); // '==='
	}

	public void testResumeAfterString() throws Exception
	{
		String code = "\"Just an example: %s %d\" \\\n% [1, 9000]";
		Document document = new Document(code);

		partition(document);
		scanner.setRange(document, 24, 14);

		assertToken(Token.WHITESPACE, 24, 1); //
		// assertToken(Token.WHITESPACE, 25, 2); // \\n
		// FIXME For whatever reason, the leading whitespace is getting folded in here...
		assertToken("keyword.operator.arithmetic.ruby", 25, 3); // '%'
		assertToken(Token.WHITESPACE, 28, 1); //
		assertToken("punctuation.section.array.ruby", 29, 1); // '['
		assertToken("constant.numeric.ruby", 30, 1); // '1'
		assertToken("punctuation.separator.object.ruby", 31, 1); // ','
		assertToken(Token.WHITESPACE, 32, 1); //
		assertToken("constant.numeric.ruby", 33, 4); // '9000'
		assertToken("punctuation.section.array.ruby", 37, 1); // ']'
	}

	public void testAPSTUD817()
	{
		String code = "def sample(array)\n" + //
				"  array.each do |element|\n" + //
				"  end\n" + //
				"end";
		setUpScanner(code);
		// def
		assertToken("keyword.control.def.ruby", 0, 3);
		assertToken(Token.WHITESPACE, 3, 1);
		// sample
		assertToken("entity.name.function.ruby", 4, 6);
		// (
		assertToken("punctuation.definition.parameters.ruby", 10, 1);
		// array
		assertToken("variable.parameter.function.ruby", 11, 5);
		// )
		assertToken("punctuation.definition.parameters.ruby", 16, 1);
		assertToken(Token.WHITESPACE, 17, 1);
		assertToken(Token.WHITESPACE, 18, 2);
		// array
		assertToken("", 20, 5);
		// .
		assertToken("punctuation.separator.method.ruby", 25, 1);
		// each
		assertToken("", 26, 4);
		assertToken(Token.WHITESPACE, 30, 1);
		// do
		assertToken("keyword.control.start-block.ruby", 31, 2);
		assertToken(Token.WHITESPACE, 33, 1);
		// |
		assertToken("punctuation.separator.variable.ruby", 34, 1);
		// element
		assertToken("variable.other.block.ruby", 35, 7);
		// |
		assertToken("punctuation.separator.variable.ruby", 42, 1);
		assertToken(Token.WHITESPACE, 43, 1);
		assertToken(Token.WHITESPACE, 44, 2);
		// end
		assertToken("keyword.control.ruby", 46, 3);
		assertToken(Token.WHITESPACE, 49, 1);
		// end
		assertToken("keyword.control.ruby", 50, 3);
	}

	protected void partition(Document document)
	{
		RubyDocumentProvider docProvider = new RubyDocumentProvider();
		IPartitioningConfiguration configuration = docProvider.getPartitioningConfiguration();
		IDocumentPartitioner partitioner = new FastPartitioner(docProvider.createPartitionScanner(),
				configuration.getContentTypes());
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		CommonEditorPlugin.getDefault().getDocumentScopeManager().registerConfiguration(document, configuration);
	}
}
