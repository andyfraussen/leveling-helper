package com.levelinghelper;

import com.levelinghelper.guide.GuideController;
import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.TrainingMethod;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class LevelingHelperOverlay extends Overlay
{
	private final Client client;
	private final LevelingHelperPlugin plugin;
	private final LevelingHelperConfig config;

	@Inject
	LevelingHelperOverlay(Client client, LevelingHelperPlugin plugin, LevelingHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		GuideController controller = plugin.controller();
		if (!plugin.active() || !controller.running() || client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}
		GuideStep step = plugin.currentStep();
		TrainingMethod method = controller.method();
		if (step == null || method == null)
		{
			return null;
		}
		Color color = config.highlightColor();
		if (config.showTiles())
		{
			renderTile(graphics, plugin.guidanceTarget(), color);
		}
		if (config.showObjects())
		{
			renderTargets(graphics, color);
		}
		if (config.showArrow())
		{
			renderArrow(graphics, plugin.guidanceTarget(), color);
		}
		return null;
	}

	private void renderTile(Graphics2D graphics, WorldPoint target, Color color)
	{
		if (target == null || target.getPlane() != client.getPlane())
		{
			return;
		}
		LocalPoint local = LocalPoint.fromWorld(client, target);
		if (local == null)
		{
			return;
		}
		Polygon polygon = Perspective.getCanvasTilePoly(client, local);
		if (polygon != null)
		{
			OverlayUtil.renderPolygon(graphics, polygon, color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 40), new BasicStroke(2));
		}
	}

	private void renderTargets(Graphics2D graphics, Color color)
	{
		for (NPC npc : plugin.highlightedNpcs())
		{
			Shape hull = npc.getConvexHull();
			if (hull != null)
			{
				OverlayUtil.renderPolygon(graphics, hull, color);
			}
		}
		for (TileObject object : plugin.highlightedObjects())
		{
			Shape hull = hull(object);
			if (hull != null)
			{
				OverlayUtil.renderPolygon(graphics, hull, color);
			}
		}
	}

	private void renderArrow(Graphics2D graphics, WorldPoint target, Color color)
	{
		if (target == null || target.getPlane() != client.getPlane())
		{
			return;
		}
		Point anchor = targetAnchor(target);
		if (anchor == null)
		{
			return;
		}
		// A small downward arrow above the target, rather than a screen-edge triangle.
		int x = anchor.getX();
		int y = anchor.getY();
		Polygon arrow = new Polygon(new int[]{x - 4, x + 4, x + 4, x + 10, x, x - 10, x - 4},
			new int[]{y - 27, y - 27, y - 14, y - 14, y - 4, y - 14, y - 14}, 7);
		Rectangle viewport = new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
			client.getViewportWidth(), client.getViewportHeight());
		if (!viewport.contains(arrow.getBounds()))
		{
			return;
		}
		Graphics2D marker = (Graphics2D) graphics.create();
		marker.setColor(color);
		marker.fill(arrow);
		marker.setColor(Color.BLACK);
		marker.setStroke(new BasicStroke(2));
		marker.draw(arrow);
		marker.dispose();
	}

	private Point targetAnchor(WorldPoint target)
	{
		for (NPC npc : plugin.highlightedNpcs())
		{
			if (target.equals(npc.getWorldLocation()))
			{
				return Perspective.localToCanvas(client, npc.getLocalLocation(), target.getPlane(), npc.getLogicalHeight() + 40);
			}
		}
		for (TileObject object : plugin.highlightedObjects())
		{
			if (target.equals(object.getWorldLocation()))
			{
				Shape shape = hull(object);
				if (shape != null)
				{
					Rectangle bounds = shape.getBounds();
					return new Point((int) bounds.getCenterX(), bounds.y - 4);
				}
			}
		}
		LocalPoint local = LocalPoint.fromWorld(client, target);
		return local == null ? null : Perspective.localToCanvas(client, local, target.getPlane(), 100);
	}

	private static Shape hull(TileObject object)
	{
		if (object instanceof GameObject)
		{
			return ((GameObject) object).getConvexHull();
		}
		if (object instanceof WallObject)
		{
			return ((WallObject) object).getConvexHull();
		}
		if (object instanceof GroundObject)
		{
			return ((GroundObject) object).getConvexHull();
		}
		if (object instanceof DecorativeObject)
		{
			return ((DecorativeObject) object).getConvexHull();
		}
		return null;
	}
}
