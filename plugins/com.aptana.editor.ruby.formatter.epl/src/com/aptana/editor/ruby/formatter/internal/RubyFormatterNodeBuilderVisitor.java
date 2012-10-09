/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *     Aptana Inc. - Modify it to work with org.jrubyparser.parser AST (Shalom Gibly)
 *******************************************************************************/
package com.aptana.editor.ruby.formatter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.jrubyparser.ISourcePositionHolder;
import org.jrubyparser.SourcePosition;
import org.jrubyparser.ast.ArgumentNode;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.BeginNode;
import org.jrubyparser.ast.BlockNode;
import org.jrubyparser.ast.CaseNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.Colon3Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.DRegexpNode;
import org.jrubyparser.ast.DStrNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.DefsNode;
import org.jrubyparser.ast.EnsureNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.ForNode;
import org.jrubyparser.ast.HashNode;
import org.jrubyparser.ast.IfNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.IterNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.Match2Node;
import org.jrubyparser.ast.Match3Node;
import org.jrubyparser.ast.MatchNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.ModuleNode;
import org.jrubyparser.ast.NilImplicitNode;
import org.jrubyparser.ast.NilNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.OptArgNode;
import org.jrubyparser.ast.PostExeNode;
import org.jrubyparser.ast.PreExeNode;
import org.jrubyparser.ast.RegexpNode;
import org.jrubyparser.ast.RescueBodyNode;
import org.jrubyparser.ast.RescueNode;
import org.jrubyparser.ast.ReturnNode;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.jrubyparser.ast.UntilNode;
import org.jrubyparser.ast.VCallNode;
import org.jrubyparser.ast.WhenNode;
import org.jrubyparser.ast.WhileNode;
import org.jrubyparser.ast.XStrNode;

import com.aptana.editor.ruby.formatter.internal.nodes.FormatterArrayNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterAtBeginNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterAtEndNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterBeginNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterCaseNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterClassNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterDoNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterElseIfNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterEnsureNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterForNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterHashNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterIfElseNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterIfEndNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterIfNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterMethodNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterModuleNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterRequireNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterRescueElseNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterRescueNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterStringNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterUntilNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterWhenElseNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterWhenNode;
import com.aptana.editor.ruby.formatter.internal.nodes.FormatterWhileNode;
import com.aptana.formatter.IFormatterDocument;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.formatter.nodes.IFormatterTextNode;
import com.aptana.ruby.core.ast.AbstractVisitor;

/**
 * This class builds the nodes by visiting the Ruby AST tree.
 */
public class RubyFormatterNodeBuilderVisitor extends AbstractVisitor
{

	private IFormatterDocument document;
	private RubyFormatterNodeBuilder builder;
	private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$

	protected RubyFormatterNodeBuilderVisitor(IFormatterDocument document, RubyFormatterNodeBuilder builder)
	{
		this.document = document;
		this.builder = builder;

	}

	protected Object visitNode(Node visited)
	{
		visitChildren(visited);
		return null;
	}

