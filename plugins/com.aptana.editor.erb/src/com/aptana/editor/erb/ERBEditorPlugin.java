/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.erb;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

import com.aptana.editor.erb.html.RHTMLDocumentProvider;
import com.aptana.editor.erb.xml.RXMLDocumentProvider;

/**
 * The activator class controls the plug-in life cycle
 */
public class ERBEditorPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.erb"; //$NON-NLS-1$

	// The shared instance
	private static ERBEditorPlugin plugin;

	private IDocumentProvider rxmlDocumentProvider;
	private IDocumentProvider rhtmlDocumentProvider;

	/**
	 * The constructor
	 */
	public ERBEditorPlugin()
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
	public static ERBEditorPlugin getDefault()
	{
		return plugin;
	}

	public static Image getImage(String string)
	{
		if (getDefault().getImageRegistry().get(string) == null)
		{
			ImageDescriptor id = imageDescriptorFromPlugin(PLUGIN_ID, string);
			if (id != null)
			{
				getDefault().getImageRegistry().put(string, id);
			}
		}
		return getDefault().getImageRegistry().get(string);
	}

	/**
	 * Returns RXML document provider
	 * 
	 * @return
	 */
	public synchronized IDocumentProvider getRXMLDocumentProvider()
	{
		if (rxmlDocumentProvider == null)
		{
			rxmlDocumentProvider = new RXMLDocumentProvider();
		}
		return rxmlDocumentProvider;
	}

	/**
	 * Returns RHTML document provider
	 * 
	 * @return
	 */
	public synchronized IDocumentProvider getRHTMLDocumentProvider()
	{
		if (rhtmlDocumentProvider == null)
		{
			rhtmlDocumentProvider = new RHTMLDocumentProvider();
		}
		return rhtmlDocumentProvider;
	}

}
