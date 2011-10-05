/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.sass;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SassPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.sass"; //$NON-NLS-1$

	// The shared instance
	private static SassPlugin plugin;

	private IDocumentProvider sassDocumentProvider;

	/**
	 * The constructor
	 */
	public SassPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static SassPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns Sass document provider
	 * 
	 * @return
	 */
	public synchronized IDocumentProvider getSassDocumentProvider()
	{
		if (sassDocumentProvider == null)
		{
			sassDocumentProvider = new SassDocumentProvider();
		}
		return sassDocumentProvider;
	}

}
