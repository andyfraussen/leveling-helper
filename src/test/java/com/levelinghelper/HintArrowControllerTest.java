package com.levelinghelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class HintArrowControllerTest
{
	private Client client;
	private HintArrowController arrows;
	private final AtomicInteger type = new AtomicInteger(HintArrowType.NONE);
	private final AtomicReference<WorldPoint> point = new AtomicReference<>();
	private final AtomicReference<NPC> npc = new AtomicReference<>();
	private final WorldPoint target = new WorldPoint(100, 200, 0);

	@Before
	public void setUp()
	{
		client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.hasHintArrow()).thenAnswer(call -> type.get() != HintArrowType.NONE);
		when(client.getHintArrowType()).thenAnswer(call -> type.get());
		when(client.getHintArrowPoint()).thenAnswer(call -> point.get());
		when(client.getHintArrowNpc()).thenAnswer(call -> npc.get());
		doAnswer(call -> { point.set(call.getArgument(0)); type.set(HintArrowType.COORDINATE); return null; })
			.when(client).setHintArrow(any(WorldPoint.class));
		doAnswer(call -> { npc.set(call.getArgument(0)); type.set(HintArrowType.NPC); return null; })
			.when(client).setHintArrow(any(NPC.class));
		doAnswer(call -> { type.set(HintArrowType.NONE); return null; }).when(client).clearHintArrow();
		arrows = new HintArrowController(client);
	}

	@Test
	public void preservesAnExistingServerOrOtherPluginArrow()
	{
		type.set(HintArrowType.PLAYER);
		arrows.update(true, null, target);
		verify(client, never()).setHintArrow(any(WorldPoint.class));
		arrows.clear();
		verify(client, never()).clearHintArrow();
	}

	@Test
	public void neverClearsOrOverwritesAnArrowThatReplacedOurs()
	{
		arrows.update(true, null, target);
		point.set(target.dx(10));
		arrows.update(true, null, target);
		arrows.clear();
		verify(client, times(1)).setHintArrow(target);
		verify(client, never()).clearHintArrow();
	}

	@Test
	public void avoidsRewritingAnUnchangedArrowAndHonorsDisable()
	{
		arrows.update(true, null, target);
		arrows.update(true, null, target);
		verify(client, times(1)).setHintArrow(target);
		arrows.update(false, null, target);
		verify(client, times(1)).clearHintArrow();
	}

	@Test
	public void switchesOwnedTargetsAndClearsOnlyTheOwnedNpc()
	{
		NPC targetNpc = mock(NPC.class);
		arrows.update(true, null, target);
		arrows.update(true, targetNpc, null);
		verify(client).setHintArrow(targetNpc);
		arrows.clear();
		verify(client).clearHintArrow();
	}

	@Test
	public void logoutClearsOurArrowOnceSoItCannotBecomeStaleOnReconnect()
	{
		arrows.update(true, null, target);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		arrows.clear();
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		arrows.clear();
		verify(client, times(1)).clearHintArrow();
	}
}
