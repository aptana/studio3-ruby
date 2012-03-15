/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;

import com.aptana.ruby.core.codeassist.CodeResolver;
import com.aptana.ruby.internal.core.codeassist.RubyCodeResolver;
import com.aptana.ruby.internal.core.index.CoreStubber;

public class RubyCorePlugin extends Plugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.ruby.core"; //$NON-NLS-1$

	// The shared instance
	private static RubyCorePlugin fgPlugin;

	private RubyCodeResolver fCodeResolver;

	/**
	 * The constructor
	 */
	public RubyCorePlugin() // $codepro.audit.disable
							// com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.enforceTheSingletonPropertyWithAPrivateConstructor
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception // $codepro.audit.disable declaredExceptions
	{
		super.start(context);
		fgPlugin = this;
		// Schedule a job to stub out core library for ruby, then index it
		Job job = new CoreStubber();
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception // $codepro.audit.disable declaredExceptions
	{
		fCodeResolver = null;
		fgPlugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RubyCorePlugin getDefault()
	{
		return fgPlugin;
	}

	public synchronized CodeResolver getCodeResolver()
	{
		if (fCodeResolver == null)
		{
			fCodeResolver = new RubyCodeResolver();
		}
		return fCodeResolver;
	}

}
