package org.radrails.rails.internal.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.radrails.rails.internal.ui.dialogs.messages"; //$NON-NLS-1$
	public static String RailsServerDialog_BindingLabel;
	public static String RailsServerDialog_EmptyNameErrorMsg;
	public static String RailsServerDialog_InvalidPortErrorMsg;
	public static String RailsServerDialog_Message;
	public static String RailsServerDialog_NameLabel;
	public static String RailsServerDialog_PortLabel;
	public static String RailsServerDialog_ProjectLabel;
	public static String RailsServerDialog_Title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
