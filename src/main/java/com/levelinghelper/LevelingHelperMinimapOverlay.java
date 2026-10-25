package com.levelinghelper;

import com.levelinghelper.guide.DirectionArrow;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/** Independent of the game's single shared hint arrow. This overlay never handles clicks. */
class LevelingHelperMinimapOverlay extends Overlay
{
	private final Client client;
	private final LevelingHelperPlugin plugin;
	private final LevelingHelperConfig config;

	@Inject
	LevelingHelperMinimapOverlay(Client client, LevelingHelperPlugin plugin, LevelingHelperConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.active() || !config.showArrow() || client.getGameState() != GameState.LOGGED_IN
			|| client.getVarbitValue(VarbitID.MINIMAP_STATE) != 0)
		{
			return null;
		}
		Player player = client.getLocalPlayer();
		WorldPoint target = plugin.guidanceTarget();
		if (player == null || target == null || target.getPlane() != player.getWorldLocation().getPlane())
		{
			return null;
		}
		Widget map = client.getWidget(!client.isResized() ? InterfaceID.Toplevel.MINIMAP
			: client.getVarbitValue(VarbitID.RESIZABLE_STONE_ARRANGEMENT) == 1
				? InterfaceID.ToplevelPreEoc.MINIMAP : InterfaceID.ToplevelOsrsStretch.MINIMAP);
		if (map == null || map.isHidden())
		{
			return null;
		}
		Rectangle bounds = map.getBounds();
		double zoom = client.getMinimapZoom();
		if (zoom <= 0)
		{
			return null;
		}
		WorldPoint origin = player.getWorldLocation();
		Polygon arrow = DirectionArrow.polygon((target.getX() - origin.getX()) * zoom,
			(target.getY() - origin.getY()) * zoom, client.getCameraYawTarget(), bounds);
		if (arrow != null)
		{
			Graphics2D marker = (Graphics2D) graphics.create();
			marker.clip(new Ellipse2D.Double(bounds.x, bounds.y, bounds.width, bounds.height));
			marker.setColor(config.highlightColor());
			marker.fill(arrow);
			marker.setColor(Color.BLACK);
			marker.setStroke(new BasicStroke(2));
			marker.draw(arrow);
			marker.dispose();
		}
		return null;
	}
}
