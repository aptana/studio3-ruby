package com.aptana.ruby.internal.debug.ui.display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.ruby.debug.ui.RubyDebugUIPlugin;

/**
 * An implementation of a data display for a text viewer
 */
class DataDisplay implements IDataDisplay
{

	/**
	 * The text viewer this data display works on
	 */
	private ITextViewer fTextViewer;

	/**
	 * Constructs a data display for the given text viewer.
	 * 
	 * @param viewer
	 *            text viewer
	 */
	public DataDisplay(ITextViewer viewer)
	{
		fTextViewer = viewer;
	}

	/**
	 * @see IDataDisplay#clear()
	 */
	public void clear()
	{
		IDocument document = getTextViewer().getDocument();
		if (document != null)
		{
			document.set(StringUtil.EMPTY);
		}
	}

	/**
	 * @see IDataDisplay#displayExpression(String)
	 */
	public void displayExpression(String expression)
	{
		IDocument document = fTextViewer.getDocument();
		int offset = document.getLength();
		try
		{
			// add a cariage return if needed.
			if (offset != document.getLineInformationOfOffset(offset).getOffset())
			{
				expression = System.getProperty("line.separator") + expression.trim(); //$NON-NLS-1$
			}
			document.replace(offset, 0, expression);
			fTextViewer.setSelectedRange(offset + expression.length(), 0);
			fTextViewer.revealRange(offset, expression.length());
		}
		catch (BadLocationException ble)
		{
			IdeLog.logError(RubyDebugUIPlugin.getDefault(), ble);
		}
	}

	/**
	 * @see IDataDisplay#displayExpressionValue(String)
	 */
	public void displayExpressionValue(String value)
	{
		// TODO Do I really want to add a newline and tab here? Or maybe just append to end of line?
		value = System.getProperty("line.separator") + '\t' + value; //$NON-NLS-1$
		ITextSelection selection = (ITextSelection) fTextViewer.getSelectionProvider().getSelection();

		int offset = selection.getOffset() + selection.getLength();
		int length = value.length();
		try
		{
			fTextViewer.getDocument().replace(offset, 0, value);
		}
		catch (BadLocationException ble)
		{
			IdeLog.logError(RubyDebugUIPlugin.getDefault(), ble);
		}
		fTextViewer.setSelectedRange(offset + length, 0);
		fTextViewer.revealRange(offset, length);
	}

	/**
	 * Returns the text viewer for this data display
	 * 
	 * @return text viewer
	 */
	private ITextViewer getTextViewer()
	{
		return fTextViewer;
	}
}
