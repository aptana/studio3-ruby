/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.preferences;

public interface IPreferenceConstants
{
	/**
	 * A pref key used to determine if comments are initially folded. Value is a boolean.
	 */
	String INITIALLY_FOLD_COMMENTS = "fold_comments"; //$NON-NLS-1$

	/**
	 * A pref key used to determine if methods are initially folded. Value is a boolean.
	 */
	String INITIALLY_FOLD_METHODS = "fold_methods"; //$NON-NLS-1$

	/**
	 * A pref key used to determine if blocks are initially folded. Value is a boolean.
	 */
	String INITIALLY_FOLD_BLOCKS = "fold_blocks"; //$NON-NLS-1$

	/**
	 * A pref key used to determine if inner types are initially folded. Value is a boolean.
	 */
	String INITIALLY_FOLD_INNER_TYPES = "fold_inner_types"; //$NON-NLS-1$
}
