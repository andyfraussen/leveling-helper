package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.*;
import com.levelinghelper.model.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import net.runelite.api.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CatalogRequirementsTest
{
	private static final class Profile implements LevelLookup
	{
		final EnumSet<Quest> quests = EnumSet.noneOf(Quest.class);
		boolean wilderness;
		public int level(Skill skill) { return 99; }
		public int combatLevel() { return 126; }
		public boolean questComplete(Quest quest) { return quests.contains(quest); }
		public boolean allowWilderness() { return wilderness; }
	}

	private TrainingMethod select(Skill skill, TrainingStyle style, int level, Profile profile)
	{
		return MethodSelector.select(TrainingCatalog.all(), skill, AccountMode.P2P, style, level, profile);
	}

	@Test
	public void questLockedMagicFallsBackAndIceBurstRequiresLevel70()
	{
		Profile profile = new Profile();
		assertEquals("Combat spells", select(Skill.MAGIC, TrainingStyle.FAST, 70, profile).name());
		profile.quests.add(Quest.DESERT_TREASURE_I);
		assertEquals("Combat spells", select(Skill.MAGIC, TrainingStyle.FAST, 69, profile).name());
		assertEquals("Ice Burst", select(Skill.MAGIC, TrainingStyle.FAST, 70, profile).name());
		TrainingMethod burst = select(Skill.MAGIC, TrainingStyle.FAST, 70, profile);
		ItemRequirement mask = burst.items().get(0);
		assertTrue(mask.equipped());
		assertTrue(mask.met(id -> id == ItemID.FACEMASK ? 1 : 0));
		assertTrue(mask.met(id -> id == ItemID.ARAXYTE_SLAYER_HELMET_I_29822 ? 1 : 0));
		assertFalse(mask.met(id -> id == ItemID.BLACK_MASK ? 1 : 0));
	}

	@Test
	public void wildernessNeedsExplicitOptInAcrossEveryAffectedSkill()
	{
		Profile profile = new Profile();
		for (Skill skill : new Skill[]{Skill.PRAYER, Skill.HUNTER, Skill.AGILITY})
		{
			TrainingMethod method = select(skill, TrainingStyle.FAST, 60, profile);
			assertNotNull(method);
			assertNotEquals(RouteRisk.WILDERNESS, method.risk());
		}
		profile.wilderness = true;
		assertEquals("Chaos altar", select(Skill.PRAYER, TrainingStyle.FAST, 60, profile).name());
		assertEquals("Wilderness course", select(Skill.AGILITY, TrainingStyle.FAST, 60, profile).name());
		assertEquals("Black chinchompas", select(Skill.HUNTER, TrainingStyle.FAST, 80, profile).name());
	}

	@Test
	public void resourceTargetsDistinguishOreTypesAndDepletedRocks()
	{
		GuideStep iron = select(Skill.MINING, TrainingStyle.FAST, 20, new Profile()).steps().get(2);
		assertTrue(iron.matchesObject(ObjectID.IRON_ROCKS, "Rocks"));
		assertFalse(iron.matchesObject(ObjectID.COPPER_ROCKS_11161, "Rocks"));
		assertFalse(iron.matchesObject(ObjectID.ROCKS_11390, "Rocks"));
		assertFalse(iron.matchesNpc(1, "Rocks"));
	}

	@Test
	public void anglerfishUseTheActualNpcAndBarbarianFishingRequiresBait()
	{
		Profile profile = new Profile();
		TrainingMethod angler = select(Skill.FISHING, TrainingStyle.PROFIT, 82, profile);
		assertEquals("Anglerfish", angler.name());
		assertTrue(angler.summary().contains("Port Piscarilius"));
		assertTrue(angler.steps().get(2).matchesNpc(6825, "Rod Fishing spot"));
		assertFalse(angler.steps().get(2).matchesNpc(1510, "Fishing spot"));
		TrainingMethod barb = select(Skill.FISHING, TrainingStyle.BALANCED, 48, profile);
		assertEquals("Barbarian fishing", barb.name());
		assertTrue(barb.items().stream().anyMatch(item -> item.met(id -> id == ItemID.FEATHER ? 100 : 0)));
	}

	@Test
	public void unlockedProfilesCoverAllStylesAndLevelsWithRealisticPrimaryLevels()
	{
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL) continue;
			for (int level = 1; level <= 99; level++)
			{
				final int primaryLevel = level;
				LevelLookup profile = new LevelLookup()
				{
					public int level(Skill queried) { return queried == skill ? primaryLevel : 99; }
					public int combatLevel() { return 126; }
					public boolean questComplete(Quest quest) { return true; }
				};
				for (TrainingStyle style : TrainingStyle.values())
				{
					TrainingMethod method = MethodSelector.select(TrainingCatalog.all(), skill, AccountMode.P2P, style, level, profile);
					// Quest rewards make some pre-unlock levels unattainable on an unlocked account.
					if (skill == Skill.HERBLORE && level < 3 || skill == Skill.SAILING && level < 2) continue;
					assertNotNull(skill + " " + style + " " + level, method);
					assertTrue(method.requirementsMet(profile));
				}
			}
		}
	}

	@Test
	public void targetNamesAreLocaleIndependentAndCatalogSetsCannotBeMutated()
	{
		Locale previous = Locale.getDefault();
		try
		{
			Locale.setDefault(Locale.forLanguageTag("tr-TR"));
			GuideStep step = GuideSteps.roam("Fish", AdvanceWhen.MANUAL, "FISHING SPOT");
			assertTrue(step.matchesNpc(1, "Fishing spot"));
		}
		finally { Locale.setDefault(previous); }
		TrainingMethod method = TrainingCatalog.all().get(0);
		method.modes().clear();
		assertFalse(method.modes().isEmpty());
	}
}
