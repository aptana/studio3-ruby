package com.aptana.ruby.internal.debug.core.breakpoints;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.ruby.internal.debug.core.breakpoints.messages"; //$NON-NLS-1$
	public static String RubyLineBreakpoint_Message;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
