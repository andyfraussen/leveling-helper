package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.GuideController;
import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.guide.GuideTick;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.guide.MethodSelector;
import com.levelinghelper.guide.TrainingMath;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Skill;
import net.runelite.api.Quest;
import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;

public class GuideLogicTest
{
	@Test
	public void xpTableAndCombatLevel()
	{
		Assert.assertEquals(83, TrainingMath.xpBetween(0, 2));
		Assert.assertEquals(0, TrainingMath.xpBetween(13034431, 99));
		Assert.assertEquals(5, TrainingMath.actions(101, 25));
		Assert.assertEquals(0, TrainingMath.actions(0, 25));
		Assert.assertEquals(-1, TrainingMath.actions(10, 0));
		Assert.assertEquals(3_600_000L, TrainingMath.millis(10000, 10000));
		Assert.assertEquals("1h 0m", TrainingMath.duration(3_600_000L));
		Assert.assertEquals("done", TrainingMath.duration(0));
		Assert.assertEquals(3, TrainingMath.combatLevel(1, 1, 1, 10, 1, 1, 1));
	}

	@Test
	public void directionUsesNorthAndEast()
	{
		Assert.assertEquals("5 tiles northeast", TrainingMath.direction(new WorldPoint(0, 0, 0), new WorldPoint(5, 5, 0)));
		Assert.assertEquals("on another floor", TrainingMath.direction(new WorldPoint(0, 0, 0), new WorldPoint(1, 1, 1)));
	}

	@Test
	public void stepsAdvanceOnceAndLoop()
	{
		List<TrainingMethod> methods = new ArrayList<TrainingMethod>();
		TrainingMethod.builder(methods, Skill.WOODCUTTING, "Loop")
			.summary("A test training loop")
			.levels(1, 99)
			.modes(AccountMode.F2P)
			.styles(TrainingStyle.BALANCED)
			.loop(1)
			.step(GuideSteps.bank("bank", new WorldPoint(0, 0, 0)))
			.step(GuideSteps.travel("walk", new WorldPoint(10, 0, 0), 5))
			.step(GuideSteps.train("chop", new WorldPoint(10, 0, 0), AdvanceWhen.INVENTORY_FULL, "Willow"))
			.step(GuideSteps.drop("drop"))
			.save();
		GuideController controller = new GuideController();
		controller.start(methods.get(0));

		Assert.assertFalse(controller.tick(tick(new WorldPoint(100, 0, 0), false, false), true, 8));
		Assert.assertEquals(0, controller.stepIndex());

		Assert.assertFalse(controller.tick(tick(new WorldPoint(0, 0, 0), true, false), true, 8));
		Assert.assertEquals(0, controller.stepIndex());
		Assert.assertTrue(controller.tick(tick(new WorldPoint(0, 0, 0), false, false), true, 8));
		Assert.assertEquals(1, controller.stepIndex());

		Assert.assertFalse(controller.tick(tick(new WorldPoint(10, 0, 1), false, false), true, 8));
		Assert.assertTrue(controller.tick(tick(new WorldPoint(12, 0, 0), false, false), true, 8));
		Assert.assertEquals(2, controller.stepIndex());

		Assert.assertFalse(controller.tick(tick(new WorldPoint(10, 0, 0), false, false), true, 8));
		Assert.assertTrue(controller.tick(tick(new WorldPoint(10, 0, 0), false, true), true, 8));
		Assert.assertEquals(3, controller.stepIndex());

		Assert.assertFalse(controller.tick(tick(new WorldPoint(10, 0, 0), false, true), true, 8));
		Assert.assertTrue(controller.tick(tick(new WorldPoint(10, 0, 0), false, false), true, 8));
		Assert.assertEquals(1, controller.stepIndex());

		controller.start(methods.get(0));
		Assert.assertFalse(controller.tick(tick(new WorldPoint(0, 0, 0), false, false), false, 8));
		Assert.assertEquals(0, controller.stepIndex());
	}

