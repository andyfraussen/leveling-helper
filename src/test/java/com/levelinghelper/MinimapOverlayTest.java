package com.levelinghelper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MinimapOverlayTest
{
	private final Client client = mock(Client.class);
	private final LevelingHelperPlugin plugin = mock(LevelingHelperPlugin.class);
	private final LevelingHelperConfig config = mock(LevelingHelperConfig.class);
	private final Widget map = mock(Widget.class);
	private final Rectangle bounds = new Rectangle(80, 30, 150, 150);
	private LevelingHelperMinimapOverlay overlay;

	@Before
	public void setUp()
	{
		Player player = mock(Player.class);
		when(player.getWorldLocation()).thenReturn(new WorldPoint(3200, 3200, 0));
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getMinimapZoom()).thenReturn(4d);
		when(client.getWidget(anyInt())).thenReturn(map);
		when(map.getBounds()).thenReturn(bounds);
		when(config.showArrow()).thenReturn(true);
		when(config.highlightColor()).thenReturn(Color.GREEN);
		when(plugin.active()).thenReturn(true);
		when(plugin.guidanceTarget()).thenReturn(new WorldPoint(3200, 3400, 0));
		overlay = new LevelingHelperMinimapOverlay(client, plugin, config);
	}

	@Test
	public void markerWorksWithoutTheNativeArrowInEveryMinimapLayout()
	{
		for (boolean resized : new boolean[]{false, true})
		{
			for (int arrangement : new int[]{0, 1})
			{
				when(client.isResized()).thenReturn(resized);
				when(client.getVarbitValue(VarbitID.RESIZABLE_STONE_ARRANGEMENT)).thenReturn(arrangement);
				assertTrue(pixels(render(), true) > 0);
			}
		}
		verify(client).getWidget(InterfaceID.ToplevelPreEoc.MINIMAP);
		verify(client).getWidget(InterfaceID.ToplevelOsrsStretch.MINIMAP);
		verify(client, times(2)).getWidget(InterfaceID.Toplevel.MINIMAP);
		verify(client, never()).setHintArrow(any(WorldPoint.class));
		verify(client, never()).clearHintArrow();
	}

	@Test
	public void cameraAndZoomKeepTheArrowInsideTheMinimap()
	{
		for (int yaw = 0; yaw < 16384; yaw += 1024)
		{
			when(client.getCameraYawTarget()).thenReturn(yaw);
			for (double zoom : new double[]{1, 4, 8})
			{
				when(client.getMinimapZoom()).thenReturn(zoom);
				BufferedImage image = render();
				assertTrue(pixels(image, true) > 0);
				assertEquals(0, pixels(image, false));
			}
		}
	}

	@Test
	public void hiddenOrRestrictedMinimapNeverGetsAMarker()
	{
		when(map.isHidden()).thenReturn(true);
		assertEquals(0, pixels(render(), true));
		when(map.isHidden()).thenReturn(false);
		for (int state : new int[]{1, 2, 3, 5})
		{
			when(client.getVarbitValue(VarbitID.MINIMAP_STATE)).thenReturn(state);
			assertEquals(0, pixels(render(), true));
		}
	}

	@Test
	public void inactiveDisabledOrOtherPlaneTargetsNeverGetAMarker()
	{
		when(config.showArrow()).thenReturn(false);
		assertEquals(0, pixels(render(), true));
		when(config.showArrow()).thenReturn(true);
		when(plugin.active()).thenReturn(false);
		assertEquals(0, pixels(render(), true));
		when(plugin.active()).thenReturn(true);
		when(plugin.guidanceTarget()).thenReturn(new WorldPoint(3200, 3400, 1));
		assertEquals(0, pixels(render(), true));
		when(plugin.guidanceTarget()).thenReturn(null);
		assertEquals(0, pixels(render(), true));
	}

	private BufferedImage render()
	{
		BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		overlay.render(graphics);
		graphics.dispose();
		return image;
	}

	private int pixels(BufferedImage image, boolean inside)
	{
		int count = 0;
		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				if ((image.getRGB(x, y) >>> 24) != 0 && bounds.contains(x, y) == inside)
				{
					count++;
				}
			}
		}
		return count;
	}
}
