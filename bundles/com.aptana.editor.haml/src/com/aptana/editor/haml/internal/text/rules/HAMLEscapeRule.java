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
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.TextUtils;

/**
 * @author Max Stepanov
 */
public class HAMLEscapeRule implements IPredicateRule
{

	private static final char ESCAPE = '\\';

	private final IToken successToken;

	public HAMLEscapeRule(IToken token)
	{
		this.successToken = token;
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
			int c = scanner.read();
			if (c != ICharacterScanner.EOF)
			{
				if (ESCAPE == c)
				{
					c = scanner.read();
					if (c != ICharacterScanner.EOF && !isNewLine(scanner, c))
					{
						return successToken;
					}
					scanner.unread();
				}
				scanner.unread();
			}
		}
		return Token.UNDEFINED;
	}

	private static boolean isNewLine(ICharacterScanner characterScanner, int c)
	{
		char[][] newLineSequences = TextUtils.rsort(characterScanner.getLegalLineDelimiters());
		for (char[] sequence : newLineSequences)
		{
			if (c == sequence[0])
			{
				return true;
			}
		}
		return false;
	}

}
