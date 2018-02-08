/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.aptana.ruby.internal.rake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.aptana.core.ShellExecutable;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.ProcessUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.ruby.debug.core.launching.IRubyLaunchConfigurationConstants;
import com.aptana.ruby.launching.RubyLaunchingPlugin;
import com.aptana.ruby.rake.IRakeHelper;
import com.aptana.ruby.rake.RakePlugin;

/**
 * @author mkent
 * @author cwilliams
 */
public class RakeTasksHelper implements IRakeHelper
{

	/**
	 * If we fail to find a ruby executable on PATH, use this command as last ditch try at running something under ruby
	 * intrerpreter.
	 */
	private static final String RUBY_EXE_NAME = RubyLaunchingPlugin.RUBY;

	/**
	 * Command line switch used to have rake list the available tasks that can be run.
	 */
	private static final String TASK_LIST_SWITCH = "--tasks"; //$NON-NLS-1$

	/**
	 * Regexp used to extract the task name and descriptions from the output of rake with the {@link #TASK_LIST_SWITCH}
	 */
	private static final Pattern RAKE_TASK_PATTERN = Pattern.compile("^rake\\s+([\\w:]+)\\s+#\\s+(.+)$"); //$NON-NLS-1$

	/**
	 * Special param used by rake to modify the ENV used when launching.
	 */
	private static final String ENV_MODIFYING_PARAM = "RAILS_ENV"; //$NON-NLS-1$

	/**
	 * Cache the task list, since rake is slow in listing tasks (~1.8s for me on Ruby 1.9)
	 */
	private Map<IProject, Map<String, String>> fCachedTasks = new HashMap<IProject, Map<String, String>>();

	private IStatus runRakeInBackground(IProject project, IProgressMonitor monitor, String... arguments)
	{
		if (monitor == null)
		{
			monitor = new NullProgressMonitor();
		}

		if (monitor.isCanceled())
		{
			return Status.CANCEL_STATUS;
		}

		IPath wd = getWorkingDirectory(project);
		IPath rubyExe = RubyLaunchingPlugin.rubyExecutablePath(wd);
		Map<String, String> env;
		if (!Platform.OS_WIN32.equals(Platform.getOS()))
		{
			env = ShellExecutable.getEnvironment(wd);
		}
		else
		{
			env = new HashMap<String, String>();
		}
		env = modifyEnv(env, arguments);

		List<String> args = new ArrayList<String>();
		args.add(RakePlugin.getDefault().getRakePath(project));
		// TODO Enforce at least one argument minimum (task name)?
		if (arguments != null)
		{
			for (String param : arguments)
			{
				args.add(param);
			}
		}
		return ProcessUtil.runInBackground((rubyExe == null) ? RUBY_EXE_NAME : rubyExe.toOSString(), wd, env,
				args.toArray(new String[args.size()]));
	}

	private IPath getWorkingDirectory(IProject project)
	{
		if (project == null)
		{
			return null;
		}
		try
		{
			RakeFileFinder finder = new RakeFileFinder();
			project.accept(finder, IResource.NONE);
			IPath workingDir = finder.getWorkingDirectory();
			if (workingDir != null)
			{
				return project.getLocation().append(workingDir);
			}
			if (IdeLog.isWarningEnabled(RakePlugin.getDefault(), null))
			{
				IdeLog.logWarning(RakePlugin.getDefault(),
						"Failed to find parent of Rakefile to use as working dir for project: " + project.getName()); //$NON-NLS-1$
			}
		}
		catch (CoreException e)
		{
			RakePlugin.log(e);
		}
		return project.getLocation();
	}

	/**
	 * Gets the rake tasks for the passed in project
	 * 
	 * @param project
	 *            The IProject to gather rake tasks for
	 * @return a Map of rake task names to their descriptions
	 */
	public Map<String, String> getTasks(IProject project, IProgressMonitor monitor)
	{
		return getTasks(project, false, monitor);
	}

	/**
	 * Gets the rake tasks for the passed in project
	 * 
	 * @param project
	 *            The IProject to gather rake tasks for
	 * @param force
	 *            Whether or not to force a refresh (don't grab cached value)
	 * @return a Map of rake task names to their descriptions
	 */
	public Map<String, String> getTasks(IProject project, boolean force, IProgressMonitor monitor)
	{
		if (!force && fCachedTasks.containsKey(project))
		{
			return Collections.unmodifiableMap(fCachedTasks.get(project));
		}

		if (monitor != null && monitor.isCanceled())
		{
			return Collections.emptyMap();
		}
		BufferedReader bufReader = null;
		try
		{
			bufReader = new BufferedReader(new StringReader(getTasksText(project)));
			String line = null;
			Map<String, String> tasks = new HashMap<String, String>();
			while ((line = bufReader.readLine()) != null) // $codepro.audit.disable assignmentInCondition
			{
				Matcher mat = RAKE_TASK_PATTERN.matcher(line);
				if (mat.matches())
				{
					tasks.put(mat.group(1), mat.group(2));
				}
			}
			fCachedTasks.put(project, tasks);
		}
		catch (IOException e)
		{
			RakePlugin.log("Error parsing rake tasks", e); //$NON-NLS-1$
			return Collections.emptyMap();
		}
		finally
		{
			if (bufReader != null)
			{
				try
				{
					bufReader.close();
				}
				catch (final IOException e) // $codepro.audit.disable emptyCatchClause
				{
					// ignore
				}
			}
		}
		return fCachedTasks.get(project);
	}

