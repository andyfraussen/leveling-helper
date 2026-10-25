package com.levelinghelper;

import com.levelinghelper.model.ItemCategory;
import java.util.Arrays;
import java.util.Locale;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

/** Read-only classifications of actual carried/banked item definitions. */
final class ItemRules
{
	private static final int[] HERBS = {ItemID.GRIMY_GUAM_LEAF, ItemID.GRIMY_MARRENTILL, ItemID.GRIMY_TARROMIN,
		ItemID.GRIMY_HARRALANDER, ItemID.GRIMY_RANARR_WEED, ItemID.GRIMY_TOADFLAX, ItemID.GRIMY_IRIT_LEAF,
		ItemID.GRIMY_AVANTOE, ItemID.GRIMY_KWUARM, ItemID.GRIMY_SNAPDRAGON, ItemID.GRIMY_CADANTINE,
		ItemID.GRIMY_LANTADYME, ItemID.GRIMY_DWARF_WEED, ItemID.GRIMY_TORSTOL};
	private static final int[] CLEAN_LEVELS = {3, 5, 11, 20, 25, 30, 40, 48, 54, 59, 65, 67, 70, 75};
	private static final int[] SEEDS = {ItemID.GUAM_SEED, ItemID.MARRENTILL_SEED, ItemID.TARROMIN_SEED,
		ItemID.HARRALANDER_SEED, ItemID.RANARR_SEED, ItemID.TOADFLAX_SEED, ItemID.IRIT_SEED, ItemID.AVANTOE_SEED,
		ItemID.KWUARM_SEED, ItemID.SNAPDRAGON_SEED, ItemID.CADANTINE_SEED, ItemID.LANTADYME_SEED,
		ItemID.DWARF_WEED_SEED, ItemID.TORSTOL_SEED};
	private static final int[] PLANT_LEVELS = {9, 14, 19, 26, 32, 38, 44, 50, 56, 62, 67, 73, 79, 85};
	private static final int[] SAPLINGS = {ItemID.OAK_SAPLING, ItemID.WILLOW_SAPLING, ItemID.MAPLE_SAPLING, ItemID.YEW_SAPLING,
		ItemID.MAGIC_SAPLING, ItemID.APPLE_SAPLING, ItemID.BANANA_SAPLING, ItemID.ORANGE_SAPLING, ItemID.CURRY_SAPLING,
		ItemID.PINEAPPLE_SAPLING, ItemID.PAPAYA_SAPLING, ItemID.PALM_SAPLING};
	private static final int[] TREE_LEVELS = {15, 30, 45, 60, 75, 27, 33, 39, 42, 51, 57, 68};
	private static final int[] POUCH_TYPES = {Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3, Varbits.RUNE_POUCH_RUNE4};
	private static final int[] POUCH_AMOUNTS = {Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4};

	private final Client client;
	private final ItemManager items;

	ItemRules(Client client, ItemManager items)
	{
		this.client = client;
		this.items = items;
	}

	boolean matches(ItemCategory category, int id)
	{
		ItemComposition item = items.getItemComposition(id);
		if (item == null || item.getNote() != -1 || item.getPlaceholderTemplateId() != -1
			|| item.isMembers() && !client.getWorldType().contains(WorldType.MEMBERS)) return false;
		String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
		ItemStats stats = items.getItemStats(id);
		ItemEquipmentStats equipment = stats == null ? null : stats.getEquipment();
		switch (category)
		{
			case FOOD:
				return !name.contains("poison") && !name.contains("rock cake") && !name.contains("nightshade")
					&& !name.contains("rotten") && (hasAction(item, "Eat")
						|| hasAction(item, "Drink") && (name.contains("wine") || name.contains("saradomin brew") || name.contains("guthix rest")));
			case MELEE_WEAPON:
				return equipment != null && equipment.getSlot() == 3
					&& (equipment.getAstab() > 0 || equipment.getAslash() > 0 || equipment.getAcrush() > 0);
			case RANGED_WEAPON:
				return equipment != null && equipment.getSlot() == 3 && (equipment.getArange() > 0 || equipment.getRstr() > 0);
			case SELF_AMMO_WEAPON:
				return equipment != null && equipment.getSlot() == 3 && (equipment.getArange() > 0 || equipment.getRstr() > 0)
					&& !name.contains("empty") && !name.contains("(u)") && !name.contains("inactive")
					&& (item.isStackable() || name.contains("crystal bow") || name.contains("faerdhinen")
						|| name.contains("blowpipe") || name.contains("craw's bow") || name.contains("webweaver"));
			case POWERED_STAFF:
				return equipment != null && equipment.getSlot() == 3 && !name.contains("uncharged") && !name.contains("(u)")
					&& (name.contains("trident") || name.contains("sanguinesti") || name.contains("tumeken") || name.contains("warped sceptre"));
			case AMMUNITION:
				return equipment != null && equipment.getSlot() == 13 && equipment.getRstr() > 0;
			case GRIMY_HERB: return leveled(id, HERBS, CLEAN_LEVELS, Skill.HERBLORE);
			case HERB_SEED: return leveled(id, SEEDS, PLANT_LEVELS, Skill.FARMING);
			case SAPLING: return leveled(id, SAPLINGS, TREE_LEVELS, Skill.FARMING);
			case LEATHER: return id == ItemID.LEATHER || id == ItemID.HARD_LEATHER && client.getRealSkillLevel(Skill.CRAFTING) >= 28;
			case PLANK: return id == ItemID.PLANK || id == ItemID.OAK_PLANK || id == ItemID.TEAK_PLANK || id == ItemID.MAHOGANY_PLANK;
			case FOUNDRY_METAL:
				return id == ItemID.BRONZE_BAR || id == ItemID.IRON_BAR || id == ItemID.STEEL_BAR
					|| id == ItemID.MITHRIL_BAR || id == ItemID.ADAMANTITE_BAR || id == ItemID.RUNITE_BAR;
			default: return false;
		}
	}

	private boolean leveled(int id, int[] ids, int[] levels, Skill skill)
	{
		for (int i = 0; i < ids.length; i++) if (ids[i] == id) return client.getRealSkillLevel(skill) >= levels[i];
		return false;
	}

	private boolean hasAction(ItemComposition item, String action)
	{
		return item.getInventoryActions() != null && Arrays.asList(item.getInventoryActions()).contains(action);
	}

	int pouchCount(int itemId)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null) return 0;
		int slots = inventory.count(ItemID.DIVINE_RUNE_POUCH) > 0 || inventory.count(ItemID.DIVINE_RUNE_POUCH_L) > 0 ? 4
			: inventory.count(ItemID.RUNE_POUCH) > 0 || inventory.count(ItemID.RUNE_POUCH_L) > 0 ? 3 : 0;
		if (slots == 0 || client.getEnum(982) == null) return 0;
		long count = 0;
		for (int i = 0; i < slots; i++)
		{
			int type = client.getVarbitValue(POUCH_TYPES[i]);
			if (type > 0 && client.getEnum(982).getIntValue(type) == itemId)
			{
				count += Math.max(0, client.getVarbitValue(POUCH_AMOUNTS[i]));
			}
		}
		return (int) Math.min(Integer.MAX_VALUE, count);
	}
}
