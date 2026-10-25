package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.TrainingMethod;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class SupplyTrackerTest
{
	private final SupplyTracker tracker = new SupplyTracker();
	private final Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
	private final LevelLookup profile = new LevelLookup()
	{
		public int level(Skill skill) { return levels.getOrDefault(skill, 1); }
		public int combatLevel() { return 3; }
	};

	static ItemContainer container(Item... items)
	{
		ItemContainer container = mock(ItemContainer.class);
		when(container.getItems()).thenReturn(items);
		when(container.count(anyInt())).thenAnswer(invocation ->
			Arrays.stream(items).filter(item -> item.getId() == (int) invocation.getArgument(0))
				.mapToInt(Item::getQuantity).sum());
		return container;
	}

	private TrainingMethod method(Skill skill, String name)
	{
		return TrainingCatalog.all().stream().filter(method -> method.skill() == skill && method.name().equals(name))
			.findFirst().get();
	}

	@Test
	public void pickaxeInEquipmentNeedsTheMiningLevelButNotTheWieldLevel()
	{
		levels.put(Skill.MINING, 34);
		SupplyTracker.Stock pick = tracker.check(method(Skill.MINING, "Iron"), container(),
			container(new Item(ItemID.ADAMANT_PICKAXE, 1)), profile, false).get(0);
		assertTrue(pick.ready());
		assertEquals(ItemID.ADAMANT_PICKAXE, pick.presentId);
		assertFalse(tracker.check(method(Skill.MINING, "Iron"), container(new Item(ItemID.RUNE_PICKAXE, 1)),
			container(), profile, false).get(0).ready());
	}

	@Test
	public void lowerUsableToolsAreEnoughAndNotedBrokenOrInactiveToolsAreRejected()
	{
		levels.put(Skill.MINING, 99);
		assertTrue(tracker.check(method(Skill.MINING, "Iron"), container(new Item(ItemID.BRONZE_PICKAXE, 1)),
			container(), profile, false).get(0).ready());
		for (int id : new int[]{ItemID.BRONZE_PICKAXE + 1, ItemID.BROKEN_PICKAXE, ItemID.CRYSTAL_PICKAXE_INACTIVE})
		{
			assertFalse(tracker.check(method(Skill.MINING, "Iron"), container(new Item(id, 1)),
				container(), profile, true).get(0).ready());
		}
		assertFalse(tracker.check(method(Skill.MINING, "Iron"), container(new Item(ItemID.DRAGON_PICKAXE, 1)),
			container(), profile, false).get(0).ready());
	}

	@Test
	public void bankIsUnknownUntilObservedAndPlaceholdersNeverCount()
	{
		TrainingMethod bones = method(Skill.PRAYER, "Bury big bones");
		assertFalse(tracker.check(bones, container(), container(), profile, false).get(0).bankKnown);
		tracker.bind("First account");
		tracker.observeBank(container(new Item(ItemID.BIG_BONES, 0)));
		assertTrue(tracker.check(bones, container(), container(), profile, false).get(0).absentFromBank());
		tracker.observeBank(container(new Item(ItemID.BIG_BONES, 100)));
		SupplyTracker.Stock stock = tracker.check(bones, container(), container(), profile, false).get(0);
		assertFalse(stock.ready());
		assertEquals(100, stock.bank);
		assertFalse(stock.absentFromBank());
		tracker.bind("Second account");
		assertFalse(tracker.bankKnown());
		assertEquals(0, tracker.check(bones, container(), container(), profile, false).get(0).bank);
	}

	@Test
	public void logFletchingRequiresAKnifeAndUnnotedLogs()
	{
		TrainingMethod bows = method(Skill.FLETCHING, "Oak longbows");
		assertTrue(tracker.check(bows, container(new Item(ItemID.OAK_LOGS, 27), new Item(ItemID.KNIFE, 1)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertFalse(tracker.check(bows, container(new Item(ItemID.OAK_LOGS + 1, 200), new Item(ItemID.KNIFE, 1)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertFalse(tracker.check(bows, container(new Item(ItemID.OAK_LOGS, 27)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
	}

	@Test
	public void dartsNeedTipsAndFeathersAndDoNotRequireAKnife()
	{
		TrainingMethod darts = method(Skill.FLETCHING, "Mithril darts");
		assertEquals(2, darts.items().size());
		assertTrue(tracker.check(darts, container(new Item(ItemID.MITHRIL_DART_TIP, 100), new Item(ItemID.FEATHER, 75)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertFalse(tracker.check(darts, container(new Item(ItemID.MITHRIL_DART_TIP, 100)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertEquals(11.2, darts.xpEach(), 0.001);
	}

	@Test
	public void recipeQuantitiesAreEnoughForAnActionRatherThanTheWholeGoal()
	{
		TrainingMethod plates = method(Skill.SMITHING, "Steel platebodies");
		assertFalse(tracker.check(plates, container(new Item(ItemID.STEEL_BAR, 4), new Item(ItemID.HAMMER, 1)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertTrue(tracker.check(plates, container(new Item(ItemID.STEEL_BAR, 5), new Item(ItemID.HAMMER, 1)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		TrainingMethod potions = method(Skill.HERBLORE, "Prayer potions");
		assertFalse(tracker.check(potions, container(new Item(ItemID.RANARR_POTION_UNF, 14)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
		assertTrue(tracker.check(potions, container(new Item(ItemID.RANARR_POTION_UNF, 1), new Item(ItemID.SNAPE_GRASS, 1)),
			container(), profile, true).stream().allMatch(SupplyTracker.Stock::ready));
	}

	@Test
	public void allSimpleRecipesAndGatheringRoutesDeclareTheirInputs()
	{
		for (TrainingMethod method : TrainingCatalog.all())
		{
			if (Arrays.asList(Skill.MINING, Skill.WOODCUTTING, Skill.FLETCHING, Skill.PRAYER).contains(method.skill()))
			{
				assertFalse(method.id(), method.items().isEmpty());
			}
		}
	}
}
