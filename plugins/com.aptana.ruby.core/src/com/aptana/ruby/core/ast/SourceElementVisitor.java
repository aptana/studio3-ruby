/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.core.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.ArgsNode;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.ClassVarAsgnNode;
import org.jrubyparser.ast.ClassVarDeclNode;
import org.jrubyparser.ast.ClassVarNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstDeclNode;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DAsgnNode;
import org.jrubyparser.ast.DStrNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.GlobalAsgnNode;
import org.jrubyparser.ast.GlobalVarNode;
import org.jrubyparser.ast.HashNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.InstVarNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.LocalVarNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.RootNode;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.SelfNode;
import org.jrubyparser.ast.SplatNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.UnnamedRestArgNode;
import org.jrubyparser.ast.VCallNode;
import org.jrubyparser.ast.YieldNode;

import com.aptana.core.util.ArrayUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.ruby.core.IRubyMethod;
import com.aptana.ruby.core.IRubyMethod.Visibility;
import com.aptana.ruby.core.ISourceElementRequestor;
import com.aptana.ruby.core.ISourceElementRequestor.FieldInfo;
import com.aptana.ruby.core.ISourceElementRequestor.MethodInfo;
import com.aptana.ruby.core.ISourceElementRequestor.TypeInfo;

/**
 * @author Chris Williams
 * @author Michael Xia
 */
public class SourceElementVisitor extends InOrderVisitor
{

	private static final String CLASS_ATTRIBUTE = "class_attribute"; //$NON-NLS-1$
	private static final String CATTR_ACCESSOR = "cattr_accessor"; //$NON-NLS-1$
	private static final String CATTR_READER = "cattr_reader"; //$NON-NLS-1$
	private static final String CATTR_WRITER = "cattr_writer"; //$NON-NLS-1$
	private static final String BELONGS_TO = "belongs_to"; //$NON-NLS-1$
	private static final String HAS_ONE = "has_one"; //$NON-NLS-1$
	private static final String HAS_MANY = "has_many"; //$NON-NLS-1$
	private static final String DELEGATE = "delegate"; //$NON-NLS-1$
	private static final String EXTEND = "extend"; //$NON-NLS-1$
	private static final String OBJECT = "Object"; //$NON-NLS-1$
	private static final String CONSTRUCTOR_NAME = "initialize"; //$NON-NLS-1$
	private static final String MODULE = "Module"; //$NON-NLS-1$
	private static final String REQUIRE = "require"; //$NON-NLS-1$
	private static final String LOAD = "load"; //$NON-NLS-1$
	private static final String INCLUDE = "include"; //$NON-NLS-1$
	private static final String PUBLIC = "public"; //$NON-NLS-1$
	private static final String PROTECTED = "protected"; //$NON-NLS-1$
	private static final String PRIVATE = "private"; //$NON-NLS-1$
	private static final String MODULE_FUNCTION = "module_function"; //$NON-NLS-1$
	private static final String ALIAS = "alias :"; //$NON-NLS-1$
	private static final String ALIAS_METHOD = "alias_method"; //$NON-NLS-1$
	private static final String ATTR = "attr"; //$NON-NLS-1$
	private static final String ATTR_ACCESSOR = "attr_accessor"; //$NON-NLS-1$
	private static final String ATTR_READER = "attr_reader"; //$NON-NLS-1$
	private static final String ATTR_WRITER = "attr_writer"; //$NON-NLS-1$
	private static final String CLASS_EVAL = "class_eval"; //$NON-NLS-1$
	private static final String NAMESPACE_DELIMETER = "::"; //$NON-NLS-1$

	private ISourceElementRequestor requestor;
	private List<Visibility> visibilities;

	private String typeName;
	private boolean inSingletonClass;
	private boolean inModuleFunction;

	/**
	 * Constructor.
	 * 
	 * @param requestor
	 *            the {@link ISourceElementRequestor} that wants to be notified of the source structure
	 */
	public SourceElementVisitor(ISourceElementRequestor requestor)
	{
		this.requestor = requestor;
		visibilities = new ArrayList<Visibility>();
	}

	@Override
	public Object visitAliasNode(AliasNode iVisited)
	{
		Node newNameNode = iVisited.getNewName();
		String name = ASTUtils.getName(newNameNode);
		int nameStart = iVisited.getPosition().getStartOffset() + ALIAS.length() - 1;
		addAliasMethod(name, iVisited.getPosition().getStartOffset(), iVisited.getPosition().getEndOffset(), nameStart);

		return super.visitAliasNode(iVisited);
	}

