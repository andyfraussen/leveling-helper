package com.levelinghelper.guide;

import net.runelite.api.coords.WorldPoint;

public final class GuideTick
{
	private final WorldPoint player;
	private final boolean bankOpen;
	private final boolean inventoryFull;
	private final boolean watchedItemGone;
	private final boolean highlightVisible;

	public GuideTick(WorldPoint player, boolean bankOpen, boolean inventoryFull, boolean watchedItemGone, boolean highlightVisible)
	{
		this.player = player;
		this.bankOpen = bankOpen;
		this.inventoryFull = inventoryFull;
		this.watchedItemGone = watchedItemGone;
		this.highlightVisible = highlightVisible;
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
