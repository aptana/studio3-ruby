/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser.NullWarnings;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.CommentNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.lexer.HeredocTerm;
import org.jrubyparser.lexer.Lexer;
import org.jrubyparser.lexer.Lexer.LexState;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.lexer.StrTerm;
import org.jrubyparser.lexer.SyntaxException;
import org.jrubyparser.lexer.SyntaxException.PID;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.ParserResult;
import org.jrubyparser.parser.ParserSupport;
import org.jrubyparser.parser.Tokens;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.CommonUtil;

public class RubySourcePartitionScanner implements IPartitionTokenScanner
{

	private static final String INDENTED_HEREDOC_MARKER_PREFIX = "<<-"; //$NON-NLS-1$
	private static final String HEREDOC_MARKER_PREFIX = "<<"; //$NON-NLS-1$
	private static final String DEFAULT_FILENAME = "filename"; //$NON-NLS-1$
	private static final String BEGIN = "=begin"; //$NON-NLS-1$

	private Lexer lexer;
	private ParserSupport parserSupport;
	private ParserResult result;
	private String fContents;
	private LexerSource lexerSource;
	private Reader reader;
	private int origOffset;
	private int origLength;
	private int fLength;
	private int fOffset;

	private List<QueuedToken> fQueue = new ArrayList<QueuedToken>();
	private String fContentType = RubySourceConfiguration.DEFAULT;
	private boolean inSingleQuote;
	private String fOpeningString;

