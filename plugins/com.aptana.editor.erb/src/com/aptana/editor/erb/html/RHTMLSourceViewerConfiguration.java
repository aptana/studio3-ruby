/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.html;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CompositeSourceViewerConfiguration;
import com.aptana.editor.common.IPartitionerSwitchStrategy;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.css.ICSSConstants;
import com.aptana.editor.erb.ERBPartitionerSwitchStrategy;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.js.IJSConstants;
import com.aptana.editor.ruby.RubySourceConfiguration;
import com.aptana.editor.ruby.core.RubyDoubleClickStrategy;
import com.aptana.ruby.core.IRubyConstants;

/**
 * @author Max Stepanov
 * @author cwilliams
 */
public class RHTMLSourceViewerConfiguration extends CompositeSourceViewerConfiguration implements IERBConstants
{

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB), new QualifiedContentType(
				TOPLEVEL_RHTML_SCOPE));
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB,
				CompositePartitionScanner.START_SWITCH_TAG), new QualifiedContentType(TOPLEVEL_RHTML_SCOPE,
				EMBEDDED_RUBY_TAG_SCOPE));
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB,
				CompositePartitionScanner.END_SWITCH_TAG), new QualifiedContentType(TOPLEVEL_RHTML_SCOPE,
				EMBEDDED_RUBY_TAG_SCOPE));

		c.addTranslation(
				new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB, IHTMLConstants.CONTENT_TYPE_HTML),
				new QualifiedContentType(TOPLEVEL_RHTML_SCOPE));
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB, ICSSConstants.CONTENT_TYPE_CSS),
				new QualifiedContentType(TOPLEVEL_RHTML_SCOPE, EMBEDDED_CSS_SCOPE));
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB, IJSConstants.CONTENT_TYPE_JS),
				new QualifiedContentType(TOPLEVEL_RHTML_SCOPE, EMBEDDED_JS_SCOPE));
		c.addTranslation(
				new QualifiedContentType(IERBConstants.CONTENT_TYPE_HTML_ERB, IRubyConstants.CONTENT_TYPE_RUBY),
				new QualifiedContentType(TOPLEVEL_RHTML_SCOPE, EMBEDDED_RUBY_SCOPE));
	}

	private RubyDoubleClickStrategy fDoubleClickStrategy;

	protected RHTMLSourceViewerConfiguration(IPreferenceStore preferences, AbstractThemeableEditor editor)
	{
		super(HTMLSourceConfiguration.getDefault(), RubySourceConfiguration.getDefault(), preferences, editor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CompositeSourceViewerConfiguration#getTopContentType()
	 */
	@Override
	protected String getTopContentType()
	{
		return IERBConstants.CONTENT_TYPE_HTML_ERB;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CompositeSourceViewerConfiguration#getLanguageSpecification()
	 */
	@Override
	protected IPartitionerSwitchStrategy getPartitionerSwitchStrategy()
	{
		return ERBPartitionerSwitchStrategy.getDefault();
	}

	protected String getStartEndTokenType()
	{
		return "punctuation.section.embedded.ruby"; //$NON-NLS-1$
	}
	
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
	{
		if (fDoubleClickStrategy == null)
		{
			fDoubleClickStrategy = new RubyDoubleClickStrategy();
		}
		return fDoubleClickStrategy;
	}

	@Override
	protected IContentAssistProcessor getContentAssistProcessor(ISourceViewer sourceViewer, String contentType)
	{
		// Just uses the HTML content assist processor for now
		// TODO: needs to check for ruby content type when the content assist is available there
		return HTMLSourceConfiguration.getDefault().getContentAssistProcessor(getEditor(), contentType);
	}
}
