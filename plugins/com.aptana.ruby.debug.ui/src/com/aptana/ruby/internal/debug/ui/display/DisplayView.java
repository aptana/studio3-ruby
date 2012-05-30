package com.aptana.ruby.internal.debug.ui.display;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.ruby.RubySourceEditor;
import com.aptana.editor.ruby.RubySourceViewerConfiguration;
import com.aptana.ruby.debug.core.model.IEvaluationResult;
import com.aptana.ruby.debug.core.model.IRubyStackFrame;
import com.aptana.ruby.debug.ui.RubyDebugUIPlugin;
import com.aptana.theme.ThemePlugin;

public class DisplayView extends ViewPart implements ITextInputListener, IPerspectiveListener2
{

	private IDataDisplay fDataDisplay;
	private IDocumentListener fDocumentListener;
	private SourceViewer fSourceViewer;
	private String fRestoredContents;

	/**
	 * This memento allows the Display view to save and restore state when it is closed and opened within a session. A
	 * different memento is supplied by the platform for persistance at workbench shutdown.
	 */
	private static IMemento fgMemento;

	/**
	 * @see ViewPart#createChild(IWorkbenchPartContainer)
	 */
	public void createPartControl(Composite parent)
	{
		fSourceViewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		fDataDisplay = new DataDisplay(fSourceViewer);
		fSourceViewer.configure(new RubySourceViewerConfiguration(RubySourceEditor.getChainedPreferenceStore(), null));

		ThemePlugin.getDefault().getControlThemerFactory().apply(fSourceViewer);

		IDocument doc = getRestoredDocument();
		fSourceViewer.setDocument(doc);
		fSourceViewer.addTextInputListener(this);
		doc.addDocumentListener(new IDocumentListener()
		{

			public void documentChanged(DocumentEvent event)
			{
				if (event == null)
					return;
				String text = event.getText();
				if (text == null)
					return;
				String newline = System.getProperty("line.separator"); //$NON-NLS-1$
				if (text.equals(newline))
				{
					// evaluate the expression on this line!
					IDocument doc = event.getDocument();
					try
					{
						String prefix = doc.get(0, event.getOffset());
						int index = prefix.lastIndexOf(newline);
						String line = null;
						if (index == -1)
						{
							line = prefix;
						}
						else
						{
							line = prefix.substring(index);
						}
						IRubyStackFrame frame = ExecuteHandler.getEvaluationContext(getSite().getWorkbenchWindow());
						if (frame == null)
							return; // no context to evaluate within! Maybe we're not running something under the
									// debugger?
						if (frame.isSuspended())
						{
							IEvaluationResult result = frame.evaluate(line);
							if (result == null)
								return;
							IDataDisplay display = (IDataDisplay) getAdapter(IDataDisplay.class);
							if (display == null)
								return;
							String toDisplay = ExecuteHandler.valueToCode(result.getValue());
							display.displayExpressionValue(toDisplay);
						}
					}
					catch (Throwable e)
					{
						IdeLog.logError(RubyDebugUIPlugin.getDefault(), e);
					}
				}
			}

			public void documentAboutToBeChanged(DocumentEvent event)
			{
				// do nothing
			}

		});
		fRestoredContents = null;

		// PlatformUI.getWorkbench().getHelpSystem().setHelp(fSourceViewer.getTextWidget(),
		// IRubyDebugHelpContextIds.DISPLAY_VIEW);
		getSite().getWorkbenchWindow().addPerspectiveListener(this);
	}

	protected IDocument getRestoredDocument()
	{
		IDocument doc = null;
		if (fRestoredContents != null)
		{
			doc = new Document(fRestoredContents);
		}
		else
		{
			doc = new Document();
		}
		// FIXME Set up RubyDocumentProvider/partitioner stuff?
		// RubyTextTools tools = RubyDebugUIPlugin.getDefault().getRubyTextTools();
		// tools.setupRubyDocumentPartitioner(doc, IRubyPartitions.RUBY_PARTITIONING);

		return doc;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		if (fSourceViewer != null)
		{
			fSourceViewer.getControl().setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class required)
	{
		if (ITextOperationTarget.class.equals(required))
		{
			return fSourceViewer.getTextOperationTarget();
		}
		if (IFindReplaceTarget.class.equals(required))
		{
			return fSourceViewer.getFindReplaceTarget();
		}
		if (IDataDisplay.class.equals(required))
		{
			return fDataDisplay;
		}
		if (ITextViewer.class.equals(required))
		{
			return fSourceViewer;
		}
		return super.getAdapter(required);
	}

	/**
	 * Saves the contents of the display view and the formatting.
	 * 
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento)
	{
		if (fSourceViewer != null)
		{
			String contents = getContents();
			if (contents != null)
			{
				memento.putTextData(contents);
			}
		}
		else if (fRestoredContents != null)
		{
			memento.putTextData(fRestoredContents);
		}
	}

	/**
	 * Restores the contents of the display view and the formatting.
	 * 
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		init(site);
		if (fgMemento != null)
		{
			memento = fgMemento;
		}
		if (memento != null)
		{
			fRestoredContents = memento.getTextData();
		}
	}

	/**
	 * Returns the entire trimmed contents of the current document. If the contents are "empty" <code>null</code> is
	 * returned.
	 */
	private String getContents()
	{
		if (fSourceViewer != null)
		{
			IDocument doc = fSourceViewer.getDocument();
			if (doc != null)
			{
				String contents = doc.get().trim();
				if (contents.length() > 0)
				{
					return contents;
				}
			}
		}
		return null;
	}

	/**
	 * @see ITextInputListener#inputDocumentAboutToBeChanged(IDocument, IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput)
	{
	}

	/**
	 * @see ITextInputListener#inputDocumentChanged(IDocument, IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput)
	{
		oldInput.removeDocumentListener(fDocumentListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		getSite().getWorkbenchWindow().removePerspectiveListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
	 * org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId)
	{
		if (partRef instanceof IViewReference && changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE))
		{
			String id = ((IViewReference) partRef).getId();
			if (id.equals(getViewSite().getId()))
			{
				// Display view closed. Persist contents.
				String contents = getContents();
				if (contents != null)
				{
					fgMemento = XMLMemento.createWriteRoot("DisplayViewMemento"); //$NON-NLS-1$
					fgMemento.putTextData(contents);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
	 * org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective)
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
	 * org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId)
	{
	}

}
