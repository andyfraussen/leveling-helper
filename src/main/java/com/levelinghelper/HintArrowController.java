package com.levelinghelper;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HintArrowType;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

/** Cooperates with server and other plugin arrows instead of claiming the shared arrow every tick. */
final class HintArrowController
{
	private final Client client;
	private NPC npc;
	private WorldPoint point;

	@Inject
	HintArrowController(Client client)
	{
		this.client = client;
	}

	void update(boolean enabled, NPC targetNpc, WorldPoint targetPoint)
	{
		if (!enabled || client.getGameState() != GameState.LOGGED_IN)
		{
			clear();
			return;
		}
		if (client.hasHintArrow() && !ownsCurrent())
		{
			npc = null;
			point = null;
			return;
		}
		if (targetNpc != null)
		{
			if (npc != targetNpc || !ownsCurrent())
			{
				client.setHintArrow(targetNpc);
			}
			npc = targetNpc;
			point = null;
		}
		else if (targetPoint != null)
		{
			if (!targetPoint.equals(point) || !ownsCurrent())
			{
				client.setHintArrow(targetPoint);
			}
			point = targetPoint;
			npc = null;
		}
		else
		{
			clear();
		}
	}

	void clear()
	{
		if (ownsCurrent())
		{
			client.clearHintArrow();
		}
		npc = null;
		point = null;
	}

	private boolean ownsCurrent()
	{
		return npc != null && client.getHintArrowType() == HintArrowType.NPC && client.getHintArrowNpc() == npc
			|| point != null && client.getHintArrowType() == HintArrowType.COORDINATE && point.equals(client.getHintArrowPoint());
	}
}
