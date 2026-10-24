package com.levelinghelper.model;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/** An interchangeable set of unnoted items, checked in the required container. */
public final class ItemRequirement
{
	public enum Container { INVENTORY, EQUIPMENT, EITHER }

	private final String label;
	private final Container container;
	private final int quantity;
	private final int[] ids;

	public ItemRequirement(String label, boolean equipped, int quantity, int... ids)
	{
		this(label, equipped ? Container.EQUIPMENT : Container.INVENTORY, quantity, ids);
	}

	public ItemRequirement(String label, Container container, int quantity, int... ids)
	{
		if (label == null || label.isEmpty() || quantity < 1 || ids.length == 0
			|| Arrays.stream(ids).anyMatch(id -> id <= 0))
		{
			throw new IllegalArgumentException("An item requirement needs a label, quantity and valid item IDs");
		}
		this.label = label;
		this.container = java.util.Objects.requireNonNull(container);
		this.quantity = quantity;
		this.ids = Arrays.stream(ids).distinct().toArray();
	}

	public String label()
	{
		return label;
	}

	public boolean equipped()
	{
		return container == Container.EQUIPMENT;
	}

	public boolean countsInventory()
	{
		return container != Container.EQUIPMENT;
	}

	public boolean countsEquipment()
	{
		return container != Container.INVENTORY;
	}

	public boolean met(IntUnaryOperator count)
	{
		long total = 0;
		for (int id : ids)
		{
			total += Math.max(0, count.applyAsInt(id));
		}
		return total >= quantity;
	}
}
