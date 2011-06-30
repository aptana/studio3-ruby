/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby.internal.contentassist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.HippieProposalProcessor;
import org.jruby.Ruby;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.ClassVarAsgnNode;
import org.jrubyparser.ast.ClassVarDeclNode;
import org.jrubyparser.ast.Colon3Node;
import org.jrubyparser.ast.ConstNode;
import org.jrubyparser.ast.InstAsgnNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonContentAssistProcessor;
import com.aptana.editor.common.contentassist.CommonCompletionProposal;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.index.core.Index;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;
import com.aptana.ruby.core.IRubyMethod.Visibility;
import com.aptana.ruby.core.ast.ASTUtils;
import com.aptana.ruby.core.ast.INodeAcceptor;
import com.aptana.ruby.core.ast.ScopedNodeLocator;
import com.aptana.ruby.core.codeassist.CompletionContext;
import com.aptana.ruby.core.index.IRubyIndexConstants;
import com.aptana.ruby.core.index.RubyIndexUtil;
import com.aptana.ruby.core.inference.ITypeGuess;
import com.aptana.scripting.model.ContentAssistElement;

/**
 * @author cwilliams
 */
public class RubyContentAssistProcessor extends CommonContentAssistProcessor
{

	private static final String NAMESPACE_DELIMITER = IRubyIndexConstants.NAMESPACE_DELIMETER;
	// TODO Move these images over to the ruby.ui plugin...
	private static final String GLOBAL_IMAGE = "icons/global_obj.png"; //$NON-NLS-1$
	private static final String INSTANCE_VAR_IMAGE = "icons/instance_var_obj.png"; //$NON-NLS-1$
	private static final String CLASS_VAR_IMAGE = "icons/class_var_obj.png"; //$NON-NLS-1$
	private static final String LOCAL_VAR_IMAGE = "icons/local_var_obj.png"; //$NON-NLS-1$
	private static final String PUBLIC_METHOD_IMAGE = "icons/method_public_obj.png"; //$NON-NLS-1$
	private static final String CLASS_IMAGE = "icons/class_obj.png"; //$NON-NLS-1$
	private static final String MODULE_IMAGE = "icons/module_obj.png"; //$NON-NLS-1$
	private static final String PROTECTED_METHOD_IMAGE = "icons/method_protected_obj.png"; //$NON-NLS-1$
	private static final String CONSTANT_IMAGE = "icons/constant_obj.png"; //$NON-NLS-1$

	/**
	 * Performance events
	 */
	private static final String CALC_SUPER_TYPE_EVENT = RubyEditorPlugin.PLUGIN_ID
			+ "/perf/content_assist/calc_hierarchy"; //$NON-NLS-1$
	private static final String METHOD_PROPOSALS_FOR_TYPE_EVENT = RubyEditorPlugin.PLUGIN_ID
			+ "/perf/content_assist/type_methods"; //$NON-NLS-1$

	/**
	 * Static list of ruby keywords
	 */
	@SuppressWarnings("nls")
	private static final String[] KEYWORDS = new String[] { "alias", "and", "BEGIN", "begin", "break", "case", "class",
			"def", "defined", "do", "else", "elsif", "END", "end", "ensure", "false", "for", "if", "in", "module",
			"next", "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true", "undef",
			"unless", "until", "when", "while", "yield" };

	private CompletionContext fContext;

