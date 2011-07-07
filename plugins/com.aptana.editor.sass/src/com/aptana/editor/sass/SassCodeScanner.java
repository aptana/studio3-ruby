/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import com.aptana.editor.common.text.rules.CharacterMapRule;
import com.aptana.editor.common.text.rules.ExtendedWordRule;
import com.aptana.editor.css.CSSCodeScanner;

/**
 * @author Chris Williams
 */
public class SassCodeScanner extends CSSCodeScanner
{

	private IToken lastToken;
	private IDocument _document;

	@Override
	protected List<IRule> createRules()
	{
		List<IRule> rules = super.createRules();
		// Stick in a rule that recognizes mixins and variables
		// FIXME This rule doesn't properly set the first char (!, =, or +) to it's own different punctuation token type
		ExtendedWordRule variableRule = new ExtendedWordRule(new VariableWordDetector(),
				createToken("variable.other.sass"), true) //$NON-NLS-1$
		{

			@Override
			protected boolean wordOK(String word, ICharacterScanner scanner)
			{
				return word.length() >= 2;
			}
		};
		rules.add(1, variableRule);
		return rules;
	}

	@SuppressWarnings("nls")
	@Override
	protected WordRule createAtWordsRule()
	{
		WordRule wordRule = super.createAtWordsRule();

		wordRule.addWord("@mixin", createToken("keyword.control.at-rule.mixin.sass"));
		wordRule.addWord("@include", createToken("keyword.control.at-rule.include.sass"));
		wordRule.addWord("@function", createToken("keyword.control.at-rule.function.sass"));
		wordRule.addWord("@while", createToken("keyword.control.at-rule.while.sass"));
		wordRule.addWord("@each", createToken("keyword.control.at-rule.each.sass"));
		wordRule.addWord("@for", createToken("keyword.control.at-rule.for.sass"));
		wordRule.addWord("@if", createToken("keyword.control.at-rule.if.sass"));
		wordRule.addWord("@warn", createToken("keyword.control.at-rule.warn.sass"));
		wordRule.addWord("@debug", createToken("keyword.control.at-rule.debug.sass"));
		wordRule.addWord("@extend", createToken("keyword.control.at-rule.extend.sass"));

		return wordRule;
	}

	@Override
	public IToken nextToken()
	{
		IToken token = super.nextToken();
		if (token.isWhitespace())
		{
			return token;
		}
		if (token.getData() instanceof String && ((String) token.getData()).contains(".css")) //$NON-NLS-1$
		{
			String cssScopeName = ((String) token.getData());
			String sassScopeName = cssScopeName.replaceAll("\\.css", "\\.sass"); //$NON-NLS-1$ //$NON-NLS-2$
			token = new Token(sassScopeName);
		}
		// FIXME If token is "meta.selector.sass", then it's whitespace. Check if it contains a newline. If so, make it
		// Token.WHITESPACE if not preceded by a comma!
		if (lastToken != null && !"punctuation.separator.sass".equals(lastToken.getData()) && //$NON-NLS-1$
				("meta.selector.sass".equals(token.getData()) || "meta.property-value.sass".equals(token.getData()))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			String src = getSource(getTokenOffset(), getTokenLength());
			if (src.contains("\n") || src.contains("\r")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				if ("meta.selector.sass".equals(token.getData())) //$NON-NLS-1$
				{
					fInSelector = false;
				}
				else if ("meta.property-value.sass".equals(token.getData())) //$NON-NLS-1$
				{
					fInPropertyValue = false;
				}
				token = Token.WHITESPACE;
			}

		}
		else if (lastToken != null
				&& ("keyword.control.at-rule.mixin.sass".equals(lastToken.getData()) || "keyword.control.at-rule.include.sass" //$NON-NLS-1$ //$NON-NLS-2$
				.equals(lastToken.getData())))
		{
			token = new Token("entity.name.function.sass"); //$NON-NLS-1$
		}
		if (token.isOther())
		{
			lastToken = token;
		}
		return token;
	}

	private String getSource(int tokenOffset, int tokenLength)
	{
		try
		{
			return _document.get(tokenOffset, tokenLength);
		}
		catch (BadLocationException e)
		{
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	protected CharacterMapRule createPunctuatorsRule()
	{
		CharacterMapRule rule = super.createPunctuatorsRule();
		// Override equals
		rule.add('=', createToken("punctuation.definition.entity.sass")); //$NON-NLS-1$
		return rule;
	}

	/**
	 * Here we override the array of static property names from CSS and make ones that have "namespaces" (as Sass calls
	 * them) also get split up so we recognize the second half (i.e. we recognize both "font-family" as well as "font"
	 * and "family" individually).
	 */
	@Override
	protected String[] getPropertyNames()
	{
		String[] origCSS = super.getPropertyNames();
		Set<String> namespaced = new HashSet<String>();
		for (String name : origCSS)
		{
			StringTokenizer tokenizer = new StringTokenizer(name, "-"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens())
				namespaced.add(tokenizer.nextToken());
			namespaced.add(name);
		}
		List<String> list = new ArrayList<String>(namespaced);
		Collections.sort(list, new Comparator<String>()
		{
			public int compare(String o1, String o2)
			{
				return o2.length() - o1.length();
			}
		});
		return list.toArray(new String[list.size()]);
	}

	private static class VariableWordDetector implements IWordDetector
	{

		public boolean isWordPart(char c)
		{
			return Character.isLetterOrDigit(c) || c == '-' || c == '_';
		}

		public boolean isWordStart(char c)
		{
			// Old SASS used !, = and + as prefixes for variables and mixins, keep them in for now
			return c == '!' || c == '$' || c == '=' || c == '+';
		}
	}

	@Override
	public void setRange(IDocument document, int offset, int length)
	{
		this.lastToken = null;
		this._document = document;
		super.setRange(document, offset, length);
	}
}
