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

	public URI getUri()
	{
		return uri;
	}

	public IRange getRange()
	{
		return range;
	}

}