	private String getTasksText(IProject project)
	{
		IStatus status = runRakeInBackground(project, new NullProgressMonitor(), TASK_LIST_SWITCH);
		if (status.isOK())
		{
			return status.getMessage();
		}
		return StringUtil.EMPTY;
	}

	public IStatus runRake(IProject project, IProgressMonitor monitor, String... arguments)
	{
		try
		{
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(project, arguments);
			if (config != null)
			{
				// FIXME Must call this in the UI thread!
				DebugUITools.launch(config, ILaunchManager.RUN_MODE);
			}
			else
			{
				return new Status(IStatus.ERROR, RakePlugin.PLUGIN_ID, MessageFormat.format(
						Messages.RakeTasksHelper_LaunchGenerationFailed, project, arguments));
			}
		}
		catch (CoreException e)
		{
			RakePlugin.log(e);
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	private Map<String, String> modifyEnv(Map<String, String> env, String... arguments)
	{
		Map<String, String> modified = new HashMap<String, String>(env);
		if (arguments != null)
		{
			for (String param : arguments)
			{
				if (param.contains(ENV_MODIFYING_PARAM + "=")) //$NON-NLS-1$
				{
					String value = param.substring(param.indexOf(ENV_MODIFYING_PARAM + "=") + 10); //$NON-NLS-1$
					if (value.indexOf(' ') != -1)
					{
						value = value.substring(0, value.indexOf(' '));
					}
					modified.put(ENV_MODIFYING_PARAM, value);
				}
			}
		}
		return modified;
	}

	@SuppressWarnings("rawtypes")
	private ILaunchConfiguration findOrCreateLaunchConfiguration(IProject project, String... arguments)
			throws CoreException
	{
		String rakeScriptPath = RakePlugin.getDefault().getRakePath(project);
		IPath wd = getWorkingDirectory(project);

		Map<String, String> env = modifyEnv(new HashMap<String, String>(), arguments);
		StringBuilder args = new StringBuilder();
		if (arguments != null && arguments.length > 0)
		{
			for (String argument : arguments)
			{
				args.append(argument).append(' ');
			}
			// delete last space
			args.deleteCharAt(args.length() - 1);
		}

		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		List<ILaunchConfiguration> candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
		for (ILaunchConfiguration config : configs)
		{
			boolean absoluteFilenamesMatch = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME,
					StringUtil.EMPTY).equals(rakeScriptPath);
			if (!absoluteFilenamesMatch)
			{
				continue;
			}
			boolean argsMatch = config.getAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
					StringUtil.EMPTY).equals(args.toString());
			if (!argsMatch)
			{
				continue;
			}
			boolean envMatches = env.equals(config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null));
			if (!envMatches)
			{
				continue;
			}
			candidateConfigs.add(config);
		}

		switch (candidateConfigs.size())
		{
			case 0:
				return createConfiguration(project, wd, rakeScriptPath, args.toString(), env);
			case 1:
				return candidateConfigs.get(0);
			default:
				Status status = new Status(Status.WARNING, RakePlugin.PLUGIN_ID, 0,
						"Multiple configurations match", null); //$NON-NLS-1$
				throw new CoreException(status);
		}
	}

	private ILaunchConfiguration createConfiguration(IProject project, IPath workingDir, String rubyFile, String args,
			Map<String, String> env) throws CoreException
	{
		// TODO Combine this into some utility method somewhere?
		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
				.generateLaunchConfigurationName(MessageFormat.format("{0} rake {1}", project.getName(), args))); //$NON-NLS-1$
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rubyFile);
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDir.toOSString());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
				IRubyLaunchConfigurationConstants.ID_RUBY_SOURCE_LOCATOR);
		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, env);
		return wc.doSave();
	}

	protected ILaunchConfigurationType getRubyLaunchConfigType()
	{
		return getLaunchManager().getLaunchConfigurationType(IRubyLaunchConfigurationConstants.ID_RUBY_APPLICATION);
	}

	protected ILaunchManager getLaunchManager()
	{
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
