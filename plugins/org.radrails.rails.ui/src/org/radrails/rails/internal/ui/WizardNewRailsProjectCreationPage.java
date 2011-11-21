/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.core.projects.templates.IProjectTemplate;
import com.aptana.ruby.ui.wizards.WizardNewRubyProjectCreationPage;

public class WizardNewRailsProjectCreationPage extends WizardNewRubyProjectCreationPage
{

	private static final String ICON_WARNING = "icons/warning_48.png"; //$NON-NLS-1$

	// widgets
	private Button runGenerator;
	private CLabel projectGenerationErrorLabel;
	private boolean hasRailsAppFiles;

	private Composite projectGenerationControls;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public WizardNewRailsProjectCreationPage(String pageName, IProjectTemplate projectTemplate)
	{
		super(pageName, projectTemplate);
	}

	@Override
	protected Composite createGenerateGroup(Composite parent)
	{
		Composite projectGenerationGroup = super.createGenerateGroup(parent);

		// Create an error label that we'll display in a case where the project
		// is created in a location that contains a Rails project files.
		projectGenerationErrorLabel = new CLabel(projectGenerationGroup, SWT.WRAP);
		projectGenerationErrorLabel.setText(Messages.WizardNewRailsProjectCreationPage_cannotCreateProjectMessage);
		projectGenerationErrorLabel.setImage(RailsUIPlugin.getImage(ICON_WARNING));

		return projectGenerationGroup;
	}

	protected void createGenerationOptions(Composite projectGenerationControls)
	{
		runGenerator = new Button(projectGenerationControls, SWT.RADIO);
		runGenerator.setText(Messages.WizardNewRailsProjectCreationPage_StandardGeneratorText);

		super.createGenerationOptions(projectGenerationControls);

		noGenerator.setSelection(false);
		runGenerator.setSelection(true);

		this.projectGenerationControls = projectGenerationControls;
	}

	@Override
	protected void selectGitCloneGeneration()
	{
		super.selectGitCloneGeneration();
		runGenerator.setSelection(false);
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage()
	{
		if (!super.validatePage())
		{
			return false;
		}

		// Validate that there is no Rails project already existing in the
		// new project location
		hasRailsAppFiles = hasRailsApp(getLocationText());
		if (hasRailsAppFiles)
		{
			setTopControl(projectGenerationErrorLabel);
		}
		else
		{
			setTopControl(projectGenerationControls);
		}
		return true;
	}

	private boolean hasRailsApp(String path)
	{
		File projectFile = new File(path);
		File env = new File(projectFile, "config" + File.separator + "environment.rb"); //$NON-NLS-1$ //$NON-NLS-2$
		return env.exists();
	}

	protected boolean runGenerator()
	{
		return !hasRailsAppFiles && runGenerator.getSelection();
	}
}
