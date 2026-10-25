package com.levelinghelper.guide;

import java.awt.Polygon;
import java.awt.Rectangle;

public final class DirectionArrow
{
	private DirectionArrow()
	{
	}

	/** Clamp a minimap direction marker inside the circular map. Yaw uses 14-bit Jagex units. */
	public static Polygon polygon(double dx, double dy, int yaw, Rectangle minimap)
	{
		if (dx == 0 && dy == 0 || minimap.width < 32 || minimap.height < 32)
		{
			return null;
		}
		double angle = (yaw & 0x3fff) * Math.PI * 2 / 16384.0;
		double x = dx * Math.cos(angle) + dy * Math.sin(angle);
		double y = dx * Math.sin(angle) - dy * Math.cos(angle);
		double length = Math.hypot(x, y);
		double ux = x / length;
		double uy = y / length;
		double radius = Math.min(minimap.width, minimap.height) / 2.0 - 12;
		double reach = Math.min(length, radius);
		double cx = minimap.getCenterX() + ux * reach;
		double cy = minimap.getCenterY() + uy * reach;
		// Shaft and arrowhead, facing the destination. The black outline is added by the overlay.
		int[] along = {-7, 0, 0, 8, 0, 0, -7};
		int[] across = {-3, -3, -7, 0, 7, 3, 3};
		Polygon arrow = new Polygon();
		for (int i = 0; i < along.length; i++)
		{
			arrow.addPoint((int) Math.round(cx + ux * along[i] - uy * across[i]),
				(int) Math.round(cy + uy * along[i] + ux * across[i]));
		}
		return arrow;
	}
}
