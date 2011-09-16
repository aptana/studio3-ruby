/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.UIJob;
import org.radrails.rails.core.RailsCorePlugin;
import org.radrails.rails.core.RailsProjectNature;

import com.aptana.core.ShellExecutable;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.ruby.core.RubyProjectNature;
import com.aptana.ruby.ui.wizards.NewRubyProjectWizard;
import com.aptana.ruby.ui.wizards.WizardNewRubyProjectCreationPage;
import com.aptana.terminal.views.TerminalView;
import com.aptana.usage.FeatureEvent;
import com.aptana.usage.StudioAnalytics;

/**
 * Rails project wizard
 * 
 * @author cwilliams, sgibly
 */
public class NewRailsProjectWizard extends NewRubyProjectWizard
{

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ruby.ui.wizards.NewRubyProjectWizard#createMainPage()
	 */
	@Override
	protected WizardNewRubyProjectCreationPage createMainPage()
	{
		WizardNewRailsProjectCreationPage mainPage = new WizardNewRailsProjectCreationPage("basicNewProjectPage"); //$NON-NLS-1$
		mainPage.setTitle(Messages.NewProject_title);
		mainPage.setDescription(Messages.NewRailsProject_description);
		return mainPage;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection)
	{
		super.init(workbench, currentSelection);
		setWindowTitle(Messages.NewRailsProject_windowTitle);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ruby.ui.wizards.NewRubyProjectWizard#getProjectNatures()
	 */
	@Override
	protected String[] getProjectNatures()
	{
		return new String[] { RailsProjectNature.ID, RubyProjectNature.ID };
	}

	protected TemplateType[] getTemplateTypes()
	{
		return new TemplateType[] { TemplateType.RAILS };
	}

	@Override
	public boolean performFinish()
	{
		// HACK I have to query for this here, because otherwise when we generate the project somehow the fields get
		// focus and that auto changes the radio selection value for generation
		IWizardPage page = getStartingPage();
		boolean runGenerator = false;
		if (page instanceof WizardNewRailsProjectCreationPage)
		{
			WizardNewRailsProjectCreationPage railsPage = (WizardNewRailsProjectCreationPage) page;
			runGenerator = railsPage.runGenerator();
		}

		if (!super.performFinish())
		{
			return false;
		}

		if (runGenerator)
			runGenerator();

		return true;
	}

	private void runGenerator()
	{
		// Pop open a confirmation dialog if the project already has a config/environment.rb file!
		final IProject project = mainPage.getProjectHandle();
		File projectFile = project.getLocation().toFile();
		File env = new File(projectFile, "config" + File.separator + "environment.rb"); //$NON-NLS-1$ //$NON-NLS-2$
		if (env.exists())
		{
			if (!MessageDialog.openConfirm(getShell(), Messages.NewProjectWizard_ContentsAlreadyExist_Title,
					Messages.NewProjectWizard_ContentsAlreadyExist_Msg))
				return;
		}
		Job job = new UIJob(Messages.NewProjectWizard_JobTitle)
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
				if (subMonitor.isCanceled())
					return Status.CANCEL_STATUS;

				// Now launch the rails command in a terminal!
				TerminalView terminal = TerminalView.openView(project.getName(), project.getName(),
						project.getLocation());
				String input = "rails .\n"; //$NON-NLS-1$
				if (requiresNewArgToGenerateApp(project))
				{
					input = "rails new .\n"; //$NON-NLS-1$
				}
				terminal.sendInput(input);

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	/**
	 * To generate an app, you need to use 'rails new APP_NAME'
	 * 
	 * @param project
	 * @return
	 */
	@SuppressWarnings("nls")
	protected boolean requiresNewArgToGenerateApp(IProject project)
	{
		IStatus result = RailsCorePlugin.runRailsInBackground(project.getLocation(), ShellExecutable.getEnvironment(),
				"-v");
		if (result == null || !result.isOK())
		{
			return false;
		}

		String version = result.getMessage();
		String[] parts = version.split("\\s");
		String lastPart = parts[parts.length - 1];
		if (lastPart.startsWith("1") || lastPart.startsWith("2"))
		{
			return false;
		}
		if (lastPart.startsWith("3.0.0beta"))
		{
			return lastPart.endsWith("beta4");
		}
		return true;
	}
	
	@Override
	protected void sendProjectCreateEvent(Map<String, String> payload)
	{
		StudioAnalytics.getInstance().sendEvent(new FeatureEvent("project.create.rails", payload)); //$NON-NLS-1$
	}
}
