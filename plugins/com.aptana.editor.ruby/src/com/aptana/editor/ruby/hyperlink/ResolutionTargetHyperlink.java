/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.hyperlink;

import java.text.MessageFormat;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.ruby.core.codeassist.ResolutionTarget;
import com.aptana.ui.util.UIUtils;

public class ResolutionTargetHyperlink implements IHyperlink
{

	private IRegion region;
	private ResolutionTarget target;

	// TODO Can we combine this and URIHyperlink?
	public ResolutionTargetHyperlink(IRegion region, ResolutionTarget target)
	{
		this.region = region;
		this.target = target;
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
		return MessageFormat.format("{0}, {1}", target.getURI().toString(), target.getRange().toString()); //$NON-NLS-1$
	}

	public void open()
	{
		try
		{
			IWorkbenchPage page = UIUtils.getActivePage();
			IFileStore store = EFS.getStore(target.getURI());
			if (store == null)
			{
				return;
			}

			IEditorPart editor = IDE.openEditorOnFileStore(page, store);
			setEditorToRange(editor);
		}
		catch (CoreException e)
		{
			IdeLog.logError(RubyEditorPlugin.getDefault(), e);
		}
	}

	private void setEditorToRange(IEditorPart editorPart)
	{
		if (!(editorPart instanceof ITextEditor))
		{
			return;
		}

		ITextEditor textEditor = (ITextEditor) editorPart;
		textEditor.selectAndReveal(target.getRange().getStartingOffset(), target.getRange().getLength());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		if (!(obj instanceof ResolutionTargetHyperlink))
		{
			return false;
		}
		ResolutionTargetHyperlink other = (ResolutionTargetHyperlink) obj;
		if (region == null)
		{
			if (other.region != null)
			{
				return false;
			}
		}
		else if (!region.equals(other.region))
		{
			return false;
		}
		if (target == null)
		{
			if (other.target != null)
			{
				return false;
			}
		}
		else if (!target.equals(other.target))
		{
			return false;
		}
		return true;
	}

}
