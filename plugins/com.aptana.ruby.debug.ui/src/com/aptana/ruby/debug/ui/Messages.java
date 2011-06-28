package com.aptana.ruby.debug.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.ruby.debug.ui.messages"; //$NON-NLS-1$
	public static String RubyModelPresentation_UnknownName;
	public static String RubyModelPresentation_UnknownType;
	public static String RubyModelPresentation_UnknwonValue;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
