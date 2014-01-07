/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Chris
 */
public class RubySourcePartitionScannerTest
{

	private IDocument document;
	private RubySourcePartitionScanner scanner;
	private IDocumentPartitioner partitioner;

	@Before
	public void setUp() throws Exception
	{
		scanner = new RubySourcePartitionScanner();
	}

	@After
	public void tearDown() throws Exception
	{
		document = null;
		scanner = null;
		partitioner = null;
	}

	@Test
	public void testUnclosedInterpolationDoesntInfinitelyLoop()
	{
		getContentType("%[\"#{\"]", 0);
		assertTrue(true);
	}

	/**
	 * http://www.aptana.com/trac/ticket/5730
	 */
	@Test
	public void testBug5730()
	{
		getContentType("# Comment\n" + "=begin\n" + "puts 'hi'\n" + "=ne", 0);
		assertTrue(true);
	}

	/**
	 * http://www.aptana.com/trac/ticket/6052
	 */
	@Test
	public void testBug6052()
	{
		getContentType("# Use this class to maintain the decision process\n" + "# To choose a next aprt of text etc.\n"
				+ "class Logic\n" + "=begin\n" + "  def initialize\n" + "  end\n"
				+ "############################################################################################\n"
				+ "  private\n"
				+ "############################################################################################ \n"
				+ "end", 0);
		assertTrue(true);
	}

	@Test
	public void testDivideAndRegexInHeredocInterpolation()
	{
		getContentType("test.execute <<END\n" + "#{/[0-9]+/ / 5}\n" + "END", 0);
		assertTrue(true);
	}

