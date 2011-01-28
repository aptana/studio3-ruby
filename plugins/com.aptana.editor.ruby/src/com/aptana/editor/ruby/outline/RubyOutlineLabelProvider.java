/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.core.IRubyElement;
import com.aptana.editor.ruby.core.IRubyMethod;
import com.aptana.editor.ruby.core.IRubyMethod.Visibility;
import com.aptana.editor.ruby.core.IRubyType;

public class RubyOutlineLabelProvider extends LabelProvider
{

	static final Image CLASS = RubyEditorPlugin.getImage("icons/class_obj.png"); //$NON-NLS-1$
	private static final Image MODULE = RubyEditorPlugin.getImage("icons/module_obj.png"); //$NON-NLS-1$
	private static final Image METHOD_PUBLIC = RubyEditorPlugin.getImage("icons/method_public_obj.png"); //$NON-NLS-1$
	private static final Image METHOD_PROTECTED = RubyEditorPlugin.getImage("icons/method_protected_obj.png"); //$NON-NLS-1$
	private static final Image METHOD_PRIVATE = RubyEditorPlugin.getImage("icons/method_private_obj.png"); //$NON-NLS-1$
	private static final Image METHOD_SINGLETON = RubyEditorPlugin.getImage("icons/class_method.png"); //$NON-NLS-1$
	static final Image METHOD_CONSTRUCTOR = RubyEditorPlugin.getImage("icons/constructor.png"); //$NON-NLS-1$
	private static final Image CLASS_VAR = RubyEditorPlugin.getImage("icons/class_var_obj.png"); //$NON-NLS-1$
	private static final Image CONSTANT = RubyEditorPlugin.getImage("icons/constant_obj.png"); //$NON-NLS-1$
	private static final Image GLOBAL = RubyEditorPlugin.getImage("icons/global_obj.png"); //$NON-NLS-1$
	static final Image INSTANCE_VAR = RubyEditorPlugin.getImage("icons/instance_var_obj.png"); //$NON-NLS-1$
	static final Image LOCAL_VAR = RubyEditorPlugin.getImage("icons/local_var_obj.png"); //$NON-NLS-1$
	private static final Image IMPORT_DECLARATION = RubyEditorPlugin.getImage("icons/import_obj.png"); //$NON-NLS-1$
	private static final Image IMPORT_CONTAINER = RubyEditorPlugin.getImage("icons/import_container_obj.png"); //$NON-NLS-1$

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof CommonOutlineItem)
		{
			return getImage(((CommonOutlineItem) element).getReferenceNode());
		}
		if (element instanceof IRubyType)
		{
			return ((IRubyType) element).isModule() ? MODULE : CLASS;
		}
		else if (element instanceof IRubyMethod)
		{
			IRubyMethod method = (IRubyMethod) element;
			if (method.isSingleton())
			{
				return METHOD_SINGLETON;
			}
			if (method.isConstructor())
			{
				return METHOD_CONSTRUCTOR;
			}
			Visibility visibility = method.getVisibility();
			switch (visibility)
			{
				case PUBLIC:
					return METHOD_PUBLIC;
				case PROTECTED:
					return METHOD_PROTECTED;
				case PRIVATE:
					return METHOD_PRIVATE;
			}
		}
		else if (element instanceof IRubyElement)
		{
			short type = ((IRubyElement) element).getNodeType();
			switch (type)
			{
				case IRubyElement.CLASS_VAR:
					return CLASS_VAR;
				case IRubyElement.CONSTANT:
					return CONSTANT;
				case IRubyElement.GLOBAL:
					return GLOBAL;
				case IRubyElement.INSTANCE_VAR:
					return INSTANCE_VAR;
				case IRubyElement.LOCAL_VAR:
				case IRubyElement.DYNAMIC_VAR: // TODO Make dynamic variable have its own image
					return LOCAL_VAR;
				case IRubyElement.IMPORT_DECLARATION:
					return IMPORT_DECLARATION;
				case IRubyElement.IMPORT_CONTAINER:
					return IMPORT_CONTAINER;
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof CommonOutlineItem)
		{
			return getText(((CommonOutlineItem) element).getReferenceNode());
		}
		return super.getText(element);
	}
}
