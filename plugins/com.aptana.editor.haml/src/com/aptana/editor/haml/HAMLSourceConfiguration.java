/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CommonUtil;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.SingleTokenScanner;
import com.aptana.editor.common.text.rules.CharacterMapRule;
import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.MultiCharacterRule;
import com.aptana.editor.common.text.rules.SingleCharacterRule;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;
import com.aptana.editor.haml.internal.HAMLElementScanner;
import com.aptana.editor.haml.internal.HAMLSubPartitionScanner;
import com.aptana.editor.haml.internal.RubyAttributesSourceConfiguration;
import com.aptana.editor.haml.internal.text.rules.HAMLElementRule;
import com.aptana.editor.haml.internal.text.rules.HAMLEscapeRule;
import com.aptana.editor.haml.internal.text.rules.HAMLSingleLineRule;
import com.aptana.editor.haml.internal.text.rules.RubyEvaluationElementRule;
import com.aptana.editor.ruby.RubySourceConfiguration;
import com.aptana.ruby.core.IRubyConstants;

/**
 * @author Max Stepanov
 * @author Chris Williams
 */
public class HAMLSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{

	private final static String PREFIX = "__haml_"; //$NON-NLS-1$
	public final static String DEFAULT = PREFIX + IDocument.DEFAULT_CONTENT_TYPE;
	public final static String DOCTYPE = PREFIX + "doctype"; //$NON-NLS-1$
	public final static String ELEMENT = PREFIX + "element"; //$NON-NLS-1$
	public final static String RUBY_EVALUATION = PREFIX + "ruby_evaluation"; //$NON-NLS-1$
	public final static String RUBY_ATTRIBUTES = PREFIX + "ruby_attributes"; //$NON-NLS-1$
	public final static String RUBY_ATTRIBUTES_CLOSE = PREFIX + "ruby_attributes_close"; //$NON-NLS-1$
	public final static String HTML_ATTRIBUTES = PREFIX + "html_attributes"; //$NON-NLS-1$
	public final static String OBJECT = PREFIX + "object"; //$NON-NLS-1$
	public final static String INTERPOLATION = PREFIX + "interpolation"; //$NON-NLS-1$
	public final static String HTML_COMMENT = PREFIX + "html_comment"; //$NON-NLS-1$
	public final static String HAML_COMMENT = PREFIX + "haml_comment"; //$NON-NLS-1$

	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, HTML_COMMENT, HAML_COMMENT, DOCTYPE, ELEMENT,
			INTERPOLATION, RUBY_EVALUATION, HTML_ATTRIBUTES, RUBY_ATTRIBUTES, RUBY_ATTRIBUTES_CLOSE, OBJECT };
	private static final String[] SPELLING_CONTENT_TYPES = new String[] { DEFAULT, HTML_COMMENT, HAML_COMMENT };

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { IHAMLConstants.CONTENT_TYPE_HAML },
			{ IHAMLConstants.CONTENT_TYPE_HAML, IRubyConstants.CONTENT_TYPE_RUBY } };

	private final IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new HAMLSingleLineRule("/", getToken(HTML_COMMENT)), //$NON-NLS-1$
			new HAMLSingleLineRule("-#", getToken(HAML_COMMENT)), //$NON-NLS-1$
			new HAMLSingleLineRule("!!!", getToken(DOCTYPE)), //$NON-NLS-1$
			new HAMLEscapeRule(getToken(StringUtil.EMPTY)),
			new SingleLineRule("#{", "}", getToken(INTERPOLATION)), //$NON-NLS-1$ //$NON-NLS-2$
			new HAMLElementRule(getToken(ELEMENT)), new RubyEvaluationElementRule(new Token(RUBY_EVALUATION)),
			new SingleCharacterRule('{', getToken(RUBY_ATTRIBUTES)),
			new SingleCharacterRule('}', getToken(RUBY_ATTRIBUTES_CLOSE)),
			new SingleLineRule("[", "]", getToken(OBJECT)), //$NON-NLS-1$ //$NON-NLS-2$
			new MultiLineRule("(", ")", getToken(HTML_ATTRIBUTES)), //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static HAMLSourceConfiguration instance;

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(IHAMLConstants.CONTENT_TYPE_HAML), new QualifiedContentType(
				IHAMLConstants.TEXT_SCOPE));
		c.addTranslation(new QualifiedContentType(HAML_COMMENT), new QualifiedContentType(
				IHAMLConstants.HAML_COMMENT_SCOPE));
		c.addTranslation(new QualifiedContentType(HTML_COMMENT), new QualifiedContentType(
				IHAMLConstants.HTML_COMMENT_SCOPE));
		c.addTranslation(new QualifiedContentType(DOCTYPE), new QualifiedContentType(IHAMLConstants.DOCTYPE_SCOPE));
		c.addTranslation(new QualifiedContentType(ELEMENT), new QualifiedContentType(IHAMLConstants.TAG_SCOPE));
		c.addTranslation(new QualifiedContentType(HTML_ATTRIBUTES), new QualifiedContentType(
				IHAMLConstants.RUBY_ATTRIBUTES_SCOPE));
		c.addTranslation(new QualifiedContentType(RUBY_ATTRIBUTES), new QualifiedContentType(
				IHAMLConstants.RUBY_ATTRIBUTES_SCOPE));
		c.addTranslation(new QualifiedContentType(RUBY_EVALUATION), new QualifiedContentType(
				IHAMLConstants.RUBY_EVAL_SCOPE));
		c.addTranslation(new QualifiedContentType(OBJECT), new QualifiedContentType(IHAMLConstants.OBJECT_SCOPE));
		c.addTranslation(new QualifiedContentType(INTERPOLATION), new QualifiedContentType(
				IHAMLConstants.INTERPOLATION_SCOPE));
		c.addTranslation(new QualifiedContentType(IHAMLConstants.CONTENT_TYPE_HAML, IRubyConstants.CONTENT_TYPE_RUBY),
				new QualifiedContentType(IHAMLConstants.TEXT_SCOPE, IHAMLConstants.RUBY_EVAL_SCOPE,
						IHAMLConstants.EMBEDDED_RUBY_SCOPE));
	}

	public synchronized static HAMLSourceConfiguration getDefault()
	{
		if (instance == null)
		{
			instance = new HAMLSourceConfiguration();
		}
		return instance;
	}

	private HAMLSourceConfiguration()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
	 */
	public String[] getContentTypes()
	{
		return TextUtils.combine(new String[][] { CONTENT_TYPES, RubySourceConfiguration.CONTENT_TYPES,
				RubyAttributesSourceConfiguration.CONTENT_TYPES });
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ITopContentTypesProvider#getTopContentTypes()
	 */
	public String[][] getTopContentTypes()
	{
		return TOP_CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getSpellingContentTypes ()
	 */
	public String[] getSpellingContentTypes()
	{
		return SPELLING_CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getPartitioningRules ()
	 */
	public IPredicateRule[] getPartitioningRules()
	{
		return partitioningRules;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#createSubPartitionScanner ()
	 */
	public ISubPartitionScanner createSubPartitionScanner()
	{
		return new HAMLSubPartitionScanner();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration# getDocumentDefaultContentType()
	 */
	public String getDocumentContentType(String contentType)
	{
		if (contentType.startsWith(PREFIX))
		{
			return IHAMLConstants.CONTENT_TYPE_HAML;
		}
		String result = RubySourceConfiguration.getDefault().getDocumentContentType(contentType);
		if (result != null)
		{
			return result;
		}
		result = RubyAttributesSourceConfiguration.getDefault().getDocumentContentType(contentType);
		if (result != null)
		{
			return result;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#
	 * setupPresentationReconciler(org.eclipse.jface.text.presentation .PresentationReconciler,
	 * org.eclipse.jface.text.source.ISourceViewer)
	 */
	public void setupPresentationReconciler(PresentationReconciler reconciler, ISourceViewer sourceViewer)
	{
		RubySourceConfiguration.getDefault().setupPresentationReconciler(reconciler, sourceViewer);
		RubyAttributesSourceConfiguration.getDefault().setupPresentationReconciler(reconciler, sourceViewer);

		DefaultDamagerRepairer dr = new ThemeingDamagerRepairer(getTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

		dr = new ThemeingDamagerRepairer(getHTMLCommentScanner());
		reconciler.setDamager(dr, HTML_COMMENT);
		reconciler.setRepairer(dr, HTML_COMMENT);

		dr = new ThemeingDamagerRepairer(getHAMLCommentScanner());
		reconciler.setDamager(dr, HAML_COMMENT);
		reconciler.setRepairer(dr, HAML_COMMENT);

		dr = new ThemeingDamagerRepairer(getDocTypeScanner());
		reconciler.setDamager(dr, DOCTYPE);
		reconciler.setRepairer(dr, DOCTYPE);

		dr = new ThemeingDamagerRepairer(getElementScanner());
		reconciler.setDamager(dr, ELEMENT);
		reconciler.setRepairer(dr, ELEMENT);

		dr = new ThemeingDamagerRepairer(getInterpolationScanner());
		reconciler.setDamager(dr, INTERPOLATION);
		reconciler.setRepairer(dr, INTERPOLATION);

		dr = new ThemeingDamagerRepairer(getObjectScanner());
		reconciler.setDamager(dr, OBJECT);
		reconciler.setRepairer(dr, OBJECT);

		dr = new ThemeingDamagerRepairer(getHTMLAttributesScanner());
		reconciler.setDamager(dr, HTML_ATTRIBUTES);
		reconciler.setRepairer(dr, HTML_ATTRIBUTES);

		dr = new ThemeingDamagerRepairer(getRubyEvaluationScanner());
		reconciler.setDamager(dr, RUBY_EVALUATION);
		reconciler.setRepairer(dr, RUBY_EVALUATION);

	}

	protected ITokenScanner getRubyEvaluationScanner()
	{
		return new SingleTokenScanner(getToken(StringUtil.EMPTY));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getContentAssistProcessor (com.aptana.editor.common.
	 * AbstractThemeableEditor, java.lang.String)
	 */
	public IContentAssistProcessor getContentAssistProcessor(AbstractThemeableEditor editor, String contentType)
	{
		return null;
	}

	private ITokenScanner getTextScanner()
	{
		RuleBasedScanner textScanner = new RuleBasedScanner();
		textScanner.setRules(new IRule[] {
				new CharacterMapRule().add('/', getToken(IHAMLConstants.TAG_TERMINATOR_PUNCTUATION_SCOPE))
						.add('/', getToken(IHAMLConstants.TAG_TERMINATOR_PUNCTUATION_SCOPE))
						.add('>', getToken(IHAMLConstants.TAG_OTHER_PUNCTUATION_SCOPE))
						.add('<', getToken(IHAMLConstants.TAG_OTHER_PUNCTUATION_SCOPE))
						.add('&', getToken(IHAMLConstants.TAG_OTHER_PUNCTUATION_SCOPE))
						.add('!', getToken(IHAMLConstants.TAG_OTHER_PUNCTUATION_SCOPE)),
				new HAMLEscapeRule(getToken(IHAMLConstants.META_ESCAPE_SCOPE)) });
		textScanner.setDefaultReturnToken(getToken(IHAMLConstants.TEXT_SCOPE));
		return textScanner;
	}

	private ITokenScanner getElementScanner()
	{
		return new HAMLElementScanner();
	}

	private ITokenScanner getInterpolationScanner()
	{
		RuleBasedScanner interpolationScanner = new RuleBasedScanner();
		interpolationScanner.setRules(new IRule[] {
				new MultiCharacterRule("#{", getToken(IHAMLConstants.SECTION_EMBEDDED_PUNCTUATION_SCOPE)), //$NON-NLS-1$
				new SingleCharacterRule('}', getToken(IHAMLConstants.SECTION_EMBEDDED_PUNCTUATION_SCOPE)) });
		interpolationScanner.setDefaultReturnToken(getToken(IHAMLConstants.EMBEDDED_RUBY_SOURCE_SCOPE));
		return interpolationScanner;
	}

	private ITokenScanner getObjectScanner()
	{
		RuleBasedScanner objectScanner = new RuleBasedScanner();
		// @formatter:off
		objectScanner.setRules(new IRule[] { 
				new CharacterMapRule()
				.add('[',	getToken(IHAMLConstants.SECTION_OTHER_PUNCTUATION_SCOPE))
				.add(']', getToken(IHAMLConstants.SECTION_OTHER_PUNCTUATION_SCOPE)),
				// TODO: add word rules here for:
				// - variable.other.readwrite.instance.ruby
				// - constant.other.symbol.ruby
				// - comma
				});
		// @formatter:on
		objectScanner.setDefaultReturnToken(getToken(IHAMLConstants.OBJECT_SCOPE));
		return objectScanner;
	}

	private ITokenScanner getHTMLAttributesScanner()
	{
		RuleBasedScanner htmlAttributesScanner = new RuleBasedScanner();
		// @formatter:off
		htmlAttributesScanner.setRules(new IRule[] { 
				new CharacterMapRule()
					.add('(', getToken(IHAMLConstants.SECTION_OTHER_PUNCTUATION_SCOPE))
					.add(')', getToken(IHAMLConstants.SECTION_OTHER_PUNCTUATION_SCOPE)),
					// TODO: add word rules here for:
					// - single quoted string
					// - double quoted string
					// - an HTML attribute name
					// - equal sign
				});
		// @formatter:on
		htmlAttributesScanner.setDefaultReturnToken(getToken(IHAMLConstants.OBJECT_SCOPE));
		return htmlAttributesScanner;
	}

	private ITokenScanner getHTMLCommentScanner()
	{
		// FIXME Use CommentScanner and subclass!
		RuleBasedScanner commentScanner = new RuleBasedScanner();
		commentScanner = new CommentScanner(getToken(IHAMLConstants.HTML_COMMENT_SCOPE))
		{
			@Override
			protected List<IRule> createRules()
			{
				List<IRule> rules = super.createRules();
				rules.add(new SingleCharacterRule('/', getToken(IHAMLConstants.COMMENT_PUNCTUATION_SCOPE)));
				return rules;
			}
		};
		return commentScanner;
	}

	private ITokenScanner getHAMLCommentScanner()
	{
		return new CommentScanner(getToken(IRubyConstants.LINE_COMMENT_SCOPE));
	}

	private ITokenScanner getDocTypeScanner()
	{
		RuleBasedScanner docTypeScanner = new RuleBasedScanner();
		docTypeScanner.setRules(new IRule[] { new SingleCharacterRule('!',
				getToken(IHAMLConstants.PROLOG_DEF_PUNCTUATION_SCOPE)) });
		docTypeScanner.setDefaultReturnToken(getToken(IHAMLConstants.DOCTYPE_SCOPE));
		return docTypeScanner;
	}

	private static IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}

}
