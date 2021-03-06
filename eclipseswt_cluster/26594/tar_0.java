package org.eclipse.swt.examples.paint;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.graphics.*;

/**
 * A drawing tool.
 */
public class RoundedRectangleTool extends DragPaintSession implements PaintTool {
	private ToolSettings settings;

	/**
	 * Constructs a RoundedRectangleTool.
	 * 
	 * @param toolSettings the new tool settings
	 * @param paintSurface the PaintSurface we will render on.
	 */
	public RoundedRectangleTool(ToolSettings toolSettings, PaintSurface paintSurface) {
		super(paintSurface);
		set(toolSettings);
	}
	
	/**
	 * Sets the tool's settings.
	 * 
	 * @param toolSettings the new tool settings
	 */
	public void set(ToolSettings toolSettings) {
		settings = toolSettings;
	}
	
	/**
	 * Returns name associated with this tool.
	 * 
	 * @return the localized name of this tool
	 */
	public String getDisplayName() {
		return PaintPlugin.getResourceString("tool.RoundedRectangle.label");
	}

	/*
	 * Template methods for drawing
	 */
	protected Figure createFigure(Point a, Point b) {
		ContainerFigure container = new ContainerFigure();
		if (settings.commonFillType != ToolSettings.ftNone)
			container.add(new SolidRoundedRectangleFigure(settings.commonBackgroundColor,
				a.x, a.y, b.x, b.y, settings.roundedRectangleCornerDiameter));
		if (settings.commonFillType != ToolSettings.ftSolid)
			container.add(new RoundedRectangleFigure(settings.commonForegroundColor, settings.commonBackgroundColor,
				settings.commonLineStyle, a.x, a.y, b.x, b.y, settings.roundedRectangleCornerDiameter));
		return container;
	}
}
