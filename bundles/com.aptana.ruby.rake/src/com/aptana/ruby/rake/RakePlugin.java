/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.rake;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.ruby.internal.rake.RakeTasksHelper;
import com.aptana.ruby.launching.RubyLaunchingPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class RakePlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.ruby.rake"; //$NON-NLS-1$

	// The shared instance
	private static RakePlugin plugin;

	private IRakeHelper rakeHelper;

	/**
	 * The constructor
	 */
	public RakePlugin()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		plugin = this;
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		rakeHelper = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RakePlugin getDefault()
	{
		return plugin;
	}

	public String getRakePath(IProject project)
	{
		IPath wd = null;
		if (project != null)
		{
			wd = project.getLocation();
		}
		IPath result = RubyLaunchingPlugin.getRakePath(wd);
		return (result == null) ? RubyLaunchingPlugin.RAKE : result.toOSString();
	}

	public static void log(String message, Exception e)
	{
		getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, -1, message, e));
	}

	public static void log(Exception e)
	{
		log(e.getMessage(), e);
	}

	public synchronized IRakeHelper getRakeHelper()
	{
		if (rakeHelper == null)
		{
			rakeHelper = new RakeTasksHelper();
		}
		return rakeHelper;
	}

}
