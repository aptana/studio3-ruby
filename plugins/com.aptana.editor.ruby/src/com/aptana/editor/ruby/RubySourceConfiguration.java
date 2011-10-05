/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.ruby;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CommonUtil;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.SingleTokenScanner;
import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.PartitionerSwitchingIgnoreRule;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;
import com.aptana.editor.ruby.internal.contentassist.RubyCommentContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyDoubleQuotedStringContentAssistProcessor;
import com.aptana.editor.ruby.internal.contentassist.RubyRegexpContentAssistProcessor;
import com.aptana.editor.ruby.internal.text.LineContinuationDamagerRepairer;
import com.aptana.ruby.core.IRubyConstants;

/**
 * @author Max Stepanov
 * @author Michael Xia
 */
public class RubySourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{

	// FIXME Move out the translations strings as constants in IRubyConstants
	public static final String PREFIX = "__rb_"; //$NON-NLS-1$
	public static final String DEFAULT = "__rb" + IDocument.DEFAULT_CONTENT_TYPE; //$NON-NLS-1$
	public static final String SINGLE_LINE_COMMENT = PREFIX + "singleline_comment"; //$NON-NLS-1$
	public static final String MULTI_LINE_COMMENT = PREFIX + "multiline_comment"; //$NON-NLS-1$
	public static final String REGULAR_EXPRESSION = PREFIX + "regular_expression"; //$NON-NLS-1$
	public static final String COMMAND = PREFIX + "command"; //$NON-NLS-1$
	public static final String STRING_SINGLE = PREFIX + "string_single"; //$NON-NLS-1$
	public static final String STRING_DOUBLE = PREFIX + "string_double"; //$NON-NLS-1$

	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT,
			REGULAR_EXPRESSION, COMMAND, STRING_SINGLE, STRING_DOUBLE };
	private static final String[] SPELLING_CONTENT_TYPES = new String[] { SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT,
			STRING_SINGLE, STRING_DOUBLE };

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { IRubyConstants.CONTENT_TYPE_RUBY } };

	private final IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new PartitionerSwitchingIgnoreRule(new EndOfLineRule("#", getToken(SINGLE_LINE_COMMENT))), //$NON-NLS-1$
			new PartitionerSwitchingIgnoreRule(new MultiLineRule(
					"=begin", "=end", getToken(MULTI_LINE_COMMENT), (char) 0, true)), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("/", "/", getToken(REGULAR_EXPRESSION), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\"", "\"", getToken(STRING_DOUBLE), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\'", "\'", getToken(STRING_SINGLE), '\\') }; //$NON-NLS-1$ //$NON-NLS-2$

	private static RubySourceConfiguration instance;

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(IRubyConstants.CONTENT_TYPE_RUBY), new QualifiedContentType(
				"source.ruby.rails")); //$NON-NLS-1$ // FIXME Should just be source.ruby! Rails bundle should contribute the more specific scope
		c.addTranslation(new QualifiedContentType(STRING_SINGLE), new QualifiedContentType(
				IRubyConstants.SINGLE_QUOTED_STRING_SCOPE));
		c.addTranslation(new QualifiedContentType(STRING_DOUBLE), new QualifiedContentType(
				IRubyConstants.DOUBLE_QUOTED_STRING_SCOPE));
		c.addTranslation(new QualifiedContentType(SINGLE_LINE_COMMENT), new QualifiedContentType(
				IRubyConstants.LINE_COMMENT_SCOPE));
		c.addTranslation(new QualifiedContentType(MULTI_LINE_COMMENT), new QualifiedContentType(
				IRubyConstants.BLOCK_COMMENT_SCOPE));
		c.addTranslation(new QualifiedContentType(REGULAR_EXPRESSION), new QualifiedContentType(
				"string.regexp.classic.ruby")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(COMMAND), new QualifiedContentType("string.interpolated.ruby")); //$NON-NLS-1$
	}

	private RubySourceConfiguration()
	{
	}

	public synchronized static RubySourceConfiguration getDefault()
	{
		if (instance == null)
		{
			instance = new RubySourceConfiguration();
		}
		return instance;
	}

	/*
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
	 */
	public String[] getContentTypes()
	{
		return CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ITopContentTypesProvider#getTopContentTypes()
	 */
	public String[][] getTopContentTypes()
	{
		return TOP_CONTENT_TYPES;
	}

	/* (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getSpellingContentTypes()
	 */
	public String[] getSpellingContentTypes()
	{
		return SPELLING_CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getPartitioningRules()
	 */
	public IPredicateRule[] getPartitioningRules()
	{
		return partitioningRules;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#createSubPartitionScanner()
	 */
	public ISubPartitionScanner createSubPartitionScanner()
	{
		return new SubPartitionScanner(partitioningRules, CONTENT_TYPES, getToken(DEFAULT));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getDocumentContentType(java.lang.String)
	 */
	public String getDocumentContentType(String contentType)
	{
		if (contentType.startsWith(PREFIX))
		{
			return IRubyConstants.CONTENT_TYPE_RUBY;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.ISourceViewerConfiguration#setupPresentationReconciler(org.eclipse.jface.text.presentation
	 * .PresentationReconciler, org.eclipse.jface.text.source.ISourceViewer)
	 */
	public void setupPresentationReconciler(PresentationReconciler reconciler, ISourceViewer sourceViewer)
	{
		DefaultDamagerRepairer dr = new LineContinuationDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

		dr = new ThemeingDamagerRepairer(getSingleLineCommentScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, RubySourceConfiguration.SINGLE_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getMultiLineCommentScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, RubySourceConfiguration.MULTI_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getRegexpScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.REGULAR_EXPRESSION);
		reconciler.setRepairer(dr, RubySourceConfiguration.REGULAR_EXPRESSION);

		dr = new ThemeingDamagerRepairer(getCommandScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.COMMAND);
		reconciler.setRepairer(dr, RubySourceConfiguration.COMMAND);

		dr = new ThemeingDamagerRepairer(getSingleQuotedStringScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.STRING_SINGLE);
		reconciler.setRepairer(dr, RubySourceConfiguration.STRING_SINGLE);

		dr = new ThemeingDamagerRepairer(getDoubleQuotedStringScanner());
		reconciler.setDamager(dr, RubySourceConfiguration.STRING_DOUBLE);
		reconciler.setRepairer(dr, RubySourceConfiguration.STRING_DOUBLE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getContentAssistProcessor(com.aptana.editor.common.
	 * AbstractThemeableEditor, java.lang.String)
	 */
	public IContentAssistProcessor getContentAssistProcessor(AbstractThemeableEditor editor, String contentType)
	{
		if (DEFAULT.equals(contentType) || IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
		{
			return new RubyContentAssistProcessor(editor);
		}
		if (RubySourceConfiguration.STRING_DOUBLE.equals(contentType)
				|| RubySourceConfiguration.COMMAND.equals(contentType))
		{
			return new RubyDoubleQuotedStringContentAssistProcessor(editor);
		}
		if (RubySourceConfiguration.REGULAR_EXPRESSION.equals(contentType))
		{
			return new RubyRegexpContentAssistProcessor(editor);
		}
		if (RubySourceConfiguration.MULTI_LINE_COMMENT.equals(contentType)
				|| RubySourceConfiguration.SINGLE_LINE_COMMENT.equals(contentType))
		{
			return new RubyCommentContentAssistProcessor(editor);
		}
		// TODO Add special content assist processor for comments
		return null;
	}

	private ITokenScanner getCodeScanner()
	{
		return new RubyCodeScanner();
	}

	private ITokenScanner getMultiLineCommentScanner()
	{
		return new CommentScanner(getToken(IRubyConstants.BLOCK_COMMENT_SCOPE));
	}

	private ITokenScanner getSingleLineCommentScanner()
	{
		return new CommentScanner(getToken(IRubyConstants.LINE_COMMENT_SCOPE));
	}

	private ITokenScanner getRegexpScanner()
	{
		return new RubyRegexpScanner();
	}

	private ITokenScanner getCommandScanner()
	{
		return new SingleTokenScanner(getToken("string.interpolated.ruby")); //$NON-NLS-1$
	}

	private ITokenScanner getSingleQuotedStringScanner()
	{
		return new SingleTokenScanner(getToken(IRubyConstants.SINGLE_QUOTED_STRING_SCOPE));
	}

	private ITokenScanner getDoubleQuotedStringScanner()
	{
		return new SingleTokenScanner(getToken(IRubyConstants.DOUBLE_QUOTED_STRING_SCOPE));
	}

	private static IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}
}
