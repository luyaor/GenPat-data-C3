package org.eclipse.swt.examples.paint;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.graphics.*;

/**
 * A drawing tool.
 */
public class EllipseTool extends DragPaintSession implements PaintTool {
	private Color drawFGColor;
	private Color drawBGColor;
	private int   fillType;

	/**
	 * Constructs a EllipseTool.
	 * 
	 * @param toolSettings the new tool settings
	 * @param paintSurface the PaintSurface we will render on.
	 */
	public EllipseTool(ToolSettings toolSettings, PaintSurface paintSurface) {
		super(paintSurface);
		set(toolSettings);
	}
	
	/**
	 * Sets the tool's settings.
	 * 
	 * @param toolSettings the new tool settings
	 */
	public void set(ToolSettings toolSettings) {
		drawFGColor = toolSettings.commonForegroundColor;
		drawBGColor = toolSettings.commonBackgroundColor;
		fillType = toolSettings.commonFillType;
	}
	
	/**
	 * Returns name associated with this tool.
	 * 
	 * @return the localized name of this tool
	 */
	public String getDisplayName() {
		return PaintPlugin.getResourceString("tool.Ellipse.label");
	}

	/*
	 * Template methods for drawing
	 */
	protected Figure createFigure(Point a, Point b) {
		ContainerFigure container = new ContainerFigure();
		if (fillType != ToolSettings.ftNone)
			container.add(new SolidEllipseFigure(drawBGColor, a.x, a.y, b.x, b.y));
		if (fillType != ToolSettings.ftSolid)
			container.add(new EllipseFigure(drawFGColor, a.x, a.y, b.x, b.y));
		return container;
	}
}
