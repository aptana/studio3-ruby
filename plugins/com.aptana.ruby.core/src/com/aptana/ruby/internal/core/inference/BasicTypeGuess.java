/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.inference;

import java.text.MessageFormat;

import com.aptana.ruby.core.inference.ITypeGuess;

public class BasicTypeGuess implements ITypeGuess
{
	private String type;
	private int confidence;
	private boolean isClass;

	public BasicTypeGuess(String type, int confidence, boolean isClass)
	{
		this.type = type;
		this.confidence = confidence;
		this.isClass = isClass;
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

	public Boolean isModule()
	{
		return !isClass();
	}

	public Boolean isClass()
	{
		return isClass;
	}

	public String toString()
	{
		return MessageFormat.format("<{0}: {1}%>", type, confidence); //$NON-NLS-1$
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + confidence;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (!(obj instanceof BasicTypeGuess))
		{
			return false;
		}
		BasicTypeGuess other = (BasicTypeGuess) obj;
		if (confidence != other.confidence)
		{
			return false;
		}
		if (type == null)
		{
			if (other.type != null)
			{
				return false;
			}
		}
		else if (!type.equals(other.type))
		{
			return false;
		}
		return true;
	}
}
