/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.radrails.rails.internal.ui.messages"; //$NON-NLS-1$

	public static String NewProjectWizard_ContentsAlreadyExist_Msg;
	public static String NewProjectWizard_ContentsAlreadyExist_Title;
	public static String NewProjectWizard_JobTitle;

	public static String NewProject_title;

	public static String NewRailsProject_description;
	public static String NewRailsProject_windowTitle;

	public static String WizardNewRailsProjectCreationPage_StandardGeneratorText;
	public static String WizardNewRailsProjectCreationPage_cannotCreateProjectMessage;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
