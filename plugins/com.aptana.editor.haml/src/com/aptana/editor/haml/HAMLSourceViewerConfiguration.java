/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.SimpleSourceViewerConfiguration;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.haml.internal.RubyAttributesSourceConfiguration;
import com.aptana.editor.ruby.RubySourceConfiguration;

/**
 * @author Max Stepanov
 */
public class HAMLSourceViewerConfiguration extends SimpleSourceViewerConfiguration
{

	public HAMLSourceViewerConfiguration(IPreferenceStore preferences, AbstractThemeableEditor editor)
	{
		super(preferences, editor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.SimpleSourceViewerConfiguration#getSourceViewerConfiguration()
	 */
	@Override
	public ISourceViewerConfiguration getSourceViewerConfiguration()
	{
		return HAMLSourceConfiguration.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.SimpleSourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source
	 * .ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
	{
		return TextUtils.combine(new String[][] { { IDocument.DEFAULT_CONTENT_TYPE },
				HAMLSourceConfiguration.CONTENT_TYPES, RubySourceConfiguration.CONTENT_TYPES,
				RubyAttributesSourceConfiguration.CONTENT_TYPES });
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		return new IAutoEditStrategy[] { new HAMLAutoIndentStrategy(contentType, this, sourceViewer,
				HAMLEditorPlugin.getDefault().getPreferenceStore()) };
	}

}
