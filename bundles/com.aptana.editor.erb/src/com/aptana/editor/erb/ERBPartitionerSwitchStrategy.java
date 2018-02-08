/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb;

import com.aptana.editor.common.PartitionerSwitchStrategy;

/**
 * @author Max Stepanov
 */
public class ERBPartitionerSwitchStrategy extends PartitionerSwitchStrategy
{

	private static ERBPartitionerSwitchStrategy instance;

	// @formatter:off
	private static final String[][] ERB_PAIRS = new String[][] { 
		{ IERBConstants.OPEN_INSERT_TAG, IERBConstants.CLOSE_NO_NEWLINE_TAG },
		{ IERBConstants.OPEN_INSERT_TAG, IERBConstants.CLOSE_W_NEWLINE_TAG },
		{ IERBConstants.OPEN_EVALUATE_TAG, IERBConstants.CLOSE_NO_NEWLINE_TAG },
		{ IERBConstants.OPEN_EVALUATE_TAG, IERBConstants.CLOSE_W_NEWLINE_TAG }
	};
	// @formatter:on

	/**
	 * 
	 */
	private ERBPartitionerSwitchStrategy()
	{
		super(ERB_PAIRS);
	}

	public synchronized static ERBPartitionerSwitchStrategy getDefault()
	{
		if (instance == null)
		{
			instance = new ERBPartitionerSwitchStrategy();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitionerSwitchStrategy#getSwitchTagPairs()
	 */
	public String[][] getSwitchTagPairs()
	{
		return ERB_PAIRS;
	}

}
