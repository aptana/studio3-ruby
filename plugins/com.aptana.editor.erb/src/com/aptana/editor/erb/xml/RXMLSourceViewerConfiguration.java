/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.xml;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CompositeSourceViewerConfiguration;
import com.aptana.editor.common.IPartitionerSwitchStrategy;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.erb.ERBPartitionerSwitchStrategy;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.ruby.RubySourceConfiguration;
import com.aptana.editor.ruby.core.RubyDoubleClickStrategy;
import com.aptana.editor.xml.XMLSourceConfiguration;

/**
 * @author Max Stepanov
 */
public class RXMLSourceViewerConfiguration extends CompositeSourceViewerConfiguration
{

	// FIXME Move these special strings out as constants on IERBConstants!
	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_XML_ERB), new QualifiedContentType(
				"text.xml.ruby")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_XML_ERB,
				CompositePartitionScanner.START_SWITCH_TAG), new QualifiedContentType(
				"text.xml.ruby", "source.erb.embedded.xml")); //$NON-NLS-1$ //$NON-NLS-2$
		c.addTranslation(new QualifiedContentType(IERBConstants.CONTENT_TYPE_XML_ERB,
				CompositePartitionScanner.END_SWITCH_TAG), new QualifiedContentType(
				"text.xml.ruby", "source.erb.embedded.xml")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private RubyDoubleClickStrategy fDoubleClickStrategy;

	public RXMLSourceViewerConfiguration(IPreferenceStore preferences, AbstractThemeableEditor editor)
	{
		super(XMLSourceConfiguration.getDefault(), RubySourceConfiguration.getDefault(), preferences, editor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CompositeSourceViewerConfiguration#getTopContentType()
	 */
	@Override
	protected String getTopContentType()
	{
		return IERBConstants.CONTENT_TYPE_XML_ERB;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CompositeSourceViewerConfiguration#getPartitionerSwitchStrategy()
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

}
