/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.core.inference;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jrubyparser.CompatVersion;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.AssignableNode;
import org.jrubyparser.ast.ClassVarNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.LocalVarNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.parser.ParserConfiguration;

import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.ClosestSpanningNodeLocator;
import com.aptana.ruby.core.ast.FirstPrecursorNodeLocator;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.OffsetNodeLocator;
import com.aptana.ruby.core.ast.ScopedNodeLocator;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.ruby.core.inference.ITypeInferrer;

@SuppressWarnings("nls")
public class TypeInferrer implements ITypeInferrer
{

	/**
	 * Hard-coded mapping from common method names to their possible return types.
	 */
	private static final Map<String, Collection<ITypeGuess>> TYPICAL_METHOD_RETURN_TYPE_NAMES = new HashMap<String, Collection<ITypeGuess>>();
	static
	{
		// TODO Read this in from some config file/property file rather than hardcode it!
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("capitalize!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("ceil", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("center", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chomp!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("chop!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("concat", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("count", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("crypt", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("downcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("dump", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("floor", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gets", createSet("String", "NilClass"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("gsub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("hash", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("index", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("inspect", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("intern", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("length", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("now", createSet("Time"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("round", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("size", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("slice!", createSet("String", "Array", "NilClass", "Object", "Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("strip!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("sub!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("swapcase!", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_a", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_ary", createSet("Array"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_i", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_int", createSet("Fixnum"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_f", createSet("Float"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_proc", createSet("Proc"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_s", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_str", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_string", createSet("String"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("to_sym", createSet("Symbol"));
		TYPICAL_METHOD_RETURN_TYPE_NAMES.put("unpack", createSet("Array"));
	}

	private static Set<ITypeGuess> createSet(String... strings)
	{
		// TODO Allow for un-equal weighting of types!
		int weight = 100 / strings.length;
		Set<ITypeGuess> set = new HashSet<ITypeGuess>();
		for (String string : strings)
		{
			set.add(new BasicTypeGuess(string, weight));
		}
		return set;
	}

	public Collection<ITypeGuess> infer(String source, int offset)
	{
		Parser parser = new Parser();
		Node root = parser.parse("", new StringReader(source), new ParserConfiguration(0, CompatVersion.BOTH)); //$NON-NLS-1$
		if (root == null)
		{
			return Collections.emptyList();
		}
		Node atOffset = new OffsetNodeLocator().find(root, offset);
		if (atOffset == null)
		{
			return Collections.emptyList();
		}
		return infer(root, atOffset);
	}

	public Collection<ITypeGuess> infer(Node rootNode, Node toInfer)
	{
		switch (toInfer.getNodeType())
		{
			case CONSTNODE:
				// FIXME This might be a reference to an actual type, but it also might be a real constant, in which
				// case we need to find it's assignment and infer type from there.
				return createSet(((ConstNode) toInfer).getName());
			case CALLNODE:
			case FCALLNODE:
			case VCALLNODE:
				return inferMethod((INameNode) toInfer);
			case SYMBOLNODE:
			case DSYMBOLNODE:
				return createSet("Symbol");
			case ARRAYNODE:
			case ZARRAYNODE:
				return createSet("Array");
			case BIGNUMNODE:
				return createSet("Bignum");
			case FIXNUMNODE:
				return createSet("Fixnum");
			case FLOATNODE:
				return createSet("Float");
			case HASHNODE:
				return createSet("Hash");
			case DREGEXPNODE:
			case REGEXPNODE:
				return createSet("Regexp");
			case TRUENODE:
				return createSet("TrueClass");
			case FALSENODE:
				return createSet("FalseClass");
			case NILNODE:
				return createSet("NilClass");
			case DSTRNODE:
			case DXSTRNODE:
			case STRNODE:
			case XSTRNODE:
				return createSet("String");
			case LOCALVARNODE:
				return inferLocal(rootNode, (LocalVarNode) toInfer);
			case INSTVARNODE:
				return inferInstance(rootNode, (LocalVarNode) toInfer);
			case CLASSVARNODE:
				return inferClassVar(rootNode, (ClassVarNode) toInfer);
			case COLON2NODE:
				return inferColon2Node((Colon2Node) toInfer);
			default:
				break;
		}
		return createSet("Object");
	}

	private Collection<ITypeGuess> inferInstance(Node rootNode, LocalVarNode toInfer)
	{
		return inferClassOrInstanceVar(rootNode, toInfer, NodeType.INSTASGNNODE);
	}

	private Collection<ITypeGuess> inferClassVar(Node rootNode, ClassVarNode toInfer)
	{
		return inferClassOrInstanceVar(rootNode, toInfer, NodeType.CLASSVARASGNNODE, NodeType.CLASSVARDECLNODE);
	}

	private Collection<ITypeGuess> inferClassOrInstanceVar(Node rootNode, final INameNode varRefNode,
			final NodeType... nodeTypes)
	{
		final String varName = varRefNode.getName();
		Node enclosingTypeNode = enclosingType(rootNode, ((Node) varRefNode).getPosition().getStartOffset());
		if (enclosingTypeNode == null)
		{
			enclosingTypeNode = rootNode;
		}
		List<Node> assigns = new ScopedNodeLocator().find(enclosingTypeNode, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				boolean found = false;
				for (NodeType type : nodeTypes)
				{
					if (node.getNodeType() == type)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					return false;
				}
				return ((INameNode) node).getName().equals(varName);
			}
		});
		if (assigns == null)
		{
			return createSet("Object");
		}
		Collection<ITypeGuess> guesses = new ArrayList<ITypeGuess>();
		for (Node assignment : assigns)
		{
			AssignableNode assignmentNode = (AssignableNode) assignment;
			guesses.addAll(infer(rootNode, assignmentNode.getValueNode()));
		}
		return guesses;
	}

	private Node enclosingType(Node rootNode, int startOffset)
	{
		return new ClosestSpanningNodeLocator().find(rootNode, startOffset, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node.getNodeType() == NodeType.CLASSNODE || node.getNodeType() == NodeType.MODULENODE;
			}
		});
	}

	private Collection<ITypeGuess> inferColon2Node(Colon2Node toInfer)
	{
		return createSet(ASTUtils.getFullyQualifiedName(toInfer));
	}

	private Collection<ITypeGuess> inferLocal(Node rootNode, LocalVarNode toInfer)
	{
		final String varName = toInfer.getName();
		Node precedingAssignment = new FirstPrecursorNodeLocator().find(rootNode, toInfer.getPosition()
				.getStartOffset() - 1, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node.getNodeType() == NodeType.LOCALASGNNODE && ((INameNode) node).getName().equals(varName);
			}
		});
		if (precedingAssignment != null)
		{
			LocalAsgnNode assign = (LocalAsgnNode) precedingAssignment;
			return infer(rootNode, assign.getValueNode());
		}
		return createSet("Object");
	}

	private Collection<ITypeGuess> inferMethod(INameNode toInfer)
	{
		String methodName = toInfer.getName();
		if (methodName.endsWith("?"))
		{
			return createSet("TrueClass", "FalseClass");
		}
		Collection<ITypeGuess> guesses = TYPICAL_METHOD_RETURN_TYPE_NAMES.get(methodName);
		if (guesses == null)
		{
			// FIXME Grab from content_assist.rb's infer_return_type
//		    # Ok, we can't cheat. We need to actually try to figure out the return type!
//		    case method_node.node_type
//		    when org.jrubyparser.ast.NodeType::CALLNODE
//		      # Figure out the type of the receiver...
//		      receiver_types = infer(method_node.getReceiverNode)
//		      # If method name is "new" return receiver as type
//		      return receiver_types if method_node.name == "new"
//		      # TODO grab this method on the receiver type and grab the return type from it
//		      "Object"
//		    when org.jrubyparser.ast.NodeType::FCALLNODE, org.jrubyparser.ast.NodeType::VCALLNODE
//		      # Grab enclosing type, search it's hierarchy for this method, grab it's return type(s)
//		      type_node = enclosing_type(method_node.position.start_offset)
//		      methods = ScopedNodeLocator.new.find(type_node) {|node| node.node_type == org.jrubyparser.ast.NodeType::DEFNNODE }
//		      # FIXME This doesn't take hierarchy of type into account!
//		      methods = methods.select {|m| m.name == method_node.name } if methods
//		      return "Object" if methods.nil? or methods.empty?
//		      
//		      # Now traverse the method and gather return types
//		      return_nodes = ScopedNodeLocator.new.find(methods.first) {|node| node.node_type == org.jrubyparser.ast.NodeType::RETURNNODE }
//		      types = []
//		      return_nodes.each {|r| types << infer(r.value_node) } if return_nodes
//		      
//		      # Get method body as a BlockNode, grab last child, that's the implicit return.
//		      implicit_return = last_statement(methods.first.body_node)
//		      if implicit_return
//		        case implicit_return.node_type
//		        when org.jrubyparser.ast.NodeType::IFNODE
//		          types << infer(last_statement(implicit_return.then_body)) if implicit_return.then_body
//		          types << infer(last_statement(implicit_return.else_body)) if implicit_return.else_body
//		        when org.jrubyparser.ast.NodeType::CASENODE
//		          implicit_return.cases.child_nodes.each do |c|
//		            types << infer(last_statement(c.body_node)) if c
//		          end
//		          types << infer(last_statement(implicit_return.else_node)) if implicit_return.else_node       
//		        when org.jrubyparser.ast.NodeType::RETURNNODE
//		          # Ignore this because it's picked up in our explicit return traversal
//		        else
//		          types << infer(implicit_return)
//		        end
//		      end
//		      return "Object" if types.empty?
//		      types.flatten!
//		      types
//		    else
//		      # Should never end up here...
//		      "Object"
//		    end
			
			return Collections.emptySet();
		}
		return guesses;
	}

}
