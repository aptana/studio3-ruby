/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.haml;

import java.util.Set;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.ui.IWorkbenchPart;

import com.aptana.ruby.debug.ui.RubyLineBreakpointAdapter;

@SuppressWarnings("rawtypes")
public class HAMLBreakpointAdapterFactory implements IAdapterFactory
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		RubyLineBreakpointAdapter adapter = new RubyLineBreakpointAdapter()
		{
			@Override
			protected Set<String> getValidContentTypes()
			{
				Set<String> contentTypes = super.getValidContentTypes();
				contentTypes.add(IHAMLConstants.CONTENT_TYPE_HAML);
				return contentTypes;
			}
		};
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
