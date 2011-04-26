/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.hyperlink;

import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.editor.ruby.RubyEditorPlugin;

public class EditorLineHyperlink implements IHyperlink
{

	private IRegion region;
	private URI uri;
	private IRegion destRegion;

	public EditorLineHyperlink(IRegion region, URI uri, IRegion destRegion)
	{
		this.region = region;
		this.uri = uri;
		this.destRegion = destRegion;
	}

	public IRegion getHyperlinkRegion()
	{
		return region;
	}

	public String getTypeLabel()
	{
		return null;
	}

	public String getHyperlinkText()
	{
		// Also include offset/line number as same file may be in more than one link
		return MessageFormat.format("{0}, {1}", uri.toString(), destRegion.toString()); //$NON-NLS-1$
	}

	public void open()
	{
		try
		{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore store = EFS.getStore(uri);
			if (store == null)
			{
				return;
			}

			IEditorPart editor = IDE.openEditorOnFileStore(page, store);
			setEditorToLine(editor);
		}
		catch (CoreException e)
		{
			RubyEditorPlugin.log(e);
		}
	}

	private void setEditorToLine(IEditorPart editorPart) throws CoreException
	{
		if (!(editorPart instanceof ITextEditor))
		{
			return;
		}

		ITextEditor textEditor = (ITextEditor) editorPart;
		textEditor.selectAndReveal(destRegion.getOffset(), destRegion.getLength());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destRegion == null) ? 0 : destRegion.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		EditorLineHyperlink other = (EditorLineHyperlink) obj;
		if (destRegion == null)
		{
			if (other.destRegion != null)
			{
				return false;
			}
		}
		else if (!destRegion.equals(other.destRegion))
		{
			return false;
		}
		if (uri == null)
		{
			if (other.uri != null)
			{
				return false;
			}
		}
		else if (!uri.equals(other.uri))
		{
			return false;
		}
		return true;
	}
}
