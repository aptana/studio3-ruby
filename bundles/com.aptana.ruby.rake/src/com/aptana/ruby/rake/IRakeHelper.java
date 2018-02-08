/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.rake;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IRakeHelper
{

	/**
	 * Attempts to run a rake task in the background. Returns the IStatus containing exit code and output.
	 * 
	 * @param project
	 * @param monitor
	 * @param arguments
	 *            First argumnent is typically task name, but may be switches to pass to rake.
	 * @return
	 */
	public IStatus runRake(IProject project, IProgressMonitor monitor, String... arguments);

	/**
	 * Same as calling {@link #getTasks(project, false)}
	 * 
	 * @param project
	 * @return
	 */
	public Map<String, String> getTasks(IProject project, IProgressMonitor monitor);

	public Map<String, String> getTasks(IProject project, boolean force, IProgressMonitor monitor);
}
