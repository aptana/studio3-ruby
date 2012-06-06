/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.radrails.rails.internal.ui.commands;

import org.eclipse.debug.core.ILaunchManager;

/**
 * @author cwilliams
 */
public class DebugServerHandler extends RunServerHandler
{

	protected String getMode()
	{
		return ILaunchManager.DEBUG_MODE;
	}
}
