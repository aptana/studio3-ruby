package com.aptana.editor.haml.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.haml.preferences.messages"; //$NON-NLS-1$
	public static String HAMLPreferencePage_HAML_Editor_Title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
