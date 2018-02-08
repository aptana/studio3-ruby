/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.codeassist;

import java.net.URI;

import com.aptana.parsing.lexer.IRange;

public class ResolutionTarget
{
	private URI uri;
	private IRange range;

	public ResolutionTarget(URI uri, IRange range)
	{
		this.uri = uri;
		this.range = range;
	}

	public URI getURI()
	{
		return uri;
	}

	public IRange getRange()
	{
		return range;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof ResolutionTarget))
		{
			return false;
		}
		ResolutionTarget other = (ResolutionTarget) obj;
		if (range == null)
		{
			if (other.range != null)
			{
				return false;
			}
		}
		else if (!range.equals(other.range))
		{
			return false;
		}

		if (uri == null)
		{
			if (other.uri != null)
			{
				return false;
			}
		}
		else if (!uri.equals(other.uri))
		{
			return false;
		}
		return true;
	}

}
