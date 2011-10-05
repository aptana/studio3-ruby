/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import java.util.HashSet;
import java.util.Set;

import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyMethod;

public class RubyMethod extends NamedMember implements IRubyMethod
{

	private String[] fParameters;
	private Visibility fVisibility;
	private boolean isSingleton;
	private Set<String> blockVars;

	public RubyMethod(String name, String[] parameters, int start, int nameStart, int nameEnd)
	{
		super(name, start, nameStart, nameEnd);
		fParameters = parameters;
		blockVars = new HashSet<String>();
	}

	public void addBlockVar(String name)
	{
		blockVars.add(name);
	}

	public String[] getBlockVars()
	{
		return blockVars.toArray(new String[blockVars.size()]);
	}

	public String[] getParameters()
	{
		return fParameters;
	}

	public Visibility getVisibility()
	{
		return fVisibility;
	}

	@Override
	public short getNodeType()
	{
		return IRubyElement.METHOD;
	}

	public boolean isSingleton()
	{
		return isSingleton;
	}

	public void setVisibility(Visibility visibility)
	{
		fVisibility = visibility;
	}

	public void setIsSingleton(boolean singleton)
	{
		isSingleton = singleton;
	}

	@Override
	public String toString()
	{
		StringBuilder text = new StringBuilder();
		text.append(getName());
		text.append('(');
		String[] params = getParameters();
		for (int i = 0; i < params.length; ++i)
		{
			text.append(params[i]);
			if (i < params.length - 1)
			{
				text.append(", "); //$NON-NLS-1$
			}
		}
		text.append(')');
		return text.toString();
	}

	public boolean isConstructor()
	{
		return !isSingleton() && getName().equals("initialize"); //$NON-NLS-1$
	}
}
