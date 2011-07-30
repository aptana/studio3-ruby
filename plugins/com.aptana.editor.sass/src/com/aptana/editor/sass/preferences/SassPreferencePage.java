/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.sass.SassPlugin;
import com.aptana.editor.sass.SassSourceEditor;

public class SassPreferencePage extends CommonEditorPreferencePage
{

	/**
	 * SassPreferencePage
	 */

	public SassPreferencePage()
	{
		super();
		setDescription(Messages.SassPreferencePage_SASS_Page_Title);
		setPreferenceStore(SassPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IEclipsePreferences getPluginPreferenceStore()
	{
		return EclipseUtil.instanceScope().getNode(SassPlugin.PLUGIN_ID);
	}

	@Override
	protected IPreferenceStore getChainedEditorPreferenceStore()
	{
		return SassSourceEditor.getChainedPreferenceStore();
	}

	@Override
	protected IEclipsePreferences getDefaultPluginPreferenceStore()
	{
		return EclipseUtil.defaultScope().getNode(SassPlugin.PLUGIN_ID);
	}

	@Override
	protected boolean getDefaultSpacesForTabs()
	{
		return ISASSPreferenceConstants.DEFAULT_SASS_SPACES_FOR_TABS;
	}

	@Override
	protected int getDefaultTabWidth()
	{
		return ISASSPreferenceConstants.DEFAULT_SASS_TAB_WIDTH;
	}

}
