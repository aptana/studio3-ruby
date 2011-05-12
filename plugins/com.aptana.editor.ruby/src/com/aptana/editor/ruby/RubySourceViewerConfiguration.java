/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.SimpleSourceViewerConfiguration;
import com.aptana.editor.common.contentassist.ContentAssistant;
import com.aptana.editor.ruby.core.RubyDoubleClickStrategy;

public class RubySourceViewerConfiguration extends SimpleSourceViewerConfiguration
{
	private RubyDoubleClickStrategy fDoubleClickStrategy;

	/**
	 * RubySourceViewerConfiguration
	 * 
	 * @param preferences
	 * @param editor
	 */
	public RubySourceViewerConfiguration(IPreferenceStore preferences, AbstractThemeableEditor editor)
	{
		super(preferences, editor);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.aptana.editor.common.CommonSourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.
	 * ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		IContentAssistant assistant = super.getContentAssistant(sourceViewer);

		if (assistant instanceof ContentAssistant)
		{
			// Turn on prefix completion (complete partially if all proposals share same prefix)
			((ContentAssistant) assistant).enablePrefixCompletion(true);
		}

		return assistant;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonSourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source
	 * .ISourceViewer, java.lang.String)
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
	{
		if (fDoubleClickStrategy == null)
		{
			fDoubleClickStrategy = new RubyDoubleClickStrategy();
		}

		return fDoubleClickStrategy;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonSourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.
	 * ISourceViewer, java.lang.String)
	 */
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		return new IAutoEditStrategy[] { new RubyAutoIndentStrategy(contentType, this, sourceViewer, RubyEditorPlugin
				.getDefault().getPreferenceStore()) };
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.SimpleSourceViewerConfiguration#getSourceViewerConfiguration()
	 */
	@Override
	public ISourceViewerConfiguration getSourceViewerConfiguration()
	{
		return RubySourceConfiguration.getDefault();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer)
	{
		Map targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("com.aptana.ruby.ui.rubyCode", getEditor()); //$NON-NLS-1$
		return targets;
	}
}
