package com.aptana.ruby.core.ast;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.ast.Node;
import org.jrubyparser.parser.ParserResult;

import com.aptana.ruby.core.IRubyMethod.Visibility;
import com.aptana.ruby.core.ISourceElementRequestor;
import com.aptana.ruby.core.ISourceElementRequestor.FieldInfo;
import com.aptana.ruby.core.ISourceElementRequestor.MethodInfo;
import com.aptana.ruby.core.ISourceElementRequestor.TypeInfo;
import com.aptana.ruby.core.RubySourceParser;

@SuppressWarnings("nls")
public class SourceElementVisitorTest
{

	private final class CollectingSourceElementRequestor implements ISourceElementRequestor
	{
		List<TypeInfo> types;
		List<MethodInfo> methods;
		List<FieldInfo> fields;
		Map<Integer, String> methodRefs;
		Map<Integer, String> fieldRefs;
		Map<Integer, String> typeRefs;
		Map<Integer, String> importRefs;

		public void enterMethod(MethodInfo methodInfo)
		{
			methods.add(methodInfo);
		}

		public void enterConstructor(MethodInfo constructor)
		{
			methods.add(constructor);
		}

		public void enterField(FieldInfo fieldInfo)
		{
			fields.add(fieldInfo);
		}

		public void enterType(TypeInfo typeInfo)
		{
			types.add(typeInfo);
		}

		public void enterScript()
		{
			types = new ArrayList<TypeInfo>();
			methods = new ArrayList<MethodInfo>();
			fields = new ArrayList<FieldInfo>();
			methodRefs = new HashMap<Integer, String>();
			fieldRefs = new HashMap<Integer, String>();
			typeRefs = new HashMap<Integer, String>();
			importRefs = new HashMap<Integer, String>();
		}

		public void exitMethod(int endOffset)
		{
			// ignore
		}

		public void exitConstructor(int endOffset)
		{
			// ignore
		}

		public void exitField(int endOffset)
		{
			// ignore
		}

		public void exitType(int endOffset)
		{
			// ignore
		}

		public void exitScript(int endOffset)
		{
			// ignore
		}

		public void acceptMethodReference(String name, int argCount, int offset)
		{
			methodRefs.put(offset, name);
		}

		public void acceptConstructorReference(String name, int argCount, int offset)
		{
			methodRefs.put(offset, name);
		}

		public void acceptFieldReference(String name, int offset)
		{
			fieldRefs.put(offset, name);
		}

		public void acceptTypeReference(String name, int startOffset, int endOffset)
		{
			typeRefs.put(startOffset, name);
		}

		public void acceptImport(String value, int startOffset, int endOffset)
		{
			importRefs.put(startOffset, value);
		}

		public void acceptMixin(String string)
		{
			// TODO Auto-generated method stub

		}

		public void acceptModuleFunction(String function)
		{
			// TODO Auto-generated method stub

		}

		public void acceptMethodVisibilityChange(String methodName, Visibility visibility)
		{
			// TODO Auto-generated method stub

		}

		public void acceptYield(String name)
		{
			// TODO Auto-generated method stub

		}

		public void enterBlock(int startOffset, int endOffset)
		{
			// TODO Auto-generated method stub

		}

		public void exitBlock(int endOffset)
		{
			// ignore
		}
	}

	private CollectingSourceElementRequestor fRequestor;

	@Before
	public void setUp() throws Exception
	{
//		super.setUp();
		fRequestor = new CollectingSourceElementRequestor();
	}

	@After
	public void tearDown() throws Exception
	{
		try
		{
			fRequestor = null;
		}
		finally
		{
//			super.tearDown();
		}
	}

	@Test
	public void testAttrAccessor() throws Exception
	{
		String source = "class Chris\n" + //
				"  attr_accessor :variable\n" + //
				"end"; //
		parseAndVisit(source);

		// Instance Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertEquals("@variable", field.name);
		assertEquals(28, field.nameSourceStart);
		assertEquals(36, field.nameSourceEnd);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 28, 36, false);

