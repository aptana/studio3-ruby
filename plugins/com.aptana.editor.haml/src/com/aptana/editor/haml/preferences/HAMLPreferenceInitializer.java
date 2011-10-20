/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.haml.HAMLEditorPlugin;

public class HAMLPreferenceInitializer extends AbstractPreferenceInitializer
{
	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = EclipseUtil.defaultScope().getNode(HAMLEditorPlugin.PLUGIN_ID);
		prefs.putBoolean(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_ENABLE_FOLDING, true);
		prefs.putBoolean(IPreferenceConstants.EDITOR_AUTO_INDENT, true);

		// mark occurrences
		// prefs.putBoolean(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES, true);

		// Check if we previously set preference to use global defaults
		IEclipsePreferences instanceScopePref = EclipseUtil.instanceScope().getNode(HAMLEditorPlugin.PLUGIN_ID);
		if (!instanceScopePref.getBoolean(IPreferenceConstants.USE_GLOBAL_DEFAULTS, false))
		{
			prefs.putInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH,
					IHAMLPreferenceConstants.DEFAULT_HAML_TAB_WIDTH);
			prefs.putBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
					IHAMLPreferenceConstants.DEFAULT_HAML_SPACES_FOR_TABS);
		}
	}
}
