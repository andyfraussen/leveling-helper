package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.GuideController;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SceneOverlayTest
{
	private final Client client = mock(Client.class);
	private final LevelingHelperPlugin plugin = mock(LevelingHelperPlugin.class);
	private final LevelingHelperConfig config = mock(LevelingHelperConfig.class);
	private final GameObject object = mock(GameObject.class);
	private final WorldPoint target = new WorldPoint(3200, 3200, 0);
	private LevelingHelperOverlay overlay;

	@Before
	public void setUp()
	{
		GuideController controller = new GuideController();
		controller.start(TrainingCatalog.all().stream()
			.filter(method -> method.skill() == Skill.MINING && method.name().equals("Iron"))
			.findFirst().get());
		when(plugin.controller()).thenReturn(controller);
		when(plugin.currentStep()).thenAnswer(invocation -> controller.current());
		when(plugin.active()).thenReturn(true);
		when(plugin.guidanceTarget()).thenReturn(target);
		when(plugin.highlightedNpcs()).thenReturn(Collections.emptyList());
		when(plugin.highlightedObjects()).thenReturn(Collections.singletonList(object));
		when(object.getWorldLocation()).thenReturn(target);
		when(object.getConvexHull()).thenReturn(new Rectangle(200, 200, 30, 50));
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getViewportWidth()).thenReturn(765);
		when(client.getViewportHeight()).thenReturn(503);
		when(config.showArrow()).thenReturn(true);
		when(config.highlightColor()).thenReturn(Color.GREEN);
		overlay = new LevelingHelperOverlay(client, plugin, config);
	}

	@Test
	public void downwardArrowSitsAboveTheVisibleTargetWithNoFloatingBox()
	{
		BufferedImage image = render();
		assertTrue(pixels(image, new Rectangle(200, 165, 30, 35)) > 0);
		assertEquals(0, pixels(image, new Rectangle(0, 0, 190, 160)));
		assertEquals(0, pixels(image, new Rectangle(200, 200, 30, 50)));
	}

	@Test
	public void arrowIsSuppressedForOffscreenAndOtherPlaneTargets()
	{
		when(object.getConvexHull()).thenReturn(new Rectangle(900, 200, 30, 50));
		assertEquals(0, pixels(render(), new Rectangle(0, 0, 765, 503)));
		when(plugin.guidanceTarget()).thenReturn(new WorldPoint(3200, 3200, 1));
		assertEquals(0, pixels(render(), new Rectangle(0, 0, 765, 503)));
	}

	@Test
	public void missingProtectionOrDisabledArrowsHaveNoMarker()
	{
		when(plugin.guidanceTarget()).thenReturn(null);
		assertEquals(0, pixels(render(), new Rectangle(0, 0, 765, 503)));
		when(plugin.guidanceTarget()).thenReturn(target);
		when(config.showArrow()).thenReturn(false);
		assertEquals(0, pixels(render(), new Rectangle(0, 0, 765, 503)));
	}

	private BufferedImage render()
	{
		BufferedImage image = new BufferedImage(765, 503, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		overlay.render(graphics);
		graphics.dispose();
		return image;
	}

	private int pixels(BufferedImage image, Rectangle area)
	{
		int count = 0;
		for (int x = area.x; x < area.x + area.width; x++)
		{
			for (int y = area.y; y < area.y + area.height; y++)
			{
				if ((image.getRGB(x, y) >>> 24) != 0) count++;
			}
		}
		return count;
	}
}
