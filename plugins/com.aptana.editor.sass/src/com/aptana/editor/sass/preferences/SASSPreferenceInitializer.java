/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.sass.Activator;

public class SASSPreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new DefaultScope().getNode(Activator.PLUGIN_ID);
		// Force standard sass indent/spaces. 2 spaces for indent, not tabs, not 4 spaces.
		prefs.putInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH,
				ISASSPreferenceConstants.DEFAULT_SASS_TAB_WIDTH);
		prefs.putBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
				ISASSPreferenceConstants.DEFAULT_SASS_SPACES_FOR_TABS);
		prefs.putBoolean(IPreferenceConstants.EDITOR_AUTO_INDENT, true);
	}

}
