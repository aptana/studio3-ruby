/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser.NullWarnings;
import org.jrubyparser.lexer.Lexer;
import org.jrubyparser.lexer.Lexer.LexState;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserResult;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.Tokens;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;

/**
 * A token scanner which returns integers for ruby tokens. These can later be mapped to colors. Does some smoothing on
 * the tokens to add additional token types that the JRuby parser ignores.
 * 
 * @author Chris Williams
 */
public class RubyTokenScanner implements ITokenScanner
{

	public static final int COMMA = 44;
	public static final int COLON = 58;
	public static final int ASSIGNMENT = 61;
	public static final int QUESTION = 63;
	public static final int NEWLINE = 10;
	public static final int CHARACTER = 128;
	static final int MIN_KEYWORD = 257;
	static final int MAX_KEYWORD = 305;
	public static final int SPACE = 32;
	private static final int LBRACK = 91;
	public static final int SEMICOLON = 59;

	private Lexer lexer;
	private LexerSource lexerSource;
	private ParserSupport parserSupport;

	private int fTokenLength;
	private int fOffset;

	private boolean isInSymbol;
	private boolean inAlias;
	private ParserResult result;
	private int origOffset;
	private int origLength;
	private String fContents;
	private BufferedReader reader;

	public RubyTokenScanner()
	{
		lexer = new Lexer();
		parserSupport = new ParserSupport();
		ParserConfiguration config = new ParserConfiguration(0, CompatVersion.RUBY1_8);
		parserSupport.setConfiguration(config);
		result = new ParserResult();
		parserSupport.setResult(result);
		lexer.setParserSupport(parserSupport);
		lexer.setWarnings(new NullWarnings());
	}

	public int getTokenLength()
	{
		return fTokenLength;
	}

	public int getTokenOffset()
	{
		return fOffset;
	}

	public IToken nextToken()
	{
		fOffset = getOffset();
		fTokenLength = 0;
		IToken returnValue = new Token(Tokens.tIDENTIFIER);
		boolean isEOF = false;
		try
		{
			isEOF = !lexer.advance(); // FIXME if we're assigning a string to a
			// variable we may get a
			// NumberFormatException here!
			if (isEOF)
			{
				returnValue = Token.EOF;
				// TODO Close the lexer's reader
			}
			else
			{
				fTokenLength = getOffset() - fOffset;
				returnValue = token(lexer.token());
			}
		}
		catch (SyntaxException se)
		{
			if (lexerSource.getOffset() - origLength == 0)
			{
				return Token.EOF; // return eof if we hit a problem found at end of parsing
			}
			fTokenLength = getOffset() - fOffset;
			return token(Tokens.yyErrorCode); // FIXME This should return a special error token!
		}
		catch (NumberFormatException nfe)
		{
			fTokenLength = getOffset() - fOffset;
			return returnValue;
		}
		catch (Exception e)
		{
			IdeLog.logError(RubyEditorPlugin.getDefault(), e);
		}

		return returnValue;
	}

	private int getOffset()
	{
		return lexerSource.getOffset() + origOffset;
	}

	private IToken token(int i)
	{

		if (isInSymbol)
		{
			if (isSymbolTerminator(i))
			{
				isInSymbol = false; // we're at the end of the symbol
				if (shouldReturnDefault(i))
				{
					return new Token(i);
				}
			}
			return new Token(Tokens.tSYMBEG);
		}
		// The next two conditionals work around a JRuby parsing bug
		// JRuby returns the number for ':' on second symbol's beginning in
		// alias calls
		if (i == Tokens.kALIAS)
		{
			inAlias = true;
		}
		if (i == COLON && inAlias)
		{
			isInSymbol = true;
			inAlias = false;
			return new Token(Tokens.tSYMBEG);
		} // end JRuby parsing hack for alias

		switch (i)
		{
			case LBRACK:
				return new Token(Tokens.tLBRACK);
			case Tokens.tSYMBEG:
				if (looksLikeTertiaryConditionalWithNoSpaces())
				{
					return new Token(Tokens.tCOLON2);
				}
				isInSymbol = true;
				// FIXME Set up a token for symbols
				return new Token(Tokens.tSYMBEG);
			case Tokens.tGVAR:
			case Tokens.tBACK_REF:
				return new Token(Tokens.tGVAR);
			case Tokens.tFLOAT:
			case Tokens.tINTEGER:
				// A character is marked as an integer, lets check for that special
				// case...
				if ((((fOffset - origOffset) + 1) < fContents.length())
						&& (fContents.charAt((fOffset - origOffset) + 1) == '?'))
				{
					return new Token(CHARACTER);
				}
				return new Token(i);
			default:
				return new Token(i);
		}
	}

