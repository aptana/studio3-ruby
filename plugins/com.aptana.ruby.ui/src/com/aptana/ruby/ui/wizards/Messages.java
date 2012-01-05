/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

	public static String NewProject_title;
	public static String NewProject_jobTitle;
	public static String NewProject_caseVariantExistsError;
	public static String NewProject_errorMessage;
	public static String NewProject_internalError;

	public static String NewRubyProject_windowTitle;
	public static String NewRubyProject_description;
	public static String NewRubyProject_stepName;

	public static String ProjectLocationSelectionDialog_locationLabel;

	public static String WizardNewProjectCreationPage_CloneGitRepoLabel;
	public static String WizardNewProjectCreationPage_GenerateAppGroupLabel;
	public static String WizardNewProjectCreationPage_nameLabel;
	public static String WizardNewProjectCreationPage_NoGeneratorText;
	public static String WizardNewProjectCreationPage_NoGeneratorText2;
	public static String WizardNewProjectCreationPage_projectNameEmpty;
	public static String WizardNewProjectCreationPage_projectExistsMessage;
	public static String WizardNewProjectCreationPage_projectLocationEmpty;
	public static String WizardNewProjectCreationPage_gitLocationEmpty;
	public static String WizardNewProjectCreationPage_location_has_existing_content_warning;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
