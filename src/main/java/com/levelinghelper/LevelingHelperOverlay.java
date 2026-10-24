package com.levelinghelper;

import com.levelinghelper.guide.DirectionArrow;
import com.levelinghelper.guide.GuideController;
import com.levelinghelper.guide.TrainingMath;
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
import net.runelite.api.Player;
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
		GuideStep step = controller.current();
		TrainingMethod method = controller.method();
		if (step == null || method == null)
		{
			return null;
		}
		Color color = config.highlightColor();
		if (config.showTiles())
		{
			renderTile(graphics, step.target(), color);
		}
		if (config.showObjects())
		{
			renderTargets(graphics, color);
		}
		if (config.showArrow())
		{
			renderArrow(graphics, step.target(), color);
		}
		if (config.showOverlay())
		{
			renderBox(graphics, method, step, color);
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
		Polygon polygon = net.runelite.api.Perspective.getCanvasTilePoly(client, local);
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
		Player player = client.getLocalPlayer();
		if (player == null || target == null || player.getWorldLocation().getPlane() != target.getPlane())
		{
			return;
		}
		Rectangle viewport = new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
			client.getViewportWidth(), client.getViewportHeight());
		LocalPoint local = LocalPoint.fromWorld(client, target);
		Polygon tile = local == null ? null : net.runelite.api.Perspective.getCanvasTilePoly(client, local);
		if (tile != null && tile.intersects(viewport))
		{
			return;
		}
		Polygon triangle = DirectionArrow.polygon(target.getX() - player.getWorldLocation().getX(),
			target.getY() - player.getWorldLocation().getY(), client.getCameraYaw(), viewport);
		if (triangle == null)
		{
			return;
		}
		graphics.setColor(color);
		graphics.fill(triangle);
	}

	private void renderBox(Graphics2D graphics, TrainingMethod method, GuideStep step, Color color)
	{
		Player player = client.getLocalPlayer();
		int level = player == null ? 1 : client.getRealSkillLevel(method.skill());
		String distance = step.target() == null || player == null
			? ""
			: TrainingMath.direction(player.getWorldLocation(), step.target());
		String[] lines = new String[]{
			method.skill().getName() + "  " + level + " / " + config.goalLevel(),
			method.name() + "   step " + (plugin.controller().stepIndex() + 1) + "/" + method.steps().size(),
			trim(step.instruction(), 52),
			distance.isEmpty() ? "Follow the step in the side panel" : distance,
			"Typical " + TrainingMath.rate(method.xpPerHour())
		};
		graphics.setFont(graphics.getFont().deriveFont(12f));
		int width = 230;
		for (String line : lines)
		{
			width = Math.max(width, graphics.getFontMetrics().stringWidth(line) + 16);
		}
		int height = 14 + lines.length * 15;
		graphics.setColor(new Color(12, 16, 14, 210));
		graphics.fillRoundRect(8, 8, width, height, 8, 8);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(1.5f));
		graphics.drawRoundRect(8, 8, width, height, 8, 8);
		int y = 24;
		for (int i = 0; i < lines.length; i++)
		{
			graphics.setColor(i == 2 ? Color.WHITE : new Color(210, 225, 215));
			graphics.drawString(lines[i], 16, y);
			y += 15;
		}
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

	private static String trim(String text, int max)
	{
		if (text == null)
		{
			return "";
		}
		String flat = text.replace('\n', ' ');
		if (flat.length() <= max)
		{
			return flat;
		}
		return flat.substring(0, max - 3) + "...";
	}
}