	private boolean looksLikeTertiaryConditionalWithNoSpaces()
	{
		if (fTokenLength > 1)
		{
			return false;
		}
		int index = (fOffset - origOffset) - 1;
		if (index < 0)
		{
			return false;
		}
		try
		{
			char c = fContents.charAt(index);
			return !Character.isWhitespace(c) && Character.isUnicodeIdentifierPart(c);
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	private boolean shouldReturnDefault(int i)
	{
		switch (i)
		{
			case NEWLINE:
			case COMMA:
			case Tokens.tASSOC:
			case Tokens.tRPAREN:
			case Tokens.tWHITESPACE:
				return true;
			default:
				return false;
		}
	}

	private boolean isSymbolTerminator(int i)
	{
		if (isRealKeyword(i))
		{
			return true;
		}
		switch (i)
		{
			case Tokens.tAREF:
			case Tokens.tCVAR:
			case Tokens.tMINUS:
			case Tokens.tPLUS:
			case Tokens.tPIPE:
			case Tokens.tCARET:
			case Tokens.tLT:
			case Tokens.tGT:
			case Tokens.tAMPER:
			case Tokens.tSTAR2:
			case Tokens.tDIVIDE:
			case Tokens.tPERCENT:
			case Tokens.tBACK_REF2:
			case Tokens.tTILDE:
			case Tokens.tCONSTANT:
			case Tokens.tFID:
			case Tokens.tASET:
			case Tokens.tIDENTIFIER:
			case Tokens.tIVAR:
			case Tokens.tGVAR:
			case Tokens.tASSOC:
			case Tokens.tLSHFT:
			case Tokens.tRPAREN:
			case Tokens.tWHITESPACE:
			case COMMA:
			case NEWLINE:
				return true;
			default:
				return false;
		}
	}

	private boolean isRealKeyword(int i)
	{
		if (i >= MIN_KEYWORD && i <= MAX_KEYWORD)
		{
			return true;
		}
		return false;
	}

	public void setRange(IDocument document, int offset, int length)
	{
		reset();
		ParserConfiguration config = new ParserConfiguration(0, CompatVersion.BOTH);
		try
		{
			fContents = document.get(offset, length);
		}
		catch (BadLocationException e)
		{
			fContents = StringUtil.EMPTY;
		}
		reader = new BufferedReader(new StringReader(fContents)); // $codepro.audit.disable closeWhereCreated
		lexerSource = LexerSource.getSource("filename", reader, config); //$NON-NLS-1$
		lexer.setSource(lexerSource);

		// FIXME If we're resuming after a string/regexp/command, set up lex state to be expression end.
		if (offset > 0)
		{
			try
			{
				ITypedRegion region = document.getPartition(offset - 1);
				if (RubySourceConfiguration.STRING_DOUBLE.equals(region.getType())
						|| RubySourceConfiguration.STRING_SINGLE.equals(region.getType())
						|| RubySourceConfiguration.REGULAR_EXPRESSION.equals(region.getType())
						|| RubySourceConfiguration.COMMAND.equals(region.getType()))
				{
					lexer.setLexState(LexState.EXPR_END);
				}
			}
			catch (BadLocationException e)
			{
				IdeLog.logError(RubyEditorPlugin.getDefault(), "Unable to get previous partition at offset: " + offset, //$NON-NLS-1$
						e);
			}
		}

		origOffset = offset;
		origLength = length;
	}

	protected void reset()
	{
		if (reader != null)
		{
			try
			{
				reader.close(); // $codepro.audit.disable closeInFinally
			}
			catch (IOException e) // $codepro.audit.disable emptyCatchClause
			{
				// ignore
			}
		}
		lexer.reset();
		lexer.setState(LexState.EXPR_BEG);
		lexer.setPreserveSpaces(true);
		parserSupport.initTopLocalVariables();
		isInSymbol = false;
		inAlias = false;
	}

	String getSource(int offset, int length)
	{
		if (fContents == null || offset < 0 || (offset + length) > fContents.length())
		{
			return null;
		}
		return new String(fContents.substring(offset, offset + length));
	}
}
