package com.levelinghelper;

import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.HighlightMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

/** Scene caches belong to the client thread; the overlay reads them on that same thread. */
final class TargetHighlighter
{
	private final Client client;
	private final List<NPC> npcs = new ArrayList<>();
	private final List<TileObject> objects = new ArrayList<>();
	private final Set<TileObject> candidates = new LinkedHashSet<>();
	private GuideStep step;
	private int plane = -1;

	@Inject
	TargetHighlighter(Client client)
	{
		this.client = client;
	}

	List<NPC> npcs()
	{
		return Collections.unmodifiableList(npcs);
	}

	List<TileObject> objects()
	{
		return Collections.unmodifiableList(objects);
	}

	void refresh(GuideStep next, boolean dirty)
	{
		if (client.getGameState() != GameState.LOGGED_IN || next == null)
		{
			clear();
			return;
		}
		boolean rebuild = dirty || step != next || plane != client.getPlane();
		step = next;
		plane = client.getPlane();
		// Moving NPCs must be checked again even when nothing has spawned or despawned.
		npcs.clear();
		if (!step.hasTargets())
		{
			objects.clear();
			return;
		}
		for (NPC npc : client.getNpcs())
		{
			considerNpc(npc);
		}
		if (!rebuild)
		{
			objects.clear();
			// Re-resolve transforms without rescanning thousands of scene tiles on every varbit change.
			for (TileObject object : candidates)
			{
				highlightObject(object);
			}
			return;
		}
		objects.clear();
		candidates.clear();
		Scene scene = client.getScene();
		if (scene == null || scene.getTiles() == null || plane < 0 || plane >= scene.getTiles().length)
		{
			return;
		}
		for (Tile[] row : scene.getTiles()[plane])
		{
			if (row == null)
			{
				continue;
			}
			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}
				if (tile.getGameObjects() != null)
				{
					for (GameObject object : tile.getGameObjects())
					{
						considerObject(object);
					}
				}
				considerObject(tile.getWallObject());
				considerObject(tile.getGroundObject());
				considerObject(tile.getDecorativeObject());
			}
		}
	}

	void considerNpc(NPC npc)
	{
		if (step != null && npc != null && inRange(npc.getWorldLocation())
			&& step.matchesNpc(npc.getId(), npc.getName()) && !npcs.contains(npc))
		{
			npcs.add(npc);
		}
	}

	void considerObject(TileObject object)
	{
		if (step == null || object == null || object.getId() < 0 || !inRange(object.getWorldLocation()))
		{
			return;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition == null)
		{
			return;
		}
		if (composition.getImpostorIds() != null || step.matchesObject(composition.getId(), composition.getName()))
		{
			candidates.add(object);
			highlightObject(object);
		}
	}

	private void highlightObject(TileObject object)
	{
		if (!inRange(object.getWorldLocation()))
		{
			return;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition == null)
		{
			return;
		}
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
			if (composition == null)
			{
				return;
			}
		}
		if (step.matchesObject(composition.getId(), composition.getName()) && !objects.contains(object))
		{
			objects.add(object);
		}
	}

	void remove(NPC npc)
	{
		npcs.remove(npc);
	}

	void remove(TileObject object)
	{
		objects.remove(object);
		candidates.remove(object);
	}

	boolean visible()
	{
		return !npcs.isEmpty() || !objects.isEmpty();
	}

	NPC closestNpc()
	{
		WorldPoint player = playerLocation();
		return player == null ? null : npcs.stream()
			.min(java.util.Comparator.comparingInt(npc -> player.distanceTo(npc.getWorldLocation())))
			.orElse(null);
	}

	TileObject closestObject()
	{
		WorldPoint player = playerLocation();
		return player == null ? null : objects.stream()
			.min(java.util.Comparator.comparingInt(object -> player.distanceTo(object.getWorldLocation())))
			.orElse(null);
	}

	void clear()
	{
		npcs.clear();
		objects.clear();
		candidates.clear();
		step = null;
		plane = -1;
	}

	private WorldPoint playerLocation()
	{
		return client.getLocalPlayer() == null ? null : client.getLocalPlayer().getWorldLocation();
	}

	private boolean inRange(WorldPoint location)
	{
		if (location == null || location.getPlane() != client.getPlane())
		{
			return false;
		}
		if (step.highlightMode() == HighlightMode.ANYWHERE)
		{
			return true;
		}
		WorldPoint origin = step.highlightMode() == HighlightMode.NEAR_PLAYER ? playerLocation() : step.target();
		return origin != null && (origin.getPlane() == location.getPlane() || step.highlightMode() == HighlightMode.NEAR_TARGET_ANY_PLANE)
			&& origin.distanceTo2D(location) <= step.highlightRadius();
	}
}
