/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.erb.Activator;
import com.aptana.editor.erb.html.RHTMLEditor;

public class ERBPreferencePage extends CommonEditorPreferencePage
{
	public ERBPreferencePage()
	{
		super();
		setDescription(Messages.ERBPreferencePage_ERB_page_title);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createMarkOccurrenceOptions(Composite parent)
	{
	}

	@Override
	protected IPreferenceStore getChainedEditorPreferenceStore()
	{
		return RHTMLEditor.getChainedPreferenceStore();
	}

	@Override
	protected IEclipsePreferences getPluginPreferenceStore()
	{
		return new InstanceScope().getNode(Activator.PLUGIN_ID);
	}

}
