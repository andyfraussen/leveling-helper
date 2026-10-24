package com.levelinghelper.guide;

import java.awt.Polygon;
import java.awt.Rectangle;

public final class DirectionArrow
{
	private DirectionArrow()
	{
	}

	/** Camera yaw uses RuneLite's 14-bit Jagex angle units. */
	public static Polygon polygon(int dx, int dy, int yaw, Rectangle viewport)
	{
		if (dx == 0 && dy == 0 || viewport.width < 32 || viewport.height < 32)
		{
			return null;
		}
		double angle = (yaw & 0x3fff) * Math.PI * 2 / 16384.0;
		double x = dx * Math.cos(angle) + dy * Math.sin(angle);
		double y = dx * Math.sin(angle) - dy * Math.cos(angle);
		double length = Math.hypot(x, y);
		double ux = x / length;
		double uy = y / length;
		double margin = Math.min(48, Math.min(viewport.width, viewport.height) / 4.0);
		double reachX = Math.abs(ux) < 1e-9 ? Double.POSITIVE_INFINITY : (viewport.width / 2.0 - margin) / Math.abs(ux);
		double reachY = Math.abs(uy) < 1e-9 ? Double.POSITIVE_INFINITY : (viewport.height / 2.0 - margin) / Math.abs(uy);
		double reach = Math.min(reachX, reachY);
		double cx = viewport.getCenterX() + ux * reach;
		double cy = viewport.getCenterY() + uy * reach;
		int size = (int) Math.min(16, margin / 2);
		Polygon arrow = new Polygon();
		arrow.addPoint((int) (cx + ux * size), (int) (cy + uy * size));
		arrow.addPoint((int) (cx - ux * size - uy * size / 2), (int) (cy - uy * size + ux * size / 2));
		arrow.addPoint((int) (cx - ux * size + uy * size / 2), (int) (cy - uy * size - ux * size / 2));
		return arrow;
	}
}
