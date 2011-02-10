package com.aptana.editor.ruby.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.ruby.preferences.messages"; //$NON-NLS-1$
	public static String RubyPreferencePage_Ruby_Page_Title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
