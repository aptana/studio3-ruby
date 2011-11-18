/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core;

import java.util.ArrayList;
import java.util.Collection;

import org.jrubyparser.IRubyWarnings;
import org.jrubyparser.SourcePosition;

import com.aptana.parsing.ast.IParseError;
import com.aptana.parsing.ast.IParseError.Severity;
import com.aptana.parsing.ast.ParseError;

/**
 * An implementation that records all warnings thrown by Parser.
 * 
 * @author cwilliams
 */
public class CollectingRubyWarnings implements IRubyWarnings
{

	private String filename;
	private Collection<IParseError> warnings;

	public CollectingRubyWarnings(String fileName)
	{
		this.filename = fileName;
		this.warnings = new ArrayList<IParseError>();
	}

	public void warn(ID id, SourcePosition position, String message, Object... data)
	{
		int length = position.getEndOffset() - position.getStartOffset();
		warnings.add(createWarning(message, position.getStartLine(), position.getStartOffset(), length, filename));
	}

	public void warn(ID id, String fileName, int lineNumber, String message, Object... data)
	{
		warnings.add(createWarning(message, lineNumber, 0, 1, filename));
	}

	public boolean isVerbose()
	{
		return true;
	}

	public void warn(ID id, String message, Object... data)
	{
		warnings.add(createWarning(message, 1, 0, 1, filename));
	}

	public void warning(ID id, String message, Object... data)
	{
		warn(id, message, data);
	}

	public void warning(ID id, SourcePosition position, String message, Object... data)
	{
		warn(id, position, message, data);
	}

	public void warning(ID id, String fileName, int lineNumber, String message, Object... data)
	{
		warn(id, fileName, lineNumber, message, data);
	}

	private IParseError createWarning(String message, int lineNumber, int offset, int length, String path)
	{
		return new ParseError(offset, length, message, Severity.WARNING);
	}

	public Collection<IParseError> getWarnings()
	{
		return warnings;
	}
}