	public RubyContentAssistProcessor(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	protected java.util.Collection<? extends ICompletionProposal> addRubleCAProposals(ITextViewer viewer, int offset,
			Ruby ruby, ContentAssistElement ce)
	{
		// HACK to force not to use the ruby ruble CA in case it's still around...
		if ("Type Inference code assist".equals(ce.getDisplayName())) //$NON-NLS-1$
		{
			return Collections.emptyList();
		}
		return super.addRubleCAProposals(viewer, offset, ruby, ce);
	};

	@Override
	protected ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset, char activationChar,
			boolean autoActivated)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		fContext = new CompletionContext(getProject(), viewer.getDocument().get(), offset - 1);
		try
		{
			if (fContext.inComment() || fContext.getPartialPrefix().endsWith(":")) //$NON-NLS-1$
			{
				return new ICompletionProposal[0];
			}
			else if (fContext.isNotParseable())
			{

				proposals.addAll(suggestKeywords());
			}
			else if (fContext.emptyPrefix())
			{
				// JDT shows locals in method then fields and constants for enclosing type, then methods for
				// enclosing type
				proposals.addAll(suggestLocalVariables());
				proposals.addAll(suggestInstanceVariables());
				proposals.addAll(suggestClassVariables());
				proposals.addAll(suggestMethodsForEnclosingType());
			}
			else if (fContext.isConstant())
			{
				// JDT suggests constants, then types
				proposals.addAll(suggestConstants());
				proposals.addAll(suggestTypeNames());
			}
			else if (fContext.isGlobal())
			{
				proposals.addAll(suggestGlobals());
			}
			else if (fContext.isInstanceVariable())
			{
				proposals.addAll(suggestInstanceVariables());
			}
			else if (fContext.isClassVariable())
			{
				proposals.addAll(suggestClassVariables());
			}
			else if (fContext.isInstanceOrClassVariable())
			{
				proposals.addAll(suggestInstanceVariables());
				proposals.addAll(suggestClassVariables());
			}
			// TODO Add symbol CA!
			else if (fContext.isDoubleSemiColon())
			{
				// This is either a qualified type name, method call, or qualified constant name
				// When after last "::", if empty or uppercase, it's a type or constant
				if (fContext.getPartialPrefix().length() == 0
						|| Character.isUpperCase(fContext.getPartialPrefix().charAt(0)))
				{
					proposals.addAll(suggestTypesInNamespace());
					proposals.addAll(suggestConstantsInNamespace());
				}

				proposals.addAll(suggestMethodsOnReceiver());
			}
			else if (fContext.isExplicitMethodInvokation())
			{
				proposals.addAll(suggestMethodsOnReceiver());
			}
			else if (fContext.isMethodInvokationOrLocal())
			{
				proposals.addAll(suggestKeywords());
				// JDT suggests locals, then methods
				proposals.addAll(suggestLocalVariables());
				proposals.addAll(suggestMethodsForEnclosingType());
				// Add word completions to round it out
				// Don't suggest duplicates of methods/locals suggested!
				Collection<? extends ICompletionProposal> wordCompletions = suggestWordCompletions(viewer, offset);
				wordCompletions = removeDuplicates(proposals, wordCompletions);
				proposals.addAll(wordCompletions);
			}
			sortByDisplayName(proposals);
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		finally
		{
			fContext = null;
		}
	}

