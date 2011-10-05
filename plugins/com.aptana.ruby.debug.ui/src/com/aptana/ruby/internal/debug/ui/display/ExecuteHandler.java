package com.aptana.ruby.internal.debug.ui.display;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aptana.core.logging.IdeLog;
import com.aptana.ruby.debug.core.model.IEvaluationResult;
import com.aptana.ruby.debug.core.model.IRubyStackFrame;
import com.aptana.ruby.debug.core.model.IRubyValue;
import com.aptana.ruby.debug.ui.RubyDebugUIPlugin;
import com.aptana.ui.util.UIUtils;

public class ExecuteHandler extends AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String expressionText = null;
		if (selection instanceof ITextSelection)
		{
			ITextSelection ts = (ITextSelection) selection;
			expressionText = ts.getText();
		}

		IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindow(event);
		IRubyStackFrame stackFrame = getEvaluationContext(win);
		if (stackFrame.isSuspended())
		{
			IEvaluationResult result = stackFrame.evaluate(expressionText);
			displayResult(result, event);
		}
		return null;
	}

	protected void displayResult(final IEvaluationResult result, final ExecutionEvent event)
	{
		final Display display = UIUtils.getDisplay();
		if (result.hasErrors())
		{
			display.asyncExec(new Runnable()
			{
				public void run()
				{
					if (display.isDisposed())
					{
						return;
					}
					reportErrors(result);
				}
			});
		}
		else
		{
			display.asyncExec(new Runnable()
			{
				public void run()
				{
					if (display.isDisposed())
					{
						return;
					}
					IValue value = result.getValue();
					IDataDisplay dataDisplay = getDataDisplay(event);
					if (dataDisplay != null)
					{
						try
						{
							dataDisplay.displayExpressionValue(valueToCode(value));
						}
						catch (DebugException e)
						{
							IdeLog.logError(RubyDebugUIPlugin.getDefault(), e);
						}
					}
				}
			});
		}
	}

	private IDataDisplay getDataDisplay(ExecutionEvent event)
	{
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part != null)
		{
			IDataDisplay display = (IDataDisplay) part.getAdapter(IDataDisplay.class);
			if (display != null)
			{
				IWorkbenchPage page = UIUtils.getActivePage();
				if (page != null)
				{
					IWorkbenchPart activePart = page.getActivePart();
					if (activePart != null)
					{
						if (activePart != part)
						{
							page.activate(part);
						}
					}
				}
				return display;
			}
		}
		IWorkbenchPage page = UIUtils.getActivePage();
		if (page != null)
		{
			IWorkbenchPart activePart = page.getActivePart();
			if (activePart != null)
			{
				IDataDisplay display = (IDataDisplay) activePart.getAdapter(IDataDisplay.class);
				if (display != null)
				{
					return display;
				}
			}
		}
		return null;
	}

	private void reportErrors(IEvaluationResult result)
	{
		// TODO Print out "error" in data display view!

	}

	static String valueToCode(IValue value) throws DebugException
	{
		StringBuilder buffer = new StringBuilder("# => "); //$NON-NLS-1$
		String string = value.getValueString();
		if (value instanceof IRubyValue)
		{
			IRubyValue rubyValue = (IRubyValue) value;
			if ("Array".equals(value.getReferenceTypeName())) //$NON-NLS-1$
			{
				buffer.append("["); //$NON-NLS-1$
				IVariable[] vars = rubyValue.getVariables();
				for (int i = 0; i < vars.length; i++)
				{
					buffer.append(vars[i].getValue().getValueString());
					if (i < vars.length - 1)
					{
						buffer.append(", "); //$NON-NLS-1$
					}
				}
				buffer.append("]"); //$NON-NLS-1$				
			}
			else if ("Hash".equals(value.getReferenceTypeName())) //$NON-NLS-1$
			{
				buffer.append("{"); //$NON-NLS-1$
				IVariable[] vars = rubyValue.getVariables();
				for (int i = 0; i < vars.length; i++)
				{
					buffer.append(vars[i]);
					if (i < vars.length - 1)
					{
						buffer.append(", "); //$NON-NLS-1$
					}
				}
				buffer.append("}"); //$NON-NLS-1$
			}
		}
		buffer.append(string);
		return buffer.toString();
	}

	static IRubyStackFrame getEvaluationContext(IWorkbenchWindow workbenchWindow)
	{
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(workbenchWindow);
		ISelection sel = service.getActiveContext();
		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection structured = (IStructuredSelection) sel;
			Object first = structured.getFirstElement();
			if (first instanceof IRubyStackFrame)
			{
				return (IRubyStackFrame) first;
			}
		}
		return null;
	}

}
