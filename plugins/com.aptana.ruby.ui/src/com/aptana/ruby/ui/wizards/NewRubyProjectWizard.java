/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import com.aptana.core.projects.templates.IProjectTemplate;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.projects.WebProjectNature;
import com.aptana.projects.internal.wizards.NewProjectWizard;
import com.aptana.projects.internal.wizards.ProjectTemplateSelectionPage;
import com.aptana.ruby.core.RubyProjectNature;
import com.aptana.ruby.ui.RubyUIPlugin;

/**
 * Ruby project wizard.
 * 
 * @author cwilliams, sgibly
 */
public class NewRubyProjectWizard extends NewProjectWizard implements IExecutableExtension
{
	protected static final String TEMPLATE_SELECTION_PAGE_NAME = "templateSelectionPage"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#initDialogSettings()
	 */
	@Override
	protected void initDialogSettings()
	{
		IDialogSettings workbenchSettings = RubyUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		if (section == null)
		{
			section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	@Override
	public void addPages()
	{
		mainPage = createMainPage();
		mainPage.setWizard(this);
		mainPage.setPageComplete(false);
		addPage(mainPage);

		// project templates
		List<IProjectTemplate> templates = getProjectTemplates(getTemplateTypes());
		if (templates.size() > 0)
		{
			addPage(templatesPage = new ProjectTemplateSelectionPage(TEMPLATE_SELECTION_PAGE_NAME, templates));
		}
	}

	protected TemplateType[] getTemplateTypes()
	{
		return new TemplateType[] { TemplateType.RUBY };
	}

	protected WizardNewRubyProjectCreationPage createMainPage()
	{
		WizardNewRubyProjectCreationPage mainPage = new WizardNewRubyProjectCreationPage("basicNewProjectPage"); //$NON-NLS-1$
		mainPage.setTitle(Messages.NewProject_title);
		mainPage.setDescription(Messages.NewRubyProject_description);
		return mainPage;
	}

	/*
	 * (non-Javadoc) Method declared on BasicNewResourceWizard.
	 */
	protected void initializeDefaultPageImageDescriptor()
	{
		ImageDescriptor desc = RubyUIPlugin.imageDescriptorFromPlugin(RubyUIPlugin.getPluginIdentifier(),
				"icons/newproj_wiz.png"); //$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#getProjectCreationDescription()
	 */
	protected String getProjectCreationDescription()
	{
		return Messages.NewRubyProject_windowTitle;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection)
	{
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(Messages.NewRubyProject_windowTitle);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#getProjectNatures()
	 */
	@Override
	protected String[] getProjectNatures()
	{
		return new String[] { RubyProjectNature.ID, WebProjectNature.ID };
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#shouldCloneFromGit()
	 */
	@Override
	protected boolean isCloneFromGit()
	{
		// First check the main page specific settings for the git clone. In case the setting is off, check the super
		// for any template that requires a clone.
		boolean cloneFromGit = mainPage.isCloneFromGit();
		return cloneFromGit || super.isCloneFromGit();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.projects.internal.wizards.NewProjectWizard#cloneFromGit(org.eclipse.core.resources.IProject,
	 * org.eclipse.core.resources.IProjectDescription)
	 */
	@Override
	protected void cloneFromGit(IProject newProjectHandle, IProjectDescription description)
	{
		// We override the default implementation since this project wizard may define a git clone location even without
		// any project templates.
		if (mainPage.isCloneFromGit())
		{
			String sourceURI = mainPage.getCloneURI();
			doCloneFromGit(sourceURI, newProjectHandle, description);
		}
		else
		{
			// The super cloneFromGit will deal with the project template cloning
			super.cloneFromGit(newProjectHandle, description);
		}
	}
}
