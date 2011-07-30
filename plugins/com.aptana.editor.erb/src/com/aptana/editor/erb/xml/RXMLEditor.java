/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb.xml;

import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.erb.ERBEditorPlugin;
import com.aptana.editor.erb.IERBConstants;

/**
 * @author Max Stepanov
 */
public class RXMLEditor extends AbstractThemeableEditor
{
	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		setSourceViewerConfiguration(new RXMLSourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(ERBEditorPlugin.getDefault().getRXMLDocumentProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#getPluginPreferenceStore()
	 */
	@Override
	protected IPreferenceStore getPluginPreferenceStore()
	{
		return ERBEditorPlugin.getDefault().getPreferenceStore();
	}

	protected String getFileServiceContentTypeId()
	{
		return IERBConstants.CONTENT_TYPE_XML_ERB;
	}
}
