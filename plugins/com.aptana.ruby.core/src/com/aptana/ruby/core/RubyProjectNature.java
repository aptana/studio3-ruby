/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.core.build.UnifiedBuilder;
import com.aptana.core.util.ResourceUtil;

public class RubyProjectNature implements IProjectNature
{

	public static final String ID = RubyCorePlugin.PLUGIN_ID + ".rubynature"; //$NON-NLS-1$

	private IProject project;

	public void configure() throws CoreException
	{
		ResourceUtil.addBuilder(getProject(), UnifiedBuilder.ID);
	}

	public void deconfigure()
	{
	}

	public IProject getProject()
	{
		return project;
	}

	public void setProject(IProject project)
	{
		this.project = project;
	}

	public static void add(IProject project, IProgressMonitor monitor) throws CoreException
	{
		IProjectDescription description = project.getDescription();
		boolean addedNature = ResourceUtil.addNature(description, ID);
		boolean addedBuilder = ResourceUtil.addBuilder(description, UnifiedBuilder.ID);
		if (addedNature || addedBuilder)
		{
			project.setDescription(description, monitor);
		}
	}

}
