/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CommonUtil;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.PartitionerSwitchingIgnoreRule;
import com.aptana.editor.common.text.rules.SingleCharacterRule;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;
import com.aptana.editor.haml.IHAMLConstants;
import com.aptana.editor.ruby.RubyCodeScanner;
import com.aptana.ruby.core.IRubyConstants;

/**
 * @author Max Stepanov
 */
public class RubyAttributesSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{

	public final static String PREFIX = "__hamlrubyattr_"; //$NON-NLS-1$
	public final static String DEFAULT = "__hamlrubyattr" + IDocument.DEFAULT_CONTENT_TYPE; //$NON-NLS-1$
	public final static String STRING_DOUBLE = PREFIX + "string_double"; //$NON-NLS-1$
	public final static String STRING_SINGLE = PREFIX + "string_single"; //$NON-NLS-1$

	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, STRING_SINGLE, STRING_DOUBLE };
	public static final String[] SPELLING_CONTENT_TYPES = new String[] { STRING_SINGLE, STRING_DOUBLE };

	private static RubyAttributesSourceConfiguration instance;

	private final IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new PartitionerSwitchingIgnoreRule(new SingleLineRule("\"", "\"", getToken(STRING_DOUBLE), '\\')), //$NON-NLS-1$ //$NON-NLS-2$
			new PartitionerSwitchingIgnoreRule(new SingleLineRule("\'", "\'", getToken(STRING_SINGLE), '\\')), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleCharacterRule('}', getToken(null)) };

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(DEFAULT), new QualifiedContentType(
				IHAMLConstants.RUBY_ATTRIBUTES_SCOPE));
		c.addTranslation(new QualifiedContentType(STRING_SINGLE), new QualifiedContentType(
				IHAMLConstants.RUBY_ATTRIBUTES_SCOPE, IRubyConstants.SINGLE_QUOTED_STRING_SCOPE));
		c.addTranslation(new QualifiedContentType(STRING_DOUBLE), new QualifiedContentType(
				IHAMLConstants.RUBY_ATTRIBUTES_SCOPE, IRubyConstants.DOUBLE_QUOTED_STRING_SCOPE));
	}

	public synchronized static RubyAttributesSourceConfiguration getDefault()
	{
		if (instance == null)
		{
			instance = new RubyAttributesSourceConfiguration();
		}
		return instance;
	}

	/**
	 * 
	 */
	private RubyAttributesSourceConfiguration()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
	 */
	public String[] getContentTypes()
	{
		return CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
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
			return IHAMLConstants.CONTENT_TYPE_HAML;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ITopContentTypesProvider#getTopContentTypes()
	 */
	public String[][] getTopContentTypes()
	{
		throw new IllegalStateException("Should never been called"); //$NON-NLS-1$
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
		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

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
		return new RubyCodeScanner();
	}

	private ITokenScanner getSingleQuotedStringScanner()
	{
		RuleBasedScanner singleQuotedStringScanner = new RuleBasedScanner();
		singleQuotedStringScanner.setDefaultReturnToken(getToken(IRubyConstants.SINGLE_QUOTED_STRING_SCOPE));
		return singleQuotedStringScanner;
	}

	private ITokenScanner getDoubleQuotedStringScanner()
	{
		RuleBasedScanner doubleQuotedStringScanner = new RuleBasedScanner();
		doubleQuotedStringScanner.setDefaultReturnToken(getToken(IRubyConstants.DOUBLE_QUOTED_STRING_SCOPE));
		return doubleQuotedStringScanner;
	}

	private static IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}

}
