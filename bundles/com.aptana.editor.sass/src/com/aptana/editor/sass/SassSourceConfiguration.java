/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.sass;

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
import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.common.text.rules.EmptyCommentRule;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;

/**
 * @author Max Stepanov
 */
public class SassSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{

	public final static String PREFIX = "__sass_"; //$NON-NLS-1$
	public final static String DEFAULT = PREFIX + IDocument.DEFAULT_CONTENT_TYPE;
	public final static String STRING_SINGLE = PREFIX + "string_single"; //$NON-NLS-1$
	public final static String STRING_DOUBLE = PREFIX + "string_double"; //$NON-NLS-1$
	public final static String SINGLE_LINE_COMMENT = PREFIX + "singleline_comment"; //$NON-NLS-1$
	public final static String MULTI_LINE_COMMENT = PREFIX + "multiline_comment"; //$NON-NLS-1$

	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT,
			STRING_SINGLE, STRING_DOUBLE };

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { ISassConstants.CONTENT_TYPE_SASS } };

	private IPredicateRule[] partitioningRules;

	private static SassSourceConfiguration instance;

	public static SassSourceConfiguration getDefault()
	{
		if (instance == null)
		{
			instance = new SassSourceConfiguration();
			// TODO Probably need to do some other massaging!
			IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
			c.addTranslation(new QualifiedContentType(ISassConstants.CONTENT_TYPE_SASS), new QualifiedContentType(
					ISassConstants.TOPLEVEL_SCOPE));
		}
		return instance;
	}

	private SassSourceConfiguration()
	{
		IToken singleLineComment = getToken(SINGLE_LINE_COMMENT);
		IToken multiLineComment = getToken(MULTI_LINE_COMMENT);

		partitioningRules = new IPredicateRule[] { new SingleLineRule("\"", "\"", getToken(STRING_DOUBLE), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
				new SingleLineRule("\'", "\'", getToken(STRING_SINGLE), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
				new EmptyCommentRule(multiLineComment), new MultiLineRule("/*", "*/", multiLineComment), //$NON-NLS-1$ //$NON-NLS-2$
				new EndOfLineRule("//", singleLineComment) //$NON-NLS-1$
		};
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
	 */
	public String[] getContentTypes()
	{
		return CONTENT_TYPES;
	}

	public String[][] getTopContentTypes()
	{
		return TOP_CONTENT_TYPES;
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
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getDocumentDefaultContentType()
	 */
	public String getDocumentContentType(String contentType)
	{
		if (contentType.startsWith(PREFIX))
		{
			return ISassConstants.CONTENT_TYPE_SASS;
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
		DefaultDamagerRepairer dr = new ThemeingDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

		dr = new ThemeingDamagerRepairer(getSingleLineCommentScanner());
		reconciler.setDamager(dr, SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, SINGLE_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getMultiLineCommentScanner());
		reconciler.setDamager(dr, MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, MULTI_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getSingleQuotedStringScanner());
		reconciler.setDamager(dr, STRING_SINGLE);
		reconciler.setRepairer(dr, STRING_SINGLE);

		dr = new ThemeingDamagerRepairer(getDoubleQuotedStringScanner());
		reconciler.setDamager(dr, STRING_DOUBLE);
		reconciler.setRepairer(dr, STRING_DOUBLE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getContentAssistProcessor(com.aptana.editor.common.
	 * AbstractThemeableEditor, java.lang.String)
	 */
	public IContentAssistProcessor getContentAssistProcessor(AbstractThemeableEditor editor, String contentType)
	{
		return null;
	}

	private ITokenScanner getCodeScanner()
	{
		return new SassCodeScanner();
	}

	private ITokenScanner getSingleLineCommentScanner()
	{
		return new CommentScanner(getToken(ISassConstants.COMMENT_LINE_SCOPE));
	}

	private ITokenScanner getMultiLineCommentScanner()
	{
		return new CommentScanner(getToken(ISassConstants.COMMENT_BLOCK_SCOPE));
	}

	private ITokenScanner getDoubleQuotedStringScanner()
	{
		return new StringScanner(ISassConstants.STRING_QUOTED_DOUBLE_SCOPE);
	}

	private ITokenScanner getSingleQuotedStringScanner()
	{
		return new StringScanner(ISassConstants.STRING_QUOTED_SINGLE_SCOPE);
	}

	private static IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}

}
