/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.projects.WebProjectNature;
import com.aptana.projects.wizards.AbstractNewProjectWizard;
import com.aptana.ruby.core.RubyProjectNature;
import com.aptana.ruby.ui.RubyUIPlugin;

/**
 * Ruby project wizard.
 * 
 * @author cwilliams, sgibly
 */
public class NewRubyProjectWizard extends AbstractNewProjectWizard implements IExecutableExtension
{
	private boolean cloneFromGit;
	private String cloneSourceURI;

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

	protected WizardNewRubyProjectCreationPage createMainPage()
	{
		WizardNewRubyProjectCreationPage mainPage = new WizardNewRubyProjectCreationPage(
				"basicNewProjectPage", selectedTemplate); //$NON-NLS-1$
		mainPage.setTitle(Messages.NewProject_title);
		mainPage.setDescription(Messages.NewRubyProject_description);
		mainPage.setWizard(this);
		mainPage.setPageComplete(false);
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

	@Override
	protected String[] getProjectNatures()
	{
		return new String[] { RubyProjectNature.ID, WebProjectNature.ID };
	}

	@Override
	protected String getProjectCreateEventName()
	{
		return "project.create.ruby"; //$NON-NLS-1$
	}

	@Override
	protected TemplateType[] getProjectTemplateTypes()
	{
		return new TemplateType[] { TemplateType.RUBY };
	}

	@Override
	protected String[] getProjectBuilders()
	{
		return new String[] { UnifiedBuilder.ID };
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
		setWindowTitle(Messages.NewRubyProject_windowTitle);
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
		return cloneFromGit || super.isCloneFromGit();
	}

	@Override
	public boolean performFinish()
	{
		cloneFromGit = mainPage.isCloneFromGit();
		cloneSourceURI = mainPage.getCloneURI();
		return super.performFinish();
	}

	@Override
	protected void cloneFromGit(IProject newProjectHandle, IProjectDescription description, IProgressMonitor monitor)
			throws InvocationTargetException
	{
		// We override the default implementation since this project wizard may define a git clone location even without
		// any project templates.
		if (cloneFromGit)
		{
			doCloneFromGit(cloneSourceURI, newProjectHandle, description, monitor);
		}
		else
		{
			// The super cloneFromGit will deal with the project template cloning
			super.cloneFromGit(newProjectHandle, description, monitor);
		}
	}
	}
