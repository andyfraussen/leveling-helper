package com.levelinghelper.model;

import com.levelinghelper.guide.LevelLookup;
import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/** An interchangeable set of unnoted items, checked in the required container. */
public final class ItemRequirement
{
	public enum Container { INVENTORY, EQUIPMENT, EITHER }

	private final String label;
	private final Container container;
	private final int quantity;
	private final int[] ids;
	private Alternative[] alternatives;
	private double unitsPerAction;
	private ItemCategory category;
	private ItemCategory providerCategory;
	private boolean optional;
	private boolean departureOnly;
	private int[] equipmentProviders = new int[0];
	private int unlessVarbit = -1;
	private String source = "Gather or make this item. Check its Wiki page for sources.";

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

	public int[] itemIds() { return ids.clone(); }

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

	public int quantity()
	{
		return quantity;
	}

	public String source()
	{
		return source;
	}

	public ItemRequirement from(String source)
	{
		ItemRequirement copy = new ItemRequirement(label, container, quantity, ids);
		copy.alternatives = alternatives;
		copy.unitsPerAction = unitsPerAction;
		copy.category = category;
		copy.providerCategory = providerCategory;
		copy.optional = optional;
		copy.departureOnly = departureOnly;
		copy.equipmentProviders = equipmentProviders;
		copy.unlessVarbit = unlessVarbit;
		copy.source = source;
		return copy;
	}

	public boolean optional() { return optional; }
	public boolean departureOnly() { return departureOnly; }
	public ItemCategory category() { return category; }
	public ItemCategory providerCategory() { return providerCategory; }
	public ItemRequirement providedByCategory(ItemCategory category)
	{
		ItemRequirement copy = from(source);
		copy.providerCategory = category;
		return copy;
	}
	public int[] equipmentProviders() { return equipmentProviders.clone(); }
	public boolean waived(LevelLookup lookup) { return unlessVarbit >= 0 && lookup.varbit(unlessVarbit) > 0; }

	public ItemRequirement asOptional()
	{
		ItemRequirement copy = from(source);
		copy.optional = true;
		return copy;
	}

	public ItemRequirement beforeDeparture()
	{
		ItemRequirement copy = from(source);
		copy.departureOnly = true;
		return copy;
	}

	public ItemRequirement providedBy(int... equipmentIds)
	{
		ItemRequirement copy = from(source);
		copy.equipmentProviders = equipmentIds.clone();
		return copy;
	}

	public ItemRequirement unlessActive(int varbit)
	{
		ItemRequirement copy = from(source);
		copy.unlessVarbit = varbit;
		return copy;
	}

	public static ItemRequirement matching(String label, Container container, int quantity, ItemCategory category, String source)
	{
		// The sentinel is never used for category matching; live candidates are supplied by the tracker.
		ItemRequirement item = new ItemRequirement(label, container, quantity, Integer.MAX_VALUE);
		item.category = java.util.Objects.requireNonNull(category);
		item.source = source;
		return item;
	}

	public ItemRequirement resolve(int[] candidates, LevelLookup lookup)
	{
		if (category == null) return this;
		int[] matching = Arrays.stream(candidates).filter(id -> lookup.matches(category, id)).distinct().toArray();
		ItemRequirement copy = new ItemRequirement(label, container, quantity, matching.length == 0 ? new int[]{Integer.MAX_VALUE} : matching);
		copy.source = source;
		copy.providerCategory = providerCategory;
		copy.optional = optional;
		copy.departureOnly = departureOnly;
		copy.unitsPerAction = unitsPerAction;
		copy.equipmentProviders = equipmentProviders;
		copy.unlessVarbit = unlessVarbit;
		return copy;
	}

	public double unitsPerAction()
	{
		return unitsPerAction;
	}

	public ItemRequirement perAction(double units)
	{
		if (!Double.isFinite(units) || units < 0)
		{
			throw new IllegalArgumentException("Invalid material quantity");
		}
		ItemRequirement copy = from(source);
		copy.unitsPerAction = units;
		return copy;
	}

	public static ItemRequirement tool(String label, String source, Alternative... alternatives)
	{
		int[] toolIds = Arrays.stream(alternatives).mapToInt(choice -> choice.id).toArray();
		if (Arrays.stream(toolIds).distinct().count() != toolIds.length)
		{
			throw new IllegalArgumentException("Tool variants must be unique");
		}
		ItemRequirement item = new ItemRequirement(label, Container.EITHER, 1, toolIds);
		item.alternatives = alternatives.clone();
		item.source = source;
		return item;
	}

	public int count(IntUnaryOperator count, LevelLookup levels, boolean membersWorld)
	{
		long total = 0;
		if (alternatives == null)
		{
			for (int id : ids)
			{
				total += Math.max(0, count.applyAsInt(id));
			}
		}
		else
		{
			for (Alternative choice : alternatives)
			{
				int amount = Math.max(0, count.applyAsInt(choice.id));
				if (amount > 0 && choice.usable(levels, membersWorld))
				{
					total += amount;
				}
			}
		}
		return (int) Math.min(Integer.MAX_VALUE, total);
	}

	public int presentId(IntUnaryOperator count, LevelLookup levels, boolean membersWorld)
	{
		if (alternatives != null)
		{
			for (Alternative choice : alternatives)
			{
				if (count.applyAsInt(choice.id) > 0 && choice.usable(levels, membersWorld))
				{
					return choice.id;
				}
			}
		}
		else
		{
			for (int id : ids)
			{
				if (count.applyAsInt(id) > 0)
				{
					return id;
				}
			}
		}
		return -1;
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

	/** An actual usable tool variant; broken, noted and inactive variants are not declared. */
	public static final class Alternative
	{
		private final int id;
		private final boolean members;
		private final SkillReq[] skills;
		private final Quest quest;

		public Alternative(int id, boolean members, Quest quest, SkillReq... skills)
		{
			this.id = id;
			this.members = members;
			this.skills = skills.clone();
			this.quest = quest;
		}

		private boolean usable(LevelLookup levels, boolean membersWorld)
		{
			if (members && !membersWorld)
			{
				return false;
			}
			for (SkillReq skill : skills)
			{
				if (levels.level(skill.skill()) < skill.level())
				{
					return false;
				}
			}
			return quest == null || levels.questComplete(quest);
		}
	}
}
