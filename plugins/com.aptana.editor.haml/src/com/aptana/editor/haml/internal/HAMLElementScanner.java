/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.text.rules.SingleCharacterRule;
import com.aptana.editor.common.text.rules.WhitespaceDetector;
import com.aptana.editor.haml.IHAMLConstants;

public class HAMLElementScanner extends BufferedRuleBasedScanner
{
	private IToken fLastToken;

	public HAMLElementScanner()
	{
		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));
		rules.add(new SingleCharacterRule('%', createToken(IHAMLConstants.PUNCTUATION_DEFINITION_TAG_HAML)));

		// tags
		rules.add(new WordRule(new TagDetector(), createToken(IHAMLConstants.ENTITY_NAME_TAG_HAML)));

		// ids
		rules.add(new WordRule(new IDDetector(), createToken(IHAMLConstants.ENTITY_NAME_TAG_ID_HAML)));

		// classes
		rules.add(new WordRule(new ClassDetector(), createToken(IHAMLConstants.ENTITY_NAME_TAG_CLASS_HAML)));

		setRules(rules.toArray(new IRule[rules.size()]));
	}

	private IToken createToken(String string)
	{
		return new Token(string);
	}

	@Override
	public IToken nextToken()
	{
		IToken token = super.nextToken();
		// If preceding is %, then entity name tag stays, otherwise it's just default token
		if (token != null && IHAMLConstants.ENTITY_NAME_TAG_HAML.equals(token.getData()))
		{
			if (fLastToken == null || !IHAMLConstants.PUNCTUATION_DEFINITION_TAG_HAML.equals(fLastToken.getData()))
			{
				token = createToken(StringUtil.EMPTY);
			}
		}
		fLastToken = token;
		return token;
	}

	@Override
	public void setRange(IDocument document, int offset, int length)
	{
		super.setRange(document, offset, length);
		fLastToken = null;
	}

	private static final class TagDetector implements IWordDetector
	{
		public boolean isWordStart(char c)
		{
			return Character.isLetterOrDigit(c);
		}

		public boolean isWordPart(char c)
		{
			return Character.isLetterOrDigit(c) || c == '_' || c == '-';
		}
	}

	private static final class IDDetector implements IWordDetector
	{
		public boolean isWordStart(char c)
		{
			return c == '#';
		}

		public boolean isWordPart(char c)
		{
			return Character.isLetterOrDigit(c) || c == '_' || c == '-';
		}
	}

	private static final class ClassDetector implements IWordDetector
	{
		public boolean isWordStart(char c)
		{
			return c == '.';
		}

		public boolean isWordPart(char c)
		{
			return Character.isLetterOrDigit(c) || c == '_' || c == '-';
		}
	}
}
