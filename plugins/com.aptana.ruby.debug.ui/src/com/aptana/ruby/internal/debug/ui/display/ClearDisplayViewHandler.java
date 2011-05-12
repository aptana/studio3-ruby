package com.aptana.ruby.internal.debug.ui.display;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aptana.ui.util.UIUtils;

public class ClearDisplayViewHandler extends AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IDataDisplay dataDisplay = getDataDisplay(event);
		if (dataDisplay != null)
		{
			dataDisplay.clear();
		}
		return null;
	}

	private IDataDisplay getDataDisplay(ExecutionEvent event)
	{
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part != null)
		{
			IDataDisplay display = (IDataDisplay) part.getAdapter(IDataDisplay.class);
			if (display != null)
			{
				IWorkbenchPage page = UIUtils.getActivePage();
				if (page != null)
				{
					IWorkbenchPart activePart = page.getActivePart();
					if (activePart != null)
					{
						if (activePart != part)
						{
							page.activate(part);
						}
					}
				}
				return display;
			}
		}
		IWorkbenchPage page = UIUtils.getActivePage();
		if (page != null)
		{
			IWorkbenchPart activePart = page.getActivePart();
			if (activePart != null)
			{
				IDataDisplay display = (IDataDisplay) activePart.getAdapter(IDataDisplay.class);
				if (display != null)
				{
					return display;
				}
			}
		}
		return null;
	}

}
