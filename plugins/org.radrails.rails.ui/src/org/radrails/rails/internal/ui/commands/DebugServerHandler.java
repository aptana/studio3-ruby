/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.console.process.ConsoleProcessFactory;
import com.aptana.ruby.debug.core.launching.IRubyLaunchConfigurationConstants;

public class DebugServerHandler extends RunServerHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IProject railsProject = getProject(event);
		if (railsProject == null)
		{
			return null;
		}

		try
		{
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(railsProject);
			if (config != null)
			{
				DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
			}
		}
		catch (CoreException e)
		{
			RailsUIPlugin.logError(e);
		}
		return null;
	}

	@SuppressWarnings("nls")
	protected ILaunchConfiguration findOrCreateLaunchConfiguration(IProject railsProject) throws CoreException
	{
		String arguments = "";
		String filename = "";
		if (scriptServerExists(railsProject))
		{
			IFile file = railsProject.getFile(new Path("script").append("server"));
			filename = file.getLocation().toOSString();
			arguments = "";
		}
		else
		{
			IFile file = railsProject.getFile(new Path("script").append("rails"));
			filename = file.getLocation().toOSString();
			arguments = "server";
		}

		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		List<ILaunchConfiguration> candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
		for (ILaunchConfiguration config : configs)
		{
			boolean absoluteFilenamesMatch = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, "") //$NON-NLS-1$
					.equals(filename);
			boolean argsMatch = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "") //$NON-NLS-1$
					.equals(arguments);
			if (absoluteFilenamesMatch && argsMatch)
			{
				candidateConfigs.add(config);
			}
		}

		switch (candidateConfigs.size())
		{
			case 0:
				return createConfiguration(railsProject, filename, arguments);
			case 1:
				return candidateConfigs.get(0);
			default:
				Status status = new Status(Status.WARNING, RailsUIPlugin.getPluginIdentifier(), 0,
						"Multiple configurations match", null); //$NON-NLS-1$
				throw new CoreException(status);
		}
	}

	@SuppressWarnings("deprecation")
	private ILaunchConfiguration createConfiguration(IProject project, String rubyFile, String args)
			throws CoreException
	{
		// TODO Combine with RubyApplicationShortcut?
		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
				.generateUniqueLaunchConfigurationNameFrom(project.getName()));
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rubyFile);
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, project.getLocation().toOSString());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
				IRubyLaunchConfigurationConstants.ID_RUBY_SOURCE_LOCATOR);
		wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, ConsoleProcessFactory.ID);
		return wc.doSave();
	}

	private ILaunchConfigurationType getRubyLaunchConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	private ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}

}
