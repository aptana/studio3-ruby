/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml.internal.text.rules;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Max Stepanov
 */
public class HAMLSingleLineRule extends EndOfLineRule
{

	private boolean fNoStartSequence;

	public HAMLSingleLineRule(String startSequence, IToken token)
	{
		super(startSequence, token);
	}

	public HAMLSingleLineRule(IToken token)
	{
		super(" ", token); //$NON-NLS-1$
		fNoStartSequence = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.rules.PatternRule#doEvaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
	 */
	@Override
	protected IToken doEvaluate(ICharacterScanner scanner, boolean resume)
	{
		if (!resume)
		{
			if (fNoStartSequence)
			{
				if (scanner.read() != ICharacterScanner.EOF)
				{
					if (endSequenceDetected(scanner))
					{
						return fToken;
					}
					scanner.unread();
				}
				return Token.UNDEFINED;
			}
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
			IToken token = super.doEvaluate(scanner, resume);
			if (token.isUndefined())
			{
				for (int j = index; j > 0; --j)
				{
					scanner.unread();
				}
			}
			return token;
		}
		return super.doEvaluate(scanner, resume);
	}

	private static boolean isWhitespace(int c)
	{
		return (c == ' ') || (c == '\t');
	}

}
