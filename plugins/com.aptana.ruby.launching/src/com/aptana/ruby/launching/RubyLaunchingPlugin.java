/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.aptana.core.util.ExecutableUtil;

public class RubyLaunchingPlugin implements BundleActivator
{

	private static final String RUBYW = "rubyw"; //$NON-NLS-1$
	private static final String RUBY = "ruby"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext()
	{
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception
	{
		RubyLaunchingPlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception
	{
		RubyLaunchingPlugin.context = null;
	}

	/**
	 * Search for the applicable ruby executable for the working dir. If no working dir is set, we won't take rvmrc into
	 * account (we'll use global PATH).
	 * 
	 * @param workingDir
	 * @return
	 */
	public static IPath rubyExecutablePath(IPath workingDir)
	{
		IPath path = null;
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			path = ExecutableUtil.find(RUBYW, true, getCommonRubyBinaryLocations(RUBYW), workingDir);
		}
		if (path == null)
		{
			path = ExecutableUtil.find(RUBY, true, getCommonRubyBinaryLocations(RUBY), workingDir);
		}
		// TODO check TM_RUBY env value too?
		return path;
	}

	/**
	 * Return an ordered list of common locations that you'd find a ruby binary.
	 * 
	 * @return
	 */
	private static List<IPath> getCommonRubyBinaryLocations(String binaryName)
	{
		List<IPath> locations = new ArrayList<IPath>();
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			locations.add(Path.fromOSString("C:\\ruby\\bin").append(binaryName).addFileExtension("exe")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			locations.add(Path.fromOSString("/opt/local/bin/").append(binaryName)); //$NON-NLS-1$
			locations.add(Path.fromOSString("/usr/local/bin/").append(binaryName)); //$NON-NLS-1$
			locations.add(Path.fromOSString("/usr/bin/").append(binaryName)); //$NON-NLS-1$
		}
		if (Platform.OS_MACOSX.equals(Platform.getOS()))
		{
			locations.add(Path.fromOSString("/System/Library/Frameworks/Ruby.framework/Versions/Current/usr/bin/") //$NON-NLS-1$
					.append(binaryName));
		}
		return locations;
	}
}
