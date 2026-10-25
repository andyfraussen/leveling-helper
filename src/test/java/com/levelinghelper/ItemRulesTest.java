package com.levelinghelper;

import com.levelinghelper.model.ItemCategory;
import java.util.EnumSet;
import net.runelite.api.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.game.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemRulesTest
{
	private final Client client = mock(Client.class);
	private final ItemManager manager = mock(ItemManager.class);
	private final ItemRules rules = new ItemRules(client, manager);

	private ItemComposition item(int id, String name, boolean members)
	{
		ItemComposition item = mock(ItemComposition.class);
		when(item.getName()).thenReturn(name);
		when(item.getNote()).thenReturn(-1);
		when(item.getPlaceholderTemplateId()).thenReturn(-1);
		when(item.isMembers()).thenReturn(members);
		when(manager.getItemComposition(id)).thenReturn(item);
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		return item;
	}

	@Test
	public void herbsAndSeedsMustBeUnnotedUsableAndAvailableOnThisWorld()
	{
		ItemComposition herb = item(ItemID.GRIMY_RANARR_WEED, "Grimy ranarr weed", true);
		when(client.getRealSkillLevel(Skill.HERBLORE)).thenReturn(24);
		assertFalse(rules.matches(ItemCategory.GRIMY_HERB, ItemID.GRIMY_RANARR_WEED));
		when(client.getRealSkillLevel(Skill.HERBLORE)).thenReturn(25);
		assertTrue(rules.matches(ItemCategory.GRIMY_HERB, ItemID.GRIMY_RANARR_WEED));
		when(herb.getNote()).thenReturn(ItemID.GRIMY_RANARR_WEED);
		assertFalse(rules.matches(ItemCategory.GRIMY_HERB, ItemID.GRIMY_RANARR_WEED));
		when(herb.getNote()).thenReturn(-1);
		when(herb.getPlaceholderTemplateId()).thenReturn(14401);
		assertFalse(rules.matches(ItemCategory.GRIMY_HERB, ItemID.GRIMY_RANARR_WEED));
		item(ItemID.RANARR_SEED, "Ranarr seed", true);
		when(client.getRealSkillLevel(Skill.FARMING)).thenReturn(31);
		assertFalse(rules.matches(ItemCategory.HERB_SEED, ItemID.RANARR_SEED));
		when(client.getRealSkillLevel(Skill.FARMING)).thenReturn(32);
		assertTrue(rules.matches(ItemCategory.HERB_SEED, ItemID.RANARR_SEED));
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		assertFalse(rules.matches(ItemCategory.HERB_SEED, ItemID.RANARR_SEED));
	}

	@Test
	public void weaponAndAmmunitionCategoriesUseActualEquipmentSlotsAndStats()
	{
		ItemComposition dart = item(ItemID.MITHRIL_DART, "Mithril dart", true);
		when(dart.isStackable()).thenReturn(true);
		when(manager.getItemStats(ItemID.MITHRIL_DART)).thenReturn(new ItemStats(true, 0, 0,
			ItemEquipmentStats.builder().slot(3).arange(8).rstr(7).build()));
		assertTrue(rules.matches(ItemCategory.RANGED_WEAPON, ItemID.MITHRIL_DART));
		assertTrue(rules.matches(ItemCategory.SELF_AMMO_WEAPON, ItemID.MITHRIL_DART));
		assertFalse(rules.matches(ItemCategory.AMMUNITION, ItemID.MITHRIL_DART));
		item(ItemID.RUNE_ARROW, "Rune arrow", false);
		when(manager.getItemStats(ItemID.RUNE_ARROW)).thenReturn(new ItemStats(true, 0, 0,
			ItemEquipmentStats.builder().slot(13).rstr(49).build()));
		assertTrue(rules.matches(ItemCategory.AMMUNITION, ItemID.RUNE_ARROW));
		assertFalse(rules.matches(ItemCategory.RANGED_WEAPON, ItemID.RUNE_ARROW));
	}

	@Test
	public void staleRunePouchVariablesNeverSupplyRunesWithoutACarriedPouch()
	{
		EnumComposition types = mock(EnumComposition.class);
		when(client.getEnum(982)).thenReturn(types);
		when(types.getIntValue(1)).thenReturn(ItemID.AIR_RUNE);
		when(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE1)).thenReturn(1);
		when(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT1)).thenReturn(500);
		when(client.getVarbitValue(Varbits.RUNE_POUCH_RUNE4)).thenReturn(1);
		when(client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT4)).thenReturn(250);
		doReturn(SupplyTrackerTest.container()).when(client).getItemContainer(InventoryID.INV);
		assertEquals(0, rules.pouchCount(ItemID.AIR_RUNE));
		doReturn(SupplyTrackerTest.container(new Item(ItemID.RUNE_POUCH, 1))).when(client).getItemContainer(InventoryID.INV);
		assertEquals(500, rules.pouchCount(ItemID.AIR_RUNE));
		doReturn(SupplyTrackerTest.container(new Item(ItemID.DIVINE_RUNE_POUCH, 1))).when(client).getItemContainer(InventoryID.INV);
		assertEquals(750, rules.pouchCount(ItemID.AIR_RUNE));
		assertEquals(0, rules.pouchCount(ItemID.FIRE_RUNE));
	}
	@Test
	public void hardLeatherOnlyCountsOnceHardleatherBodiesCanBeMade()
	{
		item(ItemID.HARD_LEATHER, "Hard leather", false);
		when(client.getRealSkillLevel(Skill.CRAFTING)).thenReturn(27);
		assertFalse(rules.matches(ItemCategory.LEATHER, ItemID.HARD_LEATHER));
		when(client.getRealSkillLevel(Skill.CRAFTING)).thenReturn(28);
		assertTrue(rules.matches(ItemCategory.LEATHER, ItemID.HARD_LEATHER));
	}

}
