/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.ruby.RubyEditorPlugin;

public class RubyPreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(RubyEditorPlugin.PLUGIN_ID);
		prefs.putBoolean(IPreferenceConstants.EDITOR_AUTO_INDENT, true);
		prefs.putBoolean(IPreferenceConstants.EDITOR_ENABLE_FOLDING, true);
		prefs.put(
				com.aptana.editor.common.contentassist.IPreferenceConstants.COMPLETION_PROPOSAL_ACTIVATION_CHARACTERS,
				".$@:"); //$NON-NLS-1$

		// mark occurrences
		// prefs.putBoolean(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES, true);

		// Check if we previously set preference to use global defaults
		IEclipsePreferences instanceScopePref = InstanceScope.INSTANCE.getNode(RubyEditorPlugin.PLUGIN_ID);
		if (!instanceScopePref.getBoolean(IPreferenceConstants.USE_GLOBAL_DEFAULTS, false))
		{
			prefs.putInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH,
					IRubyPreferenceConstants.DEFAULT_RUBY_TAB_WIDTH);
			prefs.putBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
					IRubyPreferenceConstants.DEFAULT_RUBY_SPACES_FOR_TABS);
		}
	}

}
