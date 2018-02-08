/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.sass;

/**
 * @author Max Stepanov
 */
public interface ISassConstants
{

	public String CONTENT_TYPE_SASS = "com.aptana.contenttype.sass"; //$NON-NLS-1$

	// Scopes
	static final String TOPLEVEL_SCOPE = "source.sass"; //$NON-NLS-1$

	// Strings
	static final String STRING_QUOTED_SINGLE_SCOPE = "string.quoted.single.sass"; //$NON-NLS-1$
	static final String STRING_QUOTED_DOUBLE_SCOPE = "string.quoted.double.sass"; //$NON-NLS-1$
	static final String ESCAPE_CHARACTER_SCOPE = "constant.character.escape.sass"; //$NON-NLS-1$

	// Comments
	static final String COMMENT_BLOCK_SCOPE = "comment.block.sass"; //$NON-NLS-1$
	static final String COMMENT_LINE_SCOPE = "comment.line.sass"; //$NON-NLS-1$

	// Entities
	static final String ENTITY_NAME_FUNCTION_SCOPE = "entity.name.function.sass"; //$NON-NLS-1$

	// Meta
	static final String META_SELECTOR_SCOPE = "meta.selector.sass"; //$NON-NLS-1$
	static final String META_PROPERTY_VALUE_SCOPE = "meta.property-value.sass"; //$NON-NLS-1$

	// Variables
	static final String VARIABLE_OTHER_SCOPE = "variable.other.sass"; //$NON-NLS-1$

	// Punctuation
	static final String PUNCTUATION_SEPARATOR_SCOPE = "punctuation.separator.sass"; //$NON-NLS-1$
	static final String PUNCTUATION_DEFINITION_ENTITY_SCOPE = "punctuation.definition.entity.sass"; //$NON-NLS-1$

	// Keywords
	static final String KEYWORD_CONTROL_AT_RULE_MIXIN_SCOPE = "keyword.control.at-rule.mixin.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_INCLUDE_SCOPE = "keyword.control.at-rule.include.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_FUNCTION_SCOPE = "keyword.control.at-rule.function.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_WHILE_SCOPE = "keyword.control.at-rule.while.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_EACH_SCOPE = "keyword.control.at-rule.each.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_FOR_SCOPE = "keyword.control.at-rule.for.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_IF_SCOPE = "keyword.control.at-rule.if.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_WARN_SCOPE = "keyword.control.at-rule.warn.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_DEBUG_SCOPE = "keyword.control.at-rule.debug.sass"; //$NON-NLS-1$
	static final String KEYWORD_CONTROL_AT_RULE_EXTEND_SCOPE = "keyword.control.at-rule.extend.sass"; //$NON-NLS-1$
}
