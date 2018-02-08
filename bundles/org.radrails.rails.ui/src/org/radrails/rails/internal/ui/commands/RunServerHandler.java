/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.radrails.rails.core.RailsServer;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.core.IFilter;
import com.aptana.core.logging.IdeLog;
import com.aptana.webserver.core.IServer;
import com.aptana.webserver.core.IServerManager;
import com.aptana.webserver.core.WebServerCorePlugin;

/**
 * @author cwilliams
 */
public class RunServerHandler extends AbstractRailsHandler
{
	protected String getMode()
	{
		return ILaunchManager.RUN_MODE;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IProject railsProject = getProject(event);
		if (railsProject == null)
		{
			return null;
		}

		RailsServer server = findOrCreateServer(railsProject);
		if (server == null)
		{
			return null;
		}

		String mode = getMode();
		if (server.getState() == IServer.State.STARTED)
		{
			// if already started in our target mode, restart
			if (mode.equals(server.getMode()))
			{
				// restart
				server.restart(mode, new NullProgressMonitor());
				return null;
			}

			// Already started, but in a different mode, so stop first
			server.stop(true, new NullProgressMonitor());
		}

		// start in target mode.
		server.start(mode, new NullProgressMonitor());
		return null;
	}

	private RailsServer findOrCreateServer(final IProject railsProject)
	{
		// find all the servers for this project
		List<IServer> servers = getServerManager().getServers(new IFilter<IServer>()
		{

			public boolean include(IServer item)
			{
				if (item instanceof RailsServer)
				{
					RailsServer possible = (RailsServer) item;
					if (railsProject.equals(possible.getProject()))
					{
						return true;
					}
				}
				return false;
			}
		});
		// If there are any, return the first one
		if (!servers.isEmpty())
		{
			return (RailsServer) servers.get(0);
		}

		// No matching server. We need to create one
		return addServer(railsProject);
	}

	protected RailsServer addServer(final IProject project)
	{
		try
		{
			IServerManager serverManager = getServerManager();
			RailsServer server = (RailsServer) serverManager.createServer(RailsServer.TYPE_ID);
			server.setProject(project);
			server.setName(project.getName());
			serverManager.add(server);
			return server;
		}
		catch (CoreException e)
		{
			IdeLog.logError(RailsUIPlugin.getDefault(), "Error adding server for Rails project", e); //$NON-NLS-1$
		}
		return null;
	}

	protected IServerManager getServerManager()
	{
		return WebServerCorePlugin.getDefault().getServerManager();
	}
}
