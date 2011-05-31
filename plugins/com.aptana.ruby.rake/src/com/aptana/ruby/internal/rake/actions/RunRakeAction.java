/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.rake.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;

import com.aptana.ruby.rake.IRakeHelper;
import com.aptana.ruby.rake.RakePlugin;

public class RunRakeAction extends Action
{

	private IProject project;
	private String task;
	private String description;

	public RunRakeAction(IProject project, String task, String description)
	{
		this.project = project;
		this.task = task;
		this.description = description;
	}

	@Override
	public void run()
	{
		Job job = new Job(MessageFormat.format(Messages.RunRakeAction_JobLabel, task))
		{

			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				getRakeHelper().runRake(project, monitor, task);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected IRakeHelper getRakeHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	@Override
	public String getText()
	{
		String[] parts = task.split(":"); //$NON-NLS-1$
		return parts[parts.length - 1];
	}

	@Override
	public String getToolTipText()
	{
		return description;
	}
}
