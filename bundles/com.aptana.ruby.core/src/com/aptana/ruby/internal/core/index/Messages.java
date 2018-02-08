/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.index;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.ruby.internal.core.index.messages"; //$NON-NLS-1$

	public static String CoreStubber_GatherLoadpathsMsg;

	public static String CoreStubber_GatherRubyInstallsMsg;

	public static String CoreStubber_GenerateActualStubsMsg;

	public static String CoreStubber_IndexingRuby;
	public static String CoreStubber_IndexingRubyCore;

	public static String CoreStubber_IndexSubTaskName;

	public static String CoreStubber_RubyFilesCheckMsg;

	public static String CoreStubber_TaskName;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
