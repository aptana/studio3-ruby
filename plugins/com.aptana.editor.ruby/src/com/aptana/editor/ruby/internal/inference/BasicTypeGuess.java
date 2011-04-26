/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.inference;

import java.text.MessageFormat;

import com.aptana.editor.ruby.inference.ITypeGuess;

public class BasicTypeGuess implements ITypeGuess
{
	private String type;
	private int confidence;

	public BasicTypeGuess(String type, int confidence)
	{
		this.type = type;
		this.confidence = confidence;
	}

	public int getConfidence()
	{
		return confidence;
	}

	public void setConfidence(int confidence)
	{
		this.confidence = confidence;
	}

	public String getType()
	{
		return type;
	}

	public String toString()
	{
		return MessageFormat.format("<{0}: {1}%>", type, confidence); //$NON-NLS-1$
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof BasicTypeGuess)
		{
			BasicTypeGuess other = (BasicTypeGuess) obj;
			return toString().equals(other.toString());
		}
		return false;
	}
}
