/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ruby.internal.debug.core.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

import com.aptana.ruby.debug.core.model.IEvaluationResult;
import com.aptana.ruby.debug.core.model.IRubyStackFrame;
import com.aptana.ruby.debug.core.model.IRubyThread;

public class RubyWatchExpressionDelegate implements IWatchExpressionDelegate
{

	private IWatchExpressionListener fListener;
	private String fExpressionText;

	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener)
	{
		fExpressionText = expression;
		fListener = listener;
		// find a stack frame context if possible.
		IStackFrame frame = null;
		if (context instanceof IStackFrame)
		{
			frame = (IStackFrame) context;
		}
		else if (context instanceof IThread)
		{
			try
			{
				frame = ((IThread) context).getTopStackFrame();
			}
			catch (DebugException e)
			{
			}
		}
		if (frame == null)
		{
			fListener.watchEvaluationFinished(null);
		}
		else
		{
			// consult the adapter in case of a wrappered debug model
			final IRubyStackFrame rubyStackFrame = (IRubyStackFrame) ((IAdaptable) frame)
					.getAdapter(IRubyStackFrame.class);
			if (rubyStackFrame != null)
			{
				doEvaluation(rubyStackFrame);
			}
			else
			{
				fListener.watchEvaluationFinished(null);
			}
		}

	}

	private void doEvaluation(IRubyStackFrame rubyStackFrame)
	{
		IRubyThread thread = (IRubyThread) rubyStackFrame.getThread();
		if (preEvaluationCheck(thread))
		{
			thread.queueRunnable(new EvaluationRunnable(rubyStackFrame));
		}
		else
		{
			fListener.watchEvaluationFinished(null);
		}
	}

	private boolean preEvaluationCheck(IRubyThread rubyThread)
	{
		// if (rubyThread == null || (rubyThread.isSuspended() && ((RubyThread) rubyThread).isInvokingMethod()))
		// {
		// return false;
		// }
		// return true;
		return rubyThread != null;
	}

	/**
	 * Runnable used to evaluate the expression.
	 */
	private final class EvaluationRunnable implements Runnable
	{

		private final IRubyStackFrame fStackFrame;

		private EvaluationRunnable(IRubyStackFrame frame)
		{
			fStackFrame = frame;
		}

		public void run()
		{
			IEvaluationResult result = fStackFrame.evaluate(fExpressionText);
			IWatchExpressionResult watchResult = new EvaluationWatchExpressionResult(result);
			fListener.watchEvaluationFinished(watchResult);
		}
	}

	private class EvaluationWatchExpressionResult implements IWatchExpressionResult
	{
		private IEvaluationResult result;

		EvaluationWatchExpressionResult(IEvaluationResult result)
		{
			this.result = result;
		}

		public String[] getErrorMessages()
		{
			return result.getErrorMessages();
		}

		public DebugException getException()
		{
			return result.getException();
		}

		public String getExpressionText()
		{
			return result.getSnippet();
		}

		public IValue getValue()
		{
			return result.getValue();
		}

		public boolean hasErrors()
		{
			return result.hasErrors();
		}
	}

}