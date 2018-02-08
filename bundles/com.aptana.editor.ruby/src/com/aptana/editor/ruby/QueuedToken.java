/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.text.MessageFormat;

import org.eclipse.jface.text.rules.IToken;

/**
 * Stores an IToken along with it's offset and length so that we can do lookaheads and queue up tokens along with their
 * relative positions.
 * 
 * @author cwilliams
 */
class QueuedToken
{
	private IToken token;
	private int length;
	private int offset;

	QueuedToken(IToken token, int offset, int length)
	{
		this.token = token;
		this.length = length;
		this.offset = offset;
	}

	public int getLength()
	{
		return length;
	}

	public int getOffset()
	{
		return offset;
	}

	public IToken getToken()
	{
		return token;
	}

	@Override
	public String toString()
	{
		return MessageFormat.format("{0}: offset: {1}, length: {2}", getToken().getData(), getOffset(), getLength()); //$NON-NLS-1$
	}
}