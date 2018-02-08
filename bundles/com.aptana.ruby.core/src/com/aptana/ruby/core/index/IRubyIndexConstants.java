/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.index;

/**
 * @author cwilliams
 */
public interface IRubyIndexConstants
{
	/**
	 * Categories for indexing
	 */
	public static final String REQUIRE = "ruby.require"; //$NON-NLS-1$	
	public static final String REF = "ref"; //$NON-NLS-1$
	public static final String METHOD_REF = "methodRef"; //$NON-NLS-1$
	public static final String CONSTRUCTOR_REF = "constructorRef"; //$NON-NLS-1$
	public static final String SUPER_REF = "superRef"; //$NON-NLS-1$
	public static final String TYPE_DECL = "typeDecl"; //$NON-NLS-1$
	public static final String METHOD_DECL = "methodDecl"; //$NON-NLS-1$
	public static final String CONSTRUCTOR_DECL = "constructorDecl"; //$NON-NLS-1$
	public static final String FIELD_DECL = "fieldDecl"; //$NON-NLS-1$
	public static final String CONSTANT_DECL = "constantDecl"; //$NON-NLS-1$
	public static final String GLOBAL_DECL = "globalDecl"; //$NON-NLS-1$
	public static final String LOCAL_DECL = "localDecl"; //$NON-NLS-1$

	/**
	 * Constants for index keys
	 */
	public static final char SEPARATOR = '/';
	public static final char CLASS_SUFFIX = 'C';
	public static final char MODULE_SUFFIX = 'M';
}
