/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.core.projects.templates.ProjectTemplate;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.projects.ProjectsPlugin;
import com.aptana.projects.templates.IDefaultProjectTemplate;

/**
 * The activator class controls the plug-in life cycle
 */
public class RubyUIPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	private static final String PLUGIN_ID = "com.aptana.ruby.ui"; //$NON-NLS-1$

	// The shared instance
	private static RubyUIPlugin plugin;

	private static class DefaultRubyProjectTemplate extends ProjectTemplate implements IDefaultProjectTemplate
	{

		private static final String ID = "com.aptana.ruby.default"; //$NON-NLS-1$

		public DefaultRubyProjectTemplate()
		{
			super("default.zip", TemplateType.RUBY, Messages.RubyUIPlugin_DefaultRubyProjectTemplate_Name, //$NON-NLS-1$
					false, Messages.RubyUIPlugin_DefaultRubyProjectTemplate_Description, null, ID, 1, CollectionsUtil
							.newList("Ruby")); //$NON-NLS-1$
		}

		@Override
		public IStatus apply(IProject project, boolean promptForOverwrite)
		{
			// just returns success
			return Status.OK_STATUS;
		}
	}

	/**
	 * The constructor
	 */
	public RubyUIPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		ProjectsPlugin.getDefault().getTemplatesManager().addTemplate(new DefaultRubyProjectTemplate());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RubyUIPlugin getDefault()
	{
		return plugin;
	}

	public static String getPluginIdentifier()
	{
		return PLUGIN_ID;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg)
	{
		for (String image : IRubyUIConstants.ALL_IMAGES)
		{
			reg.put(image, imageDescriptorFromPlugin(PLUGIN_ID, image));
		}
		super.initializeImageRegistry(reg);
	}
}
