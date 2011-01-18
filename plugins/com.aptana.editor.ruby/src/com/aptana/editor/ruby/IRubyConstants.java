/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.ruby;

/**
 * @author Max Stepanov
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

}
