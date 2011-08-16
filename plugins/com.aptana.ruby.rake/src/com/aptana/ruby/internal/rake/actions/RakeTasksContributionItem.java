/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.rake.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;

import com.aptana.ruby.rake.IRakeHelper;
import com.aptana.ruby.rake.RakePlugin;

public class RakeTasksContributionItem extends ContributionItem implements IWorkbenchContribution
{

	static final String RAKE_NAMESPACE_DELIMETER = ":"; //$NON-NLS-1$

	private Map<String, MenuManager> fNamespaces;
	private IServiceLocator serviceLocator;

	public RakeTasksContributionItem()
	{
	}

	public RakeTasksContributionItem(String id)
	{
		super(id);
	}

	@Override
	public void fill(Menu menu, int index)
	{
		IProject project = getProject();
		if (project == null)
		{
			return;
		}
		Map<String, String> tasks = getRakeHelper().getTasks(project, new NullProgressMonitor());

		fNamespaces = new HashMap<String, MenuManager>();
		// Please note that tehre's a lot of code mixed up in here to ensure that the menus, items and sub-menus all
		// appear alphabetically
		List<String> values = new ArrayList<String>(tasks.keySet());
		Collections.sort(values);
		for (String task : values)
		{
			String[] paths = task.split(RAKE_NAMESPACE_DELIMETER);
			if (paths.length == 1)
			{
				IAction action = new RunRakeAction(project, task, tasks.get(task));
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(menu, -1);
			}
			else
			{
				MenuManager manager = getOrCreate(paths);
				manager.add(new RunRakeAction(project, task, tasks.get(task)));
			}
		}
		values = new ArrayList<String>(fNamespaces.keySet());
		Collections.sort(values);
		Collections.reverse(values);
		for (String path : values)
		{
			MenuManager manager = fNamespaces.get(path);
			String[] parts = path.split(RAKE_NAMESPACE_DELIMETER);
			if (parts.length == 1)
			{
				int index2 = getInsertIndex(menu, manager);
				manager.fill(menu, index2);
			}
			else
			{
				MenuManager parent = getParent(parts);
				if (parent != null)
				{
					int index2 = getInsertIndex(parent, manager);
					parent.insert(index2, manager);
				}
				else
				{
					int index2 = getInsertIndex(menu, manager);
					manager.fill(menu, index2);
				}
			}
		}
	}

	private IProject getProject()
	{
		IResource resource = getSelectedResource();
		if (resource == null)
		{
			return null;
		}
		return resource.getProject();
	}

	private IResource getSelectedResource()
	{
		IEvaluationService evalService = (IEvaluationService) serviceLocator.getService(IEvaluationService.class);

		if (evalService != null)
		{
			IEvaluationContext context = evalService.getCurrentState();
			IWorkbenchPart activePart = (IWorkbenchPart) context.getVariable(ISources.ACTIVE_PART_NAME);
			if (activePart instanceof IEditorPart)
			{
				IEditorInput input = (IEditorInput) context.getVariable(ISources.ACTIVE_EDITOR_INPUT_NAME);
				return (IResource) input.getAdapter(IResource.class);
			}
			ISelection selection = (ISelection) context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection struct = (IStructuredSelection) selection;
				Object firstElement = struct.getFirstElement();
				if (firstElement instanceof IResource)
				{
					return (IResource) firstElement;
				}
				else if (firstElement instanceof IAdaptable)
				{
					IAdaptable adaptable = (IAdaptable) firstElement;
					return (IResource) adaptable.getAdapter(IResource.class);
				}
			}
		}
		return null;
	}

	/**
	 * For inserting submenus under submenus
	 * 
	 * @param parent
	 * @param item
	 * @return
	 */
	private int getInsertIndex(MenuManager parent, MenuManager item)
	{
		if (parent == null || item == null)
		{
			return 0;
		}
		String text = item.getMenuText();
		if (text == null)
		{
			return 0;
		}
		IContributionItem[] items = parent.getItems();
		if (items == null)
		{
			return 0;
		}
		int index = 0;
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == null)
				continue;
			if (items[i] instanceof ActionContributionItem)
			{
				ActionContributionItem actionItem = (ActionContributionItem) items[i];
				IAction action = actionItem.getAction();
				if (action == null)
					continue;
				String other = action.getText();
				if (text.compareTo(other) >= 0)
				{
					index = i + 1;
				}
				else
				{
					break;
				}
			}
		}
		return index;
	}

	/**
	 * For inserting submenus at first level.
	 * 
	 * @param parent
	 * @param item
	 * @return
	 */
	private int getInsertIndex(Menu parent, MenuManager item)
	{
		String text = item.getMenuText();
		MenuItem[] items = parent.getItems();
		int index = 0;
		for (int i = 0; i < items.length; i++)
		{
			String other = items[i].getText();
			if (text.compareTo(other) >= 0)
			{
				index = i + 1;
			}
			else
			{
				break;
			}
		}
		return index;
	}

	protected IRakeHelper getRakeHelper()
	{
		return RakePlugin.getDefault().getRakeHelper();
	}

	private MenuManager getParent(String[] parts)
	{
		String[] part = stripLastItem(parts);
		return fNamespaces.get(join(part));
	}

	private String join(String[] part)
	{
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < part.length; i++)
		{
			if (i != 0)
			{
				buffer.append(RAKE_NAMESPACE_DELIMETER);
			}
			buffer.append(part[i]);
		}
		return buffer.toString();
	}

	private MenuManager getOrCreate(String[] paths)
	{
		String[] part = stripLastItem(paths);
		MenuManager manager = fNamespaces.get(join(part));
		if (manager == null)
		{
			manager = new MenuManager(part[part.length - 1]);
			fNamespaces.put(join(part), manager);
		}
		return manager;
	}

	private String[] stripLastItem(String[] paths)
	{
		String[] part = new String[paths.length - 1];
		System.arraycopy(paths, 0, part, 0, part.length);
		return part;
	}

	public void initialize(IServiceLocator serviceLocator)
	{
		this.serviceLocator = serviceLocator;
	}
}
