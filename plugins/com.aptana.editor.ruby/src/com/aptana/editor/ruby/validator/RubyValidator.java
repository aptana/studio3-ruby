/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.validator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.IRubyWarnings;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.ParserSupport19;
import org.jrubyparser.parser.Ruby18Parser;
import org.jrubyparser.parser.Ruby19Parser;
import org.jrubyparser.parser.RubyParser;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.validator.IValidationItem;
import com.aptana.editor.common.validator.IValidationManager;
import com.aptana.editor.common.validator.IValidator;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.ruby.launching.RubyLaunchingPlugin;

public class RubyValidator implements IValidator
{

	public RubyValidator()
	{
	}

	public List<IValidationItem> validate(String source, final URI path, final IValidationManager manager)
	{
		List<IValidationItem> items = new ArrayList<IValidationItem>();
		// Check what the version of the current ruby interpreter is and use that to determine which parser compat
		// to use!
		CompatVersion version = CompatVersion.BOTH;
		IProject project = null;
		// get the working dir
		IPath workingDir = null;
		if (path != null && "file".equals(path.getScheme())) //$NON-NLS-1$
		{
			File file = new File(path);
			workingDir = Path.fromOSString(file.getParent());
		}
		// Find the project root for the file in question!
		if (workingDir != null)
		{
			IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(workingDir);
			if (container != null)
			{
				project = container.getProject();
			}
		}

		String rubyVersion = RubyLaunchingPlugin.getRubyVersionForProject(project);
		if (rubyVersion != null && rubyVersion.startsWith("ruby 1.9")) //$NON-NLS-1$
		{
			version = CompatVersion.RUBY1_9;
		}

		// TODO set up warnings/ version/line number/etc in RubyParseState and re-use RubyParser.parse(IParseState)!
		ParserConfiguration config = new ParserConfiguration(1, version);
		RubyParser parser;
		if (version == CompatVersion.RUBY1_8)
		{
			ParserSupport support = new ParserSupport();
			support.setConfiguration(config);
			parser = new Ruby18Parser(support);
		}
		else
		{
			ParserSupport19 support = new ParserSupport19();
			support.setConfiguration(config);
			parser = new Ruby19Parser(support);
		}

		// Hook up our own warning impl to grab them and add them as validation items!
		IRubyWarnings warnings = new IRubyWarnings()
		{
			public void warn(ID id, SourcePosition position, String message, Object... data)
			{
				int length = position.getEndOffset() - position.getStartOffset() + 1;
				manager.createWarning(message, position.getStartLine(), 0, length, path);
			}

			public void warn(ID id, String fileName, int lineNumber, String message, Object... data)
			{
				manager.createWarning(message, lineNumber, 0, 1, path);
			}

			public boolean isVerbose()
			{
				return true;
			}

			public void warn(ID id, String message, Object... data)
			{
				manager.createWarning(message, 1, 0, 1, path);
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
		};
		parser.setWarnings(warnings);
		Reader reader = new BufferedReader(new StringReader(source));
		LexerSource lexerSource = LexerSource.getSource((path == null) ? "filename" : path.getPath(), reader, config); //$NON-NLS-1$
		try
		{
			parser.parse(config, lexerSource);
		}
		catch (SyntaxException e)
		{
			int start = e.getPosition().getStartOffset();
			int end = e.getPosition().getEndOffset();
			// FIXME This seems to point at the token after the error...
			int lineNumber = e.getPosition().getStartLine();
			int charLineOffset = 0;
			try
			{
				int lineOffset = new Document(source).getLineOffset(lineNumber - 1);
				charLineOffset = start - lineOffset;
			}
			catch (BadLocationException ble)
			{
				IdeLog.logError(RubyEditorPlugin.getDefault(), "Unable to calculate offset of line: " + lineNumber, ble); //$NON-NLS-1$
			}
			if (start == end && end == source.length() && charLineOffset > 0)
			{
				charLineOffset--;
			}
			items.add(manager.createError(e.getMessage(), lineNumber, charLineOffset, end - start + 1, path));
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}

		return items;
	}

}
