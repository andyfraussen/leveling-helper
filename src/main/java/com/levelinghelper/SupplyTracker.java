package com.levelinghelper;

import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.model.ItemRequirement;
import com.levelinghelper.model.TrainingMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

/** Bank knowledge comes only from an observed bank container and is scoped to the current login. */
final class SupplyTracker
{
	private final Map<Integer, Integer> bank = new HashMap<>();
	private String account;
	private boolean bankKnown;

	void bind(String player)
	{
		if (!Objects.equals(account, player))
		{
			clear();
			account = player;
		}
	}

	void clear()
	{
		bank.clear();
		bankKnown = false;
		account = null;
	}

	boolean bankKnown()
	{
		return bankKnown;
	}

	void observeBank(ItemContainer container)
	{
		if (account == null || container == null)
		{
			return;
		}
		bank.clear();
		for (Item item : container.getItems())
		{
			// Zero-quantity placeholders and empty slots never count as stock.
			if (item.getId() > 0 && item.getQuantity() > 0)
			{
				bank.merge(item.getId(), item.getQuantity(), (a, b) -> (int) Math.min(Integer.MAX_VALUE, (long) a + b));
			}
		}
		bankKnown = true;
	}

	List<Stock> check(TrainingMethod method, ItemContainer inventory, ItemContainer equipment, LevelLookup levels, boolean membersWorld)
	{
		if (method == null)
		{
			return Collections.emptyList();
		}
		List<Stock> result = new ArrayList<>();
		java.util.Set<Integer> candidates = new java.util.HashSet<>(bank.keySet());
		if (inventory != null && inventory.getItems() != null) for (Item item : inventory.getItems()) if (item.getId() > 0 && item.getQuantity() > 0) candidates.add(item.getId());
		if (equipment != null && equipment.getItems() != null) for (Item item : equipment.getItems()) if (item.getId() > 0 && item.getQuantity() > 0) candidates.add(item.getId());
		int[] candidateIds = candidates.stream().mapToInt(Integer::intValue).toArray();
		for (ItemRequirement declaration : method.resolvedItems(levels))
		{
			ItemRequirement item = declaration.resolve(candidateIds, levels);
			int equippedProvider = java.util.Arrays.stream(item.equipmentProviders()).filter(id -> levels.providerUsable(id) && equipment != null && equipment.count(id) > 0).findFirst().orElse(-1);
			if (equippedProvider < 0 && item.providerCategory() != null && equipment != null && equipment.getItems() != null)
			{
				for (Item candidate : equipment.getItems())
				{
					if (candidate.getId() > 0 && candidate.getQuantity() > 0 && levels.matches(item.providerCategory(), candidate.getId()))
					{
						equippedProvider = candidate.getId();
						break;
					}
				}
			}
			int carriedProvider = java.util.Arrays.stream(item.equipmentProviders()).filter(id -> levels.providerUsable(id) && inventory != null && inventory.count(id) > 0).findFirst().orElse(-1);
			int bankProvider = java.util.Arrays.stream(item.equipmentProviders()).filter(id -> levels.providerUsable(id) && bank.getOrDefault(id, 0) > 0).findFirst().orElse(-1);
			if (item.providerCategory() != null)
			{
				for (int id : candidateIds)
				{
					if (levels.matches(item.providerCategory(), id))
					{
						if (carriedProvider < 0 && inventory != null && inventory.count(id) > 0) carriedProvider = id;
						if (bankProvider < 0 && bank.getOrDefault(id, 0) > 0) bankProvider = id;
					}
				}
			}
			IntUnaryOperator carried = id ->
				(int) Math.min(Integer.MAX_VALUE, (long) (item.countsInventory() && inventory != null ? inventory.count(id) : 0)
				+ (item.countsEquipment() && equipment != null ? equipment.count(id) : 0)
				+ (item.countsInventory() ? levels.extraItemCount(id) : 0));
			boolean waived = item.waived(levels);
			int held = waived || equippedProvider > 0 ? item.quantity() : item.count(carried, levels, membersWorld);
			// Equipment-only requirements may already be carried but still need equipping.
			int inInventory = inventory == null ? 0 : item.count(inventory::count, levels, membersWorld);
			result.add(new Stock(item, held, inInventory, item.count(id -> bank.getOrDefault(id, 0), levels, membersWorld),
				bankKnown, item.count(levels::storedItemCount, levels, membersWorld), equippedProvider > 0 ? equippedProvider : item.presentId(carried, levels, membersWorld),
				equippedProvider > 0 || waived, carriedProvider, bankProvider));
		}
		return result;
	}

	static final class Stock
	{
		final ItemRequirement requirement;
		final int held;
		final int inInventory;
		final int bank;
		final boolean bankKnown;
		final int presentId;
		final int stored;
		final boolean supplied;
		final int carriedProvider;
		final int bankProvider;

		Stock(ItemRequirement requirement, int held, int inInventory, int bank, boolean bankKnown, int stored, int presentId, boolean supplied, int carriedProvider, int bankProvider)
		{
			this.requirement = requirement;
			this.held = held;
			this.inInventory = inInventory;
			this.bank = bank;
			this.bankKnown = bankKnown;
			this.stored = stored;
			this.presentId = presentId;
			this.supplied = supplied;
			this.carriedProvider = carriedProvider;
			this.bankProvider = bankProvider;
		}

		boolean ready() { return held >= requirement.quantity(); }
		boolean canRetrieve() { return !ready() && stored >= requirement.quantity() - held; }
		boolean needsEquipping() { return !ready() && (carriedProvider > 0 || requirement.equipped() && inInventory >= requirement.quantity()); }
		boolean canWithdraw() { return !ready() && !needsEquipping() && !canRetrieve() && bankKnown && (bank > 0 || bankProvider > 0); }
		boolean absentFromBank() { return !ready() && !needsEquipping() && !canRetrieve() && bankKnown && bankProvider < 0 && (long) held + bank < requirement.quantity(); }
	}
}
