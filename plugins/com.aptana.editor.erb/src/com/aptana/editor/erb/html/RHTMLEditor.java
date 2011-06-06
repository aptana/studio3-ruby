/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.html;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.erb.Activator;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.editor.erb.html.outline.RHTMLOutlineContentProvider;
import com.aptana.editor.erb.html.outline.RHTMLOutlineLabelProvider;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.html.parsing.HTMLParseState;

/**
 * @author Max Stepanov
 */
@SuppressWarnings("restriction")
public class RHTMLEditor extends HTMLEditor {

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();

		setPreferenceStore(getChainedPreferenceStore());
		setSourceViewerConfiguration(new RHTMLSourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(Activator.getDefault().getRHTMLDocumentProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#installOpenTagCloser()
	 */
	@Override
	protected void installOpenTagCloser() {
		new ERBOpenTagCloser(getSourceViewer()).install();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#createFileService()
	 */
	@Override
	protected FileService createFileService() {
		return new FileService(IERBConstants.CONTENT_TYPE_HTML_ERB, new HTMLParseState());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#createOutlinePage()
	 */
	@Override
	protected CommonOutlinePage createOutlinePage() {
		CommonOutlinePage outline = super.createOutlinePage();
		outline.setContentProvider(new RHTMLOutlineContentProvider());
		outline.setLabelProvider(new RHTMLOutlineLabelProvider(getFileService().getParseState()));

		return outline;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.html.HTMLEditor#getPairMatchingCharacters()
	 */
	@Override
	protected char[] getPairMatchingCharacters() {
		char[] orig = super.getPairMatchingCharacters();
		char[] modified = new char[orig.length + 2];
		System.arraycopy(orig, 0, modified, 0, orig.length);
		modified[orig.length] = '%';
		modified[orig.length + 1] = '%';
		return modified;
	}

	public static IPreferenceStore getChainedPreferenceStore() {
		return new ChainedPreferenceStore(new IPreferenceStore[] { Activator.getDefault().getPreferenceStore(), CommonEditorPlugin.getDefault().getPreferenceStore(),
				EditorsPlugin.getDefault().getPreferenceStore() });
	}
}
