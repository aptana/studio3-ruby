/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.text;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.text.AbstractFoldingComputer;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.preferences.IPreferenceConstants;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.ruby.core.IRubyComment;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyMethod;
import com.aptana.ruby.core.IRubyType;

public class RubyFoldingComputer extends AbstractFoldingComputer
{

	public RubyFoldingComputer(AbstractThemeableEditor editor, IDocument document)
	{
		super(editor, document);
	}

	@Override
	public boolean isFoldable(IParseNode child)
	{
		// FIXME There doesn't seem to be a way to fold multiline if/unless/case/begin/rescue/end
		return (child instanceof IRubyType) || (child instanceof IRubyMethod) || (child instanceof IRubyComment)
				|| (child instanceof IRubyElement && ((IRubyElement) child).getNodeType() == IRubyElement.BLOCK);
	}

	@Override
	public boolean isCollapsed(IParseNode child)
	{
		if (child instanceof IRubyMethod)
		{
			return Platform.getPreferencesService().getBoolean(RubyEditorPlugin.PLUGIN_ID,
					IPreferenceConstants.INITIALLY_FOLD_METHODS, false, null);
		}
		if (child instanceof IRubyComment)
		{
			return Platform.getPreferencesService().getBoolean(RubyEditorPlugin.PLUGIN_ID,
					IPreferenceConstants.INITIALLY_FOLD_COMMENTS, false, null);
		}
		if (child instanceof IRubyElement && ((IRubyElement) child).getNodeType() == IRubyElement.BLOCK)
		{
			return Platform.getPreferencesService().getBoolean(RubyEditorPlugin.PLUGIN_ID,
					IPreferenceConstants.INITIALLY_FOLD_BLOCKS, false, null);
		}
		if (child instanceof IRubyType)
		{
			// Check to see if parent is another type
			IRubyType type = (IRubyType) child;
			if (type.getParent() instanceof IRubyType)
			{
				return Platform.getPreferencesService().getBoolean(RubyEditorPlugin.PLUGIN_ID,
						IPreferenceConstants.INITIALLY_FOLD_INNER_TYPES, false, null);
			}
		}
		return false;
	}

}
