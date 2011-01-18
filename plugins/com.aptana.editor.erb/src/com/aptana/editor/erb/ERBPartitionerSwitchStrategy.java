/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.erb;

import com.aptana.editor.common.PartitionerSwitchStrategy;

/**
 * @author Max Stepanov
 *
 */
public class ERBPartitionerSwitchStrategy extends PartitionerSwitchStrategy {

	private static ERBPartitionerSwitchStrategy instance;
	
	private static final String[][] ERB_PAIRS = new String[][] {
		{ "<%=", "-%>" }, //$NON-NLS-1$ //$NON-NLS-2$
		{ "<%=", "%>" }, //$NON-NLS-1$ //$NON-NLS-2$
		{ "<%", "-%>" }, //$NON-NLS-1$ //$NON-NLS-2$
		{ "<%", "%>" } //$NON-NLS-1$ //$NON-NLS-2$
	};
	
	/**
	 * 
	 */
	private ERBPartitionerSwitchStrategy() {
		super(ERB_PAIRS);
	}
	
	public static ERBPartitionerSwitchStrategy getDefault() {
		if (instance == null) {
			instance = new ERBPartitionerSwitchStrategy();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitionerSwitchStrategy#getSwitchTagPairs()
	 */
	public String[][] getSwitchTagPairs() {
		return ERB_PAIRS;
	}

}
