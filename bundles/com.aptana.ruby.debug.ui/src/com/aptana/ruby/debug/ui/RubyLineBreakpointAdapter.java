/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.debug.ui;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.erb.IERBConstants;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.debug.core.IRubyLineBreakpoint;
import com.aptana.ruby.debug.core.RubyDebugModel;
import com.aptana.ruby.debug.core.launching.IRubyLaunchConfigurationConstants;

/**
 * Adapter to create breakpoints in Ruby files.
 */
public class RubyLineBreakpointAdapter implements IToggleBreakpointsTarget
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
		IFileStore store = getFileStore(part);
		if (store == null)
		{
			return;
		}

		URI uri = store.toURI();
		IResource resource = null;
		IPath fileName = null;
		if ("file".equals(uri.getScheme())) //$NON-NLS-1$
		{
			File file = new File(uri);
			fileName = Path.fromOSString(file.getAbsolutePath());
			resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(fileName);
		}

		if (resource == null)
		{
			// External file. Stick the marker on the workspace root
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		else
		{
			fileName = resource.getProjectRelativePath();
		}

		if (IdeLog.isTraceEnabled(RubyDebugUIPlugin.getDefault(), null))
		{
			IdeLog.logTrace(RubyDebugUIPlugin.getDefault(), MessageFormat.format(
					"Toggling breakpoint for URI: {0}, filename: {1}. Marker being set on resource: {2}", //$NON-NLS-1$
					uri.toString(), fileName, resource.getLocation().toOSString()));
		}

		ITextSelection textSelection = (ITextSelection) selection;
		int lineNumber = textSelection.getStartLine();
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
				.getBreakpoints(IRubyLaunchConfigurationConstants.ID_RUBY_DEBUG_MODEL);
		// Search for matching breakpoint. If we find one with same file, same line then we remove it and return.
		for (int i = 0; i < breakpoints.length; i++)
		{
			IBreakpoint breakpoint = breakpoints[i];
			if (!(breakpoint instanceof IRubyLineBreakpoint))
			{
				continue;
			}
			IRubyLineBreakpoint rubyLineBreakpoint = (IRubyLineBreakpoint) breakpoint;
			if (rubyLineBreakpoint.getFilePath().equals(fileName))
			{
				if (rubyLineBreakpoint.getLineNumber() == (lineNumber + 1))
				{
					// remove
					breakpoint.delete(); // TODO Disable, don't remove?
					return;
				}
			}
		}
		// create line breakpoint (doc line numbers start at 0)
		RubyDebugModel.createLineBreakpoint(resource, fileName, StringUtil.EMPTY, ++lineNumber, true, null);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		return getFileStore(part) != null;
	}

	/**
	 * Returns an IFileStore pointing at the contents of the editor, only if the file appears to have a ruby, ambiguous
	 * ruby, or HTML/ERB content type.
	 * 
	 * @param part
	 * @return
	 */
	public IFileStore getFileStore(IWorkbenchPart part)
	{
		if (!(part instanceof ITextEditor))
			return null;

		try
		{
			ITextEditor editorPart = (ITextEditor) part;
			IEditorInput editorInput = editorPart.getEditorInput();
			IFileStore store = null;
			if (editorInput instanceof IURIEditorInput)
			{
				IURIEditorInput uriInput = (IURIEditorInput) editorInput;
				store = EFS.getStore(uriInput.getURI());
			}
			else if (editorInput instanceof IStorageEditorInput)
			{
				IStorageEditorInput storageInput = (IStorageEditorInput) editorInput;
				IPath path = storageInput.getStorage().getFullPath();
				if (path != null)
				{
					File file = path.toFile();
					if (!file.exists())
					{
						// path might be relative to workspace root
						IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						IPath location = iFile.getLocation();
						if (location == null)
						{
							return null;
						}
						file = location.toFile();
					}
					store = EFS.getStore(file.toURI());
				}
			}
			if (store == null)
			{
				return null;
			}

			for (String contentType : getValidContentTypes())
			{
				if (isAssociatedWith(store.getName(), contentType))
				{
					return store;
				}
			}
		}
		catch (CoreException e)
		{
			IdeLog.logError(RubyDebugUIPlugin.getDefault(), e);
		}

		return null;
	}

	protected Set<String> getValidContentTypes()
	{
		Set<String> set = new HashSet<String>();
		set.add(IRubyConstants.CONTENT_TYPE_RUBY);
		set.add(IRubyConstants.CONTENT_TYPE_RUBY_AMBIGUOUS);
		set.add(IERBConstants.CONTENT_TYPE_HTML_ERB);
		return set;
	}

	private boolean isAssociatedWith(String fileName, String contentTypeId)
	{
		IContentType targetType = Platform.getContentTypeManager().getContentType(contentTypeId);
		return targetType.isAssociatedWith(fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection)
	{
		return false;
	}
}
