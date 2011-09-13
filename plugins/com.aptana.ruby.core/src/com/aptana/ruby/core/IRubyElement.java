/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.util.List;

import com.aptana.parsing.ast.IParseNode;

public interface IRubyElement extends IParseNode
{

	public static final short SCRIPT = 1;
	public static final short TYPE = 2;
	public static final short METHOD = 3;
	public static final short BLOCK = 4;
	public static final short GLOBAL = 5;
	public static final short IMPORT_DECLARATION = 6;
	public static final short CONSTANT = 7;
	public static final short CLASS_VAR = 8;
	public static final short INSTANCE_VAR = 9;
	public static final short LOCAL_VAR = 10;
	public static final short DYNAMIC_VAR = 11;
	public static final short FIELD = 12;
	public static final short IMPORT_CONTAINER = 13;
	public static final short COMMENT = 14;

	public String getName();

	public short getNodeType();

	public IRubyElement[] getChildrenOfType(int type);

	public List<IRubyElement> getChildrenOfTypeRecursive(int type);
}
