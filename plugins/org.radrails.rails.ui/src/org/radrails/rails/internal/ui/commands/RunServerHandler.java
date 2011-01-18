/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.commands;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

import com.aptana.terminal.views.TerminalView;

public class RunServerHandler extends AbstractRailsHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IProject railsProject = getProject(event);
		if (railsProject == null)
		{
			return null;
		}
		// now determine which version so we can tell what to run...
		String viewId = MessageFormat.format("{0} script/rails server", railsProject //$NON-NLS-1$
				.getName());
		String command = "script/rails server"; //$NON-NLS-1$
		if (scriptServerExists(railsProject))
		{
			viewId = MessageFormat.format("{0} script/server", railsProject //$NON-NLS-1$
					.getName());
			command = "script/server"; //$NON-NLS-1$
		}
		// Now do the launch in terminal
		TerminalView term = TerminalView.openView(viewId, viewId, railsProject.getLocation());
		if (term != null)
		{
			term.sendInput(command + '\n');
		}
		return null;
	}

	protected boolean scriptServerExists(IProject railsProject)
	{
		IFile scriptServer = railsProject.getFile(new Path("script").append("server")); //$NON-NLS-1$ //$NON-NLS-2$
		return scriptServer != null && scriptServer.exists();
	}

}
