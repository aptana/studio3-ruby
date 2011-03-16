package com.aptana.editor.ruby.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.aptana.editor.ruby.RubyEditorPlugin;

public class RubyPreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new DefaultScope().getNode(RubyEditorPlugin.PLUGIN_ID);
		// Force standard ruby indent/spaces. 2 spaces for indent, not tabs, not 4 spaces.
		prefs.putInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 2);
		prefs.putBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, true);
	}

}
