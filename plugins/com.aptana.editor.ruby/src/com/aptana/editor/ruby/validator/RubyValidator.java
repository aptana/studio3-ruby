package com.aptana.editor.ruby.validator;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.IRubyWarnings;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.Ruby18Parser;
import org.jrubyparser.parser.RubyParser;

import com.aptana.editor.common.validator.IValidationItem;
import com.aptana.editor.common.validator.IValidationManager;
import com.aptana.editor.common.validator.IValidator;

public class RubyValidator implements IValidator
{

	public RubyValidator()
	{
	}

	public List<IValidationItem> validate(String source, final URI path, final IValidationManager manager)
	{
		List<IValidationItem> items = new ArrayList<IValidationItem>();
		// TODO Check what the version of the current ruby interpreter is and use that to determine which parser compat
		// to use!
		ParserConfiguration config = new ParserConfiguration(1, CompatVersion.RUBY1_8);
		ParserSupport support = new ParserSupport();
		support.setConfiguration(config);
		RubyParser parser = new Ruby18Parser(support);
		// Hook up our own warning impl to grab them and add them as validation items!
		IRubyWarnings warnings = new IRubyWarnings()
		{
			public void warn(ID id, SourcePosition position, String message, Object... data)
			{
				int length = position.getEndOffset() - position.getStartOffset() + 1;
				manager.addWarning(message, position.getStartLine(), 0, length, path);
			}

			public void warn(ID id, String fileName, int lineNumber, String message, Object... data)
			{
				manager.addWarning(message, lineNumber, 0, 1, path);
			}

			public boolean isVerbose()
			{
				return true;
			}

			public void warn(ID id, String message, Object... data)
			{
				manager.addWarning(message, 1, 0, 1, path);
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
		LexerSource lexerSource = LexerSource.getSource(path.getPath(), new StringReader(source), config);
		try
		{
			parser.parse(config, lexerSource);
		}
		catch (SyntaxException e)
		{
			int start = e.getPosition().getStartOffset();
			int end = e.getPosition().getEndOffset();
			// FIXME How am I supposed to know we use the IMarker constants for severity here?!
			// items.add(new ValidationItem(IMarker.SEVERITY_ERROR, e.getMessage(), start, end - start + 1, e
			// .getPosition().getStartLine(), e.getPosition().getFile()));
			// FIXME So we return a list of validation items, but that's ignored, and instead we have to call add on the
			// manager?!
			// FIXME The argument order is different between these two seemingly similar methods!
			// FIXME This expects column offset for line, but we have the absolute offset from beginning of file...
			int lineNumber = e.getPosition().getStartLine();
			int charLineOffset = 0;
			try
			{
				charLineOffset = new Document(source).getLineOffset(lineNumber - 1);
			}
			catch (BadLocationException ble)
			{
			}
			manager.addError(e.getMessage(), lineNumber, charLineOffset, end - start + 1, path);
		}

		return items;
	}

}
