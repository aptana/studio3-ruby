/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml.internal.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @author Max Stepanov
 */
public class HAMLElementRule implements IPredicateRule
{

	private final WordRule wordRule;
	private final IToken successToken;

	public HAMLElementRule(IToken token)
	{
		this.successToken = token;
		this.wordRule = new WordRule(new HAMLElementWordDetector(), token);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
	 */
	public IToken getSuccessToken()
	{
		return successToken;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner)
	{
		return evaluate(scanner, false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner,
	 * boolean)
	 */
	public IToken evaluate(ICharacterScanner scanner, boolean resume)
	{
		if (!resume)
		{
			int index = 0;
			int c;
			if (scanner.getColumn() != 0)
			{
				return Token.UNDEFINED;
			}
			while ((c = scanner.read()) != ICharacterScanner.EOF && isWhitespace(c))
			{
				++index;
			}
			if (c != ICharacterScanner.EOF)
			{
				scanner.unread();
			}
			IToken token = wordRule.evaluate(scanner);
			if (token.isUndefined())
			{
				for (int j = index; j > 0; --j)
				{
					scanner.unread();
				}
			}
			return token;
		}
		return Token.UNDEFINED;
	}

	private static boolean isWhitespace(int c)
	{
		return (c == ' ') || (c == '\t');
	}

}

/* package */class HAMLElementWordDetector implements IWordDetector
{

	private static final char PERCENT = '%';
	private static final char DOT = '.';
	private static final char HASH = '#';

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char c)
	{
		return PERCENT == c || DOT == c || HASH == c;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char c)
	{
		return Character.isLetterOrDigit(c) || DOT == c || HASH == c;
	}

}