		// writer
		method = fRequestor.methods.get(1);
		assertMethod(method, "variable=", 28, 36, false, "new_value");
	}

	@Test
	public void testAttrCallWithTrueArg() throws Exception
	{
		String source = "class Chris\n" + //
				"  attr :variable, true\n" + //
				"end"; //
		parseAndVisit(source);

		// Instance Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertEquals("@variable", field.name);
		assertEquals(19, field.nameSourceStart);
		assertEquals(27, field.nameSourceEnd);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 19, 27, false);

		// writer
		method = fRequestor.methods.get(1);
		assertMethod(method, "variable=", 19, 27, false, "new_value");
	}

	@Test
	public void testAttrCallWithMultipleSymbols() throws Exception
	{
		String source = "class Chris\n" + //
				"  attr :variable, :other\n" + //
				"end"; //
		parseAndVisit(source);

		// Instance Variables
		assertEquals(2, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertEquals("@variable", field.name);
		assertEquals(19, field.nameSourceStart);
		assertEquals(27, field.nameSourceEnd);
		// other
		field = fRequestor.fields.get(1);
		assertEquals("@other", field.name);
		assertEquals(30, field.nameSourceStart);
		assertEquals(35, field.nameSourceEnd);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// variable reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 19, 27, false);

		// other reader
		method = fRequestor.methods.get(1);
		assertMethod(method, "other", 30, 35, false);
	}

	@Test
	public void testAttrReader() throws Exception
	{
		String source = "class Chris\n" + //
				"  attr_reader :variable\n" + //
				"end"; //
		parseAndVisit(source);

		// Instance Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertEquals("@variable", field.name);
		assertEquals(26, field.nameSourceStart);
		assertEquals(34, field.nameSourceEnd);

		// Methods
		assertEquals(1, fRequestor.methods.size());

		// reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 26, 34, false);
	}

	@Test
	public void testAttr() throws Exception
	{
		String source = "class Chris\n" + //
				"  attr :variable\n" + //
				"end"; //
		parseAndVisit(source);

		// Instance Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@variable", 19, 27);

		// Methods
		assertEquals(1, fRequestor.methods.size());

		// reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 19, 27, false);
	}

	private void assertField(FieldInfo field, String expectedName, int start, int end)
	{
		assertEquals(expectedName, field.name);
		assertEquals(start, field.nameSourceStart);
		assertEquals(end, field.nameSourceEnd);
	}

	@Test
	public void testCattrAccessor() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_accessor :variable\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@variable", 29, 37);

		// Methods
		assertEquals(4, fRequestor.methods.size());

		// Class-level reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "variable", 29, 37, true);

		// Instance reader
		method = fRequestor.methods.get(1);
		assertMethod(method, "variable", 29, 37, false);

		// Class-level writer
		method = fRequestor.methods.get(2);
		assertMethod(method, "variable=", 29, 37, true, "new_value");

		// Instance writer
		method = fRequestor.methods.get(3);
		assertMethod(method, "variable=", 29, 37, false, "new_value");
	}

	@Test
	public void testCattrReader() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_reader :default_timezone\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 27, 43);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// Class-level reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone", 27, 43, true);

		// Instance-level reader
		method = fRequestor.methods.get(1);
		assertMethod(method, "default_timezone", 27, 43, false);
	}

	@Test
	public void testCattrReaderWithInstanceReaderArgFalse() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_reader :default_timezone, :instance_reader => false\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 27, 43);

		// Methods
		assertEquals(1, fRequestor.methods.size());

		// reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone", 27, 43, true);
	}

	@Test
	public void testCattrWriter() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_writer :default_timezone\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 27, 43);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// Class-level writer
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone=", 27, 43, true, "new_value");

		// Instance-level writer
		method = fRequestor.methods.get(1);
		assertMethod(method, "default_timezone=", 27, 43, false, "new_value");
	}

	@Test
	public void testCattrAccessorWithInstanceWriterFalseArg() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_accessor :default_timezone, :instance_writer => false\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 29, 45);

		// Methods
		assertEquals(3, fRequestor.methods.size());

		// Class-level reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone", 29, 45, true);

		// Instance-level reader
		method = fRequestor.methods.get(1);
		assertMethod(method, "default_timezone", 29, 45, false);

		// Class-level writer
		method = fRequestor.methods.get(2);
		assertMethod(method, "default_timezone=", 29, 45, true, "new_value");
	}

	@Test
	public void testCattrAccessorWithInstanceReaderFalseArg() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_accessor :default_timezone, :instance_reader => false\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 29, 45);

		// Methods
		assertEquals(3, fRequestor.methods.size());

		// Class-level reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone", 29, 45, true);

		// Class-level writer
		method = fRequestor.methods.get(1);
		assertMethod(method, "default_timezone=", 29, 45, true, "new_value");

		// Instance-level writer
		method = fRequestor.methods.get(2);
		assertMethod(method, "default_timezone=", 29, 45, false, "new_value");
	}

	@Test
	public void testCattrAccessorWithInstanceReaderAndWritersFalse() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_accessor :default_timezone, :instance_reader => false, :instance_writer => false\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 29, 45);

		// Methods
		assertEquals(2, fRequestor.methods.size());

		// Class-level reader
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone", 29, 45, true);

		// Class-level writer
		method = fRequestor.methods.get(1);
		assertMethod(method, "default_timezone=", 29, 45, true, "new_value");
	}

	@Test
	public void testCattrWriterWithInstanceWriterArgFalse() throws Exception
	{
		String source = "class Chris\n" + //
				"  cattr_writer :default_timezone, :instance_writer => false\n" + //
				"end"; //
		parseAndVisit(source);

		// Class Variable
		assertEquals(1, fRequestor.fields.size());
		FieldInfo field = fRequestor.fields.get(0);
		assertField(field, "@@default_timezone", 27, 43);

		// Methods
		assertEquals(1, fRequestor.methods.size());

		// Class-level writer
		MethodInfo method = fRequestor.methods.get(0);
		assertMethod(method, "default_timezone=", 27, 43, true, "new_value");
	}

	@Test
	public void testClassAttribute() throws Exception
	{
		String source = "class Chris\n" + //
				"  class_attribute :default_timezone\n" + //
				"end"; //
		parseAndVisit(source);

		// Methods
		assertEquals(6, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "default_timezone?", 30, 46, false);
		assertMethod(fRequestor.methods.get(1), "default_timezone?", 30, 46, true);
		assertMethod(fRequestor.methods.get(2), "default_timezone", 30, 46, true);
		assertMethod(fRequestor.methods.get(3), "default_timezone=", 30, 46, true, "new_value");
		assertMethod(fRequestor.methods.get(4), "default_timezone", 30, 46, false);
		assertMethod(fRequestor.methods.get(5), "default_timezone=", 30, 46, false, "new_value");
	}

	@Test
	public void testDelegate() throws Exception
	{
		String source = "class Chris\n" + //
				"  delegate :hello, :to => :greeter\n" + //
				"end"; //
		parseAndVisit(source);

		// Methods
		assertEquals(1, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "hello", 23, 28, false);
	}

	@Test
	public void testDelegateMultiple() throws Exception
	{
		String source = "class Chris\n" + //
				"  delegate :hello, :goodbye, :to => :greeter\n" + //
				"end"; //
		parseAndVisit(source);

		// Methods
		assertEquals(2, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "hello", 23, 28, false);
		assertMethod(fRequestor.methods.get(1), "goodbye", 31, 38, false);
	}

	@Test
	public void testDelegateMultipleWithPrefixTrue() throws Exception
	{
		String source = "class Chris\n" + //
				"  delegate :name, :address, :to => :client, :prefix => true\n" + //
				"end"; //
		parseAndVisit(source);

		// Methods
		assertEquals(2, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "client_name", 23, 27, false);
		assertMethod(fRequestor.methods.get(1), "client_address", 30, 37, false);
	}

	@Test
	public void testDelegateMultipleWithCustomPrefix() throws Exception
	{
		String source = "class Chris\n" + //
				"  delegate :name, :address, :to => :client, :prefix => :customer\n" + //
				"end"; //
		parseAndVisit(source);

		// Methods
		assertEquals(2, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "customer_name", 23, 27, false);
		assertMethod(fRequestor.methods.get(1), "customer_address", 30, 37, false);
	}

	@Test
	public void testRequire() throws Exception
	{
		String source = "require 'set'\n"; //
		parseAndVisit(source);

		assertEquals(1, fRequestor.importRefs.size());
		Entry<Integer, String> entry = fRequestor.importRefs.entrySet().iterator().next();
		assertEquals((Integer) 0, entry.getKey());
		assertEquals("set", entry.getValue());
	}

	@Test
	public void testLoad() throws Exception
	{
		String source = "load 'activerecord/base'\n"; //
		parseAndVisit(source);

		assertEquals(1, fRequestor.importRefs.size());
		Entry<Integer, String> entry = fRequestor.importRefs.entrySet().iterator().next();
		assertEquals((Integer) 0, entry.getKey());
		assertEquals("activerecord/base", entry.getValue());
	}

	@Test
	public void testBasicClassDefinition() throws Exception
	{
		String source = "class Chris\n" + //
				"  def initialize()\n" + //
				"    @instance = 123\n" + //
				"  end\n" + //
				"  protected\n" + //
				"  def protected_method\n" + //
				"  end\n" + //
				"  private\n" + //
				"  def private_method\n" + //
				"  end\n" + //
				"end"; //
		parseAndVisit(source);

		assertEquals(1, fRequestor.fields.size());
		assertField(fRequestor.fields.get(0), "@instance", 35, 43);

		assertEquals(3, fRequestor.methods.size());
		assertMethod(fRequestor.methods.get(0), "initialize", 18, 27, false, true, Visibility.PROTECTED);
		assertMethod(fRequestor.methods.get(1), "protected_method", 75, 90, false, false, Visibility.PROTECTED);
		assertMethod(fRequestor.methods.get(2), "private_method", 114, 127, false, false, Visibility.PRIVATE);

		assertEquals(1, fRequestor.types.size());
		assertType(fRequestor.types.get(0), "Chris", false, 0, 6, 10);
	}

	@Test
	public void testModulePositionWithPrecedingComment() throws Exception
	{
		String source = "# comment\n" + //
				"\n" + //
				"module Outer\n" + //
				"  class Inner\n" + //
				"  end\n" + //
				"end\n"; //
		parseAndVisit(source);

		assertEquals(2, fRequestor.types.size());
		assertType(fRequestor.types.get(0), "Outer", true, 11, 18, 22);
		assertType(fRequestor.types.get(1), "Inner", false, 26, 32, 36);
	}

	@Test
	public void testClassPositionWithPrecedingComment() throws Exception
	{
		String source = "# comment\n" + //
				"\n" + //
				"class Outer\n" + //
				"             \n" + //
				"     \n" + //
				"end\n"; //
		parseAndVisit(source);

		assertEquals(1, fRequestor.types.size());
		assertType(fRequestor.types.get(0), "Outer", false, 11, 17, 21);
	}

	protected void assertType(TypeInfo typeInfo, String expectedTypeName, boolean isModule, int start, int nameStart,
			int nameEnd)
	{
		assertEquals("Type name doesn't match", expectedTypeName, typeInfo.name);
		assertEquals(isModule, typeInfo.isModule);
		assertEquals("Type start offset doesn't match", start, typeInfo.declarationStart);
		assertEquals("Type name start offset doesn't match", nameStart, typeInfo.nameSourceStart);
		assertEquals("Type name end offset doesn't match", nameEnd, typeInfo.nameSourceEnd);
	}

	protected void parseAndVisit(String source)
	{
		Node root = parse(source);
		SourceElementVisitor visitor = new SourceElementVisitor(fRequestor);
		visitor.acceptNode(root);
	}

	protected Node parse(String source)
	{
		RubySourceParser sourceParser = new RubySourceParser(CompatVersion.BOTH);
		ParserResult result = sourceParser.parse("filename.rb", source);
		return result.getAST();
	}

	protected void assertMethod(MethodInfo method, String expectedName, int start, int end, boolean classLevel,
			String... args)
	{
		assertMethod(method, expectedName, start, end, classLevel, false, Visibility.PUBLIC, args);
	}

	protected void assertMethod(MethodInfo method, String expectedName, int start, int end, boolean classLevel,
			boolean isConstructor, Visibility vis, String... args)
	{
		assertEquals("Method name does not match", expectedName, method.name);
		assertEquals("Method name start offset doesn't match", start, method.nameSourceStart);
		assertEquals("Method name end offset doesn't match", end, method.nameSourceEnd);
		assertEquals(classLevel, method.isClassLevel);
		assertEquals(isConstructor, method.isConstructor);
		assertEquals("Method visibility doesn't match", vis, method.visibility);
		assertEquals("Number of arguments don't match", args.length, method.parameterNames.length);
		int i = 0;
		for (String arg : args)
		{
			assertEquals(arg, method.parameterNames[i++]);
		}
	}

}