	public Object visitClassNode(ClassNode visited)
	{
		FormatterClassNode classNode = new FormatterClassNode(document);
		SourcePosition position = visited.getPosition();
		classNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), visited
				.getCPath().getPosition().getStartOffset()));
		builder.push(classNode);
		// visitChildren(visited);
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset;
		if (NodeType.NILNODE.equals(bodyNode.getNodeType()))
		{
			bodyEndOffset = classNode.getEndOffset();
		}
		else
		{
			bodyNode.accept(this);
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}
		builder.checkedPop(classNode, bodyEndOffset);
		classNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitSClassNode(SClassNode visited)
	{
		FormatterClassNode classNode = new FormatterClassNode(document);
		SourcePosition position = visited.getPosition();
		classNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), visited
				.getReceiverNode().getPosition().getStartOffset()));
		builder.push(classNode);
		visitChildren(visited);
		Node bodyNode = visited.getBodyNode();
		Node receiverNode = visited.getReceiverNode();
		int bodyEndOffset = position.getEndOffset();
		if (bodyNode != null)
		{
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}
		else if (receiverNode != null)
		{
			bodyEndOffset = receiverNode.getPosition().getEndOffset();
		}
		builder.checkedPop(classNode, bodyEndOffset);
		classNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitModuleNode(ModuleNode visited)
	{
		FormatterModuleNode moduleNode = new FormatterModuleNode(document);
		Colon3Node pathNode = visited.getCPath();
		moduleNode.setBegin(createTextNode(document, pathNode));
		builder.push(moduleNode);
		visitChildren(visited);
		SourcePosition position = visited.getPosition();
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset;
		if (bodyNode instanceof NilImplicitNode)
		{
			// empty 'module' body
			bodyEndOffset = moduleNode.getEndOffset();
		}
		else
		{
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}

		builder.checkedPop(moduleNode, bodyEndOffset);
		moduleNode
				.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitDefnNode(DefnNode visited)
	{
		return visitMethodDefNode(visited);
	}

	public Object visitDefsNode(DefsNode visited)
	{
		return visitMethodDefNode(visited);
	}

	private Object visitMethodDefNode(MethodDefNode visited)
	{
		FormatterMethodNode methodNode = new FormatterMethodNode(document);
		SourcePosition position = visited.getPosition();
		methodNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), visited
				.getNameNode().getPosition().getEndOffset()));
		builder.push(methodNode);
		visitChildren(visited);
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset;
		if (bodyNode != null)
		{
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}
		else
		{
			bodyEndOffset = locateEndOffset(document, position.getEndOffset());
		}
		builder.checkedPop(methodNode, bodyEndOffset);
		methodNode
				.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitWhileNode(WhileNode visited)
	{
		FormatterWhileNode whileNode = new FormatterWhileNode(document);
		SourcePosition position = visited.getPosition();
		Node conditionNode = visited.getConditionNode();
		whileNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(),
				conditionNode.getPosition().getEndOffset()));
		builder.push(whileNode);
		Node bodyNode = visited.getBodyNode();
		visitChildren(bodyNode);
		int bodyEndOffset;
		if (bodyNode instanceof NilImplicitNode)
		{
			// empty 'while' body
			bodyEndOffset = whileNode.getEndOffset();
		}
		else
		{
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}
		builder.checkedPop(whileNode, bodyEndOffset);
		whileNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitIterNode(IterNode visited)
	{
		FormatterDoNode forNode = new FormatterDoNode(document);
		// Note: The iteration may or may not have a 'varNode'. Also, it may or may not have a 'body'.

		Node varNode = visited.getVarNode();
		Node bodyNode = visited.getBodyNode();
		SourcePosition position = visited.getPosition();
		boolean isCurlyIteration = document.charAt(position.getStartOffset()) == '{';
		// Here we try to figure out what to place in the begin segment
		int beginEndOffset = position.getEndOffset();
		if (bodyNode != null)
		{
			beginEndOffset = bodyNode.getPosition().getStartOffset();
		}
		else
		{
			// we have an iteration without a body.
			if (varNode != null)
			{
				// we look for the start offset of the 'end'
				// keyword, right after the var-node
				beginEndOffset = varNode.getPosition().getEndOffset();
				if (isCurlyIteration)
				{
					// we need to locate the ending curly
					if (document.charAt(beginEndOffset - 1) != '}')
					{
						// cover a case like:
						// iteration {|x|
						// }
						beginEndOffset = position.getEndOffset() - 1;
					}
				}
				else
				{
					// we need to locate the starting of the 'end' keyword
					int endKeywordStart = charLookup(document, beginEndOffset, 'e');
					if (endKeywordStart > -1)
					{
						beginEndOffset = endKeywordStart;
					}
				}
			}
			else
			{
				// we have no body-node, nor var-node.
				// in this case we just look for the start of the 'end' keyword.
				beginEndOffset = locateEndOffset(document, position.getEndOffset());
			}
		}
		forNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(),
				beginEndOffset));
		builder.push(forNode);
		visitChildren(visited);
		int bodyEndOffset;
		if (bodyNode != null)
		{
			bodyEndOffset = bodyNode.getPosition().getEndOffset();
		}
		else
		{
			bodyEndOffset = forNode.getEndOffset();
		}
		builder.checkedPop(forNode, bodyEndOffset);
		forNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset,
				Math.max(bodyEndOffset, position.getEndOffset())));
		return null;
	}

	public Object visitForNode(ForNode visited)
	{
		FormatterForNode forNode = new FormatterForNode(document);
		Node bodyNode = visited.getBodyNode();
		Node iterNode = visited.getIterNode();
		int bodyStart = -1;
		int bodyEnd = -1;
		SourcePosition position = visited.getPosition();
		if (iterNode != null)
		{
			bodyStart = iterNode.getPosition().getEndOffset();
		}
		if (bodyNode != null)
		{
			if (bodyStart < 0)
			{
				bodyStart = locateEndOffset(document, position.getEndOffset());
			}
			bodyEnd = bodyNode.getPosition().getEndOffset() - 1;
		}
		else
		{
			if (bodyStart < 0)
			{
				bodyStart = locateEndOffset(document, position.getEndOffset());
			}
			if (bodyEnd < 0)
			{
				bodyEnd = bodyStart;
			}
		}
		forNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), bodyStart));
		builder.push(forNode);
		if (bodyNode != null)
		{
			bodyNode.accept(this);
		}
		builder.checkedPop(forNode, bodyEnd);
		forNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEnd, position.getEndOffset()));
		return null;
	}

	public Object visitUntilNode(UntilNode visited)
	{
		FormatterUntilNode untilNode = new FormatterUntilNode(document);
		untilNode.setBegin(createTextNode(document, visited));
		builder.push(untilNode);
		visitChildren(visited);
		SourcePosition position = visited.getPosition();
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset = bodyNode.getPosition().getEndOffset();
		builder.checkedPop(untilNode, bodyEndOffset);
		untilNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitCaseNode(CaseNode visited)
	{
		FormatterCaseNode caseNode = new FormatterCaseNode(document);
		SourcePosition position = visited.getPosition();
		caseNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), visited
				.getCaseNode().getPosition().getEndOffset()));
		builder.push(caseNode);
		Node branch = visited.getFirstWhenNode();
		List<Node> children;
		if (branch instanceof ArrayNode)
		{
			children = branch.childNodes();
		}
		else
		{
			children = new ArrayList<Node>();
			children.add(branch);
		}
		int bodyEndOffset = position.getEndOffset();
		for (Node child : children)
		{
			if (child instanceof WhenNode)
			{
				WhenNode whenBranch = (WhenNode) child;
				FormatterWhenNode whenNode = new FormatterWhenNode(document);
				SourcePosition branchPosition = child.getPosition();
				whenNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document,
						branchPosition.getStartOffset(), whenBranch.getExpressionNodes().getPosition().getEndOffset()));
				builder.push(whenNode);
				Node whenBodyNode = whenBranch.getBodyNode();
				visitChild(whenBodyNode);
				builder.checkedPop(whenNode, whenBodyNode.getPosition().getEndOffset());
				bodyEndOffset = whenNode.getEndOffset();
			}
			else
			{
				FormatterWhenElseNode whenElseNode = new FormatterWhenElseNode(document);
				SourcePosition elsePosition = child.getPosition();
				whenElseNode.setBegin(createTextNode(document, child));
				builder.push(whenElseNode);
				visitChildren(child.childNodes());
				builder.checkedPop(whenElseNode, elsePosition.getEndOffset());
				bodyEndOffset = whenElseNode.getEndOffset();
			}
		}
		builder.checkedPop(caseNode, bodyEndOffset);
		caseNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitIfNode(IfNode visited)
	{
		SourcePosition position = visited.getPosition();
		if (isInSameLineExcludingWhitespaces(position))
		{
			// Inline if
			List<Node> children = new ArrayList<Node>(3);
			if (visited.getThenBody() != null)
			{
				children.add(visited.getThenBody());
			}
			if (visited.getElseBody() != null)
			{
				children.add(visited.getElseBody());
			}
			if (visited.getCondition() != null)
			{
				children.add(visited.getCondition());
			}
			if (!children.isEmpty())
			{
				Collections.sort(children, POSITION_COMPARATOR);
				visitChildren(children);
			}
			return null;
		}
		FormatterIfNode ifNode = new FormatterIfNode(document);
		ifNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(), visited
				.getCondition().getPosition().getEndOffset()));
		builder.push(ifNode);
		Node thenBody = visited.getThenBody();
		Node elseBody = visited.getElseBody();

		if (thenBody instanceof ReturnNode || elseBody instanceof ReturnNode)
		{
			// we have a special case of 'return if x' or 'return unless x' expression
			builder.checkedPop(ifNode, ifNode.getEndOffset());
			return null;
		}
		// Flip the 'else' and 'then' in case the 'then' appears 'after the 'else'
		// This is the case with 'unless', so we flip it to make it easier to handle.
		int ifNodeEnd = -1; // the end position for the entire if block.
		if (elseBody != null)
		{
			ifNodeEnd = elseBody.getPosition().getEndOffset();
		}
		else if (thenBody != null)
		{
			ifNodeEnd = thenBody.getPosition().getEndOffset();
		}
		if (elseBody != null && thenBody != null)
		{
			if (thenBody.getPosition().getStartOffset() > elseBody.getPosition().getStartOffset())
			{
				// we also need to update the end position of the entire if block
				ifNodeEnd = thenBody.getPosition().getEndOffset();
				// flip the 'the' and 'else'
				Node temp = thenBody;
				thenBody = elseBody;
				elseBody = temp;
			}
		}
		visitChild(thenBody);
		if (thenBody == null && elseBody != null)
		{
			// We have an 'unless' case, so we just visit the else-boby
			visitChild(elseBody);
		}
		builder.checkedPop(ifNode, ifNode.getEndOffset());

		while (elseBody != null && thenBody != null)
		{
			if (elseBody instanceof IfNode)
			{
				// elsif
				IfNode elseIfBranch = (IfNode) elseBody;
				FormatterElseIfNode elseIfNode = new FormatterElseIfNode(document);
				elseIfNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, thenBody.getPosition()
						.getEndOffset(), elseIfBranch.getCondition().getPosition().getEndOffset()));
				builder.push(elseIfNode);
				thenBody = elseIfBranch.getThenBody();
				visitChild(thenBody);
				elseBody = elseIfBranch.getElseBody();
				builder.checkedPop(elseIfNode, elseIfNode.getEndOffset());
			}
			else
			{
				// else
				FormatterIfElseNode elseNode = new FormatterIfElseNode(document);
				elseNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, thenBody.getPosition()
						.getEndOffset(), elseBody.getPosition().getStartOffset()));
				builder.push(elseNode);
				visitChild(elseBody);
				builder.checkedPop(elseNode, elseNode.getEndOffset());
				elseBody = null;
			}
		}

		if (ifNodeEnd < 0)
		{
			// locate the 'end'
			ifNodeEnd = locateEndOffset(document, position.getEndOffset());
		}
		builder.addChild(new FormatterIfEndNode(document, ifNodeEnd, position.getEndOffset()));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.ruby.parsing.ast.AbstractVisitor#visitBeginNode(org.jrubyparser.ast.BeginNode)
	 */
	public Object visitBeginNode(BeginNode visited)
	{
		/**
		 * TODO -NEED TO SUPPORT THIS
		 * 
		 * <pre>
		 * begin
		 *   # main code here                                 				
		 * rescue SomeException                               				
		 *   # ...                                            				
		 * rescue AnotherException                            				
		 *   # ..                                             				
		 * else                                               				
		 *   # stuff you want to happen AFTER the main code,  				
		 *   # but BEFORE the ensure block, but only if there 				
		 *   # were no exceptions raised. Note, too, that     				
		 *   # exceptions raised here won't be rescued by the 				
		 *   # rescue clauses above.                          				
		 * ensure                                             				
		 *   # stuff that should happen dead last, and        				
		 *   # regardless of whether any exceptions were      				
		 *   # raised or not.                                 				
		 * end
		 * </pre>
		 */
		FormatterBeginNode beginNode = new FormatterBeginNode(document);
		SourcePosition beginPosition = visited.getPosition();
		// We first need to find the body-node of the begin section.
		// It can be right under the BeginNode, but in cases where we have
		// A RescueNode or an EnsureNode, it will be under those nodes.
		Node bodyNode = visited.getBodyNode();
		RescueBodyNode rescueBodyNode = null;
		int beginEndOffset = -1;
		if (bodyNode instanceof EnsureNode)
		{
			EnsureNode ensureNode = (EnsureNode) bodyNode;
			bodyNode = ensureNode.getBodyNode();
			Node innerEnsureNode = ensureNode.getEnsureNode();
			if (!(innerEnsureNode instanceof NilImplicitNode))
			{
				beginEndOffset = innerEnsureNode.getPosition().getEndOffset();
			}
		}
		if (bodyNode instanceof RescueNode)
		{
			RescueNode rescueNode = (RescueNode) bodyNode;
			bodyNode = rescueNode.getBodyNode();
			rescueBodyNode = rescueNode.getRescueNode();
		}
		if (bodyNode instanceof NilImplicitNode)
		{
			// we have an emply body here
			if (beginEndOffset < 0)
			{
				beginEndOffset = locateEndOffset(document, beginPosition.getEndOffset());
			}
			beginNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, beginPosition.getStartOffset(),
					beginEndOffset));
			builder.push(beginNode);
			builder.checkedPop(beginNode, beginEndOffset);
			beginNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, beginEndOffset,
					beginPosition.getEndOffset()));
		}
		else
		{
			SourcePosition bodyNodePosition = bodyNode.getPosition();
			if (beginEndOffset < 0)
			{
				if (rescueBodyNode instanceof RescueBodyNode)
				{
					Node innerBodyNode = rescueBodyNode.getBodyNode();
					if (!(innerBodyNode instanceof NilImplicitNode))
					{
						beginEndOffset = innerBodyNode.getPosition().getEndOffset();
					}
					else
					{
						beginEndOffset = rescueBodyNode.getPosition().getEndOffset();
					}
				}
				else
				{
					beginEndOffset = bodyNode.getPosition().getEndOffset();
				}
			}
			beginNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, beginPosition.getStartOffset(),
					bodyNodePosition.getStartOffset()));
			builder.push(beginNode);
			visitChild(visited.getBodyNode());
			builder.checkedPop(beginNode, -1);
			beginNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, beginNode.getEndOffset(),
					beginPosition.getEndOffset()));
		}
		return null;
	}

	public Object visitRescueNode(RescueNode visited)
	{
		visitChild(visited.getBodyNode());
		RescueBodyNode rescueBody = visited.getRescueNode();
		int lastBodyEndOffset = -1;
		while (rescueBody != null)
		{
			FormatterRescueNode rescueNode = new FormatterRescueNode(document);
			Node bodyInnerNode = rescueBody.getBodyNode();
			int rescueBeginEndOffset = -1;
			SourcePosition position = rescueBody.getPosition();
			if (!(bodyInnerNode instanceof NilImplicitNode))
			{
				SourcePosition innerBodyPosition = bodyInnerNode.getPosition();
				rescueBeginEndOffset = innerBodyPosition.getStartOffset();
				lastBodyEndOffset = innerBodyPosition.getEndOffset();
			}
			else
			{
				// try to look into the exception nodes
				Node exceptionNodes = rescueBody.getExceptionNodes();
				if (exceptionNodes != null)
				{
					rescueBeginEndOffset = exceptionNodes.getPosition().getEndOffset();
				}
				else if (visited.getElseNode() != null)
				{
					rescueBeginEndOffset = visited.getElseNode().getPosition().getStartOffset();
				}
				else
				{
					// FIXME - This will probably fail if we have nested rescue blocks
					rescueBeginEndOffset = document.get(position.getStartOffset(), position.getEndOffset())
							.lastIndexOf("rescue") + position.getStartOffset() + 6; //$NON-NLS-1$
				}
				lastBodyEndOffset = rescueBeginEndOffset;
			}
			rescueNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(),
					rescueBeginEndOffset));
			builder.push(rescueNode);
			visitChild(rescueBody.getBodyNode());
			rescueBody = rescueBody.getOptRescueNode();
			final int rescueEnd;
			if (rescueBody != null)
			{
				rescueEnd = position.getStartOffset();
			}
			else if (visited.getElseNode() != null)
			{
				rescueEnd = lastBodyEndOffset;
			}
			else
			{
				rescueEnd = rescueNode.getEndOffset();
			}
			builder.checkedPop(rescueNode, rescueEnd);
		}
		if (visited.getElseNode() != null)
		{
			final Node elseBranch = visited.getElseNode();
			FormatterRescueElseNode elseNode = new FormatterRescueElseNode(document);
			elseNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, lastBodyEndOffset, elseBranch
					.getPosition().getStartOffset()));
			builder.push(elseNode);
			visitChildren(elseBranch.childNodes());
			builder.checkedPop(elseNode, -1);
		}
		return null;
	}

	public Object visitEnsureNode(EnsureNode visited)
	{
		Node bodyNode = visited.getBodyNode();
		visitChild(bodyNode);
		FormatterEnsureNode ensureNode = new FormatterEnsureNode(document);
		Node node = visited.getEnsureNode();
		SourcePosition position = visited.getPosition();
		// FIXME - This will probably fail if we have nested rescue blocks
		int ensureStartOffset = document.get(position.getStartOffset(), position.getEndOffset()).lastIndexOf("ensure") //$NON-NLS-1$
				+ position.getStartOffset();
		ensureNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, ensureStartOffset, node.getPosition()
				.getStartOffset()));
		builder.push(ensureNode);
		visitChildren(node.childNodes());
		builder.checkedPop(ensureNode, -1);
		return null;
	}

	public Object visitPreExeNode(PreExeNode visited)
	{
		FormatterAtBeginNode endNode = new FormatterAtBeginNode(document);
		endNode.setBegin(createTextNode(document, visited));
		builder.push(endNode);
		visitChildren(visited);
		SourcePosition position = visited.getPosition();
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset = bodyNode.getPosition().getEndOffset();
		builder.checkedPop(endNode, bodyEndOffset);
		endNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitPostExeNode(PostExeNode visited)
	{
		FormatterAtEndNode endNode = new FormatterAtEndNode(document);
		endNode.setBegin(createTextNode(document, visited));
		builder.push(endNode);
		visitChildren(visited);
		SourcePosition position = visited.getPosition();
		Node bodyNode = visited.getBodyNode();
		int bodyEndOffset = bodyNode.getPosition().getEndOffset();
		builder.checkedPop(endNode, bodyEndOffset);
		endNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEndOffset, position.getEndOffset()));
		return null;
	}

	public Object visitNilNode(NilNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitConstNode(ConstNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ruby.core.ast.AbstractVisitor#visitSymbolNode(org.jrubyparser.ast.SymbolNode)
	 */
	@Override
	public Object visitSymbolNode(SymbolNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitStrNode(StrNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitVCallNode(VCallNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitDStrNode(DStrNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitMatch2Node(Match2Node visited)
	{
		SourcePosition position = visited.getPosition();
		SourcePosition receiverPosition = visited.getReceiverNode().getPosition();
		pushTextNode(position.getStartOffset(), receiverPosition.getEndOffset());
		return null;
	}

	public Object visitMatch3Node(Match3Node visited)
	{
		visitNode(visited.getValueNode());
		visitNode(visited.getReceiverNode());
		return null;
	}

	public Object visitMatchNode(MatchNode visited)
	{
		SourcePosition position = visited.getPosition();
		SourcePosition receiverPosition = visited.getRegexpNode().getPosition();
		pushTextNode(position.getStartOffset(), receiverPosition.getEndOffset());
		return null;
	}

	public Object visitRegexpNode(RegexpNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitDRegxNode(DRegexpNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitXStrNode(XStrNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	public Object visitFCallNode(FCallNode visited)
	{
		if (isRequireMethod(visited))
		{
			SourcePosition position = visited.getPosition();
			FormatterRequireNode requireNode = new FormatterRequireNode(document, position.getStartOffset(),
					position.getEndOffset());
			builder.addChild(requireNode);
		}
		else if (visited.getIterNode() != null)
		{
			// it's a block iteration node
			int iterStart = visited.getIterNode().getPosition().getStartOffset();
			pushTextNode(visited.getPosition().getStartOffset(), iterStart);
			visitChild(visited.getIterNode());
		}
		else
		{
			pushTextNode(visited.getPosition());
		}
		return null;
	}

	public Object visitArrayNode(ArrayNode visited)
	{
		IFormatterContainerNode containerNode = builder.peek();
		if (containerNode.getStartOffset() == visited.getPosition().getStartOffset())
		{
			// just analyze the children. This is an ArrayNode inside an ArrayNode
			visitChildren(visited);
			return null;
		}
		SourcePosition position = visited.getPosition();
		String right = document.get(position.getStartOffset(), position.getStartOffset() + 1);
		String left = document.get(position.getEndOffset() - 1, position.getEndOffset());
		if ("[".equals(right) && "]".equals(left)) { //$NON-NLS-1$ //$NON-NLS-2$
			final FormatterArrayNode arrayNode = new FormatterArrayNode(document);
			arrayNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(),
					position.getStartOffset() + 1));
			builder.push(arrayNode);
			visitChildren(visited);
			builder.checkedPop(arrayNode, position.getEndOffset() - 1);
			arrayNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, position.getEndOffset() - 1,
					position.getEndOffset()));
			return null;
		}
		else
		{
			// we should probably better handle ArrayNodes that arrive without the brackets.
			// For example:
			// job = Delayed::Job.find :first,
			// :order => "run_at ASC"
			return super.visitArrayNode(visited);
		}
	}

	public Object visitHashNode(HashNode visited)
	{

		SourcePosition position = visited.getPosition();
		String left = document.get(position.getStartOffset(),
				Math.min(position.getStartOffset() + 1, document.getLength()));
		String right = document.get(Math.max(0, position.getEndOffset() - 1), position.getEndOffset());
		if ("{".equals(left) && "}".equals(right)) { //$NON-NLS-1$ //$NON-NLS-2$
			final FormatterHashNode hashNode = new FormatterHashNode(document);
			hashNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, position.getStartOffset(),
					position.getStartOffset() + 1));
			builder.push(hashNode);
			// builder.checkedPop(hashNode, right.getStartOffset());
			visitChildren(visited);
			builder.checkedPop(hashNode, position.getEndOffset() - 1);
			hashNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, position.getEndOffset() - 1,
					position.getEndOffset()));
			return null;
		}
		else
		{
			return super.visitHashNode(visited);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ruby.core.ast.AbstractVisitor#visitBlockNode(org.jrubyparser.ast.BlockNode)
	 */
	@Override
	public Object visitBlockNode(BlockNode visited)
	{
		visitChildren(visited.childNodes());
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ruby.core.ast.AbstractVisitor#visitInstAsgnNode(org.jrubyparser.ast.InstAsgnNode)
	 */
	@Override
	public Object visitInstAsgnNode(InstAsgnNode visited)
	{
		pushTextNode(visited.getPosition());
		return null;
	}

	private boolean isRequireMethod(FCallNode call)
	{
		if ("require".equals(call.getName())) //$NON-NLS-1$
		{
			if (call.getArgsNode() instanceof ArrayNode)
			{
				return true;
			}
		}
		return false;
	}

	private void visitChildren(Node visited)
	{
		final List<Node> children = visited.childNodes();
		if (!children.isEmpty())
		{
			visitChildren(children);
		}
	}

	private void visitChildren(List<Node> children)
	{
		for (Node child : children)
		{
			visitChild(child);
		}
	}

	private void visitChild(final Node child)
	{
		if (child != null && isVisitable(child))
		{
			try
			{
				child.accept(this);
			}
			catch (UnsupportedOperationException e)
			{
				if (child instanceof OptArgNode)
				{
					visitOptArgNode((OptArgNode) child);
				}
			}
		}
	}

	private boolean isVisitable(Node node)
	{
		return !(node instanceof ArgumentNode) && node.getClass() != ListNode.class;
	}

	/**
	 * @param positionHolder
	 * @return
	 */
	protected IFormatterTextNode createTextNode(IFormatterDocument document, ISourcePositionHolder positionHolder)
	{
		return createTextNode(document, positionHolder.getPosition());
	}

	/**
	 * Locate the word 'end' by searching for it from right to left in the given document. The assumption here is that
	 * the 'end' word is actually there, and the only thing that separate us from reaching it are white-spaces.
	 * 
	 * @param document
	 * @param rightOffset
	 * @return The start offset of the 'end' word
	 */
	protected int locateEndOffset(IFormatterDocument document, int rightOffset)
	{
		String toLocate = "end"; //$NON-NLS-1$
		int wordLength = toLocate.length();
		do
		{
			int leftOffset = rightOffset - wordLength;
			String endString = document.get(leftOffset, rightOffset);
			if (toLocate.equals(endString))
			{
				// found it!
				return leftOffset;
			}
			rightOffset--;
		}
		while (rightOffset - wordLength >= 0);
		// if we got here, we didn't find the 'end'
		return rightOffset;
	}

	/**
	 * Look for the given char in the document and return it's position.
	 * 
	 * @param document
	 * @param offset
	 * @param c
	 * @return The char offset; -1 if no matching char was found
	 */
	protected int charLookup(final IFormatterDocument document, int offset, char c)
	{
		while (offset + 1 < document.getLength())
		{
			if (document.charAt(offset) == c)
			{
				return offset;
			}
			offset++;
		}
		return -1;
	}

	/**
	 * @param position
	 * @return
	 */
	private IFormatterTextNode createTextNode(IFormatterDocument document, SourcePosition position)
	{
		return AbstractFormatterNodeBuilder
				.createTextNode(document, position.getStartOffset(), position.getEndOffset());
	}

	/**
	 * Push a FormatterStringNode for the given position.
	 * 
	 * @param position
	 */
	private void pushTextNode(SourcePosition position)
	{
		int startOffset = position.getStartOffset();
		int endOffset = position.getEndOffset();
		if (startOffset < endOffset)
		{
			pushTextNode(startOffset, endOffset);
		}
	}

	/**
	 * Push a FormatterStringNode for the given start and end offsets.
	 * 
	 * @param startOffset
	 * @param endOffset
	 */
	private void pushTextNode(int startOffset, int endOffset)
	{
		FormatterStringNode strNode = new FormatterStringNode(document, startOffset, endOffset);
		builder.addChild(strNode);
	}

	/**
	 * Returns true if the given position start and end lines are equal, or if the end-line was pushed only because
	 * new-lines and white spaces appear in the code.
	 * 
	 * @param position
	 * @return
	 */
	private boolean isInSameLineExcludingWhitespaces(SourcePosition position)
	{
		if (position.getStartLine() == position.getEndLine())
		{
			return true;
		}
		// We split on new-lines, and in case the split result is giving us a String array of one element, we know that
		// all the rest of the lines in that text were new-lines terminators.
		String text = document.get(position.getStartOffset(), position.getEndOffset());
		String[] linesSplit = LINE_SPLIT_PATTERN.split(text);
		if (linesSplit.length == 1)
		{
			return true;
		}
		else if (linesSplit.length > 1)
		{
			int start = (linesSplit[0].trim().length() == 0) ? 0 : 1;
			int end = (linesSplit[linesSplit.length - 1].trim().length() == 0) ? linesSplit.length
					: linesSplit.length - 1;
			boolean allWhiteSpace = true;
			for (int i = start; i < end && allWhiteSpace; i++)
			{
				allWhiteSpace &= linesSplit[i].trim().length() == 0;
			}
			return allWhiteSpace;
		}
		return false;
	}

	protected static final Comparator<Node> POSITION_COMPARATOR = new Comparator<Node>()
	{

		public int compare(Node n1, Node n2)
		{
			return n1.getPosition().getStartOffset() - n2.getPosition().getStartOffset();
		}
	};
}
