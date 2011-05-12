package com.aptana.ruby.internal.debug.ui.display;

public interface IDataDisplay
{

	/**
	 * Clears the content of this data display.
	 */
	public void clear();

	/**
	 * Displays the expression in the content of this data display.
	 */
	public void displayExpression(String expression);

	/**
	 * Displays the expression value in the content of this data display.
	 */
	public void displayExpressionValue(String value);
}
