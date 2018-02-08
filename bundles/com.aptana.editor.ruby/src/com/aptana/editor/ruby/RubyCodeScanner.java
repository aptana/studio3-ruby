/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.jrubyparser.parser.Tokens;

import com.aptana.core.util.StringUtil;

public class RubyCodeScanner implements ITokenScanner
{

	private RubyTokenScanner fScanner;
	private boolean nextIsMethodName;
	private boolean nextIsModuleName;
	private boolean nextIsClassName;
	private boolean inPipe;
	private boolean lookForBlock;
	private boolean nextAreArgs;
	private List<QueuedToken> queue;
	private int fLength;
	private int fOffset;
	private int fOrigOffset;

	public RubyCodeScanner()
	{
		fScanner = new RubyTokenScanner();
	}

	public int getTokenLength()
	{
		return fLength;
	}

	public int getTokenOffset()
	{
		return fOffset;
	}

	public IToken nextToken()
	{
		IToken intToken = pop();
		if (intToken.isEOF())
		{
			return Token.EOF;
		}
		Integer data = (Integer) intToken.getData();

		if (lookForBlock)
		{
			if (!inPipe && data.intValue() != Tokens.tPIPE && data.intValue() != Tokens.tWHITESPACE)
			{
				lookForBlock = false;
			}
		}

		if (nextAreArgs && (isNewline(data) || data.intValue() == RubyTokenScanner.SEMICOLON))
		{
			nextAreArgs = false;
		}

		// Convert the integer tokens into tokens containing color information!
		if (isKeyword(data.intValue()))
		{
			switch (data.intValue())
			{
				case Tokens.k__FILE__:
				case Tokens.k__LINE__:
				case Tokens.kSELF:
					if (nextIsClassName)
					{
						nextIsClassName = false;
						return getToken(IRubyScopeConstants.CLASS_NAME);
					}
					return getToken(IRubyScopeConstants.LANGUAGE_VARIABLE);
				case Tokens.kNIL:
				case Tokens.kTRUE:
				case Tokens.kFALSE:
					return getToken(IRubyScopeConstants.LANGUAGE_CONSTANT);
				case Tokens.kAND:
				case Tokens.kNOT:
				case Tokens.kOR:
					return getToken(IRubyScopeConstants.OPERATOR_KEYWORD);
				case Tokens.kDO_BLOCK:
				case Tokens.kDO:
					lookForBlock = true;
					return getToken(IRubyScopeConstants.DO_KEYWORD);
				case Tokens.kCLASS:
					nextAreArgs = false;
					nextIsClassName = true;
					return getToken(IRubyScopeConstants.CLASS_KEYWORD);
				case Tokens.kMODULE:
					nextAreArgs = false;
					nextIsModuleName = true;
					return getToken(IRubyScopeConstants.MODULE_KEYWORD);
				case Tokens.kDEF:
					nextAreArgs = false;
					nextIsMethodName = true;
					return getToken(IRubyScopeConstants.DEF_KEYWORD);
				default:
					if (nextIsMethodName)
					{
						nextIsMethodName = false;
						nextAreArgs = true;
						return getToken(IRubyScopeConstants.FUNCTION_NAME);
					}
					return getToken(IRubyScopeConstants.CONTROL_KEYWORD);
			}
		}
		switch (data.intValue())
		{
			case RubyTokenScanner.ASSIGNMENT:
				return getToken(IRubyScopeConstants.OPERATOR_ASSIGNMENT);
			case Tokens.tCMP: /* <=> */
			case Tokens.tMATCH: /* =~ */
			case Tokens.tNMATCH: /* !~ */
			case Tokens.tEQ: /* == */
			case Tokens.tEQQ: /* === */
			case Tokens.tNEQ: /* != */
			case Tokens.tGEQ: /* >= */
			case Tokens.tLEQ:
			case Tokens.tLT:
			case Tokens.tGT:
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				return getToken(IRubyScopeConstants.OPERATOR_COMPARISON);
			case Tokens.tSTAR:
				if (nextAreArgs) // could be un-named rest arg
				{
					return getToken(IRubyScopeConstants.FUNCTION_PARAMETER);
				}
				// intentionally fall-through
			case Tokens.tAMPER: // $codepro.audit.disable nonTerminatedCaseClause
			case Tokens.tPERCENT:
			case Tokens.tPOW:
			case Tokens.tSTAR2:
			case Tokens.tPLUS:
			case Tokens.tMINUS:
			case Tokens.tDIVIDE:
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				return getToken(IRubyScopeConstants.OPERATOR_ARITHMETIC);
			case Tokens.tANDOP:
			case Tokens.tAMPER2: // &
			case Tokens.tTILDE:
			case Tokens.tBANG:
			case Tokens.tOROP:
			case Tokens.tCARET:
			case RubyTokenScanner.QUESTION:
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				return getToken(IRubyScopeConstants.OPERATOR_LOGICAL);
			case Tokens.tAREF:
			case Tokens.tASET:
			case Tokens.tUPLUS:
			case Tokens.tUMINUS:
			case Tokens.tUMINUS_NUM:
				nextIsMethodName = false;
				nextAreArgs = true;
				return getToken(IRubyScopeConstants.FUNCTION_NAME);
			case Tokens.tPIPE:
				if (lookForBlock)
				{
					inPipe = !inPipe;
					if (!inPipe)
					{
						lookForBlock = false;
					}
					return getToken(IRubyScopeConstants.VARIABLE_SEPARATOR);
				}
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				return getToken(IRubyScopeConstants.OPERATOR_LOGICAL);
			case Tokens.tLPAREN:
			case Tokens.tLPAREN2:
				if (nextAreArgs)
				{
					return getToken(IRubyScopeConstants.FUNCTION_DEF_PAREN);
				}
				return getToken(IRubyScopeConstants.PAREN);
			case Tokens.tLBRACE:
				lookForBlock = true;
				return getToken(IRubyScopeConstants.SCOPE_PUNCTUATION);
			case Tokens.tLBRACK:
			case Tokens.tRBRACK:
				return getToken(IRubyScopeConstants.ARRAY_PUNCTUATION);
			case Tokens.tLCURLY:
			case Tokens.tRCURLY:
				return getToken(IRubyScopeConstants.SCOPE_PUNCTUATION);
			case RubyTokenScanner.COMMA:
				return getToken(IRubyScopeConstants.COMMA);
			case Tokens.tRPAREN:
				if (nextAreArgs)
				{
					nextAreArgs = false;
					return getToken(IRubyScopeConstants.FUNCTION_DEF_PAREN);
				}
				return getToken(IRubyScopeConstants.PAREN);
			case Tokens.tLSHFT:
				if (nextIsClassName)
				{
					return getToken(IRubyScopeConstants.CLASS_NAME);
				}
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				return getToken(IRubyScopeConstants.AUGMENTED_ASSIGNMENT);
			case Tokens.tOP_ASGN:
				return getToken(IRubyScopeConstants.AUGMENTED_ASSIGNMENT);
			case Tokens.tASSOC:
				return getToken(IRubyScopeConstants.HASH_SEPARATOR);
			case RubyTokenScanner.CHARACTER:
				return getToken(IRubyScopeConstants.CHARACTER);
			case Tokens.tCOLON2:
			case Tokens.tCOLON3:
				return getToken(IRubyScopeConstants.INHERITANCE_PUNCTUATION);
			case Tokens.tFLOAT:
			case Tokens.tINTEGER:
				return getToken(IRubyScopeConstants.NUMERIC);
			case Tokens.tSYMBEG:
				return getToken(IRubyScopeConstants.SYMBOL);
			case Tokens.tGVAR:
				return getToken(IRubyScopeConstants.GLOBAL_VARIABLE);
			case Tokens.tIVAR:
				return getToken(IRubyScopeConstants.INSTANCE_VARIABLE);
			case Tokens.tCVAR:
				return getToken(IRubyScopeConstants.CLASS_VARIABLE);
			case Tokens.tCONSTANT:
				if (nextIsModuleName)
				{
					nextIsModuleName = false;
					return getToken(IRubyScopeConstants.MODULE_NAME);
				}
				if (nextIsClassName)
				{
					nextIsClassName = false;
					return getToken(IRubyScopeConstants.CLASS_NAME);
				}
				int nextToken = peek();
				if (nextToken == Tokens.tCOLON2 || nextToken == Tokens.tDOT)
				{
					return getToken(IRubyScopeConstants.SUPPORT_CLASS);
				}
				return getToken(IRubyScopeConstants.CONSTANT_OTHER);
			case Tokens.yyErrorCode:
				return getToken(IRubyScopeConstants.ERROR);
			case Tokens.tWHITESPACE:
				return Token.WHITESPACE;
			case Tokens.tDOT:
				return getToken(IRubyScopeConstants.SEPARATOR_METHOD);
			case Tokens.tIDENTIFIER:
			case Tokens.tFID:
				if (nextIsMethodName)
				{
					nextIsMethodName = false;
					nextAreArgs = true;
					return getToken(IRubyScopeConstants.FUNCTION_NAME);
				}
				if (nextAreArgs)
				{
					return getToken(IRubyScopeConstants.FUNCTION_PARAMETER);
				}
				if (lookForBlock && inPipe)
				{
					return getToken(IRubyScopeConstants.BLOCK_VARIABLE);
				}
				if ("new".equals(getSourceForCurrentToken())) //$NON-NLS-1$
				{
					return getToken(IRubyScopeConstants.SPECIAL_METHOD);
				}
				// intentionally fall through
			default: // $codepro.audit.disable nonTerminatedCaseClause
				return getToken(StringUtil.EMPTY);
		}
	}

