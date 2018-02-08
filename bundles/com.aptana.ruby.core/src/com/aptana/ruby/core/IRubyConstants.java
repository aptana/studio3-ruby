/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.ruby.core;

/**
 * @author Max Stepanov
 * @author cwilliams
 */
public interface IRubyConstants
{

	public String CONTENT_TYPE_RUBY = "com.aptana.contenttype.ruby"; //$NON-NLS-1$
	public String CONTENT_TYPE_RUBY_AMBIGUOUS = "com.aptana.contenttype.ruby.ambiguous"; //$NON-NLS-1$
	public String SINGLE_QUOTED_STRING_SCOPE = "string.quoted.single.ruby"; //$NON-NLS-1$
	public String DOUBLE_QUOTED_STRING_SCOPE = "string.quoted.double.ruby"; //$NON-NLS-1$
	public String LINE_COMMENT_SCOPE = "comment.line.number-sign.ruby"; //$NON-NLS-1$
	public String BLOCK_COMMENT_SCOPE = "comment.block.documentation.ruby"; //$NON-NLS-1$

	/**
	 * ID of the marker/annotation type used to highlight do/end, def/end, class/end, module/end pairs
	 */
	public String BLOCK_PAIR_OCCURRENCES_ID = "com.aptana.ruby.blockPair.occurrences"; //$NON-NLS-1$

	// Constants used for type names, common things like namespace delimeters
	public static final String NAMESPACE_DELIMETER = "::"; //$NON-NLS-1$
	// Types
	public static final String STRING = "String"; //$NON-NLS-1$
	public static final String FIXNUM = "Fixnum"; //$NON-NLS-1$
	public static final String NIL_CLASS = "NilClass"; //$NON-NLS-1$
	public static final String SYMBOL = "Symbol"; //$NON-NLS-1$
	public static final String TIME = "Time"; //$NON-NLS-1$
	public static final String ARRAY = "Array"; //$NON-NLS-1$
	public static final String OBJECT = "Object"; //$NON-NLS-1$
	public static final String FLOAT = "Float"; //$NON-NLS-1$
	public static final String PROC = "Proc"; //$NON-NLS-1$
	public static final String BIGNUM = "Bignum"; //$NON-NLS-1$
	public static final String HASH = "Hash"; //$NON-NLS-1$
	public static final String REGEXP = "Regexp"; //$NON-NLS-1$
	public static final String TRUE_CLASS = "TrueClass"; //$NON-NLS-1$
	public static final String FALSE_CLASS = "FalseClass"; //$NON-NLS-1$

}
