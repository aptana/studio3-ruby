/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package com.aptana.ruby.debug.ui;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.ruby.debug.core.IRubyLineBreakpoint;
import com.aptana.ruby.debug.core.model.IRubyVariable;
import com.aptana.ruby.internal.debug.ui.StorageEditorInput;
import com.aptana.ruby.ui.IRubyUIConstants;
import com.aptana.ruby.ui.RubyUIPlugin;

/**
 * Renders Ruby debug elements
 */
public class RubyModelPresentation extends LabelProvider implements IDebugModelPresentation
{

	protected Map<String, Object> fAttributes = new HashMap<String, Object>(3);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attribute, Object value)
	{
		if (value == null)
		{
			return;
		}

		fAttributes.put(attribute, value);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element)
	{
		try
		{
			if (element instanceof IVariable)
			{
				return getVariableImage((IVariable) element);
			}
			else if (element instanceof IBreakpoint)
			{
				return getBreakpointImage((IBreakpoint) element);
			}
			else if (element instanceof IMarker)
			{
				IBreakpoint breakpoint = getBreakpoint((IMarker) element);
				if (breakpoint != null)
				{
					return getBreakpointImage(breakpoint);
				}
			}
		}
		catch (CoreException e)
		{
			IdeLog.logError(RubyDebugUIPlugin.getDefault(), e);
		}
		return super.getImage(element);
	}

	/**
	 * getBreakpointImage
	 * 
	 * @param breakpoint
	 * @return Image
	 * @throws CoreException
	 */
	private Image getBreakpointImage(IBreakpoint breakpoint) throws CoreException
	{
		// TODO Handle conditional overlay for condition enabled or hit count
		if (breakpoint.isEnabled())
		{
			return DebugUITools.getImage(org.eclipse.debug.ui.IDebugUIConstants.IMG_OBJS_BREAKPOINT);
		}
		return DebugUITools.getImage(org.eclipse.debug.ui.IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
	}

	/**
	 * getVariableImage
	 * 
	 * @param variable
	 * @return Image
	 * @throws DebugException
	 */
	protected Image getVariableImage(IVariable variable) throws DebugException
	{
		if (variable instanceof IRubyVariable)
		{
			IRubyVariable rubyVar = (IRubyVariable) variable;
			if (rubyVar.isConstant())
			{
				return RubyUIPlugin.getDefault().getImageRegistry().get(IRubyUIConstants.IMG_OBJS_CONSTANT);
			}
			if (rubyVar.isLocal())
			{
				// TODO Check to see if this is a complex object and show class image?
				return RubyUIPlugin.getDefault().getImageRegistry().get(IRubyUIConstants.IMG_OBJS_LOCAL_VARIABLE);
			}
			if (rubyVar.isInstance())
			{
				return RubyUIPlugin.getDefault().getImageRegistry().get(IRubyUIConstants.IMG_OBJS_INSTANCE_VARIABLE);
			}
			if (rubyVar.isStatic())
			{
				return RubyUIPlugin.getDefault().getImageRegistry().get(IRubyUIConstants.IMG_OBJS_CLASS_VARIABLE);
			}
			return DebugUITools.getImage(org.eclipse.debug.ui.IDebugUIConstants.IMG_OBJS_VARIABLE);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element)
	{
		try
		{
			/*
			 * if (element instanceof IStackFrame) { return getStackFrameText((IStackFrame) element); } else if (element
			 * instanceof IThread) { return getThreadText((IThread) element); } else
			 */if (element instanceof IBreakpoint)
			{
				return getBreakpointText((IBreakpoint) element);
			}
			else if (element instanceof IVariable)
			{
				return getVariableText((IVariable) element);
			}
			else if (element instanceof IValue)
			{
				return getValueText((IValue) element);
			}
			else if (element instanceof IMarker)
			{
				IBreakpoint breakpoint = getBreakpoint((IMarker) element);
				if (breakpoint != null)
				{
					return getBreakpointText(breakpoint);
				}
			}
		}
		catch (CoreException e)
		{
			IdeLog.logError(RubyDebugUIPlugin.getDefault(), e);
		}
		return null;
	}

	/**
	 * getBreakpoint
	 * 
	 * @param marker
	 * @return IBreakpoint
	 */
	private IBreakpoint getBreakpoint(IMarker marker)
	{
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
	}

	private String getBreakpointText(IBreakpoint element)
	{
		if (element instanceof IRubyLineBreakpoint)
		{
			try
			{
				IRubyLineBreakpoint rlbp = (IRubyLineBreakpoint) element;
				return MessageFormat.format(
						"{0} [line: {1}]", rlbp.getFilePath().toPortableString(), rlbp.getLineNumber()); //$NON-NLS-1$
			}
			catch (CoreException e)
			{
				// ignore
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue,
	 * org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener)
	{
		String detail = StringUtil.EMPTY;
		try
		{
			detail = value.getValueString();
		}
		catch (DebugException e)
		{
		}
		listener.detailComputed(value, detail);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element)
	{
		IFile file = getFile(element);
		if (file != null)
		{
			return new FileEditorInput(file);
		}
		if (element instanceof IStorage)
		{
			return new StorageEditorInput((IStorage) element);
		}
		IFileStore store = getFileStore(element);
		if (store != null)
		{
			return new FileStoreEditorInput(store);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object element)
	{
		IEditorDescriptor desc = null;
		IFile file = getFile(element);
		if (file != null)
		{
			desc = IDE.getDefaultEditor(file);
			if (desc != null)
			{
				return desc.getId();
			}
		}

		String filename = null;
		if (element instanceof IStorage)
		{
			IStorage storage = (IStorage) element;
			filename = storage.getName();
		}
		else
		{
			IFileStore store = getFileStore(element);
			if (store != null)
			{
				filename = store.getName();
			}
		}
		if (filename == null)
		{
			return null;
		}
		IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		desc = editorReg.getDefaultEditor(filename, contentType);
		if (desc != null)
		{
			return desc.getId();
		}
		return null;
	}

	private IFile getFile(Object element)
	{
		if (element instanceof IFile)
			return (IFile) element;
		if (element instanceof ILineBreakpoint)
		{
			IResource resource = ((ILineBreakpoint) element).getMarker().getResource();
			if (resource instanceof IFile)
			{
				return (IFile) resource;
			}
		}
		if (element instanceof LocalFileStorage)
		{
			File file = ((LocalFileStorage) element).getFile();
			IResource resource = ResourcesPlugin.getWorkspace().getRoot()
					.getFileForLocation(new Path(file.getAbsolutePath()));
			if (resource != null)
				return (IFile) resource;
		}

		return null;
	}

	private IFileStore getFileStore(Object element)
	{
		if (element instanceof IRubyLineBreakpoint)
		{
			try
			{
				IPath fileName = ((IRubyLineBreakpoint) element).getLocation();
				return EFS.getStore(fileName.toFile().toURI());
			}
			catch (CoreException e)
			{
				// ignore
			}
		}
		return null;
	}

	/**
	 * getVariableText
	 * 
	 * @param variable
	 * @return String
	 */
	public String getVariableText(IVariable variable)
	{
		StringBuilder sb = new StringBuilder();
		// Variable type
		if (showVariableTypeNames())
		{
			String typeName = Messages.RubyModelPresentation_UnknownType;
			try
			{
				typeName = variable.getReferenceTypeName();
			}
			catch (DebugException e)
			{
			}
			sb.append(typeName).append(' ');
		}

		// Variable name
		String varLabel = Messages.RubyModelPresentation_UnknownName;
		try
		{
			varLabel = variable.getName();
		}
		catch (DebugException e)
		{
		}
		sb.append(varLabel);

		// Variable value
		IValue value = null;
		try
		{
			value = variable.getValue();
		}
		catch (DebugException e)
		{
		}
		String valueString = Messages.RubyModelPresentation_UnknwonValue;
		if (value != null)
		{
			try
			{
				valueString = getValueText(value);
			}
			catch (DebugException e)
			{
			}
		}
		if (valueString.length() != 0)
		{
			sb.append(" = "); //$NON-NLS-1$
			sb.append(valueString);
		}
		return sb.toString();
	}

	protected boolean showVariableTypeNames()
	{
		Boolean show = (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
		show = show == null ? Boolean.FALSE : show;
		return show.booleanValue();
	}

	/**
	 * getValueText
	 * 
	 * @param value
	 * @return String
	 * @throws DebugException
	 */
	protected String getValueText(IValue value) throws DebugException
	{
		return value.getValueString();
	}
}
