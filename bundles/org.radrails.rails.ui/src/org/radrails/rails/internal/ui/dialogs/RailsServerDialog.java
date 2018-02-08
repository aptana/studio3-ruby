/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.radrails.rails.core.RailsProjectNature;
import org.radrails.rails.core.RailsServer;
import org.radrails.rails.ui.RailsUIPlugin;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.ui.IPropertyDialog;
import com.aptana.ui.IPropertyDialogProvider;
import com.aptana.webserver.core.WebServerCorePlugin;

public class RailsServerDialog extends TitleAreaDialog implements IPropertyDialog
{

	private static final int LABEL_WIDTH = 70;

	private RailsServer source;

	private Text nameText;
	private Text hostNameText;
	private Text portText;
	private Combo projectCombo;

	private ModifyListener modifyListener;

	/**
	 * @param parentShell
	 */
	public RailsServerDialog(Shell parentShell)
	{
		super(parentShell);
		setHelpAvailable(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ui.IPropertyDialog#getPropertySource()
	 */
	public Object getPropertySource()
	{
		return source;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ui.IPropertyDialog#setPropertySource(java.lang.Object)
	 */
	public void setPropertySource(Object element)
	{
		source = null;
		if (element instanceof RailsServer)
		{
			source = (RailsServer) element;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		setTitle(Messages.RailsServerDialog_Title);
		getShell().setText(Messages.RailsServerDialog_Message);

		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		PixelConverter converter = new PixelConverter(composite);
		composite.setLayout(GridLayoutFactory
				.swtDefaults()
				.margins(converter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
						converter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN))
				.spacing(converter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
						converter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING)).numColumns(3)
				.create());

		/* name of the server */
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.hint(new PixelConverter(label).convertHorizontalDLUsToPixels(LABEL_WIDTH), SWT.DEFAULT).create());
		label.setText(StringUtil.makeFormLabel(Messages.RailsServerDialog_NameLabel));

		nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults()
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH), SWT.DEFAULT).span(2, 1)
				.grab(true, false).create());

		/* Project we're running server for */
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.hint(new PixelConverter(label).convertHorizontalDLUsToPixels(LABEL_WIDTH), SWT.DEFAULT).create());
		label.setText(StringUtil.makeFormLabel(Messages.RailsServerDialog_ProjectLabel));

		projectCombo = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		projectCombo.setLayoutData(GridDataFactory.fillDefaults()
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH), SWT.DEFAULT).span(2, 1)
				.grab(true, false).create());
		// Populate combo with all the rails projects
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
		{
			try
			{
				if (project.isOpen() && project.hasNature(RailsProjectNature.ID))
				{
					projectCombo.add(project.getName());
				}
			}
			catch (CoreException e)
			{
				IdeLog.logError(RailsUIPlugin.getDefault(), e);
			}
		}
		if (projectCombo.getItemCount() > 0)
		{
			projectCombo.setText(projectCombo.getItems()[0]);
		}
		/* host/ip to bind to: 0.0.0.0, 127.0.0.1? */
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.hint(new PixelConverter(label).convertHorizontalDLUsToPixels(LABEL_WIDTH), SWT.DEFAULT).create());
		label.setText(StringUtil.makeFormLabel(Messages.RailsServerDialog_BindingLabel));

		hostNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		hostNameText.setLayoutData(GridDataFactory
				.swtDefaults()
				.hint(new PixelConverter(hostNameText)
						.convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH), SWT.DEFAULT).span(2, 1)
				.align(SWT.FILL, SWT.CENTER).grab(true, false).create());
		hostNameText.setText(RailsServer.DEFAULT_BINDING);

		/* Port: default is 3000 */
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.hint(new PixelConverter(label).convertHorizontalDLUsToPixels(LABEL_WIDTH), SWT.DEFAULT).create());
		label.setText(StringUtil.makeFormLabel(Messages.RailsServerDialog_PortLabel));

		portText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		portText.setLayoutData(GridDataFactory
				.swtDefaults()
				.hint(new PixelConverter(portText).convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH),
						SWT.DEFAULT).grab(true, false).create());
		portText.setText(Integer.toString(RailsServer.DEFAULT_PORT));

		// Set up values to reflect server we're editing.
		if (source != null)
		{
			String name = source.getName();
			nameText.setText((name != null) ? name : StringUtil.EMPTY);
			String host = source.getHostname();
			hostNameText.setText((host != null) ? host : RailsServer.DEFAULT_BINDING);
			portText.setText(Integer.toString(source.getPort()));
			IProject project = source.getProject();
			if (project != null)
			{
				projectCombo.setText(project.getName());
			}
		}

		addListeners();

		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse. swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		try
		{
			return super.createContents(parent);
		}
		finally
		{
			validate();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed()
	{
		if (!isValid())
		{
			return;
		}
		if (source != null)
		{
			source.setName(nameText.getText());
			source.setPort(Integer.parseInt(portText.getText()));
			source.setHost(hostNameText.getText());
			source.setProject(ResourcesPlugin.getWorkspace().getRoot().getProject(projectCombo.getText()));
		}
		WebServerCorePlugin.getDefault().saveServerConfigurations();
		super.okPressed();
	}

	private boolean isValid()
	{
		// TODO Ensure name is unique!
		if (nameText.getText().length() == 0)
		{
			setErrorMessage(Messages.RailsServerDialog_EmptyNameErrorMsg);
			return false;
		}

		// make sure port is an integer.
		try
		{
			Integer.parseInt(portText.getText());
		}
		catch (NumberFormatException e)
		{
			setErrorMessage(Messages.RailsServerDialog_InvalidPortErrorMsg);
			return false;
		}

		// TODO Make sure a rails project is selected and exists

		// clear errors and warnings, return valid
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	private void validate()
	{
		boolean valid = isValid();
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null)
		{
			okButton.setEnabled(valid);
		}
	}

	protected void addListeners()
	{
		if (modifyListener == null)
		{
			modifyListener = new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					validate();
				}
			};
		}
		nameText.addModifyListener(modifyListener);
		hostNameText.addModifyListener(modifyListener);
		portText.addModifyListener(modifyListener);
		projectCombo.addModifyListener(modifyListener);
	}

	protected void removeListeners()
	{
		if (modifyListener != null)
		{
			nameText.removeModifyListener(modifyListener);
			hostNameText.removeModifyListener(modifyListener);
			portText.removeModifyListener(modifyListener);
			projectCombo.removeModifyListener(modifyListener);
		}
	}

	public static class Provider implements IPropertyDialogProvider
	{

		/*
		 * (non-Javadoc)
		 * @see com.aptana.ui.IPropertyDialogProvider#createPropertyDialog(org.eclipse .jface.window.IShellProvider)
		 */
		public Dialog createPropertyDialog(IShellProvider shellProvider)
		{
			return new RailsServerDialog(shellProvider.getShell());
		}

	}

}
