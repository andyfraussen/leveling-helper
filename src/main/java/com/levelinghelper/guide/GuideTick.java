package com.levelinghelper.guide;

import net.runelite.api.coords.WorldPoint;

public final class GuideTick
{
	private final WorldPoint player;
	private final boolean bankOpen;
	private final boolean inventoryFull;
	private final boolean watchedItemGone;
	private final boolean highlightVisible;
	private final boolean prepared;
	private final boolean inventoryKnown;

	public GuideTick(WorldPoint player, boolean bankOpen, boolean inventoryFull, boolean watchedItemGone, boolean highlightVisible)
	{
		this(player, bankOpen, inventoryFull, watchedItemGone, highlightVisible, true);
	}

	public GuideTick(WorldPoint player, boolean bankOpen, boolean inventoryFull, boolean watchedItemGone, boolean highlightVisible, boolean prepared)
	{
		this(player, bankOpen, inventoryFull, watchedItemGone, highlightVisible, prepared, true);
	}

	public GuideTick(WorldPoint player, boolean bankOpen, boolean inventoryFull, boolean watchedItemGone, boolean highlightVisible, boolean prepared, boolean inventoryKnown)
	{
		this.player = player;
		this.bankOpen = bankOpen;
		this.inventoryFull = inventoryFull;
		this.watchedItemGone = watchedItemGone;
		this.highlightVisible = highlightVisible;
		this.prepared = prepared;
		this.inventoryKnown = inventoryKnown;
	}

	public boolean inventoryKnown()
	{
		return inventoryKnown;
	}

	public boolean prepared()
	{
		return prepared;
	}

	public WorldPoint player()
	{
		return player;
	}

	public boolean bankOpen()
	{
		return bankOpen;
	}

	public boolean inventoryFull()
	{
		return inventoryFull;
	}

	public boolean watchedItemGone()
	{
		return watchedItemGone;
	}

	public boolean highlightVisible()
	{
		return highlightVisible;
	}
}
