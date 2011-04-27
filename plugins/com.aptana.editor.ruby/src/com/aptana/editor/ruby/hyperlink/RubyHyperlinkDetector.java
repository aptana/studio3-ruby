package com.aptana.editor.ruby.hyperlink;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jrubyparser.ast.Node;

import com.aptana.editor.common.text.hyperlink.IndexQueryingHyperlinkDetector;
import com.aptana.editor.ruby.RubyEditorPlugin;
import com.aptana.ruby.core.RubyCorePlugin;
import com.aptana.ruby.core.codeassist.CodeResolver;
import com.aptana.ruby.core.codeassist.ResolutionTarget;
import com.aptana.ruby.core.codeassist.ResolveContext;

public class RubyHyperlinkDetector extends IndexQueryingHyperlinkDetector
{

	private IRegion srcRegion;

	public RubyHyperlinkDetector()
	{
		super();
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks)
	{
		List<IHyperlink> hyperlinks = new ArrayList<IHyperlink>();
		try
		{
			IDocument doc = textViewer.getDocument();
			ResolveContext context = new ResolveContext(getURI(), doc.get(), region.getOffset());

			Node atOffset = context.getSelectedNode();
			if (atOffset == null)
			{
				return null;
			}
			// Expand hyperlink region to the node bounds
			srcRegion = new Region(atOffset.getPosition().getStartOffset(), atOffset.getPosition().getEndOffset()
					- atOffset.getPosition().getStartOffset());
			CodeResolver resolver = RubyCorePlugin.getDefault().getCodeResolver();
			resolver.resolve(context);

			List<ResolutionTarget> resolved = context.getResolved();
			if (resolved != null)
			{
				for (ResolutionTarget target : resolved)
				{
					hyperlinks.add(new ResolutionTargetHyperlink(srcRegion, target));
				}
			}
		}
		catch (Exception e)
		{
			RubyEditorPlugin.log(e);
		}
		try
		{
			if (hyperlinks.isEmpty())
			{
				return null;
			}
			// Remove duplicates!
			Set<IHyperlink> uniques = new LinkedHashSet<IHyperlink>(hyperlinks);
			return uniques.toArray(new IHyperlink[uniques.size()]);
		}
		finally
		{
			srcRegion = null;
		}
	}
}