	@Test
	public void catalogCoversEveryLevel()
	{
		Set<String> ids = new HashSet<String>();
		for (TrainingMethod method : TrainingCatalog.all())
		{
			Assert.assertTrue(ids.add(method.id()));
			Assert.assertFalse(method.steps().isEmpty());
			Assert.assertTrue(method.maxLevel() >= method.minLevel());
			Assert.assertTrue(method.maxLevel() <= 99);
			Assert.assertTrue(method.minLevel() >= 1);
			Assert.assertTrue(method.xpPerHour() > 0);
			Assert.assertTrue(method.loopFrom() >= 0 && method.loopFrom() < method.steps().size());
			Assert.assertFalse(method.summary().isEmpty());
			if (method.skill().isMembers())
			{
				Assert.assertFalse(method.modes().contains(AccountMode.F2P));
			}
		}

		for (Skill skill : Skill.values())
		{
			if ("Overall".equals(skill.getName()))
			{
				continue;
			}
			for (int level = 1; level <= 99; level++)
			{
				Assert.assertNotNull(skill.getName() + " members " + level,
					MethodSelector.select(TrainingCatalog.all(), skill, AccountMode.P2P, TrainingStyle.BALANCED, level, maxed()));
				TrainingMethod free = MethodSelector.select(TrainingCatalog.all(), skill, AccountMode.F2P, TrainingStyle.BALANCED, level, maxed());
				if (skill.isMembers())
				{
					Assert.assertNull(skill.getName(), free);
				}
				else
				{
					Assert.assertNotNull(skill.getName() + " free " + level, free);
				}
			}
		}
	}

	@Test
	public void selectorRespectsModeStyleAndRequirements()
	{
		Assert.assertTrue(MethodSelector.select(TrainingCatalog.all(), Skill.WOODCUTTING, AccountMode.F2P, TrainingStyle.BALANCED, 50, maxed()).name().contains("Willow"));
		Assert.assertTrue(MethodSelector.select(TrainingCatalog.all(), Skill.WOODCUTTING, AccountMode.P2P, TrainingStyle.BALANCED, 50, maxed()).name().contains("Teak"));
		Assert.assertTrue(MethodSelector.select(TrainingCatalog.all(), Skill.WOODCUTTING, AccountMode.P2P, TrainingStyle.FAST, 80, maxed()).name().contains("Teak"));
		Assert.assertEquals("Redwoods", MethodSelector.select(TrainingCatalog.all(), Skill.WOODCUTTING, AccountMode.P2P, TrainingStyle.BALANCED, 95, maxed()).name());

		Assert.assertFalse(MethodSelector.select(TrainingCatalog.all(), Skill.MAGIC, AccountMode.P2P, TrainingStyle.FAST, 70, levels(1, 3)).name().contains("Burst"));
		Assert.assertTrue(MethodSelector.select(TrainingCatalog.all(), Skill.MAGIC, AccountMode.P2P, TrainingStyle.FAST, 70, levels(99, 126)).name().contains("Burst"));

		Assert.assertEquals("Turael", MethodSelector.select(TrainingCatalog.all(), Skill.SLAYER, AccountMode.P2P, TrainingStyle.BALANCED, 20, levels(1, 3)).name());
		Assert.assertEquals("Nieve", MethodSelector.select(TrainingCatalog.all(), Skill.SLAYER, AccountMode.P2P, TrainingStyle.FAST, 40, levels(40, 90)).name());
		Assert.assertEquals("Duradel", MethodSelector.select(TrainingCatalog.all(), Skill.SLAYER, AccountMode.P2P, TrainingStyle.FAST, 60, levels(60, 110)).name());
		Assert.assertNull(MethodSelector.select(TrainingCatalog.all(), Skill.SAILING, AccountMode.F2P, TrainingStyle.BALANCED, 20, maxed()));
	}

	private static GuideTick tick(WorldPoint player, boolean bankOpen, boolean inventoryFull)
	{
		return new GuideTick(player, bankOpen, inventoryFull, false, false);
	}

	private static LevelLookup maxed()
	{
		return levels(99, 126);
	}

	private static LevelLookup levels(final int skillLevel, final int combat)
	{
		return new LevelLookup()
		{
			@Override
			public int level(Skill skill)
			{
				return skillLevel;
			}

			@Override
			public int combatLevel()
			{
				return combat;
			}

			@Override
			public boolean questComplete(Quest quest)
			{
				return true;
			}

			@Override
			public boolean allowWilderness()
			{
				return true;
			}
		};
	}
}
