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

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.text.rules.CharacterMapRule;
import com.aptana.editor.common.text.rules.ExtendedWordRule;
import com.aptana.editor.css.CSSCodeScannerRuleBased;

/**
 * @author Chris Williams
 */
@SuppressWarnings("deprecation")
public class SassCodeScanner extends CSSCodeScannerRuleBased
{

	private IToken lastToken;
	private IDocument _document;

	@Override
	protected List<IRule> createRules()
	{
		List<IRule> rules = super.createRules();
		// Stick in a rule that recognizes mixins and variables
		ExtendedWordRule variableRule = new VariableWordRule(new VariableWordDetector(),
				createToken(ISassConstants.VARIABLE_OTHER_SCOPE), true);
		rules.add(1, variableRule);
		return rules;
	}

	@SuppressWarnings("nls")
	@Override
	protected WordRule createAtWordsRule()
	{
		WordRule wordRule = super.createAtWordsRule();

		wordRule.addWord("@mixin", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_MIXIN_SCOPE));
		wordRule.addWord("@include", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_INCLUDE_SCOPE));
		wordRule.addWord("@function", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_FUNCTION_SCOPE));
		wordRule.addWord("@while", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_WHILE_SCOPE));
		wordRule.addWord("@each", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_EACH_SCOPE));
		wordRule.addWord("@for", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_FOR_SCOPE));
		wordRule.addWord("@if", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_IF_SCOPE));
		wordRule.addWord("@warn", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_WARN_SCOPE));
		wordRule.addWord("@debug", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_DEBUG_SCOPE));
		wordRule.addWord("@extend", createToken(ISassConstants.KEYWORD_CONTROL_AT_RULE_EXTEND_SCOPE));

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
		if (lastToken != null
				&& !ISassConstants.PUNCTUATION_SEPARATOR_SCOPE.equals(lastToken.getData())
				&& (ISassConstants.META_SELECTOR_SCOPE.equals(token.getData()) || ISassConstants.META_PROPERTY_VALUE_SCOPE
						.equals(token.getData())))
		{
			String src = getSource(getTokenOffset(), getTokenLength());
			if (src.contains("\n") || src.contains("\r")) //$NON-NLS-1$ //$NON-NLS-2$ // $codepro.audit.disable platformSpecificLineSeparator
			{
				if (ISassConstants.META_SELECTOR_SCOPE.equals(token.getData()))
				{
					fInSelector = false;
				}
				else if (ISassConstants.META_PROPERTY_VALUE_SCOPE.equals(token.getData()))
				{
					fInPropertyValue = false;
				}
				token = Token.WHITESPACE;
			}

		}
		else if (lastToken != null
				&& (ISassConstants.KEYWORD_CONTROL_AT_RULE_MIXIN_SCOPE.equals(lastToken.getData()) || ISassConstants.KEYWORD_CONTROL_AT_RULE_INCLUDE_SCOPE
						.equals(lastToken.getData())))
		{
			token = new Token(ISassConstants.ENTITY_NAME_FUNCTION_SCOPE);
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
			return StringUtil.EMPTY;
		}
	}

	@Override
	protected CharacterMapRule createPunctuatorsRule()
	{
		CharacterMapRule rule = super.createPunctuatorsRule();
		// Override equals
		rule.add('=', createToken(ISassConstants.PUNCTUATION_DEFINITION_ENTITY_SCOPE));
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

	@Override
	public void setRange(IDocument document, int offset, int length)
	{
		this.lastToken = null;
		this._document = document;
		super.setRange(document, offset, length);
	}

	// FIXME This rule doesn't properly set the first char (!, =, or +) to it's own different punctuation token type
	private static final class VariableWordRule extends ExtendedWordRule
	{
		private VariableWordRule(IWordDetector detector, IToken defaultToken, boolean ignoreCase)
		{
			super(detector, defaultToken, ignoreCase);
		}

		@Override
		protected boolean wordOK(String word, ICharacterScanner scanner)
		{
			return word.length() >= 2;
		}
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
}
