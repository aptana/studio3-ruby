/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;

import com.aptana.core.epl.IMemento;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.ruby.debug.core.launching.IRubyLaunchConfigurationConstants;
import com.aptana.ruby.debug.core.launching.InterruptingProcessFactory;
import com.aptana.webserver.core.AbstractWebServer;
import com.aptana.webserver.core.URLtoURIMapper;

public class RailsServer extends AbstractWebServer
{

	public static final String TYPE_ID = "org.radrails.rails.railsServer"; //$NON-NLS-1$

	/**
	 * Default values for IP/binding and port.
	 */
	public static final String DEFAULT_BINDING = "0.0.0.0"; //$NON-NLS-1$
	public static final int DEFAULT_PORT = 3000;

	private static final String RAILS = "rails"; //$NON-NLS-1$
	private static final String SERVER = "server"; //$NON-NLS-1$
	private static final String SCRIPT = "script"; //$NON-NLS-1$

	/**
	 * Properties to persist that user can change
	 */
	private static final String ELEMENT_PORT = "port"; //$NON-NLS-1$
	private static final String ELEMENT_HOST = "host"; //$NON-NLS-1$
	private static final String ELEMENT_PROJECT = "project"; //$NON-NLS-1$

	private IProject fProject;
	private String fMode;
	private ILaunch fLaunch;
	private Integer fPort;
	private String fHost;

	public RailsServer()
	{
		super();
		this.fPort = DEFAULT_PORT;
		this.fHost = DEFAULT_BINDING;
	}

	public IStatus stop(boolean force, IProgressMonitor monitor)
	{
		// if there is no launch or it is terminated already, just make sure we're marked as stopped and return OK.
		if (getLaunch() == null || getLaunch().isTerminated())
		{
			updateState(State.STOPPED);
			return Status.OK_STATUS;
		}

		updateState(State.STOPPING);
		try
		{
			getLaunch().terminate();
			updateState(State.STOPPED);
		}
		catch (DebugException e)
		{
			updateState(State.STARTED);
			return new Status(IStatus.ERROR, RailsCorePlugin.PLUGIN_ID, Messages.RailsServer_StopFailedErrorMsg, e);
		}
		return Status.OK_STATUS;
	}