	private Collection<? extends ICompletionProposal> suggestMethodsOnReceiver()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Collection<ITypeGuess> guesses = fContext.inferReceiver();
		Set<String> typeNames = new HashSet<String>();
		for (ITypeGuess guess : guesses)
		{
			String typeName = guess.getType();
			typeNames.add(typeName);
			// Include supertypes
			typeNames.addAll(calculateSuperTypes(typeName));
		}
		// Based on what the receiver is (if it's a type name) we should toggle instance/singleton
		// methods!
		boolean receiverIsType = receiverIsType();
		proposals.addAll(suggestMethodsForType(typeNames, !receiverIsType, false));
		if (receiverIsType)
		{
			// TODO Insert the class name as the "location"?
			proposals.add(createProposal("new", RubyEditorPlugin.getImage(PUBLIC_METHOD_IMAGE))); //$NON-NLS-1$
		}
		return proposals;
	}

	private Collection<? extends ICompletionProposal> suggestKeywords()
	{
		List<ICompletionProposal> keywords = new ArrayList<ICompletionProposal>();
		String prefix = fContext.getPartialPrefix();
		for (String keyword : KEYWORDS)
		{
			if (keyword.startsWith(prefix))
			{
				keywords.add(createProposal(keyword, null));
			}
		}
		return keywords;
	}

	private boolean receiverIsType()
	{
		String constantName = null;
		String namespace = StringUtil.EMPTY;
		String typeName = StringUtil.EMPTY;
		Node receiver = fContext.getReceiver();
		if (receiver instanceof Colon3Node || receiver instanceof ConstNode)
		{
			// FIXME If the receiver as text equals the inferred type, then it's a type... That's probably way
			// quicker...
			String fullName = ASTUtils.getFullyQualifiedName(receiver);
			constantName = fullName;
			int namespaceIndex = fullName.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER);
			if (namespaceIndex != -1)
			{
				typeName = fullName.substring(0, namespaceIndex);
				constantName = fullName.substring(namespaceIndex + 2);

				namespaceIndex = typeName.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER);
				if (namespaceIndex != -1)
				{
					namespace = typeName.substring(0, namespaceIndex);
					typeName = typeName.substring(namespaceIndex + 2);
				}
			}
		}
		else
		{
			return false;
		}

		// Check the indices to see if this is a constant or a type! If constant, we need to infer that
		// constant decl!
		final String key = constantName + IRubyIndexConstants.SEPARATOR + typeName + IRubyIndexConstants.SEPARATOR
				+ namespace;
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.CONSTANT_DECL }, key,
					SearchPattern.EXACT_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null || results.isEmpty())
			{
				continue;
			}
			// Found at least one match, assume that the receiver isn't a constant...
			return false;
		}
		return true;
	}

	/**
	 * Remove word completions that already exist as local or method proposals.
	 * 
	 * @param proposals
	 * @param wordCompletions
	 * @return
	 */
	private Collection<? extends ICompletionProposal> removeDuplicates(List<ICompletionProposal> proposals,
			Collection<? extends ICompletionProposal> wordCompletions)
	{
		if (wordCompletions == null || wordCompletions.isEmpty())
		{
			return wordCompletions;
		}
		List<ICompletionProposal> uniques = new ArrayList<ICompletionProposal>();
		Set<String> displayStrings = new HashSet<String>();
		for (ICompletionProposal proposal : proposals)
		{
			displayStrings.add(proposal.getDisplayString());
		}
		for (ICompletionProposal woProposal : wordCompletions)
		{
			if (!displayStrings.contains(woProposal.getDisplayString()))
			{
				uniques.add(woProposal);
			}
		}
		return uniques;
	}

	/**
	 * Suggests possible types that live under a namespace matching the full prefix.
	 * 
	 * @return
	 */
	@SuppressWarnings("nls")
	private Collection<? extends ICompletionProposal> suggestTypesInNamespace()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String fullPrefix = fContext.getFullPrefix();
		String namespace = fullPrefix.substring(0, fullPrefix.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER));
		String key = "^[^/]+?" + IRubyIndexConstants.SEPARATOR + namespace + "[^/]*?" + IRubyIndexConstants.SEPARATOR
				+ ".+$";
		Map<String, Boolean> proposalToIsClass = new HashMap<String, Boolean>();
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.TYPE_DECL }, key,
					SearchPattern.REGEX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				String aNamespace = getNamespace(result.getWord());
				if (namespace.equals(aNamespace))
				{
					// Exact namespace match. Suggest the simple type name
					String typeName = getTypeName(result.getWord());
					proposalToIsClass.put(typeName, true);
				}
				else if (aNamespace.startsWith(fullPrefix))
				{
					// Suggest next segment of aNamespace
					int previousDelim = aNamespace.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER,
							fullPrefix.length());
					if (previousDelim == -1)
					{
						previousDelim = 0;
					}
					else
					{
						previousDelim += 2;
					}
					int nextDelim = aNamespace.indexOf(IRubyIndexConstants.NAMESPACE_DELIMETER, fullPrefix.length());
					if (nextDelim == -1)
					{
						nextDelim = aNamespace.length();
					}

					String nextSegment = aNamespace.substring(previousDelim, nextDelim);
					proposalToIsClass.put(nextSegment, false);
				}
			}
		}
		// Enforce unique proposals by using map
		for (Map.Entry<String, Boolean> entry : proposalToIsClass.entrySet())
		{
			proposals.add(createProposal(entry.getKey(),
					RubyEditorPlugin.getImage(entry.getValue() ? CLASS_IMAGE : MODULE_IMAGE)));
		}
		return proposals;
	}

	/**
	 * Suggests constants living under the given namespace in prefix.
	 * 
	 * @return
	 */
	@SuppressWarnings("nls")
	private Collection<? extends ICompletionProposal> suggestConstantsInNamespace()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String fullPrefix = fContext.getFullPrefix();
		String namespace = fullPrefix.substring(0, fullPrefix.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER));
		String typeName = null;
		int namespaceIndex = namespace.lastIndexOf(IRubyIndexConstants.NAMESPACE_DELIMETER);
		if (namespaceIndex == -1)
		{
			typeName = namespace;
			namespace = StringUtil.EMPTY;
		}
		else
		{
			typeName = namespace.substring(namespaceIndex + 2);
			namespace = namespace.substring(0, namespaceIndex);
		}
		String key = "^" + fContext.getPartialPrefix() + "[^/]*?" + IRubyIndexConstants.SEPARATOR + typeName
				+ IRubyIndexConstants.SEPARATOR + namespace + "[^/]*$";
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.CONSTANT_DECL }, key,
					SearchPattern.REGEX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				String indexKey = result.getWord();
				proposals.add(createProposal(indexKey.substring(0, indexKey.indexOf(IRubyIndexConstants.SEPARATOR)),
						RubyEditorPlugin.getImage(CONSTANT_IMAGE)));
			}
		}
		return proposals;
	}

	protected void sortByDisplayName(List<ICompletionProposal> proposals)
	{
		// Sort by display string, ignoring case
		Collections.sort(proposals, new Comparator<ICompletionProposal>()
		{

			public int compare(ICompletionProposal o1, ICompletionProposal o2)
			{
				return o1.getDisplayString().compareToIgnoreCase(o2.getDisplayString());
			}
		});
	}

	private Collection<? extends ICompletionProposal> suggestWordCompletions(ITextViewer viewer, int offset)
	{
		ICompletionProposal[] hippieProposals = new HippieProposalProcessor()
				.computeCompletionProposals(viewer, offset);
		if (hippieProposals == null || hippieProposals.length == 0)
		{
			return Collections.emptyList();
		}
		return Arrays.asList(hippieProposals);
	}

	private Collection<? extends ICompletionProposal> suggestLocalVariables()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (String localName : fContext.getLocalsInScope())
		{
			CommonCompletionProposal proposal = createProposal(localName, RubyEditorPlugin.getImage(LOCAL_VAR_IMAGE));
			proposals.add(proposal);
		}
		return proposals;
	}

	private Collection<? extends ICompletionProposal> suggestInstanceVariables()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Node enclosing = fContext.getEnclosingTypeNode();
		List<Node> instAssignments = new ScopedNodeLocator().find(enclosing, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node instanceof InstAsgnNode;
			}
		});
		for (Node assign : instAssignments)
		{
			String instanceVarName = ASTUtils.getName(assign);
			CommonCompletionProposal proposal = createProposal(instanceVarName,
					RubyEditorPlugin.getImage(INSTANCE_VAR_IMAGE));
			proposals.add(proposal);
		}
		return proposals;
	}

	private Collection<? extends ICompletionProposal> suggestMethodsForEnclosingType()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Node enclosing = fContext.getEnclosingTypeNode();
		String enclosingTypeName = fContext.getEnclosingType();
		List<Node> methodDefNodes = new ScopedNodeLocator().find(enclosing, new INodeAcceptor()
		{
			public boolean accepts(Node node)
			{
				return node instanceof MethodDefNode;
			}
		});
		for (Node methodDefNode : methodDefNodes)
		{
			// TODO Determine visibility of the method!
			String methodName = ASTUtils.getName(methodDefNode);
			if (!methodName.startsWith(fContext.getPartialPrefix()))
			{
				continue;
			}
			CommonCompletionProposal proposal = createProposal(methodName,
					RubyEditorPlugin.getImage(PUBLIC_METHOD_IMAGE), enclosingTypeName);
			proposals.add(proposal);
		}
		if (enclosing instanceof ClassNode)
		{
			ClassNode classNode = (ClassNode) enclosing;
			Set<String> allTypes = new HashSet<String>();
			Node superNode = classNode.getSuperNode();
			// Need to also include suggestions against index normally!
			allTypes.add(enclosingTypeName);
			String superTypeName = IRubyIndexConstants.OBJECT;
			if (superNode != null)
			{
				superTypeName = ASTUtils.getFullyQualifiedName(superNode);
			}
			allTypes.add(superTypeName);
			// Need to suggest methods up the hierarchy too...
			PerformanceStats stats = null;
			if (PerformanceStats.isEnabled(CALC_SUPER_TYPE_EVENT))
			{
				stats = PerformanceStats.getStats(CALC_SUPER_TYPE_EVENT, this);
				stats.startRun(superTypeName);
			}
			allTypes.addAll(calculateSuperTypes(superTypeName));
			if (stats != null)
			{
				stats.endRun();
				stats = null;
			}
			if (PerformanceStats.isEnabled(METHOD_PROPOSALS_FOR_TYPE_EVENT))
			{
				stats = PerformanceStats.getStats(METHOD_PROPOSALS_FOR_TYPE_EVENT, this);
				stats.startRun(allTypes.toString());
			}
			proposals.addAll(suggestMethodsForType(allTypes, true, true));
			if (stats != null)
			{
				stats.endRun();
				stats = null;
			}
		}
		else
		{
			// Module/Toplevel...
			Set<String> allTypes = new HashSet<String>();
			// Need to also include suggestions against index normally!
			allTypes.add(enclosingTypeName);
			proposals.addAll(suggestMethodsForType(allTypes, true, true));
		}
		return proposals;
	}

	/**
	 * Bulk query the index for all methods starting with prefix on the set of fully qualified type names passed in.
	 * This generates a complex regexp pattern to use.
	 * 
	 * @param typeNames
	 * @param includeInstance
	 * @param includeProtected
	 * @return
	 */
	@SuppressWarnings("nls")
	private Collection<? extends ICompletionProposal> suggestMethodsForType(Set<String> typeNames,
			boolean includeInstance, boolean includeProtected)
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Set<String> possibles = new HashSet<String>();
		for (String typeName : typeNames)
		{
			String simpleName = typeName;
			String namespace = StringUtil.EMPTY;
			int lastDelim = typeName.lastIndexOf(NAMESPACE_DELIMITER);
			if (lastDelim != -1)
			{
				namespace = typeName.substring(0, lastDelim);
				simpleName = typeName.substring(lastDelim + 2);
			}
			possibles.add(simpleName + IRubyIndexConstants.SEPARATOR + namespace);
		}
		StringBuilder keyBuilder = new StringBuilder();
		// method prefix typed...
		keyBuilder.append("^");
		keyBuilder.append(fContext.getPartialPrefix());
		keyBuilder.append("[^/]*?");
		keyBuilder.append(IRubyIndexConstants.SEPARATOR);
		// All the possible types
		keyBuilder.append("(");
		for (String possible : possibles)
		{
			keyBuilder.append(possible);
			keyBuilder.append("|");
		}
		keyBuilder.deleteCharAt(keyBuilder.length() - 1);
		keyBuilder.append(")");
		keyBuilder.append(IRubyIndexConstants.SEPARATOR);
		// visibility
		keyBuilder.append("(P");
		if (includeProtected)
		{
			keyBuilder.append("|R");
		}
		keyBuilder.append(")");
		keyBuilder.append(IRubyIndexConstants.SEPARATOR);
		// singleton/instance
		keyBuilder.append("(S");
		if (includeInstance)
		{
			keyBuilder.append("|I");
		}
		keyBuilder.append(")");
		keyBuilder.append(IRubyIndexConstants.SEPARATOR);
		// Followed by whatever number of args
		keyBuilder.append("[^/]*$");
		final String key = keyBuilder.toString();
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.METHOD_DECL }, key,
					SearchPattern.REGEX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				String methodName = getMethodNameFromMethodDefKey(result.getWord());
				String typeNameInKey = getTypeNameFromMethodDefKey(result.getWord());
				Visibility vis = getVisibilityFromMethodDefKey(result.getWord());
				Image image;
				switch (vis)
				{
					case PROTECTED:
						image = RubyEditorPlugin.getImage(PROTECTED_METHOD_IMAGE);
						break;

					default:
						image = RubyEditorPlugin.getImage(PUBLIC_METHOD_IMAGE);
						break;
				}
				proposals.add(createProposal(methodName, image, typeNameInKey));
			}
		}
		return proposals;
	}

	private Visibility getVisibilityFromMethodDefKey(String word)
	{
		String[] parts = word.split("" + IRubyIndexConstants.SEPARATOR); //$NON-NLS-1$
		String namespace = parts[3];
		if (namespace != null && namespace.length() > 0)
		{
			char c = namespace.charAt(0);
			switch (c)
			{
				case 'P':
					return Visibility.PUBLIC;
				case 'R':
					return Visibility.PROTECTED;
				case 'V':
					return Visibility.PRIVATE;
			}
		}
		return Visibility.PUBLIC;
	}

	private String getTypeNameFromMethodDefKey(String word)
	{
		String[] parts = word.split("" + IRubyIndexConstants.SEPARATOR); //$NON-NLS-1$
		String simpleName = parts[1];
		String namespace = parts[2];
		if (namespace != null && namespace.length() > 0)
		{
			return namespace + IRubyIndexConstants.NAMESPACE_DELIMETER + simpleName;
		}
		return simpleName;
	}

	private String getMethodNameFromMethodDefKey(String word)
	{
		return word.substring(0, word.indexOf(IRubyIndexConstants.SEPARATOR));
	}

	private void trace(String format)
	{
		// System.out.println(format);
	}

	private Collection<? extends ICompletionProposal> suggestClassVariables()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Node enclosing = fContext.getEnclosingTypeNode();
		List<Node> assignments = new ScopedNodeLocator().find(enclosing, new INodeAcceptor()
		{

			public boolean accepts(Node node)
			{
				return node instanceof ClassVarAsgnNode || node instanceof ClassVarDeclNode;
			}
		});
		for (Node assign : assignments)
		{
			String varName = ASTUtils.getName(assign);
			CommonCompletionProposal proposal = createProposal(varName, RubyEditorPlugin.getImage(CLASS_VAR_IMAGE));
			proposals.add(proposal);
		}
		return proposals;
	}

	private List<ICompletionProposal> suggestConstants()
	{
		Index index = getIndex();
		List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.CONSTANT_DECL },
				fContext.getPartialPrefix(), SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
		if (results == null)
		{
			return Collections.emptyList();
		}
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (QueryResult result : results)
		{
			CommonCompletionProposal proposal = createProposal(result.getWord(),
					RubyEditorPlugin.getImage(CONSTANT_IMAGE));
			proposals.add(proposal);
		}
		return proposals;
	}

	private List<ICompletionProposal> suggestGlobals()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Set<String> globalNames = new TreeSet<String>();
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.GLOBAL_DECL },
					fContext.getPartialPrefix(), SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				globalNames.add(result.getWord());
			}
		}
		for (String globalName : globalNames)
		{
			CommonCompletionProposal proposal = createProposal(globalName, RubyEditorPlugin.getImage(GLOBAL_IMAGE));
			proposals.add(proposal);
		}
		return proposals;
	}

	private Collection<? extends ICompletionProposal> suggestTypeNames()
	{
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		Set<String> typeKeys = new TreeSet<String>();
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.TYPE_DECL },
					fContext.getPartialPrefix(), SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				typeKeys.add(result.getWord());
			}
		}
		for (String typeKey : typeKeys)
		{
			String typeName = getTypeName(typeKey);
			Image image = isClass(typeKey) ? RubyEditorPlugin.getImage(CLASS_IMAGE) : RubyEditorPlugin
					.getImage(MODULE_IMAGE);
			CommonCompletionProposal proposal = createProposal(typeName, image);
			proposals.add(proposal);
		}
		return proposals;
	}

	private boolean isClass(String typeKey)
	{
		return typeKey.charAt(typeKey.length() - 1) == IRubyIndexConstants.CLASS_SUFFIX;
	}

	/**
	 * Grab the simple type name out of the type declaration index key.
	 * 
	 * @param typeKey
	 * @return
	 */
	private String getTypeName(String typeKey)
	{
		return new String(typeKey.substring(0, typeKey.indexOf(IRubyIndexConstants.SEPARATOR)));
	}

	/**
	 * Grab the namespace out of the type declaration index key.
	 * 
	 * @param typeKey
	 * @return
	 */
	private String getNamespace(String typeKey)
	{
		int firstSep = typeKey.indexOf(IRubyIndexConstants.SEPARATOR);
		return new String(typeKey.substring(firstSep + 1, typeKey.indexOf(IRubyIndexConstants.SEPARATOR, firstSep + 1)));
	}

	protected CommonCompletionProposal createProposal(String name, Image image)
	{
		return createProposal(name, image, null);
	}

	protected CommonCompletionProposal createProposal(String name, Image image, String location)
	{
		CommonCompletionProposal proposal = new CommonCompletionProposal(name, fContext.getReplaceStart(), fContext
				.getPartialPrefix().length(), name.length(), image, name, null, null);
		if (location != null)
		{
			proposal.setFileLocation(location);
		}
		proposal.setTriggerCharacters(getProposalTriggerCharacters());
		return proposal;
	}

	// Actually generate the list of super types and return them all, so we can query up the hierarchy for methods!
	@SuppressWarnings("nls")
	private Set<String> calculateSuperTypes(String typeName)
	{
		Set<String> typeNames = new HashSet<String>();
		if (typeName == null)
		{
			return typeNames;
		}
		if (IRubyIndexConstants.OBJECT.equals(typeName))
		{
			typeNames.add("Kernel");
			return typeNames;
		}
		// Break type_name up into type name and namespace...
		String simpleName = typeName;
		String namespace = StringUtil.EMPTY;
		int lastDelim = typeName.lastIndexOf(NAMESPACE_DELIMITER);
		if (lastDelim != -1)
		{
			namespace = typeName.substring(0, lastDelim);
			simpleName = typeName.substring(lastDelim + 2);
		}
		// For performance reasons we don't recurse on modules
		Set<String> moduleNames = new HashSet<String>();

		final String key = "^[^/]*" + IRubyIndexConstants.SEPARATOR + "[^/]*" + IRubyIndexConstants.SEPARATOR
				+ simpleName + IRubyIndexConstants.SEPARATOR + namespace + IRubyIndexConstants.SEPARATOR + ".*$";
		// Take the type name and find all the super types and included modules
		for (Index index : RubyIndexUtil.allIndices(getProject()))
		{
			if (index == null)
			{
				continue;
			}
			List<QueryResult> results = index.query(new String[] { IRubyIndexConstants.SUPER_REF }, key,
					SearchPattern.REGEX_MATCH | SearchPattern.CASE_SENSITIVE);
			if (results == null)
			{
				continue;
			}
			for (QueryResult result : results)
			{
				char classOrModule = result.getWord().charAt(result.getWord().length() - 2);
				if (IRubyIndexConstants.MODULE_SUFFIX == classOrModule)
				{
					moduleNames.add(getTypeNameFromSuperRefKey(result.getWord()));
				}
				else
				{
					typeNames.add(getTypeNameFromSuperRefKey(result.getWord()));
				}
			}
		}
		trace(MessageFormat.format("Supertypes of {0}: {1}", typeName, typeNames));
		trace(MessageFormat.format("Included modules: {0}", moduleNames));
		// Now grab all the supertypes of these super types! RECURSION!!!!1!1!
		Set<String> typeNamesCopy = new HashSet<String>(typeNames);
		for (String superType : typeNamesCopy)
		{
			typeNames.addAll(calculateSuperTypes(superType));
		}
		typeNames.addAll(moduleNames);
		return typeNames;
	}

	/**
	 * Returns the fully qualified type name stored in the Super Reference index key.
	 * 
	 * @param superRefKey
	 * @return
	 */
	private String getTypeNameFromSuperRefKey(String superRefKey)
	{
		int firstSep = superRefKey.indexOf(IRubyIndexConstants.SEPARATOR);
		String simpleName = superRefKey.substring(0, firstSep);
		String namespace = superRefKey.substring(firstSep + 1,
				superRefKey.indexOf(IRubyIndexConstants.SEPARATOR, firstSep + 1));
		if (namespace.length() == 0)
		{
			return simpleName;
		}
		return namespace + NAMESPACE_DELIMITER + simpleName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.CommonContentAssistProcessor#getPreferenceNodeQualifier()
	 */
	protected String getPreferenceNodeQualifier()
	{
		return RubyEditorPlugin.PLUGIN_ID;
	}
}
