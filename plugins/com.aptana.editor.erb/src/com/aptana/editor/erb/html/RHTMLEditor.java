/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.html;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.erb.html.outline.RHTMLOutlineContentProvider;
import com.aptana.editor.erb.html.outline.RHTMLOutlineLabelProvider;
import com.aptana.editor.html.HTMLEditor;

/**
 * @author Max Stepanov
 */
@SuppressWarnings("restriction")
public class RHTMLEditor extends HTMLEditor
{

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		setPreferenceStore(getChainedPreferenceStore());
		setSourceViewerConfiguration(new RHTMLSourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(ERBEditorPlugin.getDefault().getRHTMLDocumentProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#installOpenTagCloser()
	 */
	@Override
	protected void installOpenTagCloser()
	{
		new ERBOpenTagCloser(getSourceViewer()).install();
	}

	@Override
	protected String getFileServiceContentTypeId()
	{
		return IERBConstants.CONTENT_TYPE_HTML_ERB;
	}

	@Override
	public ITreeContentProvider getOutlineContentProvider()
	{
		return new RHTMLOutlineContentProvider();
	}

	@Override
	public ILabelProvider getOutlineLabelProvider()
	{
		return new RHTMLOutlineLabelProvider(getFileService().getParseState());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#getPairMatchingCharacters()
	 */
	@Override
	protected char[] getPairMatchingCharacters()
	{
		char[] orig = super.getPairMatchingCharacters();
		char[] modified = new char[orig.length + 2];
		System.arraycopy(orig, 0, modified, 0, orig.length);
		modified[orig.length] = '%';
		modified[orig.length + 1] = '%';
		return modified;
	}

	public static IPreferenceStore getChainedPreferenceStore() // $codepro.audit.disable hidingInheritedStaticMethods
	{
		return new ChainedPreferenceStore(new IPreferenceStore[] { ERBEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() });
	}
}
