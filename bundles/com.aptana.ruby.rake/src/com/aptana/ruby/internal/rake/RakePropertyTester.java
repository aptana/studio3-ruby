/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.rake;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.aptana.ruby.launching.RubyLaunchingPlugin;

public class RakePropertyTester extends PropertyTester
{

	public RakePropertyTester()
	{
	}

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		if (!(receiver instanceof IResource))
			return false;
		IResource res = (IResource) receiver;
		if ("hasRakefile".equals(property)) //$NON-NLS-1$
		{
			IProject project = res.getProject();
			IFile file = project.getFile(RubyLaunchingPlugin.RAKEFILE);
			return file.exists();
		}
		return false;
	}

}
