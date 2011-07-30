/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.debug.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Creates a toggle breakpoint adapter
 */
@SuppressWarnings("rawtypes")
public class RubyBreakpointAdapterFactory implements IAdapterFactory
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		RubyLineBreakpointAdapter adapter = new RubyLineBreakpointAdapter();
		if (adaptableObject instanceof IWorkbenchPart)
		{
			if (adapter.getFileStore((IWorkbenchPart) adaptableObject) != null)
				return adapter;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList()
	{
		return new Class[] { IToggleBreakpointsTarget.class };
	}
}