	@Test
	public void testPartitioningOfSingleLineComment()
	{
		String source = "# This is a comment\n";

		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 0);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 1);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 18);
	}

	@Test
	public void testRecognizeSpecialCase()
	{
		String source = "a,b=?#,'This is not a comment!'\n";

		assertContentType(RubySourceConfiguration.DEFAULT, source, 5);
		assertContentType(RubySourceConfiguration.DEFAULT, source, 6);
	}

	@Test
	public void testMultilineComment_1()
	{
		String source = "=begin\nComment\n=end";

		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, 10);
	}

	@Test
	public void testMultilineComment_2()
	{
		String source = "=begin\n" + "  for multiline comments, the =begin and =end must\n"
				+ "  appear in the first column\n" + "=end";
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, source.length() / 2);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, source.length() - 2);
	}

	@Test
	public void testMultilineCommentNotOnFirstColumn()
	{
		String source = " =begin\nComment\n=end";

		setUp(source);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 1, 1); // =
		assertToken(RubySourceConfiguration.DEFAULT, 2, 5); // begin
		assertToken(RubySourceConfiguration.DEFAULT, 7, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 8, 7); // Comment
		assertToken(RubySourceConfiguration.DEFAULT, 15, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 16, 1); // =
		assertToken(RubySourceConfiguration.DEFAULT, 17, 3); // end
	}

	@Test
	public void testRecognizeDivision()
	{
		String source = "1/3 #This is a comment\n";

		assertContentType(RubySourceConfiguration.DEFAULT, source, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, source, 3);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 5);
	}

	@Test
	public void testRecognizeOddballCharacters()
	{
		String source = "?\" #comment\n";

		assertContentType(RubySourceConfiguration.DEFAULT, source, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, source, 2);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 5);

		source = "?' #comment\n";

		assertContentType(RubySourceConfiguration.DEFAULT, source, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, source, 2);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 5);

		source = "?/ #comment\n";

		assertContentType(RubySourceConfiguration.DEFAULT, source, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, source, 2);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, 5);
	}

	@Test
	public void testPoundCharacterIsntAComment()
	{
		String source = "?#";
		assertContentType(RubySourceConfiguration.DEFAULT, source, 1);
	}

	@Test
	public void testSinglelineCommentJustAfterMultilineComment()
	{
		String source = "=begin\nComment\n=end\n# this is a singleline comment\n";

		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, 0);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, source, 10);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, source, source.length() - 5);
	}

	@Test
	public void testMultipleCommentsInARow()
	{
		String code = "# comment 1\n# comment 2\nclass Chris\nend\n";

		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 6);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 17);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 26);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 29);
	}

	@Test
	public void testCommentAfterEnd()
	{
		String code = "class Chris\nend # comment\n";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 12);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 17);
	}

	@Test
	public void testCommentAfterEndWhileEditing()
	{
		String code = "=begin\r\n" + "c\r\n" + "=end\r\n" + "#hmm\r\n" + "#comment here why is ths\r\n"
				+ "class Chris\r\n" + "  def thing\r\n" + "  end  #ocmm \r\n" + "end";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 76);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 83);
	}

	@Test
	public void testCommentAtEndOfLineWithStringAtBeginning()
	{
		String code = "hash = {\n" + "  \"string\" => { # comment\n" + "    123\n" + "  }\n" + "}";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 4);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 6);

		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 11);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 12);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 18);

		assertContentType(RubySourceConfiguration.DEFAULT, code, 19);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 22);

		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 25);
	}

	@Test
	public void testLinesWithJustSpaceBeforeComment()
	{
		String code = "  \n" + "  # comment\n" + "  def method\n" + "    \n" + "  end";
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 5);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 17);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 20);
	}

	@Test
	public void testCommentsWithAlotOfPrecedingSpaces()
	{
		String code = "                # We \n" + "                # caller-requested until.\n" + "return self\n";
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 16);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 64);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 70);
	}

	@Test
	public void testCodeWithinString()
	{
		String code = "string = \"here's some code: #{1} there\"";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2); // st'r'...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 10); // "'h'er...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 28); // '#'{1...
		assertContentType(RubySourceConfiguration.DEFAULT, code, 30); // '1'} t...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 31); // '}' th...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 35); // th'e're..
	}

	@Test
	public void testCodeWithinSingleQuoteString()
	{
		String code = "string = 'here s some code: #{1} there'";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2); // st'r'...
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 10); // "'h'er...
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 28); // '#'{1...
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 30); // '1'} t...
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 31); // '}' th...
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 35); // th'e're..
	}

	@Test
	public void testVariableSubstitutionWithinString()
	{
		String code = "string = \"here's some code: #$global there\"";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2); // st'r'...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 10); // "'h'er...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 28); // '#'$glo...
		assertContentType(RubySourceConfiguration.DEFAULT, code, 29); // '$'global
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 36);// ' 'there...
	}

	@Test
	public void testStringWithinCodeWithinString()
	{
		String code = "string = \"here's some code: #{var = 'string'} there\"";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2); // st'r'...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 10); // "'h'er...
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 28); // '#'{var
		assertContentType(RubySourceConfiguration.DEFAULT, code, 30); // 'v'ar =
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 36); // '''string
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 46); // 't'here
	}

	// public void testStringWithEndBraceWithinCodeWithinString()
	// {
	// // FIXME JRubyparser's lexer doesn't properly handle nested strings inside DExpr, so it grabs the wrong } as the
	// // end of the DExpr
	// String code = "string = \"here's some code: #{var = '}'; 1} there\"";
	// setUp(code);
	//
	// assertToken(RubySourceConfiguration.DEFAULT, 0, 6); // string
	// assertToken(RubySourceConfiguration.DEFAULT, 6, 1); //
	// assertToken(RubySourceConfiguration.DEFAULT, 7, 1); // =
	// assertToken(RubySourceConfiguration.DEFAULT, 8, 1); //
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 9, 1); // "
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 10, 18); // here's some code:
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 28, 2); // #{
	// assertToken(RubySourceConfiguration.DEFAULT, 30, 3); // var
	// assertToken(RubySourceConfiguration.DEFAULT, 33, 1); //
	// assertToken(RubySourceConfiguration.DEFAULT, 34, 1); // =
	// assertToken(RubySourceConfiguration.DEFAULT, 35, 1); //
	// assertToken(RubySourceConfiguration.STRING_SINGLE, 36, 1); // '
	// assertToken(RubySourceConfiguration.STRING_SINGLE, 37, 1); // }
	// assertToken(RubySourceConfiguration.STRING_SINGLE, 38, 1); // '
	// assertToken(RubySourceConfiguration.DEFAULT, 39, 1); // ;
	// assertToken(RubySourceConfiguration.DEFAULT, 40, 1); //
	// assertToken(RubySourceConfiguration.DEFAULT, 41, 1); // 1
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 42, 1); // }
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 43, 6); // there
	// assertToken(RubySourceConfiguration.STRING_DOUBLE, 49, 1); // "
	// }

	@Test
	public void testRegex()
	{
		String code = "regex = /hi there/";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2); // re'g'ex
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 9); // '/'hi the
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 11); // /h'i' the
	}

	@Test
	public void testRegexWithDynamicCode()
	{
		String code = "/\\.#{Regexp.escape(extension.to_s)}$/ # comment";
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 3);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 38); // '#' co
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 40); // # 'c'ommen
	}

	@Test
	public void testEscapedCharactersAndSingleQuoteInsideDoubleQuote()
	{
		String code = "quoted_value = \"'#{quoted_value[1..-2].gsub(/\\'/, \"\\\\\\\\'\")}'\" if quoted_value.include?(\"\\\\\\'\") # (for ruby mode) \"\n"
				+ "quoted_value";
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 16); //
		assertContentType(RubySourceConfiguration.DEFAULT, code, 19); // #{'q'uoted
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 44); // /
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 51); // "\
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 59); // '" if
		assertContentType(RubySourceConfiguration.DEFAULT, code, 62); // 'i'f quoted_
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 87); // include?('"'
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 95); // '#' (for ruby mode)
		assertContentType(RubySourceConfiguration.DEFAULT, code, code.length() - 3);
	}

	@Test
	public void testSingleQuotedString()
	{
		String code = "require 'commands/server'";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 1);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 8);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 9);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 17);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 18);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 24);
	}

	@Test
	public void testCommands()
	{
		// @formatter:off
		String code = 
				"if OPTIONS[:detach]\n" + 
				"  `mongrel_rails #{parameters.join(\" \")} -d`\n" + 
				"else\n" +
				"  ENV[\"RAILS_ENV\"] = OPTIONS[:environment]";
		// @formatter:on

		setUp(code);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 2); // if
		assertToken(RubySourceConfiguration.DEFAULT, 2, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 3, 7); // OPTIONS
		assertToken(RubySourceConfiguration.DEFAULT, 10, 1); // [
		assertToken(RubySourceConfiguration.DEFAULT, 11, 1); // :
		assertToken(RubySourceConfiguration.DEFAULT, 12, 6); // detach
		assertToken(RubySourceConfiguration.DEFAULT, 18, 1); // ]
		assertToken(RubySourceConfiguration.DEFAULT, 19, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 20, 2); //
		assertToken(RubySourceConfiguration.COMMAND, 22, 1); // `
		assertToken(RubySourceConfiguration.COMMAND, 23, 14); // mongrel_rails
		assertToken(RubySourceConfiguration.COMMAND, 37, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 39, 10); // parameters
		assertToken(RubySourceConfiguration.DEFAULT, 49, 1); // .
		assertToken(RubySourceConfiguration.DEFAULT, 50, 4); // join
		assertToken(RubySourceConfiguration.DEFAULT, 54, 1); // (
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 55, 1); // "
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 56, 1); //
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 57, 1); // "
		assertToken(RubySourceConfiguration.DEFAULT, 58, 1); // )
		assertToken(RubySourceConfiguration.COMMAND, 59, 1); // }
		assertToken(RubySourceConfiguration.COMMAND, 60, 3); // -d
		assertToken(RubySourceConfiguration.COMMAND, 63, 1); // `
		assertToken(RubySourceConfiguration.DEFAULT, 64, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 65, 4); // else
		assertToken(RubySourceConfiguration.DEFAULT, 69, 1); // \n
	}

	@Test
	public void testPercentXCommand()
	{
		String code = "if (@options.do_it)\n" + "  %x{#{cmd}}\n" + "end\n";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 1); // i'f'
		assertContentType(RubySourceConfiguration.COMMAND, code, 22); // '%'x
		assertContentType(RubySourceConfiguration.COMMAND, code, 24); // %x'{'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 27); // 'c'md
		assertContentType(RubySourceConfiguration.COMMAND, code, 30); // cmd'}'
		assertContentType(RubySourceConfiguration.COMMAND, code, 31); // cmd}'}'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 33); // 'e'nd
	}

	@Test
	public void testHeredocInArgumentList()
	{
		// @formatter:off
		String code = 
				"connection.delete <<-end_sql, \"#{self.class.name} Destroy\"\n" +
				"  DELETE FROM #{self.class.table_name}\n" +
				"  WHERE #{connection.quote_column_name(self.class.primary_key)} = #{quoted_id}\n" + 
				"end_sql\n";
		// @formatter:on

		assertContentType(RubySourceConfiguration.DEFAULT, code, 1); // c'o'nnection
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 18); // '<'<-end_sql
		assertContentType(RubySourceConfiguration.DEFAULT, code, 33); // 's'elf.class
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 48); // '}' Destroy
		assertContentType(RubySourceConfiguration.DEFAULT, code, 75); // 's'elf.class
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 96); // name'}'\n
		assertContentType(RubySourceConfiguration.DEFAULT, code, 108); // {'c'onnection
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 160); // '}' =
		assertContentType(RubySourceConfiguration.DEFAULT, code, 166); // {'q'uoted
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 175); // _id'}'\n
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 177); // 'e'nd_sql
	}

	@Test
	public void testScaryString()
	{
		// @formatter:off
		String code = 
				"puts \"match|#{$`}<<#{$&}>>#{$'}|\"\n" + 
				"pp $~";
		// @formatter:on
		assertContentType(RubySourceConfiguration.DEFAULT, code, 1); // p'u'ts
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 5); // '"'match
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 13); // #'{'$`
		assertContentType(RubySourceConfiguration.DEFAULT, code, 14); // $
		assertContentType(RubySourceConfiguration.DEFAULT, code, 15); // `
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 16); // }
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 20); // {
		assertContentType(RubySourceConfiguration.DEFAULT, code, 21); // $
		assertContentType(RubySourceConfiguration.DEFAULT, code, 22); // &
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 23); // }
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 27); // {
		assertContentType(RubySourceConfiguration.DEFAULT, code, 28); // $
		assertContentType(RubySourceConfiguration.DEFAULT, code, 29); // '
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 30); // }
		assertContentType(RubySourceConfiguration.DEFAULT, code, 34); // 'p'p $~
	}

	// TODO Handle yet even wackier heredoc syntax:
	// http://blog.jayfields.com/2006/12/ruby-multiline-strings-here-doc-or.html

	@Test
	public void testNestedHeredocs()
	{
		// @formatter:off
		String code = 
				"methods += <<-BEGIN + nn_element_def(element) + <<-END\n" +
				"  def #{element.downcase}(attributes = {})\n" + 
				"BEGIN\n" + 
				"  end\n" + 
				"END\n" + 
				"\n" +
				"puts :symbol\n";
		// @formatter:on

		setUp(code);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 7); // methods
		assertToken(RubySourceConfiguration.DEFAULT, 7, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 8, 2); // +=
		assertToken(RubySourceConfiguration.DEFAULT, 10, 1); //
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 11, 8); // <<-BEGIN
		assertToken(RubySourceConfiguration.DEFAULT, 19, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 20, 1); // +
		assertToken(RubySourceConfiguration.DEFAULT, 21, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 22, 14); // nn_element_def
		assertToken(RubySourceConfiguration.DEFAULT, 36, 1); // (
		assertToken(RubySourceConfiguration.DEFAULT, 37, 7); // element
		assertToken(RubySourceConfiguration.DEFAULT, 44, 1); // )
		assertToken(RubySourceConfiguration.DEFAULT, 45, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 46, 1); // +
		assertToken(RubySourceConfiguration.DEFAULT, 47, 1); //
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 48, 6); // <<-END
		assertToken(RubySourceConfiguration.DEFAULT, 54, 1); // \n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 55, 6); // def
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 61, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 63, 7); // element
		assertToken(RubySourceConfiguration.DEFAULT, 70, 1); // .
		assertToken(RubySourceConfiguration.DEFAULT, 71, 8); // downcase
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 79, 19); // }(attributes = {})\n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 98, 6); // BEGIN\n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 104, 6); // end\n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 110, 4); // END\n
		assertToken(RubySourceConfiguration.DEFAULT, 114, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 115, 4); // puts
		assertToken(RubySourceConfiguration.DEFAULT, 119, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 120, 1); // :
		assertToken(RubySourceConfiguration.DEFAULT, 121, 6); // symbol
		assertToken(RubySourceConfiguration.DEFAULT, 127, 1); // \n
	}

	@Test
	public void testBug5448()
	{
		String code = "m.class_collisions controller_class_path,       \"#{controller_class_name}Controller\", # Sessions Controller\r\n"
				+ "    \"#{controller_class_name}Helper\"";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 1);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 40);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 48);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 50);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 51);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 71);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 72);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 83);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 84);
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, code, 86);
	}

	@Test
	public void testBug5208()
	{
		String code = "=begin\r\n" + "  This is a comment\r\n" + "=end\r\n" + "require 'gosu'";
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, code, 0);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, code, 32); // =en'd'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 36); // 'r'equire
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 44);
	}

	@Test
	public void testROR255()
	{
		String code = "\"all_of(#{@matchers.map { |matcher| matcher.mocha_inspect }.join(\", \") })\"";
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 1); // "'a'll_of
		assertContentType(RubySourceConfiguration.DEFAULT, code, 10); // #{'@'match
		assertContentType(RubySourceConfiguration.DEFAULT, code, 60); // }.'j'oin
		assertContentType(RubySourceConfiguration.DEFAULT, code, 70);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 71); // ) '}')"
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 72); // ) }')'"
	}

	@Test
	public void testROR950_1()
	{
		String code = "config.load_paths += [\"#{RAILS_ROOT}/vendor/plugins/sql_session_store/lib\"]";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0); // 'c'onfig
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 22); // ['"'#{
		assertContentType(RubySourceConfiguration.DEFAULT, code, 25); // #{'R'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 34);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 36); // '/'vendor
	}

	@Test
	public void testROR950_2()
	{
		String code = "config.load_paths += %W(#{RAILS_ROOT}/vendor/plugins/sql_session_store/lib)";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0); // 'c'onfig
		assertContentType(RubySourceConfiguration.DEFAULT, code, 26); // #{'R'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 35); // OO'T'}
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 37); // '/'vendor
	}

	@Test
	public void testSmallQString()
	{
		String code = "%q(string)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_SINGLE, code, i);
		}
	}

	@Test
	public void testLargeQString()
	{
		String code = "%Q(string)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, i);
		}
	}

	@Test
	public void testPercentXCommand2()
	{
		String code = "%x(command)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.COMMAND, code, i);
		}
	}

	@Test
	public void testPercentSyntax()
	{
		String code = "%(unknown)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, i);
		}
	}

	@Test
	public void testPercentSSymbol()
	{
		String code = "%s(symbol)";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 1);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 2);
		for (int i = 3; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_SINGLE, code, i);
		}
	}

	@Test
	public void testSmallWString()
	{
		String code = "%w(string two)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_SINGLE, code, i);
		}
	}

	@Test
	public void testLargeWString()
	{
		String code = "%W(string two)";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, i);
		}
	}

	@Test
	public void testSingleQuotedHeredoc()
	{
		String code = "heredoc =<<'END'\n  hello world!\nEND\n";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 8);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 9);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 32);
	}

	// TODO Write tests for nested heredocs and heredocs in middle of line with the heredoc being single-quoted (or
	// mixture)

	@Test
	public void testROR975()
	{
		String code = "exist_sym = :\"#{row['PROVVPI']}.#{row['vpi']}.#{row['vci']}.#{row['seq_num']}\"";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0); // 'e'xist
		assertContentType(RubySourceConfiguration.DEFAULT, code, 12); // ':'
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 13); // '"'#
	}

	@Test
	public void testROR1278()
	{
		// @formatter:off
		String code = 
				"hash = {:user=>{:emailaddr=>'4mydemo@4mypasswords.com', :login=>'4MyDemo',\n" +
				":password=>'password', :password_confirmation=>'password'},\n" +
				":userpin => {:pin =>'test', :pin_confirmation=>'test'}\n" + 
				"}";
		// @formatter:on
		setUp(code, 136, 19, 132);
		IToken token = scanner.nextToken();
		assertEquals(RubySourceConfiguration.DEFAULT, token.getData());
	}

	@Test
	public void testCGILib()
	{
		String code = "warn \"Warning:#{caller[0].sub(/'/, '')}: cgi-lib is deprecated after Ruby 1.8.1; use cgi instead\"";
		// warn
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		// "Warning
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 5);
		// caller
		assertContentType(RubySourceConfiguration.DEFAULT, code, 16);
		// /'/
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 30);
		assertContentType(RubySourceConfiguration.REGULAR_EXPRESSION, code, 32);
		// ,
		assertContentType(RubySourceConfiguration.DEFAULT, code, 33);
		// ''
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 35);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 36);
		// )
		assertContentType(RubySourceConfiguration.DEFAULT, code, 37);
		// }: cgi-lib
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 38);
	}

	@Test
	public void testROR1307StringSymbols()
	{
		// @formatter:off
		String code = 
				"class AbiBuilderTest\n" +
				"  def method\n" +
				"    assert_equal array, Abi.send(:\"to_#{type}\", number), \"#{type} failed on Ruby number -> array\"\n\n" +
				"    assert_equal [255], Abi.signed_to_udword(-1), 'Failed on signed_to_udword'\n" + 
				"  end\n" +
				"\n" +
				"  def test_packed_number_encoding\n" + 
				"    packed = { :p => 0x123, :q => 0xABCDEF, :n => 5 }\n" +
				"    gold_packed = [0x02, 0x00, 0x03, 0x00, 0x01, 0x00, 0x23, 0x01, 0xEF, 0xCD, 0xAB, 0x05]\n" +
				"    assert_equal gold_packed, Abi.to_packed(packed), 'packed'\n" + 
				"  end\n" + 
				"end";
		// @formatter:on

		// class
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		// send'('
		assertContentType(RubySourceConfiguration.DEFAULT, code, 66);
		// send(':'
		assertContentType(RubySourceConfiguration.DEFAULT, code, 67);
		// "to_#{
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 68);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 73);
		// type
		assertContentType(RubySourceConfiguration.DEFAULT, code, 74);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 77);
		// }"
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 78);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 79);
		// , number)
		assertContentType(RubySourceConfiguration.DEFAULT, code, 80);
		// "#{
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 91);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 93);
		// type
		assertContentType(RubySourceConfiguration.DEFAULT, code, 94);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 97);
		// } failed on Ruby number -> array"
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 98);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 130);
		// assert_equal
		assertContentType(RubySourceConfiguration.DEFAULT, code, 137);
		// 'Failed on signed_to_udword'
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 183);
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 210);
	}

	@Test
	public void testROR1248()
	{
		String code = "`wc -l \"#{ f.gsub('\"','\\\"') }\"`\n\n" + "puts \"syntax hilighting broken here\"";
		// `wc -l "#{
		assertContentType(RubySourceConfiguration.COMMAND, code, 0); // `
		assertContentType(RubySourceConfiguration.COMMAND, code, 7); // "
		assertContentType(RubySourceConfiguration.COMMAND, code, 9); // {
		// f.gsub(
		assertContentType(RubySourceConfiguration.DEFAULT, code, 11); // f
		assertContentType(RubySourceConfiguration.DEFAULT, code, 17); // (
		// '"'
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 18); // '
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 20); // '
		// ,
		assertContentType(RubySourceConfiguration.DEFAULT, code, 21); // ,
		// '\"'
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 22); // '
		assertContentType(RubySourceConfiguration.STRING_SINGLE, code, 25); // '
		// )
		assertContentType(RubySourceConfiguration.DEFAULT, code, 26);
		// }"`
		assertContentType(RubySourceConfiguration.COMMAND, code, 28);
		assertContentType(RubySourceConfiguration.COMMAND, code, 30);
		// puts
		assertContentType(RubySourceConfiguration.DEFAULT, code, 33);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 36);
		// "syntax hilighting broken here"
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 38);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 68);
	}

	@Test
	public void testCGI()
	{
		// @formatter:off
		String src = 
				"module TagMaker # :nodoc:\n" + 
				"		\n" + 
				"    # Generate code for an element with required start and end tags.\n" + 
				"    #\n" +
				"    #   - -\n" + 
				"    def nn_element_def(element)\n" + 
				"      nOE_element_def(element, <<-END)\n" +
				"          if block_given?\n" + 
				"            yield.to_s\n" + 
				"          else\n" +
				"            \"\"\n" + 
				"          end +\n" + 
				"          \"</#{element.upcase}>\"\n" + 
				"      END\n" +
				"    end\n" + 
				"    \n" + 
				"    # Generate code for an empty element.\n" + 
				"    #\n" +
				"    #   - O EMPTY\n" + 
				"    def nOE_element_def(element, append = nil)\n" + 
				"   end\n" + 
				"end";
		// @formatter:on

		// module TagMaker # :nodoc:
		assertContentType(RubySourceConfiguration.DEFAULT, src, 0); // m
		assertContentType(RubySourceConfiguration.SINGLE_LINE_COMMENT, src, 16); // #

		// nOE_element_def(element, <<-END)
		assertContentType(RubySourceConfiguration.DEFAULT, src, 177); // ,
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 179); // <
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 184); // D
		assertContentType(RubySourceConfiguration.DEFAULT, src, 185); // )

		// </#{element.upcase}>
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 296); // {
		assertContentType(RubySourceConfiguration.DEFAULT, src, 297); // e
		assertContentType(RubySourceConfiguration.DEFAULT, src, 310); // e
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 311); // }

		// END
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 321); // E
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, src, 323); // D

		// end
		assertContentType(RubySourceConfiguration.DEFAULT, src, 329); // e
	}

	@Test
	public void testBlockComment()
	{
		String code = "=begin\n=end";
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, code, 0);
		assertContentType(RubySourceConfiguration.MULTI_LINE_COMMENT, code, code.length() - 1);
	}

	@Test
	public void testSymbolBeginningWithS()
	{
		String code = "hash[:symbol]";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.DEFAULT, code, i);
		}
	}

	@Test
	public void testSymbolWithString()
	{
		String code = "hash[:\"symbol\"]";
		assertContentType(RubySourceConfiguration.DEFAULT, code, 0);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 5);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 6);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 13);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 14);
	}

	@Test
	public void testSymbolHitsEndOfFile()
	{
		String code = "hash[:";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.DEFAULT, code, i);
		}
	}

	@Test
	public void testPercentSSymbolHitsEndOfFile()
	{
		String code = "hash[%s";
		for (int i = 0; i < code.length(); i++)
		{
			assertContentType(RubySourceConfiguration.DEFAULT, code, i);
		}
	}

	@Test
	public void testSymbolStringHitsEndOfFile()
	{
		String code = "hash[:\"";
		for (int i = 0; i < code.length() - 1; i++)
		{
			assertContentType(RubySourceConfiguration.DEFAULT, code, i);
		}
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, code.length() - 1);
	}

	@Test
	public void testHeredoc()
	{
		// @formatter:off
		String code = 
				"def index\n" +
				"    heredoc =<<-END\n" + 
				"  This is a heredoc, I think\n" + 
				"END\n" +
				"end\n";
		// @formatter:on

		setUp(code);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 3); // def
		assertToken(RubySourceConfiguration.DEFAULT, 3, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 4, 5); // index
		assertToken(RubySourceConfiguration.DEFAULT, 9, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 10, 4); //
		assertToken(RubySourceConfiguration.DEFAULT, 14, 7); // heredoc
		assertToken(RubySourceConfiguration.DEFAULT, 21, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 22, 1); // =
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 23, 6); // <<-END
		assertToken(RubySourceConfiguration.DEFAULT, 29, 1); // \n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 30, 29); // This is a heredoc, I think\n
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 59, 4); // END\n
		assertToken(RubySourceConfiguration.DEFAULT, 63, 3); // end
		assertToken(RubySourceConfiguration.DEFAULT, 66, 1); // \n
	}

	@Test
	public void testReturnsRubyDefaultContentTypeNotDocumentDefaultContentType()
	{
		String src = "  config.parameters << :password";
		setUp(src);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 2);
		assertToken(RubySourceConfiguration.DEFAULT, 2, 6);
		assertToken(RubySourceConfiguration.DEFAULT, 8, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 9, 10);
		assertToken(RubySourceConfiguration.DEFAULT, 19, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 20, 2);
		assertToken(RubySourceConfiguration.DEFAULT, 22, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 23, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 24, 8);
	}

	/*
	 * https://aptana.lighthouseapp.com/projects/45260/tickets/372-color-syntax-when-dividing-inline-ruby
	 */
	@Test
	public void testBug372()
	{
		// @formatter:off
		String code = 
				"\"#{@mem / 100.0}\", @test_object\n" +
				"\"#{@mem / 100.0}\", @test_object";
		// @formatter:on

		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 0);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 2);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 3);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 14);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 15);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 16);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 17);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 31);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 32);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 34);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 35);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 46);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 47);
		assertContentType(RubySourceConfiguration.STRING_DOUBLE, code, 48);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 49);
		assertContentType(RubySourceConfiguration.DEFAULT, code, 62);
	}

	@Test
	public void testBug676() throws Exception
	{
		// @formatter:off
		String code = 
				"\"Just an example: %s %d\" \\\n" +
				"% [1, 9000]";
		// @formatter:on
		setUp(code);

		assertToken(RubySourceConfiguration.STRING_DOUBLE, 0, 1); // "
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 1, 22); // Just an example: %s %d
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 23, 1); // "
		assertToken(RubySourceConfiguration.DEFAULT, 24, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 25, 3); // \\n%
		// assertToken(RubySourceConfiguration.DEFAULT, 26, 1); // \n
		// assertToken(RubySourceConfiguration.DEFAULT, 27, 1); // %
		assertToken(RubySourceConfiguration.DEFAULT, 28, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 29, 1); // [
		assertToken(RubySourceConfiguration.DEFAULT, 30, 1); // 1
		assertToken(RubySourceConfiguration.DEFAULT, 31, 1); // ,
		assertToken(RubySourceConfiguration.DEFAULT, 32, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 33, 4); // 9000
		assertToken(RubySourceConfiguration.DEFAULT, 37, 1); // ]
	}

	@Test
	public void testAPSTUD3261()
	{
		// @formatter:off
		String src = 
			"module ApplicationHelper\n" +
			"  def bob5(str, n)\n" +
			"      tags + rest.gsub(/(\\S{#{n.to_i/2},#{n}})/, \"\\\\1<wbr />\")\n" +
			"  end\n" +
			"  def bob4(time)\n" +
			"    if secs < 45.seconds\n" +
			"      [secs.round, \"secs\"]\n" +
			"    elsif secs < 1.hour\n" +
			"      [(secs / 1.minute.to_f).round, \"mins\"]\n" +
			"    elsif secs < 1.day\n" +
			"      [(secs / 1.hour.to_f).round, \"hours\"]\n" +
			"    elsif secs < 1.week\n" +
			"      [(secs / 1.day.to_f).round, \"days\"]\n" +
			"    elsif secs < 35.days\n" +
			"      [(secs / 1.week.to_f).round, \"weeks\"]\n" +
			"    else\n" +
			"      [(secs / 1.month.to_f).round, \"months\"]\n" +
			"    end\n" +
			"  end\n" +
			"  def bob3(txt)\n" +
			"    txt.gsub /<a href=\"/, '<a target=\"_blank\" class=\"about\" href=\"'\n" + 
			"  end\n" +
			"  def bob2(user, size, html_options={})\n" +
			"    # bob (6/09): we're\n" +
			"  end\n" +
			"  def bob1(clip, size, html_options={})\n" +
			"    # bob (6/09):\n" +
			"  end\n" +
			"  def bob6(bobs=Hash.new, options=Hash.new, &block)\n" +
			"      options[:onsubmit] += <<-EOF\n" +
			"        bob.show('#{spinner_id}');\n" +
			"      EOF\n" +
			"  end\n" +
			"end";
		// @formatter:on

		setUp(src);

		assertToken(RubySourceConfiguration.DEFAULT, 0, 6); // module
		assertToken(RubySourceConfiguration.DEFAULT, 6, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 7, 17); // ApplicationHelper
		assertToken(RubySourceConfiguration.DEFAULT, 24, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 25, 2);
		assertToken(RubySourceConfiguration.DEFAULT, 27, 3); // def
		assertToken(RubySourceConfiguration.DEFAULT, 30, 1);
		assertToken(RubySourceConfiguration.DEFAULT, 31, 4); // bob5
		assertToken(RubySourceConfiguration.DEFAULT, 35, 1); // (
		assertToken(RubySourceConfiguration.DEFAULT, 36, 3); // str
		assertToken(RubySourceConfiguration.DEFAULT, 39, 1); // ,
		assertToken(RubySourceConfiguration.DEFAULT, 40, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 41, 1); // n
		assertToken(RubySourceConfiguration.DEFAULT, 42, 1); // )
		assertToken(RubySourceConfiguration.DEFAULT, 43, 1); // \n
		assertToken(RubySourceConfiguration.DEFAULT, 44, 6); //
		// tags + rest.gsub(/(\S{#{n.to_i/2},#{n}})/, "\\1<wbr />")
		assertToken(RubySourceConfiguration.DEFAULT, 50, 4); // tags
		assertToken(RubySourceConfiguration.DEFAULT, 54, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 55, 1); // +
		assertToken(RubySourceConfiguration.DEFAULT, 56, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 57, 4); // rest
		assertToken(RubySourceConfiguration.DEFAULT, 61, 1); // .
		assertToken(RubySourceConfiguration.DEFAULT, 62, 4); // gsub
		assertToken(RubySourceConfiguration.DEFAULT, 66, 1); // (
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 67, 1); // /
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 68, 4); // (\S{
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 72, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 74, 1); // n
		assertToken(RubySourceConfiguration.DEFAULT, 75, 1); // .
		assertToken(RubySourceConfiguration.DEFAULT, 76, 4); // to_i
		assertToken(RubySourceConfiguration.DEFAULT, 80, 1); // /
		assertToken(RubySourceConfiguration.DEFAULT, 81, 1); // 2
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 82, 1); // }
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 83, 1); // ,
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 84, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 86, 1); // n
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 87, 1); // }
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 88, 2); // })
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 90, 1); // /
		assertToken(RubySourceConfiguration.DEFAULT, 91, 1); // ,
		assertToken(RubySourceConfiguration.DEFAULT, 92, 1); //
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 93, 1); // "
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 94, 10); // \\1<wbr />
		assertToken(RubySourceConfiguration.STRING_DOUBLE, 104, 1); // "
	}

	@Test
	public void testRegexpWithStringInterpolation()
	{
		String src = "/(\\S{#{n.to_i/2},#{n}})/";
		setUp(src);

		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 0, 1); // /
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 1, 4); // (\S{
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 5, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 7, 1); // n
		assertToken(RubySourceConfiguration.DEFAULT, 8, 1); // .
		assertToken(RubySourceConfiguration.DEFAULT, 9, 4); // to_i
		assertToken(RubySourceConfiguration.DEFAULT, 13, 1); // /
		assertToken(RubySourceConfiguration.DEFAULT, 14, 1); // 2
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 15, 1); // }
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 16, 1); // ,
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 17, 2); // #{
		assertToken(RubySourceConfiguration.DEFAULT, 19, 1); // n
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 20, 1); // }
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 21, 2); // })
		assertToken(RubySourceConfiguration.REGULAR_EXPRESSION, 23, 1); // /
	}

	@Test
	public void testResumeAfterStringAndLineContinuationMidPartition()
	{
		// @formatter:off
		String code = "\"Just an example: %s %d\" \\\n" +
				"% [1, 9000]";
		// @formatter:on

		setUp(code, 27, 11, 24);
		assertToken(RubySourceConfiguration.DEFAULT, 24, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 25, 3); // \\n%
		assertToken(RubySourceConfiguration.DEFAULT, 28, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 29, 1); // [
		assertToken(RubySourceConfiguration.DEFAULT, 30, 1); // 1
		assertToken(RubySourceConfiguration.DEFAULT, 31, 1); // ,
		assertToken(RubySourceConfiguration.DEFAULT, 32, 1); //
		assertToken(RubySourceConfiguration.DEFAULT, 33, 4); // 9000
		assertToken(RubySourceConfiguration.DEFAULT, 37, 1); // ]
		// TODO set up another setPartialrange call and make sure we set up state properly...
	}

	// TODO Add a test that we clean up/close reader on EOF?

	private void setUp(String src)
	{
		setUp(src, 0, src.length(), 0);
	}

	private synchronized void setUp(String src, int offset, int length, int partitionOffset)
	{
		getPartitioner(src); // make sure partitioner is set up
		scanner.setPartialRange(document, offset, length, RubySourceConfiguration.DEFAULT, partitionOffset);
	}

	protected void assertToken(String contentType, int offset, int length)
	{
		IToken token = scanner.nextToken();
		assertEquals("Token partition type doesn't match", contentType, token.getData());
		assertEquals("Token offset doesn't match", offset, scanner.getTokenOffset());
		assertEquals("Token length doesn't match", length, scanner.getTokenLength());
	}

	/**
	 * @deprecated
	 * @param contentType
	 * @param code
	 * @param offset
	 */
	protected void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	private synchronized String getContentType(String content, int offset)
	{
		return getPartitioner(content).getContentType(offset);
	}

	private synchronized IDocumentPartitioner getPartitioner(String content)
	{
		if (partitioner == null)
		{
			IDocument doc = getDocument(content);
			partitioner = new FastPartitioner(new MergingPartitionScanner(scanner),
					RubySourceConfiguration.CONTENT_TYPES);
			partitioner.connect(doc);
			doc.setDocumentPartitioner(partitioner);
		}
		return partitioner;
	}

	protected synchronized IDocument getDocument(String content)
	{
		if (document == null)
		{
			document = new Document(content);
		}
		return document;
	}
}
