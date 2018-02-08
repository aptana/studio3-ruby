/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyField;
import com.aptana.ruby.core.IRubyMethod;
import com.aptana.ruby.core.IRubyType;

public class RubyType extends NamedMember implements IRubyType
{

	private static final String[] EMPTY_ARRAY = new String[0];

	/**
	 * the names of the module this type includes
	 */
	private String[] includedModuleNames;

	/**
	 * the name of the superclass for this type
	 */
	private String superclassName;

	public RubyType(String name, int start, int nameStart, int nameEnd)
	{
		super(name, start, nameStart, nameEnd);
		includedModuleNames = EMPTY_ARRAY;
	}

	public IRubyField[] getFields()
	{
		List<IRubyElement> elements = new ArrayList<IRubyElement>();
		elements.addAll(Arrays.asList(getChildrenOfType(IRubyElement.CONSTANT)));
		elements.addAll(Arrays.asList(getChildrenOfType(INSTANCE_VAR)));
		elements.addAll(Arrays.asList(getChildrenOfType(CLASS_VAR)));
		return elements.toArray(new IRubyField[elements.size()]);
	}

	public IRubyMethod[] getMethods()
	{
		IRubyElement[] elements = getChildrenOfType(IRubyElement.METHOD);
		IRubyMethod[] methods = new IRubyMethod[elements.length];
		System.arraycopy(elements, 0, methods, 0, elements.length);
		return methods;
	}

	public String[] getIncludedModuleNames()
	{
		if (isAnonymous())
		{
			return EMPTY_ARRAY;
		}
		return includedModuleNames;
	}

	public String getSuperclassName()
	{
		if (isAnonymous())
		{
			if (includedModuleNames.length > 0)
			{
				return includedModuleNames[0];
			}
		}
		return superclassName;
	}

	@Override
	public short getNodeType()
	{
		return IRubyElement.TYPE;
	}

	public boolean isClass()
	{
		return true;
	}

	public boolean isModule()
	{
		return false;
	}

	/**
	 * Sets the names of the modules this type includes.
	 */
	public void setIncludedModuleNames(String[] includedModuleNames)
	{
		this.includedModuleNames = includedModuleNames;
	}

	/**
	 * Sets the name of this type's superclass.
	 */
	public void setSuperclassName(String superclassName)
	{
		this.superclassName = superclassName;
	}

	private boolean isAnonymous()
	{
		return getName() == null || getName().length() == 0;
	}
}
