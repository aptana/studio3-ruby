/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.haml.internal.HAMLFoldingComputer;

@SuppressWarnings("restriction")
public class HAMLEditor extends AbstractThemeableEditor
{
	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();
		setPreferenceStore(getChainedPreferenceStore());
		setSourceViewerConfiguration(new HAMLSourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(HAMLEditorPlugin.getDefault().getHAMLDocumentProvider());
	}

	public static IPreferenceStore getChainedPreferenceStore()
	{
		return new ChainedPreferenceStore(new IPreferenceStore[] { HAMLEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() });
	}

	@Override
	public IFoldingComputer createFoldingComputer(IDocument document)
	{
		return new HAMLFoldingComputer(this, document);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#getPluginPreferenceStore()
	 */
	@Override
	protected IPreferenceStore getPluginPreferenceStore()
	{
		return HAMLEditorPlugin.getDefault().getPreferenceStore();
	}

	protected String getFileServiceContentTypeId()
	{
		return IHAMLConstants.CONTENT_TYPE_HAML;
	}
}
