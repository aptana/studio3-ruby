/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.haml;

/**
 * @author Max Stepanov
 */
public interface IHAMLConstants
{
	// Content Type name
	public String CONTENT_TYPE_HAML = "com.aptana.contenttype.haml"; //$NON-NLS-1$

	// Scopes used in HAML
	public String RUBY_ATTRIBUTES_SCOPE = "meta.section.attributes.haml"; //$NON-NLS-1$
	public String DOCTYPE_SCOPE = "meta.prolog.haml"; //$NON-NLS-1$
	public String TAG_SCOPE = "meta.tag.haml"; //$NON-NLS-1$
	public String TEXT_SCOPE = "text.haml"; //$NON-NLS-1$
	public String RUBY_EVAL_SCOPE = "meta.line.ruby.haml"; //$NON-NLS-1$
	public String HTML_COMMENT_SCOPE = "comment.line.slash.haml"; //$NON-NLS-1$
	public String INTERPOLATION_SCOPE = "meta.section.other.haml"; //$NON-NLS-1$
	public String EMBEDDED_RUBY_SCOPE = "source.ruby.embedded.haml"; //$NON-NLS-1$
	public String EMBEDDED_RUBY_SOURCE_SCOPE = "source.ruby.embedded.source"; //$NON-NLS-1$
	public String OBJECT_SCOPE = "meta.section.object.haml"; //$NON-NLS-1$	
	public String HAML_COMMENT_SCOPE = RUBY_EVAL_SCOPE + " " + EMBEDDED_RUBY_SCOPE; //$NON-NLS-1$
	public String COMMENT_PUNCTUATION_SCOPE = "punctuation.section.comment.haml"; //$NON-NLS-1$
	public String SECTION_OTHER_PUNCTUATION_SCOPE = "punctuation.section.other.haml"; //$NON-NLS-1$
	public String SECTION_EMBEDDED_PUNCTUATION_SCOPE = "punctuation.section.embedded.ruby"; //$NON-NLS-1$
	public String TAG_TERMINATOR_PUNCTUATION_SCOPE = "punctuation.terminator.tag.haml"; //$NON-NLS-1$	
	public String TAG_OTHER_PUNCTUATION_SCOPE = "punctuation.other.tag.haml"; //$NON-NLS-1$
	public String META_ESCAPE_SCOPE = "meta.escape.haml"; //$NON-NLS-1$
	public String PROLOG_DEF_PUNCTUATION_SCOPE = "punctuation.definition.prolog.haml"; //$NON-NLS-1$
	public static final String PUNCTUATION_DEFINITION_TAG_HAML = "punctuation.definition.tag.haml"; //$NON-NLS-1$
	public static final String ENTITY_NAME_TAG_HAML = "entity.name.tag.haml"; //$NON-NLS-1$
	public static final String ENTITY_NAME_TAG_ID_HAML = "entity.name.tag.id.haml"; //$NON-NLS-1$
	public static final String ENTITY_NAME_TAG_CLASS_HAML = "entity.name.tag.class.haml"; //$NON-NLS-1$

}
