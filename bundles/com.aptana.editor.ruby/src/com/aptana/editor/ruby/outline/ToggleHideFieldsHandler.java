/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.outline;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyField;

/**
 * Toggles whether fields (instance, class, local or dynamic variables) are visible in the Ruby Outline.
 * 
 * @author cwilliams
 */
public class ToggleHideFieldsHandler extends AbstractHandler
{

	private ViewerFilter fFilter;

	public Object execute(ExecutionEvent event)
	{
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof AbstractThemeableEditor)
		{
			AbstractThemeableEditor ate = (AbstractThemeableEditor) editor;
			CommonOutlinePage page = ate.getOutlinePage();
			TreeViewer viewer = page.getTreeViewer();
			if (fFilter == null)
			{
				fFilter = new FieldsFilter();
				viewer.addFilter(fFilter);
			}
			else
			{
				viewer.removeFilter(fFilter);
				fFilter = null;
			}
		}
		return null;
	}

	/**
	 * Filters out instance/class variables
	 * 
	 * @author cwilliams
	 */
	private static class FieldsFilter extends ViewerFilter
	{
		@Override
		public boolean select(Viewer viewer, Object parent, Object element)
		{
			if (element instanceof CommonOutlineItem)
			{
				CommonOutlineItem coi = (CommonOutlineItem) element;
				return select(viewer, parent, coi.getReferenceNode());
			}

			if (element instanceof IRubyField)
			{
				IRubyField field = (IRubyField) element;
				short type = field.getNodeType();
				return type == IRubyElement.CONSTANT || type == IRubyElement.GLOBAL;
			}
			return true;
		}
	}
}