	public IStatus start(String mode, IProgressMonitor monitor)
	{
		// TODO Check to make sure this isn't already started?
		updateState(State.STARTING);
		try
		{
			ILaunchConfiguration config = findOrCreateLaunchConfiguration(fProject);
			if (config != null)
			{
				this.fMode = mode;
				this.fLaunch = config.launch(mode, monitor);
				DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new ILaunchesListener2()
				{

					public void launchesRemoved(ILaunch[] launches)
					{
						// do nothing
					}

					public void launchesChanged(ILaunch[] launches)
					{
						// do nothing
					}

					public void launchesAdded(ILaunch[] launches)
					{
						// do nothing
					}

					public void launchesTerminated(ILaunch[] launches)
					{
						for (ILaunch launch : launches)
						{
							if (launch.equals(fLaunch))
							{
								// TODO Remove ourselves as a listener?
								updateState(State.STOPPED);
								break;
							}
						}

					}
				});
				updateState(State.STARTED);
			}
		}
		catch (CoreException e)
		{
			updateState(State.STOPPED);
			return e.getStatus();
		}
		// We may throw a DebuggerNotFoundException which is a RuntimeException...
		catch (Exception e)
		{
			updateState(State.STOPPED);
			return new Status(IStatus.ERROR, RailsCorePlugin.PLUGIN_ID, e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	public String getMode()
	{
		return this.fMode;
	}

	public ILaunch getLaunch()
	{
		return fLaunch;
	}

	public IProcess[] getProcesses()
	{
		return getLaunch().getProcesses();
	}

	public String getHostname()
	{
		return this.fHost;
	}

	public int getPort()
	{
		return this.fPort;
	}

	public URI getDocumentRoot()
	{
		if (fProject == null)
		{
			return null;
		}
		IPath projectLocation = fProject.getLocation();
		if (projectLocation == null)
		{
			return null;
		}
		File file = projectLocation.append("public").toFile(); //$NON-NLS-1$
		if (file == null)
		{
			return null;
		}
		return file.toURI();
	}

	public URL getBaseURL()
	{
		try
		{
			return new URL("http", getHostname(), getPort(), StringUtil.EMPTY); //$NON-NLS-1$
		}
		catch (MalformedURLException e)
		{
			IdeLog.logError(RailsCorePlugin.getDefault(), e);
			return null;
		}
	}

	public URI resolve(IFileStore file)
	{
		return new URLtoURIMapper(getBaseURL(), getDocumentRoot()).resolve(file);
	}

	public IFileStore resolve(URI uri)
	{
		// TODO We need to implement a mapper that understands rails routing. Even a mapper that assumes
		// controller/action/id would be a good start...
		return new URLtoURIMapper(getBaseURL(), getDocumentRoot()).resolve(uri);
	}

	@Override
	public void loadState(IMemento memento)
	{
		super.loadState(memento);
		Integer port = memento.getInteger(ELEMENT_PORT);
		if (port != null)
		{
			this.fPort = port;
		}
		this.fHost = memento.getString(ELEMENT_HOST);
		String location = memento.getString(ELEMENT_PROJECT);
		if (location != null)
		{
			this.fProject = (IProject) ResourcesPlugin.getWorkspace().getRoot()
					.getContainerForLocation(Path.fromPortableString(location));
		}
		// TODO Sniff to see if server is actually already started?
	}

	@Override
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		memento.putInteger(ELEMENT_PORT, this.fPort);
		memento.putString(ELEMENT_HOST, this.fHost);
		if (this.fProject != null && this.fProject.getLocation() != null)
		{
			memento.putString(ELEMENT_PROJECT, this.fProject.getLocation().toPortableString());
		}
	}

	protected ILaunchConfiguration findOrCreateLaunchConfiguration(IProject railsProject) throws CoreException
	{
		StringBuilder args = new StringBuilder();
		String filename = StringUtil.EMPTY;
		if (scriptServerExists(railsProject))
		{
			IFile file = railsProject.getFile(new Path(SCRIPT).append(SERVER));
			filename = file.getLocation().toOSString();
		}
		else
		{
			IFile file = railsProject.getFile(new Path(SCRIPT).append(RAILS));
			filename = file.getLocation().toOSString();
			args.append(SERVER);
		}
		args.append(" --binding=").append(getHostname()); //$NON-NLS-1$
		args.append(" --port=").append(getPort()); //$NON-NLS-1$

		// Always generate a new launch config
		return createConfiguration(railsProject, filename, args.toString());
	}

	private ILaunchConfiguration createConfiguration(IProject project, String rubyFile, String args)
			throws CoreException
	{
		ILaunchConfigurationType configType = getRubyLaunchConfigType();
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
				.generateLaunchConfigurationName(project.getName()));
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_FILE_NAME, rubyFile);
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, project.getLocation().toOSString());
		wc.setAttribute(IRubyLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		wc.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
				IRubyLaunchConfigurationConstants.ID_RUBY_SOURCE_LOCATOR);
		wc.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
		wc.setAttribute("org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND", false); //$NON-NLS-1$
		wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, InterruptingProcessFactory.ID);
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

	protected boolean scriptServerExists(IProject railsProject)
	{
		IFile scriptServer = railsProject.getFile(new Path(SCRIPT).append(SERVER));
		return scriptServer != null && scriptServer.exists();
	}

	public void setPort(int port)
	{
		this.fPort = port;
	}

	public void setHost(String host)
	{
		this.fHost = host;
	}

	public void setProject(IProject project)
	{
		this.fProject = project;
	}

	public IProject getProject()
	{
		return this.fProject;
	}

	public Set<String> getAvailableModes()
	{
		return CollectionsUtil.newSet(ILaunchManager.RUN_MODE, ILaunchManager.DEBUG_MODE);
	}
}
