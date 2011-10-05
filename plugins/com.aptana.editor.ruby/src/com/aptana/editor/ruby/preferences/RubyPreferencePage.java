/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.editor.ruby.RubySourceEditor;

public class RubyPreferencePage extends CommonEditorPreferencePage
{

	private BooleanFieldEditor foldComments;
	private BooleanFieldEditor foldMethods;
	private BooleanFieldEditor foldBlocks;
	private BooleanFieldEditor foldInnerTypes;
	private Composite foldingGroup;

	/**
	 * RubyPreferencePage
	 */
	public RubyPreferencePage()
	{
		super();
		setDescription(Messages.RubyPreferencePage_Ruby_Page_Title);
		setPreferenceStore(RubyEditorPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected IEclipsePreferences getPluginPreferenceStore()
	{
		return EclipseUtil.instanceScope().getNode(RubyEditorPlugin.PLUGIN_ID);
	}

	@Override
	protected IPreferenceStore getChainedEditorPreferenceStore()
	{
		return RubySourceEditor.getChainedPreferenceStore();
	}

	@Override
	protected IEclipsePreferences getDefaultPluginPreferenceStore()
	{
		return EclipseUtil.defaultScope().getNode(RubyEditorPlugin.PLUGIN_ID);
	}

	@Override
	protected boolean getDefaultSpacesForTabs()
	{
		return IRubyPreferenceConstants.DEFAULT_RUBY_SPACES_FOR_TABS;
	}

	@Override
	protected int getDefaultTabWidth()
	{
		return IRubyPreferenceConstants.DEFAULT_RUBY_TAB_WIDTH;
	}

	@Override	
	protected Composite createFoldingOptions(Composite parent)
	{
		this.foldingGroup = super.createFoldingOptions(parent);

		// Initially fold these elements:
		Label initialFoldLabel = new Label(foldingGroup, SWT.WRAP);
		initialFoldLabel.setText(Messages.RubyPreferencePage_initial_fold_options_label);

		// Comments
		foldComments = new BooleanFieldEditor(IPreferenceConstants.INITIALLY_FOLD_COMMENTS,
				Messages.RubyPreferencePage_fold_comments_label, foldingGroup);
		addField(foldComments);

		// Methods
		foldMethods = new BooleanFieldEditor(IPreferenceConstants.INITIALLY_FOLD_METHODS,
				Messages.RubyPreferencePage_fold_methods_label, foldingGroup);
		addField(foldMethods);

		// Inner Types
		foldInnerTypes = new BooleanFieldEditor(IPreferenceConstants.INITIALLY_FOLD_INNER_TYPES,
				Messages.RubyPreferencePage_fold_inner_types_label, foldingGroup);
		addField(foldInnerTypes);

		// Blocks
		foldBlocks = new BooleanFieldEditor(IPreferenceConstants.INITIALLY_FOLD_BLOCKS,
				Messages.RubyPreferencePage_fold_blocks_label, foldingGroup);
		addField(foldBlocks);

		return foldingGroup;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getSource().equals(enableFolding))
		{
			boolean optionsEnabled = false;
			Object newValue = event.getNewValue();
			if (Boolean.TRUE.equals(newValue))
			{
				optionsEnabled = true;
			}

			foldComments.setEnabled(optionsEnabled, foldingGroup);
			foldMethods.setEnabled(optionsEnabled, foldingGroup);
			foldBlocks.setEnabled(optionsEnabled, foldingGroup);
			foldInnerTypes.setEnabled(optionsEnabled, foldingGroup);
		}
		super.propertyChange(event);
	}

}