	@SuppressWarnings("nls")
	protected boolean isNewline(Integer data)
	{
		if (data.intValue() == RubyTokenScanner.NEWLINE)
		{
			return true;
		}
		if (data.intValue() != Tokens.tWHITESPACE)
		{
			return false;
		}
		// make sure it's actually a newline
		String tokenSrc = getSourceForCurrentToken();
		if (tokenSrc == null)
		{
			return false;
		}
		return tokenSrc.equals("\r\n") || tokenSrc.equals("\n") || tokenSrc.equals("\r"); // $codepro.audit.disable
																							// platformSpecificLineSeparator
	}

	private String getSourceForCurrentToken()
	{
		return fScanner.getSource(fOffset - fOrigOffset, fLength);
	}

	protected IToken getToken(String tokenName)
	{
		return new Token(tokenName);
	}

	private IToken pop()
	{
		IToken intToken = null;
		if (queue == null || queue.isEmpty())
		{
			intToken = fScanner.nextToken();
			fOffset = fScanner.getTokenOffset();
			fLength = fScanner.getTokenLength();
		}
		else
		{
			QueuedToken queued = queue.remove(0);
			fOffset = queued.getOffset();
			fLength = queued.getLength();
			intToken = queued.getToken();
		}
		if (intToken == null)
			return Token.EOF;
		Integer data = (Integer) intToken.getData();
		if (data == null)
			return Token.EOF;

		return intToken;
	}

	private int peek()
	{
		int oldOffset = getTokenOffset();
		int oldLength = getTokenLength();
		IToken next = pop();
		push(next);
		fOffset = oldOffset;
		fLength = oldLength;
		if (next.isEOF())
		{
			return -1;
		}
		Integer data = (Integer) next.getData();
		return data.intValue();
	}

	private void push(IToken next)
	{
		if (queue == null)
		{
			queue = new Vector<QueuedToken>();
		}
		queue.add(new QueuedToken(next, getTokenOffset(), getTokenLength()));
	}

	public void setRange(IDocument document, int offset, int length)
	{
		fScanner.setRange(document, offset, length);
		reset();
		fOrigOffset = offset;
	}

	private void reset()
	{
		nextIsMethodName = false;
		nextIsModuleName = false;
		nextIsClassName = false;
		inPipe = false;
		lookForBlock = false;
		nextAreArgs = false;
		queue = null;
	}

	private boolean isKeyword(int i)
	{
		return i >= RubyTokenScanner.MIN_KEYWORD && i <= RubyTokenScanner.MAX_KEYWORD;
	}
}
