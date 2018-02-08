package org.radrails.rails.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.radrails.rails.internal.messages"; //$NON-NLS-1$
	public static String RailsServer_StopFailedErrorMsg;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
