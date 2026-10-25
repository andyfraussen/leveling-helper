package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.model.*;
import java.util.*;
import net.runelite.api.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GlobalSupplyTest
{
	private final SupplyTracker tracker = new SupplyTracker();
	private final Map<Integer, Integer> inventory = new HashMap<>();
	private final Map<Integer, Integer> worn = new HashMap<>();
	private final Map<Integer, Integer> stored = new HashMap<>();
	private final LevelLookup lookup = new LevelLookup()
	{
		public int level(Skill skill) { return 99; }
		public int combatLevel() { return 126; }
		public boolean membersWorld() { return true; }
		public boolean matches(ItemCategory category, int id) { return category == ItemCategory.SELF_AMMO_WEAPON && id == ItemID.MITHRIL_DART || category == ItemCategory.RANGED_WEAPON && id == ItemID.MITHRIL_DART; }
		public int itemCount(int id) { return inventory.getOrDefault(id, 0); }
		public int equippedCount(int id) { return worn.getOrDefault(id, 0); }
		public int storedItemCount(int id) { return stored.getOrDefault(id, 0); }
	};

	private TrainingMethod method(Skill skill, String name)
	{
		return TrainingCatalog.all().stream().filter(method -> method.skill() == skill && method.name().equals(name)).findFirst().get();
	}

	private net.runelite.api.ItemContainer container(Map<Integer, Integer> stock)
	{
		return SupplyTrackerTest.container(stock.entrySet().stream().map(entry -> new Item(entry.getKey(), entry.getValue())).toArray(Item[]::new));
	}

	private List<SupplyTracker.Stock> stock(TrainingMethod method)
	{
		return tracker.check(method, container(inventory), container(worn), lookup, true);
	}

	private SupplyTracker.Stock row(TrainingMethod method, String contains)
	{
		return stock(method).stream().filter(item -> item.requirement.label().contains(contains)).findFirst().get();
	}

	@Test
	public void elementalStaffIsRecognizedEquippedCarriedAndInTheObservedBank()
	{
		TrainingMethod fire = method(Skill.MAGIC, "Fire Strike");
		inventory.put(ItemID.AIR_RUNE, 100); inventory.put(ItemID.MIND_RUNE, 100);
		worn.put(ItemID.STAFF_OF_FIRE, 1);
		SupplyTracker.Stock element = row(fire, "Fire runes");
		assertTrue(element.ready()); assertTrue(element.supplied); assertEquals(ItemID.STAFF_OF_FIRE, element.presentId);
		worn.clear(); inventory.put(ItemID.STAFF_OF_FIRE, 1);
		element = row(fire, "Fire runes");
		assertFalse(element.ready()); assertTrue(element.needsEquipping()); assertEquals(ItemID.STAFF_OF_FIRE, element.carriedProvider);
		inventory.remove(ItemID.STAFF_OF_FIRE);
		tracker.bind("Caster"); tracker.observeBank(SupplyTrackerTest.container(new Item(ItemID.STAFF_OF_FIRE, 1)));
		element = row(fire, "Fire runes");
		assertTrue(element.canWithdraw()); assertFalse(element.absentFromBank()); assertEquals(ItemID.STAFF_OF_FIRE, element.bankProvider);
	}

	@Test
	public void spellRecommendationsPreferACompleteCastThePlayerAlreadyHas()
	{
		inventory.put(ItemID.AIR_RUNE, 100); inventory.put(ItemID.FIRE_RUNE, 100); inventory.put(ItemID.MIND_RUNE, 100);
		TrainingMethod combat = method(Skill.MAGIC, "Combat spells");
		assertTrue(stock(combat).stream().filter(item -> !item.requirement.optional()).allMatch(SupplyTracker.Stock::ready));
		assertTrue(combat.resolvedItems(lookup).stream().anyMatch(item -> item.label().startsWith("Fire Strike:")));
		assertFalse(combat.resolvedItems(lookup).stream().anyMatch(item -> item.label().contains("Death")));
	}

	@Test
	public void equippedThrownWeaponsDoNotRequireSeparateArrows()
	{
		TrainingMethod ranged = TrainingCatalog.all().stream().filter(method -> method.skill() == Skill.RANGED).findFirst().get();
		worn.put(ItemID.MITHRIL_DART, 100);
		assertTrue(row(ranged, "Compatible ammunition").ready());
		worn.clear(); inventory.put(ItemID.MITHRIL_DART, 100);
		assertTrue(row(ranged, "Compatible ammunition").needsEquipping());
		inventory.clear();
		assertFalse(row(ranged, "Compatible ammunition").ready());
	}

	@Test
	public void exactConstructionActionQuantitiesAndStoredFarmingToolsAreDetected()
	{
		TrainingMethod larders = method(Skill.CONSTRUCTION, "Oak larders");
		inventory.put(ItemID.OAK_PLANK, 7);
		assertFalse(row(larders, "Oak planks").ready());
		inventory.put(ItemID.OAK_PLANK, 8);
		assertTrue(row(larders, "Oak planks").ready());
		TrainingMethod farming = method(Skill.FARMING, "Allotments");
		stored.put(ItemID.SPADE, 1);
		SupplyTracker.Stock spade = row(farming, "Spade");
		assertFalse(spade.ready()); assertTrue(spade.canRetrieve()); assertFalse(spade.absentFromBank());
		inventory.put(ItemID.SPADE, 1);
		assertTrue(row(farming, "Spade").ready());
	}

	@Test
	public void altarTiaraReplacesTalismanAndMagicImbueReplacesEarthTalismanOnlyWhileActive()
	{
		TrainingMethod lava = method(Skill.RUNECRAFT, "Lava runes");
		worn.put(ItemID.FIRE_TIARA, 1);
		assertTrue(row(lava, "Fire altar access").ready());
		assertFalse(row(lava, "Earth talisman or Magic Imbue").ready());
		LevelLookup imbue = new LevelLookup()
		{
			public int level(Skill skill) { return 99; }
			public int combatLevel() { return 126; }
			public int varbit(int id) { return id == net.runelite.api.gameval.VarbitID.MAGIC_IMBUE_ACTIVE ? 1 : 0; }
		};
		assertTrue(tracker.check(lava, container(inventory), container(worn), imbue, true).stream()
			.filter(item -> item.requirement.label().equals("Earth talisman or Magic Imbue")).findFirst().get().ready());
	}
	@Test
	public void poweredStaffDoesNotReplaceNamedSpellRunesOrItsRequiredSpellbook()
	{
		LevelLookup powered = new LevelLookup()
		{
			public int level(Skill skill) { return 99; }
			public int combatLevel() { return 126; }
			public boolean equippedMatches(ItemCategory category) { return category == ItemCategory.POWERED_STAFF; }
			public int varbit(int id) { return id == net.runelite.api.gameval.VarbitID.SPELLBOOK ? 1 : 0; }
		};
		assertTrue(method(Skill.MAGIC, "Combat spells").resolvedItems(powered).stream().anyMatch(item -> item.label().equals("Charged powered staff")));
		assertTrue(method(Skill.MAGIC, "Combat spells").setupIssues(powered).isEmpty());
		assertFalse(method(Skill.MAGIC, "High Alchemy").setupIssues(powered).isEmpty());
		assertFalse(method(Skill.MAGIC, "Curse splash").setupIssues(powered).isEmpty());
		assertTrue(method(Skill.MAGIC, "Fire Strike").resolvedItems(powered).stream().anyMatch(item -> item.label().startsWith("Fire Strike:")));
	}

}
