/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

public interface IRubyMethod extends IRubyMember
{
	public enum Visibility
	{
		PUBLIC, PROTECTED, PRIVATE
	}

	public Visibility getVisibility();

	public String[] getParameters();

	public String[] getBlockVars();

	public boolean isSingleton();

	public boolean isConstructor();
}
