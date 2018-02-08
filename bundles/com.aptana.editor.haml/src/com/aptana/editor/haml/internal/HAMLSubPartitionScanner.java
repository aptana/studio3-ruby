/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.aptana.editor.common.IPartitionScannerSwitchStrategy;
import com.aptana.editor.common.IPartitionScannerSwitchStrategy.ISequenceBypassHandler;
import com.aptana.editor.common.PartitionScannerSwitchStrategy;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.text.rules.CompositeSubPartitionScanner;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.haml.HAMLSourceConfiguration;
import com.aptana.editor.ruby.RubySourceConfiguration;

/**
 * @author Max Stepanov
 */
public class HAMLSubPartitionScanner extends CompositeSubPartitionScanner
{

	private static final int TYPE_RUBY_EVALUATION = 1;
	private static final int TYPE_RUBY_ATTRIBUTES = 2;

	private static final String[] RUBY_EVALUATION_SWITCH_SEQUENCES = new String[] { "\r\n", "\n", "\r" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ // $codepro.audit.disable platformSpecificLineSeparator
	private static final String[] RUBY_ATTRIBUTES_SWITCH_SEQUENCES = new String[] { "}" }; //$NON-NLS-1$

	private static final char COMMA = ',';
	private static final char VERTICAL = '|';

	private static final ISequenceBypassHandler RUBY_BYPASS_HANDLER = new ISequenceBypassHandler()
	{
		public boolean bypassSequence(ICharacterScanner characterScanner, char[] sequenceFound)
		{
			if (characterScanner.getColumn() > 0)
			{
				characterScanner.unread();
				int c = characterScanner.read();
				if (COMMA == c)
				{
					return true;
				}
				else if (VERTICAL == c)
				{
					char[][] newLineSequences = TextUtils.rsort(characterScanner.getLegalLineDelimiters());
					int index = 0;
					try
					{
						// skip found sequence
						for (; index < sequenceFound.length; ++index)
						{
							characterScanner.read();
						}
						// search for newline, remember previous character to compare with vertical
						int previous = 0;
						while ((c = characterScanner.read()) != ICharacterScanner.EOF)
						{
							++index;
							for (char[] sequence : newLineSequences)
							{
								if (c == sequence[0] && TextUtils.sequenceDetected(characterScanner, sequence, false))
								{
									return VERTICAL == previous;
								}
							}
							previous = c;
						}
					}
					finally
					{
						for (int j = index; j > 0; --j)
						{
							characterScanner.unread();
						}
					}

				}
			}
			return false;
		}
	};

	/**
	 *
	 */
	public HAMLSubPartitionScanner()
	{
		super(new ISubPartitionScanner[] {
				new SubPartitionScanner(HAMLSourceConfiguration.getDefault().getPartitioningRules(),
						HAMLSourceConfiguration.CONTENT_TYPES, new Token(HAMLSourceConfiguration.DEFAULT)),
				RubySourceConfiguration.getDefault().createSubPartitionScanner(),
				RubyAttributesSourceConfiguration.getDefault().createSubPartitionScanner() },
				new IPartitionScannerSwitchStrategy[] {
						new PartitionScannerSwitchStrategy(RUBY_EVALUATION_SWITCH_SEQUENCES, RUBY_BYPASS_HANDLER),
						new PartitionScannerSwitchStrategy(RUBY_ATTRIBUTES_SWITCH_SEQUENCES) });
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CompositeSubPartitionScanner#setLastToken(org.eclipse.jface.text.rules.IToken)
	 */
	@Override
	public void setLastToken(IToken token)
	{
		super.setLastToken(token);
		if (token == null)
		{
			return;
		}
		if (!(token.getData() instanceof String))
		{
			current = TYPE_DEFAULT;
			return;
		}
		String contentType = (String) token.getData();
		if (HAMLSourceConfiguration.RUBY_EVALUATION.equals(contentType))
		{
			current = TYPE_RUBY_EVALUATION;
			super.setLastToken(null);
		}
		else if (HAMLSourceConfiguration.RUBY_ATTRIBUTES.equals(contentType))
		{
			current = TYPE_RUBY_ATTRIBUTES;
			super.setLastToken(null);
		}
		else if (HAMLSourceConfiguration.DEFAULT.equals(contentType)
				|| IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
		{
			current = TYPE_DEFAULT;
		}
		else
		{
			for (int i = 0; i < subPartitionScanners.length; ++i)
			{
				if (subPartitionScanners[i].hasContentType(contentType))
				{
					current = i;
					break;
				}
			}
		}
	}

}
