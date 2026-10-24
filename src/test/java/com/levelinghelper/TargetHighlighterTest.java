package com.levelinghelper;

import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.GuideStep;
import java.util.Arrays;
import java.util.Collections;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TargetHighlighterTest
{
	private final WorldPoint at = new WorldPoint(100, 100, 0);
	private Client client;
	private TargetHighlighter targets;

	@Before
	public void setUp()
	{
		client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getNpcs()).thenReturn(Collections.emptyList());
		targets = new TargetHighlighter(client);
	}

	@Test
	public void movingNpcsAreReevaluatedWithoutSpawnEvents()
	{
		NPC npc = mock(NPC.class);
		when(npc.getId()).thenReturn(6825);
		when(npc.getName()).thenReturn("Rod Fishing spot");
		when(npc.getWorldLocation()).thenReturn(at);
		when(client.getNpcs()).thenReturn(Collections.singletonList(npc));
		GuideStep step = GuideSteps.train("Fish", at, AdvanceWhen.MANUAL).npcIds(6825);
		targets.refresh(step, true);
		assertEquals(Collections.singletonList(npc), targets.npcs());
		when(npc.getWorldLocation()).thenReturn(at.dx(100));
		targets.refresh(step, false);
		assertFalse(targets.visible());
	}

	@Test
	public void objectIdsRejectOtherOresAndNullImpostors()
	{
		GuideStep step = GuideSteps.train("Mine", at, AdvanceWhen.MANUAL, "Rocks").objectIds(11364);
		targets.refresh(step, true);
		GameObject object = mock(GameObject.class);
		when(object.getId()).thenReturn(11364);
		when(object.getWorldLocation()).thenReturn(at);
		ObjectComposition composition = mock(ObjectComposition.class);
		when(client.getObjectDefinition(11364)).thenReturn(composition);
		when(composition.getId()).thenReturn(11364);
		when(composition.getName()).thenReturn("Rocks");
		targets.considerObject(object);
		targets.considerObject(object);
		assertEquals(1, targets.objects().size());
		targets.remove(object);
		when(composition.getImpostorIds()).thenReturn(new int[]{11364, -1});
		when(composition.getImpostor()).thenReturn(null);
		targets.considerObject(object);
		assertFalse(targets.visible());
	}

	@Test
	public void roamingTargetsStillHonorTheCurrentPlaneAndNpcIds()
	{
		NPC right = mock(NPC.class);
		NPC wrongFloor = mock(NPC.class);
		NPC wrongId = mock(NPC.class);
		when(right.getId()).thenReturn(6825);
		when(right.getWorldLocation()).thenReturn(at);
		when(wrongFloor.getId()).thenReturn(6825);
		when(wrongFloor.getWorldLocation()).thenReturn(at.dz(1));
		when(wrongId.getId()).thenReturn(1510);
		when(wrongId.getWorldLocation()).thenReturn(at);
		when(client.getNpcs()).thenReturn(Arrays.asList(right, wrongFloor, wrongId));
		targets.refresh(GuideSteps.roam("Fish", AdvanceWhen.MANUAL).npcIds(6825), true);
		assertEquals(Collections.singletonList(right), targets.npcs());
	}

	@Test
	public void rooftopTargetsCanBeAboveTheGroundLevelStartTile()
	{
		when(client.getPlane()).thenReturn(3);
		NPC target = mock(NPC.class);
		when(target.getId()).thenReturn(1);
		when(target.getName()).thenReturn("Obstacle");
		when(target.getWorldLocation()).thenReturn(at.dz(3));
		when(client.getNpcs()).thenReturn(Collections.singletonList(target));
		targets.refresh(GuideSteps.trainWide("Course", at, 45, AdvanceWhen.MANUAL, "Obstacle").onAnyPlane(), true);
		assertTrue(targets.visible());
	}

	@Test
	public void stoppingClearsCachedTargetsAndIgnoresLaterSpawnEvents()
	{
		targets.refresh(GuideSteps.roam("Find", AdvanceWhen.MANUAL, "Cow"), true);
		targets.clear();
		NPC npc = mock(NPC.class);
		when(npc.getName()).thenReturn("Cow");
		when(npc.getWorldLocation()).thenReturn(at);
		targets.considerNpc(npc);
		assertFalse(targets.visible());
	}
}
