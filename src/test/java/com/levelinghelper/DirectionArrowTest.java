package com.levelinghelper;

import com.levelinghelper.guide.DirectionArrow;
import java.awt.Polygon;
import java.awt.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionArrowTest
{
	private final Rectangle viewport = new Rectangle(20, 40, 800, 500);

	@Test
	public void arrowRotatesWithTheCameraInAllFourCardinalDirections()
	{
		Polygon north = DirectionArrow.polygon(0, 100, 0, viewport);
		Polygon east = DirectionArrow.polygon(0, 100, 4096, viewport);
		Polygon south = DirectionArrow.polygon(0, 100, 8192, viewport);
		Polygon west = DirectionArrow.polygon(0, 100, 12288, viewport);
		assertTrue(north.ypoints[0] < viewport.getCenterY());
		assertTrue(east.xpoints[0] > viewport.getCenterX());
		assertTrue(south.ypoints[0] > viewport.getCenterY());
		assertTrue(west.xpoints[0] < viewport.getCenterX());
		for (Polygon arrow : new Polygon[]{north, east, south, west})
		{
			for (int i = 0; i < arrow.npoints; i++) assertTrue(viewport.contains(arrow.xpoints[i], arrow.ypoints[i]));
		}
	}

	@Test
	public void degenerateAndSmallViewportsDoNotProduceOffscreenArrows()
	{
		assertNull(DirectionArrow.polygon(0, 0, 0, viewport));
		assertNull(DirectionArrow.polygon(1, 0, 0, new Rectangle(10, 10)));
		Rectangle small = new Rectangle(20, 30, 40, 40);
		Polygon arrow = DirectionArrow.polygon(-100, 100, 0, small);
		for (int i = 0; i < arrow.npoints; i++) assertTrue(small.contains(arrow.xpoints[i], arrow.ypoints[i]));
	}
}
