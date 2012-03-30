/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.outline.CommonOutlineItem;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.ruby.internal.text.RubyFoldingComputer;
import com.aptana.editor.ruby.outline.RubyOutlineContentProvider;
import com.aptana.editor.ruby.outline.RubyOutlineLabelProvider;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;
import com.aptana.ruby.core.IImportContainer;
import com.aptana.ruby.core.IRubyConstants;
import com.aptana.ruby.core.IRubyElement;
import com.aptana.ruby.core.IRubyMethod;
import com.aptana.ruby.core.IRubyType;

@SuppressWarnings("restriction")
public class RubySourceEditor extends AbstractThemeableEditor
{
	private static final char[] PAIR_MATCHING_CHARS = new char[] { '(', ')', '{', '}', '[', ']', '`', '`', '\'', '\'',
			'"', '"', '|', '|', '\u201C', '\u201D', '\u2018', '\u2019' }; // curly double quotes, curly single quotes

	private Map<Annotation, Position> fTagPairOccurrences;
	private boolean fIncludeBlocks;

	@Override
	protected void initializeEditor()
	{
		super.initializeEditor();

		setPreferenceStore(getChainedPreferenceStore());

		setSourceViewerConfiguration(new RubySourceViewerConfiguration(getPreferenceStore(), this));
		setDocumentProvider(RubyEditorPlugin.getDefault().getRubyDocumentProvider());
	}

	public static IPreferenceStore getChainedPreferenceStore()
	{
		return new ChainedPreferenceStore(new IPreferenceStore[] { RubyEditorPlugin.getDefault().getPreferenceStore(),
				CommonEditorPlugin.getDefault().getPreferenceStore(), EditorsPlugin.getDefault().getPreferenceStore() });
	}

	public char[] getPairMatchingCharacters()
	{
		return PAIR_MATCHING_CHARS;
	}

	@Override
	public ITreeContentProvider getOutlineContentProvider()
	{
		return new RubyOutlineContentProvider();
	}

	@Override
	public ILabelProvider getOutlineLabelProvider()
	{
		return new RubyOutlineLabelProvider();
	}

	@Override
	protected void setSelectedElement(IRange element)
	{
		if (element instanceof CommonOutlineItem)
		{
			IParseNode node = ((CommonOutlineItem) element).getReferenceNode();
			if (node instanceof IImportContainer)
			{
				// just sets the highlight range and moves the cursor
				setHighlightRange(element.getStartingOffset(), element.getLength(), true);
				return;
			}
		}
		super.setSelectedElement(element);
	}

	@Override
	protected void selectionChanged()
	{
		super.selectionChanged();

		ISelection selection = getSelectionProvider().getSelection();
		if (selection.isEmpty())
		{
			return;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		updateOccurrences(textSelection);
	}

	@Override
	protected Object getOutlineElementAt(int caret)
	{
		fIncludeBlocks = false;
		Object obj = super.getOutlineElementAt(caret);
		fIncludeBlocks = true;
		return obj;
	}

	protected IParseNode getASTNodeAt(int offset)
	{
		IParseNode root = getAST();
		if (root == null)
		{
			return null;
		}
		IParseNode node = root.getNodeAtOffset(offset);
		if (!fIncludeBlocks && node != null && node.getNodeType() == IRubyElement.BLOCK)
		{
			node = node.getParent();
		}
		return node;
	}

	private void updateOccurrences(ITextSelection textSelection)
	{
		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider == null)
		{
			return;
		}
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null)
		{
			return;
		}

		int offset = textSelection.getOffset();
		IParseNode currentNode = getASTNodeAt(offset);
		if (fTagPairOccurrences != null)
		{
			// if the offset is included by one of these two positions, we don't need to wipe and re-calculate!
			for (Position pos : fTagPairOccurrences.values())
			{
				if (pos.includes(offset))
				{
					return;
				}
			}
			// New position, wipe the existing annotations in preparation for re-calculating...
			for (Annotation a : fTagPairOccurrences.keySet())
			{
				annotationModel.removeAnnotation(a);
			}
			fTagPairOccurrences = null;
		}

		// Calculate current pair
		Map<Annotation, Position> occurrences = new HashMap<Annotation, Position>();
		List<Position> positions = new ArrayList<Position>();
		if (currentNode != null)
		{
			if (currentNode instanceof IRubyType)
			{
				// Match "end" to "class/module ..."
				int endOffset = currentNode.getEndingOffset();
				int startOffset = currentNode.getStartingOffset();

				int length = 5;
				IRubyType type = (IRubyType) currentNode;
				if (type.isModule())
				{
					length = 6;
				}
				if ((offset <= endOffset && offset >= endOffset - 2)
						|| (offset >= startOffset && offset <= startOffset + length))
				{
					positions.add(new Position(startOffset, length));
					positions.add(new Position(endOffset - 2, 3));
				}
			}
			else if (currentNode instanceof IRubyMethod)
			{
				// Match "end" to "def ..."
				int endOffset = currentNode.getEndingOffset();
				int startOffset = currentNode.getStartingOffset();
				if ((offset <= endOffset && offset >= endOffset - 2)
						|| (offset >= startOffset && offset <= startOffset + 3))
				{
					positions.add(new Position(startOffset, 3));
					positions.add(new Position(endOffset - 2, 3));
				}
			}
			else if (currentNode.getNodeType() == IRubyElement.BLOCK)
			{
				// Match "end" to "do ..." only if it's a do/end block
				int endOffset = currentNode.getEndingOffset();
				IDocument document = getSourceViewer().getDocument();
				if (endOffset >= document.getLength())
				{
					endOffset--;
				}
				char endText = 'a';
				try
				{
					endText = document.getChar(endOffset);
				}
				catch (BadLocationException e)
				{
					IdeLog.logError(RubyEditorPlugin.getDefault(), "Unable to get text at end of block, end offset: " //$NON-NLS-1$
							+ endOffset, e);
				}
				if (endText == 'd')
				{
					int startOffset = currentNode.getStartingOffset();
					if ((offset <= endOffset && offset >= endOffset - 2)
							|| (offset >= startOffset && offset <= startOffset + 3))
					{
						positions.add(new Position(startOffset, 2));
						positions.add(new Position(endOffset - 2, 3));
					}
				}
			}
			// else if (currentNode instanceof IRubyField)
			// {
			// TODO Find occurrences of variables!
			// }
			// TODO Also match if/else/unless/begin/rescue/end blocks!
		}

		if (!positions.isEmpty())
		{
			for (Position pos : positions)
			{
				occurrences.put(new Annotation(IRubyConstants.BLOCK_PAIR_OCCURRENCES_ID, false, null), pos);
			}

			for (Map.Entry<Annotation, Position> entry : occurrences.entrySet())
			{
				annotationModel.addAnnotation(entry.getKey(), entry.getValue());
			}
			fTagPairOccurrences = occurrences;
		}
		else
		{
			// no new pair, so don't highlight anything
			fTagPairOccurrences = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.AbstractThemeableEditor#getPluginPreferenceStore()
	 */
	@Override
	protected IPreferenceStore getPluginPreferenceStore()
	{
		return RubyEditorPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public IFoldingComputer createFoldingComputer(IDocument document)
	{
		return new RubyFoldingComputer(this, document);
	}

	@Override
	public String getContentType()
	{
		return IRubyConstants.CONTENT_TYPE_RUBY;
	}
}