	public RubySourcePartitionScanner()
	{
		lexer = new Lexer();
		parserSupport = new ParserSupport();
		ParserConfiguration config = new ParserConfiguration(0, CompatVersion.BOTH);
		parserSupport.setConfiguration(config);
		result = new ParserResult();
		parserSupport.setResult(result);
		lexer.setParserSupport(parserSupport);
		lexer.setWarnings(new NullWarnings());
	}

	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset)
	{
		reset();
		int myOffset = offset;
		if (contentType != null)
		{
			int diff = offset - partitionOffset;
			// backtrack to beginning of partition so we don't get in weird
			// state
			myOffset = partitionOffset;
			length += diff; // $codepro.audit.disable questionableAssignment
			this.fContentType = contentType;
			if (this.fContentType.equals(RubySourceConfiguration.SINGLE_LINE_COMMENT)
					|| this.fContentType.equals(IDocument.DEFAULT_CONTENT_TYPE))
			{
				this.fContentType = RubySourceConfiguration.DEFAULT;
			}
			// FIXME What if a heredoc with dynamic code inside is broken? contents will start with "}" rather than
			// expected
		}
		if (myOffset == -1)
		{
			myOffset = 0;
		}
		ParserConfiguration config = new ParserConfiguration(0, CompatVersion.BOTH);
		try
		{
			fContents = document.get(myOffset, length);
		}
		catch (BadLocationException e)
		{
			fContents = StringUtil.EMPTY;
		}
		reader = new BufferedReader(new StringReader(fContents)); // $codepro.audit.disable closeWhereCreated
		lexerSource = LexerSource.getSource(DEFAULT_FILENAME, reader, config);
		lexer.setSource(lexerSource);

		// FIXME If we're resuming after a string/regexp/command, set up lex state to be expression end.
		if (partitionOffset > 0)
		{
			try
			{
				ITypedRegion region = document.getPartition(partitionOffset - 1);
				if (RubySourceConfiguration.STRING_DOUBLE.equals(region.getType())
						|| RubySourceConfiguration.STRING_SINGLE.equals(region.getType())
						|| RubySourceConfiguration.REGULAR_EXPRESSION.equals(region.getType())
						|| RubySourceConfiguration.COMMAND.equals(region.getType()))
				{
					lexer.setLexState(LexState.EXPR_END);
				}
			}
			catch (BadLocationException e)
			{
				IdeLog.logError(RubyEditorPlugin.getDefault(), "Unable to get previous partition at offset: " + offset, //$NON-NLS-1$
						e);
			}
		}

		origOffset = myOffset;
		origLength = length;
	}

	public int getTokenLength()
	{
		return fLength;
	}

	public int getTokenOffset()
	{
		return fOffset;
	}

	public IToken nextToken()
	{
		if (!fQueue.isEmpty())
		{
			return popTokenOffQueue();
		}
		setOffset(getAdjustedOffset());
		setLength(0);
		IToken returnValue = createToken(fContentType);
		boolean isEOF = false;
		try
		{
			isEOF = !lexer.advance();
			if (isEOF)
			{
				returnValue = Token.EOF;
				// TODO Close the lexer's reader?
			}
			else
			{
				int lexerToken = lexer.token();
				if (isSingleVariableStringInterpolation(lexerToken))
				{
					return handleSingleVariableStringInterpolation();
				}
				else if (isStringInterpolation(lexerToken))
				{
					return handleStringInterpolation();
				}
				// Set up lexer to process embedded code in strings!
				else if (lexerToken == Tokens.tSTRING_BEG || lexerToken == Tokens.tREGEXP_BEG
						|| lexerToken == Tokens.tXSTRING_BEG || lexerToken == Tokens.tQWORDS_BEG
						|| lexerToken == Tokens.tWORDS_BEG || lexerToken == Tokens.tSYMBEG)
				{
					StrTerm strTerm = lexer.getStrTerm();
					if (strTerm != null)
					{
						strTerm.splitEmbeddedTokens();
					}
				}
				returnValue = getToken(lexerToken);
			}
			// TODO Are there ever comment nodes anymore? Do we need this code?!
			List<CommentNode> comments = result.getCommentNodes();
			if (comments != null && !comments.isEmpty())
			{
				parseOutComments(comments);
				// Queue the normal token we just ate up
				addQueuedToken(returnValue);
				comments.clear();
				return popTokenOffQueue();
			}
		}
		catch (SyntaxException se)
		{
			if ("embedded document meets end of file".equals(se.getMessage())) //$NON-NLS-1$
			{
				return handleUnterminedMultilineComment(se);
			}
			else if (se.getPid().equals(PID.STRING_MARKER_MISSING) || se.getPid().equals(PID.STRING_HITS_EOF))
			{
				return handleUnterminatedString(se);
			}

			if (lexerSource.getOffset() - origLength == 0)
			{
				// return eof if we hit a problem found at end of parsing
				return Token.EOF;
			}
			setLength(getAdjustedOffset() - fOffset);
			return createToken(RubySourceConfiguration.DEFAULT);
		}
		catch (IOException e)
		{
			IdeLog.logError(RubyEditorPlugin.getDefault(), e);
		}
		if (!isEOF)
		{
			setLength(getAdjustedOffset() - fOffset);
			// HACK End of heredocs are returning a zero length token for end of string that hoses us
			if (fLength == 0
					&& (returnValue.getData().equals(RubySourceConfiguration.STRING_DOUBLE) || returnValue.getData()
							.equals(RubySourceConfiguration.STRING_SINGLE)))
			{
				return nextToken();
			}
		}
		return returnValue;
	}

	private boolean isSingleVariableStringInterpolation(int lexerToken)
	{
		return !inSingleQuote && lexerToken == Tokens.tSTRING_DVAR;
	}

	private boolean isStringInterpolation(int lexerToken)
	{
		return !inSingleQuote && lexerToken == Tokens.tSTRING_DBEG;
	}

	private void setLength(int newLength)
	{
		fLength = newLength;
		Assert.isTrue(fLength >= 0);
	}

	private IToken handleUnterminedMultilineComment(SyntaxException se)
	{
		return handleUnterminatedPartition(se.getPosition().getStartOffset(),
				RubySourceConfiguration.MULTI_LINE_COMMENT);
	}

	private IToken handleUnterminatedString(SyntaxException se)
	{
		return handleUnterminatedPartition(se.getPosition().getStartOffset(), fContentType);
	}

	private IToken handleUnterminatedPartition(int start, String contentType)
	{
		// Add to the queue (at end), then try to just do the rest of
		// the file...
		// TODO recover somehow by removing this chunk out of the
		// fContents?
		int length = fContents.length() - start;
		QueuedToken qtoken = new QueuedToken(createToken(contentType), start + origOffset, length);
		if (fOffset == origOffset)
		{
			// If we never got to read in beginning contents
			RubySourcePartitionScanner scanner = new RubySourcePartitionScanner();
			String possible = new String(fContents.substring(0, start));
			IDocument document = new Document(possible);
			scanner.setRange(document, origOffset, possible.length());
			IToken token;
			while (!(token = scanner.nextToken()).isEOF()) // $codepro.audit.disable assignmentInCondition
			{
				push(new QueuedToken(token, scanner.getTokenOffset() + fOffset, scanner.getTokenLength()));
			}
		}
		push(qtoken);
		push(new QueuedToken(Token.EOF, start + origOffset + length, 0));
		return popTokenOffQueue();
	}

	private IToken handleSingleVariableStringInterpolation() throws IOException
	{
		addPoundToken();
		// let lexer scan the dynamic variable...
		int start = lexerSource.getOffset();
		lexer.nextToken();
		int end = lexerSource.getOffset();
		String content = fContents.substring(start, end);
		// push the dynamic var onto the queue
		push(new QueuedToken(createToken(RubySourceConfiguration.DEFAULT), fOffset, content.length()));
		setOffset(fOffset + content.length()); // move past dynamic var after we're done with queue

		return popTokenOffQueue();
	}

	private IToken handleStringInterpolation() throws IOException
	{
		// Can we just treat the arg token normally somehow?
		addPoundBraceToken();

		// We need to record the offset here, and the offset after asking for next token. Then grab code in between to
		// recurse on!
		int start = lexerSource.getOffset();

		// Seems like next token returned is considered string content and contains the interpolated code. We need to
		// dive into it specially.
		// FIXME JRuby parser lexer StringTerm doesn't properly handle nested strings inside DExpr. It just stops at
		// first '}'.

		lexer.nextToken();
		int end = lexerSource.getOffset();
		String content = fContents.substring(start, end);
		scanTokensInsideDynamicPortion(content);
		// Then lexer will resume by returning the "}" token as string content too

		return popTokenOffQueue();
	}

	public void setRange(IDocument document, int offset, int length)
	{
		setPartialRange(document, offset, length, RubySourceConfiguration.DEFAULT, 0);
	}

	private void reset()
	{
		// Close the lexer's reader?
		if (reader != null)
		{
			try
			{
				reader.close(); // $codepro.audit.disable closeInFinally
			}
			catch (IOException e) // $codepro.audit.disable emptyCatchClause
			{
				// ignore
			}
			reader = null;
		}

		lexer.reset();
		lexer.setState(LexState.EXPR_BEG);
		lexer.setPreserveSpaces(true);
		parserSupport.initTopLocalVariables();
		fQueue.clear();
		inSingleQuote = false;
		fContentType = RubySourceConfiguration.DEFAULT;
	}

	private void setOffset(int offset)
	{
		fOffset = offset;
	}

	private void addPoundToken()
	{
		addStringToken(1);// add token for the #
	}

	private void scanTokensInsideDynamicPortion(String content)
	{
		RubySourcePartitionScanner scanner = new RubySourcePartitionScanner();
		IDocument document = new Document(content);
		scanner.setRange(document, 0, content.length());
		IToken token;
		while (!(token = scanner.nextToken()).isEOF()) // $codepro.audit.disable assignmentInCondition
		{
			push(new QueuedToken(token, scanner.getTokenOffset() + fOffset, scanner.getTokenLength()));
		}
		setOffset(fOffset + content.length());
	}

	private void addPoundBraceToken()
	{
		addStringToken(2); // add token for the #{
	}

	private void addStringToken(int length)
	{
		String contentType = getStringType();
		if (RubySourceConfiguration.DEFAULT.equals(contentType))
		{
			contentType = RubySourceConfiguration.STRING_DOUBLE;
		}
		push(new QueuedToken(createToken(contentType), fOffset, length));
		setOffset(fOffset + length); // move past token
	}

	private void parseOutComments(List<CommentNode> comments)
	{
		for (CommentNode comment : comments)
		{
			int offset = correctOffset(comment);
			int length = comment.getContent().length();
			if (isCommentMultiLine(comment))
			{
				length = (origOffset + comment.getPosition().getEndOffset()) - offset;
				if (comment.getContent().charAt(0) != '=')
				{
					length++;
				}
			}
			IToken token = createToken(getContentType(comment));
			push(new QueuedToken(token, offset, length));
		}
	}

	private IToken popTokenOffQueue()
	{
		QueuedToken token = fQueue.remove(0);
		setOffset(token.getOffset());
		setLength(token.getLength());
		return token.getToken();
	}

	private IToken getToken(int i)
	{
		// We have an unresolved heredoc
		if (fContentType.equals(RubySourceConfiguration.STRING_DOUBLE) && insideHeredoc())
		{
			if (reachedEndOfHeredoc())
			{
				fContentType = RubySourceConfiguration.DEFAULT;
				inSingleQuote = false;
				return createToken(RubySourceConfiguration.STRING_DOUBLE);
			}
		}
		if (fContentType.equals(RubySourceConfiguration.MULTI_LINE_COMMENT) && i != Tokens.tWHITESPACE)
		{
			fContentType = RubySourceConfiguration.DEFAULT;
		}

		switch (i)
		{
			case RubyTokenScanner.SPACE:
			case Tokens.tWHITESPACE:
				return createToken(getStringType());
			case Tokens.tCOMMENT:
				return createToken(RubySourceConfiguration.SINGLE_LINE_COMMENT);
			case Tokens.tDOCUMENTATION:
				return createToken(fContentType = RubySourceConfiguration.MULTI_LINE_COMMENT);
			case Tokens.tSTRING_CONTENT:
				return createToken(fContentType = getStringType());
			case Tokens.tSTRING_BEG:
				String opening = getOpeningString();
				if ("%".equals(opening)) // space after percent sign, it's an operator //$NON-NLS-1$
				{
					return createToken(fContentType);
				}
				fOpeningString = opening;

				if (fOpeningString.equals("'") || fOpeningString.startsWith("%q")) //$NON-NLS-1$//$NON-NLS-2$
				{
					inSingleQuote = true;
					fContentType = RubySourceConfiguration.STRING_SINGLE;
				}
				else if (fOpeningString.startsWith(HEREDOC_MARKER_PREFIX)) // here-doc
				{
					// FIXME If it's a heredoc mid-line, don't change the content type!
					fOpeningString = generateOpeningStringForHeredocMarker(fOpeningString);
					if (fOpeningString.length() > 0 && fOpeningString.charAt(0) == '\'')
					{
						return createToken(RubySourceConfiguration.STRING_SINGLE);
					}
					return createToken(RubySourceConfiguration.STRING_DOUBLE);
				}
				else
				{
					fContentType = RubySourceConfiguration.STRING_DOUBLE;
				}
				return createToken(fContentType);
			case Tokens.tXSTRING_BEG:
				fOpeningString = getOpeningString();
				return createToken(fContentType = RubySourceConfiguration.COMMAND);
			case Tokens.tQWORDS_BEG:
			case Tokens.tWORDS_BEG:
				fOpeningString = getOpeningString();
				fContentType = RubySourceConfiguration.STRING_SINGLE;
				if (fOpeningString.length() > 1 && fOpeningString.charAt(0) == '%'
						&& Character.isUpperCase(fOpeningString.charAt(1)))
				{
					fContentType = RubySourceConfiguration.STRING_DOUBLE;
				}
				return createToken(fContentType);
			case Tokens.tSTRING_END:
				String oldContentType = fContentType;
				// FIXME What if this is a nested heredoc?
				// FIXME What if the old content type wass default? make it a string of some sort in string content...

				fContentType = RubySourceConfiguration.DEFAULT;
				// at end of string, the strterm is wiped, how can we tell what string type it was?
				return createToken(oldContentType);
			case Tokens.tREGEXP_BEG:
				fOpeningString = getOpeningString();
				return createToken(fContentType = RubySourceConfiguration.REGULAR_EXPRESSION);
			case Tokens.tREGEXP_END:
				fContentType = RubySourceConfiguration.DEFAULT;
				return createToken(RubySourceConfiguration.REGULAR_EXPRESSION);
			case Tokens.tSYMBEG:
				// Sometimes we need to add 1, sometimes two. Depends on if there's
				// a space preceding the ':'
				int charAt = fOffset - origOffset;
				char c = fContents.charAt(charAt);
				int nextCharOffset = (fOffset + 1);
				while (c == ' ') // skip past space if it's there
				{
					nextCharOffset++;
					c = fContents.charAt(++charAt);
				}
				if (fContents.length() <= charAt + 1)
				{
					return createToken(RubySourceConfiguration.DEFAULT);
				}
				if (c == '%') // %s syntax
				{
					fOpeningString = getOpeningString();
					fContentType = RubySourceConfiguration.STRING_SINGLE;
				}
				else if (c == ':') // normal syntax (i.e. ":symbol")
				{
					if (fContents.length() <= charAt + 1)
					{
						return createToken(RubySourceConfiguration.DEFAULT);
					}
					nextCharOffset++;
					c = fContents.charAt(++charAt);
					if (c == '"') // Check for :"symbol" syntax
					{
						fOpeningString = "\""; //$NON-NLS-1$
						push(new QueuedToken(createToken(RubySourceConfiguration.STRING_DOUBLE), nextCharOffset - 1, 1));
						fContentType = RubySourceConfiguration.STRING_DOUBLE;
					}
				}
				return createToken(RubySourceConfiguration.DEFAULT);
			default:
				return createToken(fContentType);
		}
	}

	/**
	 * Wrap generating tokens so we can re-use the same object for the same data.
	 * 
	 * @param data
	 * @return
	 */
	protected IToken createToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}

	private String getStringType()
	{
		StrTerm strTerm = lexer.getStrTerm();
		if (strTerm != null)
		{
			if (strTerm instanceof HeredocTerm)
			{
				strTerm.splitEmbeddedTokens();
			}
			if (strTerm.isSubstituting())
			{
				if (RubySourceConfiguration.REGULAR_EXPRESSION.equals(fContentType)
						|| RubySourceConfiguration.COMMAND.equals(fContentType))
				{
					return fContentType;
				}
				return RubySourceConfiguration.STRING_DOUBLE;
			}
			inSingleQuote = true;
			return RubySourceConfiguration.STRING_SINGLE;
		}
		return fContentType;
	}

	private boolean insideHeredoc()
	{
		return fOpeningString != null && fOpeningString.endsWith("\n"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
	}

	private boolean reachedEndOfHeredoc()
	{
		return fContents.startsWith(fOpeningString.trim(), (fOffset - origOffset));
	}

	private String generateOpeningStringForHeredocMarker(String marker)
	{
		if (marker.startsWith(INDENTED_HEREDOC_MARKER_PREFIX))
		{
			marker = marker.substring(3); // $codepro.audit.disable questionableAssignment
		}
		else if (marker.startsWith(HEREDOC_MARKER_PREFIX))
		{
			marker = marker.substring(2); // $codepro.audit.disable questionableAssignment
		}
		return marker + "\n"; //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
	}

	private String getOpeningString()
	{
		return getUntrimmedOpeningString().trim();
	}

	private String getUntrimmedOpeningString()
	{
		int start = fOffset - origOffset;
		List<CommentNode> comments = result.getCommentNodes();
		if (comments != null && !comments.isEmpty())
		{
			Node comment = comments.get(comments.size() - 1);
			int end = comment.getPosition().getEndOffset();
			start = end;
		}
		return new String(fContents.substring(start, lexerSource.getOffset()));
	}

	/**
	 * correct start offset, since when a line with nothing but spaces on it appears before comment, we get messed up
	 * positions
	 */
	private int correctOffset(CommentNode comment)
	{
		return origOffset + comment.getPosition().getStartOffset();
	}

	private boolean isCommentMultiLine(CommentNode comment)
	{
		String src = getSource(fContents, comment);
		return src != null && src.startsWith(BEGIN);
	}

	private String getContentType(CommentNode comment)
	{
		if (isCommentMultiLine(comment))
		{
			return RubySourceConfiguration.MULTI_LINE_COMMENT;
		}
		return RubySourceConfiguration.SINGLE_LINE_COMMENT;
	}

	private void addQueuedToken(IToken returnValue)
	{
		// grab end of last comment (last thing in queue)
		QueuedToken token = peek();
		setOffset(token.getOffset() + token.getLength());
		int length = getAdjustedOffset() - fOffset;
		if (length < 0)
		{
			length = 0;
		}
		push(new QueuedToken(returnValue, fOffset, length));
	}

	private QueuedToken peek()
	{
		return fQueue.get(fQueue.size() - 1);
	}

	private void push(QueuedToken token)
	{
		Assert.isTrue(token.getLength() >= 0);
		fQueue.add(token);
	}

	private int getAdjustedOffset()
	{
		return lexerSource.getOffset() + origOffset;
	}

	private static String getSource(String contents, Node node)
	{
		if (node == null || contents == null)
		{
			return null;
		}
		SourcePosition pos = node.getPosition();
		if (pos == null)
		{
			return null;
		}
		if (pos.getStartOffset() >= contents.length())
		{
			return null; // position is past end of our source
		}
		if (pos.getEndOffset() > contents.length())
		{
			return null; // end is past end of source
		}
		return new String(contents.substring(pos.getStartOffset(), pos.getEndOffset()));
	}
}
