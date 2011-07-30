/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.haml.HAMLEditor;
import com.aptana.editor.haml.HAMLEditorPlugin;

public class HAMLPreferencePage extends CommonEditorPreferencePage
{

	/**
	 * HamlPreferencesPage
	 */

	public HAMLPreferencePage()
	{
		super();
		setDescription(Messages.HAMLPreferencePage_HAML_Editor_Title);
		setPreferenceStore(HAMLEditorPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IEclipsePreferences getPluginPreferenceStore()
	{
		return EclipseUtil.instanceScope().getNode(HAMLEditorPlugin.PLUGIN_ID);
	}

	@Override
	protected IPreferenceStore getChainedEditorPreferenceStore()
	{
		return HAMLEditor.getChainedPreferenceStore();
	}

	@Override
	protected IEclipsePreferences getDefaultPluginPreferenceStore()
	{
		return EclipseUtil.defaultScope().getNode(HAMLEditorPlugin.PLUGIN_ID);
	}

	@Override
	protected boolean getDefaultSpacesForTabs()
	{
		return IHAMLPreferenceConstants.DEFAULT_HAML_SPACES_FOR_TABS;
	}

	@Override
	protected int getDefaultTabWidth()
	{
		return IHAMLPreferenceConstants.DEFAULT_HAML_TAB_WIDTH;
	}

}
