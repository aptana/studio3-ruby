package com.aptana.ruby.debug.core.launching;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.ruby.debug.core.launching.messages"; //$NON-NLS-1$
	public static String InterruptingProcessFactory_UnableToGrabPIDMsg;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
