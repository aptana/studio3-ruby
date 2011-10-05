/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.rake;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.IPath;

class RakeFileFinder implements IResourceProxyVisitor
{

	private static final String RAKEFILE = "Rakefile"; //$NON-NLS-1$

	private IPath workingDirectory;

	public boolean visit(IResourceProxy proxy)
	{
		if (proxy.getType() == IResource.FILE)
		{
			if (RAKEFILE.equalsIgnoreCase(proxy.getName()))
			{
				workingDirectory = proxy.requestResource().getProjectRelativePath().removeLastSegments(1);
			}
		}
		return workingDirectory == null
				&& (proxy.getType() == IResource.FOLDER || proxy.getType() == IResource.PROJECT || proxy.getType() == IResource.ROOT);
	}

	public IPath getWorkingDirectory()
	{
		return workingDirectory;
	}
}