	@Override
	public Object visitArgsNode(ArgsNode iVisited)
	{
		ListNode args = iVisited.getPre();
		if (args != null)
		{
			Node arg;
			int size = args.size();
			for (int i = 0; i < size; ++i)
			{
				arg = args.get(i);

				requestor.enterField(createFieldInfo(arg));
				requestor.exitField(getFieldEndOffset(arg));
			}
		}
		ArgumentNode restArg = iVisited.getRest();
		if (restArg != null && !(restArg instanceof UnnamedRestArgNode))
		{
			FieldInfo field = createFieldInfo(restArg);
			// account for the leading "*"
			field.declarationStart += 1;
			field.nameSourceStart += 1;
			field.nameSourceEnd += 1;
			requestor.enterField(field);
			requestor.exitField(getFieldEndOffset(restArg) + 1);
		}

		return super.visitArgsNode(iVisited);
	}

	@Override
	public Object visitCallNode(CallNode iVisited)
	{
		List<String> arguments = ASTUtils.getArgumentsFromFunctionCall(iVisited);
		String name = iVisited.getName();
		if (name.equals(PUBLIC))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PUBLIC));
			}
		}
		else if (name.equals(PRIVATE))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PRIVATE));
			}
		}
		else if (name.equals(PROTECTED))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PROTECTED));
			}
		}
		else if (name.equals(MODULE_FUNCTION))
		{
			for (String methodName : arguments)
			{
				requestor.acceptModuleFunction(methodName);
			}
		}
		else if (name.equals(CLASS_EVAL))
		{
			Node receiver = iVisited.getReceiverNode();
			if (receiver instanceof ConstNode || receiver instanceof Colon2Node)
			{
				String receiverName = null;
				if (receiver instanceof Colon2Node)
				{
					receiverName = ASTUtils.getFullyQualifiedName((Colon2Node) receiver);
				}
				else
				{
					receiverName = ASTUtils.getName(receiver);
				}
				requestor.acceptMethodReference(name, arguments.size(), iVisited.getPosition().getStartOffset());

				pushVisibility(Visibility.PUBLIC);

				TypeInfo typeInfo = new TypeInfo();
				typeInfo.name = receiverName;
				typeInfo.declarationStart = iVisited.getPosition().getStartOffset();
				typeInfo.nameSourceStart = receiver.getPosition().getStartOffset();
				typeInfo.nameSourceEnd = receiver.getPosition().getEndOffset() - 1;
				typeInfo.modules = ArrayUtil.NO_STRINGS;
				requestor.enterType(typeInfo);

				Object ins = super.visitCallNode(iVisited);

				popVisibility();
				requestor.exitType(iVisited.getPosition().getEndOffset() - 2);
				return ins;
			}
		}
		requestor.acceptMethodReference(name, arguments.size(), iVisited.getPosition().getStartOffset());

		// TODO try and do some heuristics here to store possible value of DVarNodes inside blocks
		// i.e. if method is each/map/whatever, assume receiver is a collection/array, and that each item will be the
		// value passed into the DVarNode

		return super.visitCallNode(iVisited);
	}

	@Override
	public Object visitClassNode(ClassNode iVisited)
	{
		// This resets the visibility when opening or declaring a class to
		// public
		pushVisibility(Visibility.PUBLIC);

		TypeInfo typeInfo = createTypeInfo(iVisited.getCPath());

		// count back from name, since preceding comment can incorrectly affect the start position!
		typeInfo.declarationStart = iVisited.getCPath().getPosition().getStartOffset() - 6;

		if (!typeInfo.name.equals(OBJECT))
		{
			Node superNode = iVisited.getSuperNode();
			if (superNode == null)
			{
				typeInfo.superclass = OBJECT;
			}
			else
			{
				typeInfo.superclass = ASTUtils.getFullyQualifiedName(superNode);
			}
		}
		typeName = typeInfo.name;
		requestor.enterType(typeInfo);

		Object ins = super.visitClassNode(iVisited);

		popVisibility();
		requestor.exitType(iVisited.getPosition().getEndOffset() - 2);
		return ins;
	}

	@Override
	public Object visitClassVarAsgnNode(ClassVarAsgnNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitClassVarAsgnNode(iVisited);
	}

	@Override
	public Object visitClassVarDeclNode(ClassVarDeclNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitClassVarDeclNode(iVisited);
	}

	@Override
	public Object visitClassVarNode(ClassVarNode iVisited)
	{
		requestor.acceptFieldReference(iVisited.getName(), iVisited.getPosition().getStartOffset());

		return super.visitClassVarNode(iVisited);
	}

	@Override
	public Object visitConstDeclNode(ConstDeclNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitConstDeclNode(iVisited);
	}

	@Override
	public Object visitConstNode(ConstNode iVisited)
	{
		requestor.acceptTypeReference(iVisited.getName(), iVisited.getPosition().getStartOffset(), iVisited
				.getPosition().getEndOffset());

		return super.visitConstNode(iVisited);
	}

	@Override
	public Object visitDAsgnNode(DAsgnNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		field.isDynamic = true;
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitDAsgnNode(iVisited);
	}

	@Override
	public Object visitDefnNode(DefnNode iVisited)
	{
		Visibility visibility = getCurrentVisibility();
		MethodInfo methodInfo = createMethodInfo(iVisited);
		if (methodInfo.name.equals(CONSTRUCTOR_NAME))
		{
			visibility = Visibility.PROTECTED;
			methodInfo.isConstructor = true;
		}
		methodInfo.isClassLevel = inSingletonClass || inModuleFunction;
		methodInfo.visibility = convertVisibility(visibility);
		if (methodInfo.isConstructor)
		{
			requestor.enterConstructor(methodInfo);
		}
		else
		{
			requestor.enterMethod(methodInfo);
		}

		Object ins = super.visitDefnNode(iVisited);

		int end = iVisited.getPosition().getEndOffset() - 2;
		if (methodInfo.isConstructor)
		{
			requestor.exitConstructor(end);
		}
		else
		{
			requestor.exitMethod(end);
		}
		return ins;
	}

	@Override
	public Object visitDefsNode(DefsNode iVisited)
	{
		MethodInfo methodInfo = createMethodInfo(iVisited);
		methodInfo.isClassLevel = true;
		methodInfo.visibility = convertVisibility(getCurrentVisibility());
		requestor.enterMethod(methodInfo);

		Object ins = super.visitDefsNode(iVisited);

		requestor.exitMethod(iVisited.getPosition().getEndOffset() - 2);
		return ins;
	}

	@Override
	public Object visitFCallNode(FCallNode iVisited)
	{
		List<String> arguments = ASTUtils.getArgumentsFromFunctionCall(iVisited);
		String name = iVisited.getName();
		if (REQUIRE.equals(name) || LOAD.equals(name))
		{
			addImport(iVisited);
		}
		// Handle "extend", which acts like "include" but for instances. If this is done in class_eval, treat the same
		else if (EXTEND.equals(name))
		{
			// Collect included mixins
			includeModule(iVisited);
		}
		else if (INCLUDE.equals(name))
		{
			// Collect included mixins
			includeModule(iVisited);
		}
		else if (PUBLIC.equals(name))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PUBLIC));
			}
		}
		else if (PRIVATE.equals(name))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PRIVATE));
			}
		}
		else if (PROTECTED.equals(name))
		{
			for (String methodName : arguments)
			{
				requestor.acceptMethodVisibilityChange(methodName, convertVisibility(Visibility.PROTECTED));
			}
		}
		else if (MODULE_FUNCTION.equals(name))
		{
			for (String methodName : arguments)
			{
				requestor.acceptModuleFunction(methodName);
			}
		}
		else if (ALIAS_METHOD.equals(name))
		{
			String newName = arguments.get(0).substring(1);
			int nameStart = iVisited.getPosition().getStartOffset() + name.length() + 2;
			addAliasMethod(newName, iVisited.getPosition().getStartOffset(), iVisited.getPosition().getEndOffset(),
					nameStart);
		}
		// delegate method idiom from Rails 3.x
		else if (DELEGATE.equals(name))
		{
			addDelegatedMethods(iVisited);
		}
		// class attribute method idiom from Rails 3.x
		else if (CLASS_ATTRIBUTE.equals(name))
		{
			List<Node> nodes = ASTUtils.getArgumentNodesFromFunctionCall(iVisited);
			int size = arguments.size();
			for (int i = 0; i < size; ++i)
			{
				// FIXME Check last arg if it's a hash, check :instance_writer value...
				Node node = nodes.get(i);
				if (node instanceof HashNode)
				{
					continue;
				}
				String arg = arguments.get(i);
				// Add instance query method
				MethodInfo info = createPublicMethod(dropLeadingColon(arg) + "?", node); //$NON-NLS-1$
				requestor.enterMethod(info);
				requestor.exitMethod(node.getPosition().getEndOffset() - 1);
				// Singleton query method
				info = createPublicMethod(dropLeadingColon(arg) + "?", node); //$NON-NLS-1$
				info.isClassLevel = true;
				requestor.enterMethod(info);
				requestor.exitMethod(node.getPosition().getEndOffset() - 1);
				// Singleton read/write methods
				addClassLevelReadMethod(arg, node);
				addClassLevelWriteMethod(arg, node);
				// Instance read/write methods
				addReadMethod(arg, node);
				// FIXME Check :instance_writer hash value in arg list! if not there, assume true
				addWriteMethod(arg, node);
			}
		}
		// Rails relationships...
		else if (HAS_MANY.equals(name))
		{
			addHasManyAssociationMethods(iVisited, arguments.get(0));
		}
		else if (HAS_ONE.equals(name))
		{
			addHasOneAssociationMethods(iVisited, arguments.get(0));
		}
		else if (BELONGS_TO.equals(name))
		{
			// Adds the same methods as has_one...
			addHasOneAssociationMethods(iVisited, arguments.get(0));
		}
		// class level attributes
		else if (CATTR_ACCESSOR.equals(name) || CATTR_READER.equals(name) || CATTR_WRITER.equals(name))
		{
			boolean addRead = false;
			boolean addInstanceRead = true;
			boolean addWrite = false;
			boolean addInstanceWrite = true;
			if (CATTR_ACCESSOR.equals(name))
			{
				addRead = true;
				addWrite = true;
			}
			else if (CATTR_READER.equals(name))
			{
				addRead = true;
			}
			else if (CATTR_WRITER.equals(name))
			{
				addWrite = true;
			}

			List<Node> nodes = ASTUtils.getArgumentNodesFromFunctionCall(iVisited);
			// Check if last node is hash, if so look for special key/value pairs to not generate instance methods...
			Node lastNode = nodes.get(nodes.size() - 1);
			if (lastNode instanceof HashNode)
			{
				HashNode hash = (HashNode) lastNode;
				ListNode hashValues = hash.getListNode();
				for (int x = 0; x < hashValues.size(); x += 2)
				{
					Node key = hashValues.get(x);
					Node value = hashValues.get(x + 1);
					if ("false".equals(ASTUtils.getStringRepresentation(value))) //$NON-NLS-1$
					{
						if (":instance_writer".equals(ASTUtils.getStringRepresentation(key))) //$NON-NLS-1$
						{
							addInstanceWrite = false;
						}
						else if (":instance_reader".equals(ASTUtils.getStringRepresentation(key))) //$NON-NLS-1$
						{
							addInstanceRead = false;
						}
					}
				}
			}

			int size = arguments.size();
			for (int i = 0; i < size; ++i)
			{
				Node node = nodes.get(i);
				// Skip last hash, if provided...
				if (node instanceof HashNode)
				{
					continue;
				}
				String arg = arguments.get(i);
				if (addRead)
				{
					addClassLevelReadMethod(arg, node);
					if (addInstanceRead)
					{
						addReadMethod(arg, node);
					}
				}
				if (addWrite)
				{
					addClassLevelWriteMethod(arg, node);
					if (addInstanceWrite)
					{
						addWriteMethod(arg, node);
					}
				}
				addClassVar(arg, node);
			}
		}
		// Instance level attributes
		else if (ATTR.equals(name) || ATTR_ACCESSOR.equals(name) || ATTR_READER.equals(name)
				|| ATTR_WRITER.equals(name))
		{
			List<Node> nodes = ASTUtils.getArgumentNodesFromFunctionCall(iVisited);
			boolean addRead = false;
			boolean addWrite = false;
			if (ATTR_ACCESSOR.equals(name))
			{
				addRead = true;
				addWrite = true;
			}
			else if (ATTR_READER.equals(name))
			{
				addRead = true;
			}
			else if (ATTR_WRITER.equals(name))
			{
				addWrite = true;
			}
			if (ATTR.equals(name))
			{
				addRead = true;
				// Add write if second arg is "true"
				if (arguments.size() == 2 && "true".equals(arguments.get(1))) //$NON-NLS-1$
				{
					addWrite = true;
				}
			}

			int size = arguments.size();
			for (int i = 0; i < size; ++i)
			{
				// Only handle first arg for attr if using old second arg boolean call!
				if (ATTR.equals(name) && addWrite && i > 0)
				{
					break;
				}

				Node node = nodes.get(i);
				String arg = arguments.get(i);
				if (addRead)
				{
					addReadMethod(arg, node);
				}
				if (addWrite)
				{
					addWriteMethod(arg, node);
				}
				addInstanceVar(arg, node);
			}
		}
		requestor.acceptMethodReference(name, arguments.size(), iVisited.getPosition().getStartOffset());

		return super.visitFCallNode(iVisited);
	}

	private void addInstanceVar(String arg, Node node)
	{
		FieldInfo field = new FieldInfo();
		field.declarationStart = node.getPosition().getStartOffset();
		String argName = dropLeadingColon(arg);
		field.name = "@" + argName; //$NON-NLS-1$
		field.nameSourceStart = node.getPosition().getStartOffset();
		field.nameSourceEnd = node.getPosition().getEndOffset() - 1;
		requestor.enterField(field);
		requestor.exitField(node.getPosition().getEndOffset() - 1);
	}

	private MethodInfo createPublicMethod(String methodName, Node node)
	{
		MethodInfo info = new MethodInfo();
		info.declarationStart = node.getPosition().getStartOffset();
		info.name = methodName;
		info.nameSourceStart = node.getPosition().getStartOffset();
		info.nameSourceEnd = node.getPosition().getEndOffset() - 1;
		info.visibility = IRubyMethod.Visibility.PUBLIC;
		info.parameterNames = ArrayUtil.NO_STRINGS;
		return info;
	}

	private void addClassVar(String arg, Node node)
	{
		FieldInfo field = new FieldInfo();
		field.name = "@@" + dropLeadingColon(arg); //$NON-NLS-1$
		field.declarationStart = node.getPosition().getStartOffset();
		field.nameSourceStart = node.getPosition().getStartOffset();
		int end = node.getPosition().getStartOffset() + field.name.length() - 2; // subtract the @@
		field.nameSourceEnd = end;
		requestor.enterField(field);
		requestor.exitField(end);
	}

	@SuppressWarnings("nls")
	protected void addHasManyAssociationMethods(FCallNode iVisited, String association)
	{
		// http://api.rubyonrails.org/classes/ActiveRecord/Associations/ClassMethods.html#method-i-has_many
		String firstArg = dropLeadingColon(association);
		Node argsNode = iVisited.getArgsNode();
		Node firstArgNode = argsNode.childNodes().iterator().next();
		// FIXME Add "force_reload = false" as param
		addReadMethod(firstArg, firstArgNode);
		// FIXME take in "objects" arg as param
		addReadMethod(firstArg + "=", firstArgNode);
		// FIXME Singularize the firstArg for these last two methods!
		// addReadMethod(firstArg + "_ids", firstArgNode);
		// FIXME Add "ids" as param
		// addReadMethod(firstArg + "_ids=", firstArgNode);
	}

	@SuppressWarnings("nls")
	protected void addHasOneAssociationMethods(FCallNode iVisited, String association)
	{
		// http://api.rubyonrails.org/classes/ActiveRecord/Associations/ClassMethods.html#method-i-has_one
		String firstArg = dropLeadingColon(association);
		Node argsNode = iVisited.getArgsNode();
		Node firstArgNode = argsNode.childNodes().iterator().next();
		// FIXME Add "force_reload = false" as param
		addReadMethod(firstArg, firstArgNode);
		// FIXME take in "associate" arg as param
		addReadMethod(firstArg + "=", firstArgNode);
		// FIXME Add "attributes = {}" as param
		addReadMethod("build_" + firstArg, firstArgNode);
		// FIXME Add "attributes = {}" as param
		addReadMethod("create_" + firstArg, firstArgNode);
	}

	private String dropLeadingColon(String association)
	{
		if (association.length() > 0 && association.charAt(0) == ':')
		{
			return association.substring(1);
		}
		return association;
	}

	protected void addDelegatedMethods(FCallNode iVisited)
	{
		List<Node> nodes = ASTUtils.getArgumentNodesFromFunctionCall(iVisited);

		String prefix = StringUtil.EMPTY;
		String to = StringUtil.EMPTY;
		boolean useToForPrefix = false;

		Node lastNode = nodes.get(nodes.size() - 1);
		if (lastNode instanceof HashNode)
		{
			HashNode hash = (HashNode) lastNode;
			ListNode hashValues = hash.getListNode();
			for (int x = 0; x < hashValues.size(); x += 2)
			{
				Node key = hashValues.get(x);
				Node value = hashValues.get(x + 1);
				if (":to".equals(ASTUtils.getStringRepresentation(key))) //$NON-NLS-1$
				{
					to = ASTUtils.getStringRepresentation(value);
				}
				else if (":prefix".equals(ASTUtils.getStringRepresentation(key))) //$NON-NLS-1$
				{
					String blah = ASTUtils.getStringRepresentation(value);
					if ("true".equals(blah)) //$NON-NLS-1$
					{
						useToForPrefix = true;
					}
					else
					{
						prefix = dropLeadingColon(blah);
					}
				}
			}
		}
		if (useToForPrefix)
		{
			prefix = dropLeadingColon(to);
		}

		if (prefix.length() > 0)
		{
			prefix = prefix + "_"; //$NON-NLS-1$
		}

		for (Node arg : nodes)
		{
			// skip the :to hash
			if (arg instanceof HashNode)
			{
				continue;
			}
			String methodName = prefix + dropLeadingColon(ASTUtils.getStringRepresentation(arg));

			int start = arg.getPosition().getStartOffset();
			int end = arg.getPosition().getEndOffset() - 1;

			MethodInfo method = new MethodInfo();
			// TODO Use the visibility for the original method that this is aliasing?
			Visibility visibility = getCurrentVisibility();
			method.declarationStart = start;
			method.isClassLevel = inSingletonClass;
			method.name = methodName;
			method.visibility = convertVisibility(visibility);
			method.nameSourceStart = start;
			method.nameSourceEnd = end;
			// TODO Use the parameters of the original method
			method.parameterNames = ArrayUtil.NO_STRINGS;
			requestor.enterMethod(method);
			requestor.exitMethod(end);
		}
	}

	@Override
	public Object visitGlobalAsgnNode(GlobalAsgnNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitGlobalAsgnNode(iVisited);
	}

	@Override
	public Object visitGlobalVarNode(GlobalVarNode iVisited)
	{
		requestor.acceptFieldReference(iVisited.getName(), iVisited.getPosition().getStartOffset());

		return super.visitGlobalVarNode(iVisited);
	}

	@Override
	public Object visitInstAsgnNode(InstAsgnNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitInstAsgnNode(iVisited);
	}

	@Override
	public Object visitInstVarNode(InstVarNode iVisited)
	{
		requestor.acceptFieldReference(iVisited.getName(), iVisited.getPosition().getStartOffset());

		return super.visitInstVarNode(iVisited);
	}

	@Override
	public Object visitIterNode(IterNode iVisited)
	{
		requestor.enterBlock(iVisited.getPosition().getStartOffset(), iVisited.getPosition().getEndOffset() - 1);

		Object ins = super.visitIterNode(iVisited);

		requestor.exitBlock(iVisited.getPosition().getEndOffset() - 1);
		return ins;
	}

	@Override
	public Object visitModuleNode(ModuleNode iVisited)
	{
		pushVisibility(Visibility.PUBLIC);

		TypeInfo typeInfo = createTypeInfo(iVisited.getCPath());
		// count back from name, since preceding comment can incorrectly affect the start position!
		typeInfo.declarationStart = iVisited.getCPath().getPosition().getStartOffset() - 7;
		typeInfo.superclass = MODULE;
		typeInfo.isModule = true;
		typeName = typeInfo.name;
		requestor.enterType(typeInfo);

		Object ins = super.visitModuleNode(iVisited);

		popVisibility();
		requestor.exitType(iVisited.getPosition().getEndOffset() - 2);
		inModuleFunction = false;
		return ins;
	}

	@Override
	public Object visitLocalAsgnNode(LocalAsgnNode iVisited)
	{
		FieldInfo field = createFieldInfo(iVisited);
		requestor.enterField(field);
		requestor.exitField(getFieldEndOffset(iVisited));

		return super.visitLocalAsgnNode(iVisited);
	}

	@Override
	public Object visitRootNode(RootNode iVisited)
	{
		requestor.enterScript();
		pushVisibility(Visibility.PUBLIC);

		Object ins = super.visitRootNode(iVisited);

		popVisibility();
		requestor.exitScript(iVisited.getPosition().getEndOffset());
		return ins;
	}

	@Override
	public Object visitSClassNode(SClassNode iVisited)
	{
		Node receiver = iVisited.getReceiverNode();
		if (receiver instanceof SelfNode)
		{
			inSingletonClass = true;
		}
		pushVisibility(Visibility.PUBLIC);

		Object ins = super.visitSClassNode(iVisited);

		popVisibility();
		if (receiver instanceof SelfNode)
		{
			inSingletonClass = false;
		}
		return ins;
	}

	@Override
	public Object visitVCallNode(VCallNode iVisited)
	{
		String functionName = iVisited.getName();
		if (functionName.equals(PUBLIC))
		{
			setVisibility(Visibility.PUBLIC);
		}
		else if (functionName.equals(PRIVATE))
		{
			setVisibility(Visibility.PRIVATE);
		}
		else if (functionName.equals(PROTECTED))
		{
			setVisibility(Visibility.PROTECTED);
		}
		else if (functionName.equals(MODULE_FUNCTION))
		{
			inModuleFunction = true;
		}
		requestor.acceptMethodReference(functionName, 0, iVisited.getPosition().getStartOffset());

		return super.visitVCallNode(iVisited);
	}

	@Override
	public Object visitYieldNode(YieldNode iVisited)
	{
		Node argsNode = iVisited.getArgsNode();
		if (argsNode instanceof LocalVarNode)
		{
			requestor.acceptYield(((LocalVarNode) argsNode).getName());
		}
		else if (argsNode instanceof SelfNode)
		{
			String name = null;
			if (typeName == null)
			{
				name = "var"; //$NON-NLS-1$
			}
			else
			{
				name = typeName.toLowerCase();
				if (name.indexOf(NAMESPACE_DELIMETER) > -1)
				{
					name = name.substring(name.lastIndexOf(NAMESPACE_DELIMETER) + 2);
				}
			}
			requestor.acceptYield(name);
		}

		return super.visitYieldNode(iVisited);
	}

	private void pushVisibility(Visibility visibility)
	{
		visibilities.add(visibility);
	}

	private void popVisibility()
	{
		visibilities.remove(visibilities.size() - 1);
	}

	private Visibility getCurrentVisibility()
	{
		return visibilities.get(visibilities.size() - 1);
	}

	private void setVisibility(Visibility visibility)
	{
		popVisibility();
		pushVisibility(visibility);
	}

	private void addImport(FCallNode iVisited)
	{
		Node argsNode = iVisited.getArgsNode();
		// TODO What if this is a SplatNode?!
		if (argsNode instanceof ArrayNode)
		{
			ArrayNode node = (ArrayNode) argsNode;
			String arg = getString(node);
			if (arg != null)
			{
				requestor.acceptImport(arg, iVisited.getPosition().getStartOffset(), iVisited.getPosition()
						.getEndOffset());
			}
		}
	}

	private void addAliasMethod(String name, int start, int end, int nameStart)
	{
		MethodInfo method = new MethodInfo();
		// TODO Use the visibility for the original method that this is aliasing?
		Visibility visibility = getCurrentVisibility();
		if (name.equals(CONSTRUCTOR_NAME))
		{
			visibility = Visibility.PROTECTED;
			method.isConstructor = true;
		}
		method.declarationStart = start;
		method.isClassLevel = inSingletonClass;
		method.name = name;
		method.visibility = convertVisibility(visibility);
		method.nameSourceStart = nameStart;
		method.nameSourceEnd = nameStart + name.length() - 1;
		// TODO Use the parameters of the original method
		method.parameterNames = ArrayUtil.NO_STRINGS;
		requestor.enterMethod(method);
		requestor.exitMethod(end);
	}

	private MethodInfo createReadMethod(String argument, Node node)
	{
		argument = dropLeadingColon(argument);
		MethodInfo info = new MethodInfo();
		info.declarationStart = node.getPosition().getStartOffset();
		info.name = argument;
		info.nameSourceStart = node.getPosition().getStartOffset();
		info.nameSourceEnd = node.getPosition().getEndOffset() - 1;
		info.visibility = IRubyMethod.Visibility.PUBLIC;
		info.parameterNames = ArrayUtil.NO_STRINGS;
		return info;
	}

	private void addReadMethod(String argument, Node node)
	{
		requestor.enterMethod(createReadMethod(argument, node));
		requestor.exitMethod(node.getPosition().getEndOffset() - 1);
	}

	private void addClassLevelReadMethod(String argument, Node node)
	{
		MethodInfo info = createReadMethod(argument, node);
		info.isClassLevel = true;
		requestor.enterMethod(info);
		requestor.exitMethod(node.getPosition().getEndOffset() - 1);
	}

	private MethodInfo createWriteMethod(String argument, int start, int end)
	{
		argument = dropLeadingColon(argument);
		MethodInfo info = new MethodInfo();
		info.declarationStart = start;
		info.name = argument + "="; //$NON-NLS-1$
		info.nameSourceStart = start;
		info.nameSourceEnd = end;
		info.visibility = IRubyMethod.Visibility.PUBLIC;
		info.parameterNames = new String[] { "new_value" }; //$NON-NLS-1$
		return info;
	}

	private void addWriteMethod(String argument, Node node)
	{
		addWriteMethod(argument, node.getPosition().getStartOffset(), node.getPosition().getEndOffset() - 1);
	}

	private void addWriteMethod(String argument, int start, int end)
	{
		MethodInfo info = createWriteMethod(argument, start, end);
		requestor.enterMethod(info);
		requestor.exitMethod(end);
	}

	private void addClassLevelWriteMethod(String argument, Node node)
	{
		int start = node.getPosition().getStartOffset();
		int end = node.getPosition().getEndOffset() - 1;
		MethodInfo info = createWriteMethod(argument, start, end);
		info.isClassLevel = true;
		requestor.enterMethod(info);
		requestor.exitMethod(end);
	}

	private void includeModule(FCallNode iVisited)
	{
		List<String> mixins = new LinkedList<String>();

		Iterator<Node> iter = null;
		Node argsNode = iVisited.getArgsNode();
		if (argsNode instanceof SplatNode)
		{
			iter = ((SplatNode) argsNode).childNodes().iterator();
		}
		else if (argsNode instanceof ArrayNode)
		{
			iter = ((ArrayNode) argsNode).childNodes().iterator();
		}
		if (iter != null)
		{
			Node node;
			while (iter.hasNext())
			{
				node = iter.next();
				if (node instanceof StrNode)
				{
					mixins.add(((StrNode) node).getValue());
				}
				else if (node instanceof ConstNode)
				{
					mixins.add(((ConstNode) node).getName());
				}
				else if (node instanceof Colon2Node)
				{
					mixins.add(ASTUtils.getFullyQualifiedName((Colon2Node) node));
				}
				else if (node instanceof DStrNode)
				{
					Node next = ((DStrNode) node).childNodes().iterator().next();
					if (next instanceof StrNode)
					{
						mixins.add(((StrNode) next).getValue());
					}
				}
				// else if (node instanceof DVarNode)
				// {
					// FIXME track DAsgnNodes, then infer value, then try and also look at the callnode beforeiternode
					// and use heuristics?

				// }
			}
		}

		for (String string : mixins)
		{
			requestor.acceptMixin(string);
		}
	}

	private static FieldInfo createFieldInfo(Node iVisited)
	{
		FieldInfo field = new FieldInfo();
		field.name = ASTUtils.getName(iVisited);
		field.declarationStart = iVisited.getPosition().getStartOffset();
		field.nameSourceStart = iVisited.getPosition().getStartOffset();
		field.nameSourceEnd = iVisited.getPosition().getStartOffset() + field.name.length() - 1;
		return field;
	}

	private static TypeInfo createTypeInfo(Node iVisited)
	{
		TypeInfo typeInfo = new TypeInfo();
		typeInfo.name = ASTUtils.getFullyQualifiedName(iVisited);
		typeInfo.nameSourceStart = iVisited.getPosition().getStartOffset();
		typeInfo.nameSourceEnd = iVisited.getPosition().getEndOffset() - 1;
		typeInfo.modules = ArrayUtil.NO_STRINGS;
		return typeInfo;
	}

	private static MethodInfo createMethodInfo(MethodDefNode iVisited)
	{
		MethodInfo methodInfo = new MethodInfo();
		methodInfo.declarationStart = iVisited.getPosition().getStartOffset();
		methodInfo.name = iVisited.getName();
		methodInfo.nameSourceStart = iVisited.getNameNode().getPosition().getStartOffset();
		methodInfo.nameSourceEnd = iVisited.getNameNode().getPosition().getEndOffset() - 1;
		methodInfo.parameterNames = ASTUtils.getArgs(iVisited.getArgsNode(), iVisited.getScope());
		return methodInfo;
	}

	private static int getFieldEndOffset(Node iVisited)
	{
		return iVisited.getPosition().getEndOffset() - 1;
	}

	private static String getString(ArrayNode node)
	{
		Node child = node.childNodes().iterator().next();
		if (child instanceof DStrNode)
		{
			DStrNode dstrNode = (DStrNode) child;
			child = dstrNode.childNodes().iterator().next();
		}
		if (child instanceof StrNode)
		{
			return ((StrNode) child).getValue();
		}
		return null;
	}

	private static IRubyMethod.Visibility convertVisibility(Visibility visibility)
	{
		if (visibility == Visibility.PUBLIC)
		{
			return IRubyMethod.Visibility.PUBLIC;
		}
		if (visibility == Visibility.PROTECTED)
		{
			return IRubyMethod.Visibility.PROTECTED;
		}
		return IRubyMethod.Visibility.PRIVATE;
	}
}
