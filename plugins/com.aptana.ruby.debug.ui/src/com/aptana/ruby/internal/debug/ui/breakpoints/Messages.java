package com.aptana.ruby.internal.debug.ui.breakpoints;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{

	private static final String BUNDLE_NAME = "com.aptana.ruby.internal.debug.ui.breakpoints.messages"; //$NON-NLS-1$

	public static String AddExceptionAction_DefaultInput;
	public static String AddExceptionAction_ERR_EmptyInput;
	public static String AddExceptionAction_InputMessage;
	public static String AddExceptionAction_InputTitle;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
