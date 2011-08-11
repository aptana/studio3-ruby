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

public class HAMLElementScanner extends BufferedRuleBasedScanner
{

	private static final String ENTITY_NAME_TAG_CLASS_HAML = "entity.name.tag.class.haml"; //$NON-NLS-1$
	private static final String ENTITY_NAME_TAG_ID_HAML = "entity.name.tag.id.haml"; //$NON-NLS-1$
	private static final String ENTITY_NAME_TAG_HAML = "entity.name.tag.haml"; //$NON-NLS-1$
	private static final String PUNCTUATION_DEFINITION_TAG_HAML = "punctuation.definition.tag.haml"; //$NON-NLS-1$

	private IToken fLastToken;

	public HAMLElementScanner()
	{
		List<IRule> rules = new ArrayList<IRule>();

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));
		rules.add(new SingleCharacterRule('%', createToken(PUNCTUATION_DEFINITION_TAG_HAML)));

		// tags
		WordRule rule = new WordRule(new IWordDetector()
		{
			public boolean isWordStart(char c)
			{
				return Character.isLetterOrDigit(c);
			}

			public boolean isWordPart(char c)
			{
				return Character.isLetterOrDigit(c) || c == '_' || c == '-';
			}
		}, createToken(ENTITY_NAME_TAG_HAML));
		rules.add(rule);

		// ids
		rule = new WordRule(new IWordDetector()
		{
			public boolean isWordStart(char c)
			{
				return c == '#';
			}

			public boolean isWordPart(char c)
			{
				return Character.isLetterOrDigit(c) || c == '_' || c == '-';
			}
		}, createToken(ENTITY_NAME_TAG_ID_HAML));
		rules.add(rule);

		// classes
		rule = new WordRule(new IWordDetector()
		{

			public boolean isWordStart(char c)
			{
				return c == '.';
			}

			public boolean isWordPart(char c)
			{
				return Character.isLetterOrDigit(c) || c == '_' || c == '-';
			}
		}, createToken(ENTITY_NAME_TAG_CLASS_HAML));
		rules.add(rule);

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
		if (token != null && ENTITY_NAME_TAG_HAML.equals(token.getData()))
		{
			if (fLastToken == null || !PUNCTUATION_DEFINITION_TAG_HAML.equals(fLastToken.getData()))
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

}
