/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

@SuppressWarnings("nls")
public interface IRubyScopeConstants
{
	// ENTITIES
	String FUNCTION_NAME = "entity.name.function.ruby";
	String CLASS_NAME = "entity.name.type.class.ruby";
	String MODULE_NAME = "entity.name.type.module.ruby";

	// PUNCTUATION
	String PAREN = "punctuation.section.function.ruby";
	String FUNCTION_DEF_PAREN = "punctuation.definition.parameters.ruby";
	String COMMA = "punctuation.separator.object.ruby";
	String VARIABLE_SEPARATOR = "punctuation.separator.variable.ruby";
	String SEPARATOR_METHOD = "punctuation.separator.method.ruby";
	String SCOPE_PUNCTUATION = "punctuation.section.scope.ruby";
	String ARRAY_PUNCTUATION = "punctuation.section.array.ruby";
	String INHERITANCE_PUNCTUATION = "punctuation.separator.inheritance.ruby";
	String HASH_SEPARATOR = "punctuation.separator.key-value";

	// CONSTANTS
	String LANGUAGE_CONSTANT = "constant.language.ruby";
	String NUMERIC = "constant.numeric.ruby";
	String SYMBOL = "constant.other.symbol.ruby";
	String CHARACTER = "constant.character.ruby";

	// KEYWORDS
	String DEF_KEYWORD = "keyword.control.def.ruby";
	String MODULE_KEYWORD = "keyword.control.module.ruby";
	String CLASS_KEYWORD = "keyword.control.class.ruby";
	String DO_KEYWORD = "keyword.control.start-block.ruby";
	String OPERATOR_KEYWORD = "keyword.operator.logical.ruby";
	String CONTROL_KEYWORD = "keyword.control.ruby";
	String OPERATOR_ASSIGNMENT = "keyword.operator.assignment.ruby";
	String OPERATOR_COMPARISON = "keyword.operator.comparison.ruby";
	String OPERATOR_ARITHMETIC = "keyword.operator.arithmetic.ruby";
	String OPERATOR_LOGICAL = "keyword.operator.logical.ruby";
	String SPECIAL_METHOD = "keyword.other.special-method.ruby";
	String AUGMENTED_ASSIGNMENT = "keyword.operator.assignment.augmented.ruby";

	// VARIABLES
	String LANGUAGE_VARIABLE = "variable.language.ruby";
	String CONSTANT_OTHER = "variable.other.constant.ruby";
	String BLOCK_VARIABLE = "variable.other.block.ruby";
	String FUNCTION_PARAMETER = "variable.parameter.function.ruby";
	String GLOBAL_VARIABLE = "variable.other.readwrite.global.ruby";
	String INSTANCE_VARIABLE = "variable.other.readwrite.instance.ruby";
	String CLASS_VARIABLE = "variable.other.readwrite.class.ruby";

	// SUPPORT
	String SUPPORT_CLASS = "support.class.ruby";

	// MISC
	String ERROR = "error.ruby";
}
