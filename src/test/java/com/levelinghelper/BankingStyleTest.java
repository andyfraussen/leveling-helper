package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.guide.MethodSelector;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.StepKind;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class BankingStyleTest
{
	private final LevelLookup profile = new LevelLookup()
	{
		public int level(Skill skill) { return 99; }
		public int combatLevel() { return 126; }
		public boolean questComplete(Quest quest) { return true; }
	};

	private TrainingMethod iron(AccountMode mode, TrainingStyle style, int level)
	{
		return MethodSelector.alternatives(TrainingCatalog.all(), Skill.MINING, mode, style, level, profile)
			.stream().filter(method -> method.name().equals("Iron")).findFirst().get();
	}

	@Test
	public void profitStillRecommendsCoalButManuallySelectedIronBanks()
	{
		assertEquals("Coal", MethodSelector.select(TrainingCatalog.all(), Skill.MINING,
			AccountMode.F2P, TrainingStyle.PROFIT, 35, profile).name());
		TrainingMethod iron = iron(AccountMode.F2P, TrainingStyle.PROFIT, 35);
		assertEquals(0, iron.loopFrom());
		assertEquals(3, iron.steps().size());
		assertEquals(StepKind.BANK, iron.steps().get(0).kind());
		assertFalse(iron.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
		assertFalse(iron.summary().toLowerCase().contains("drop"));
		assertTrue(iron.toString().contains("banking"));
		assertEquals(0, iron.xpPerHour()); // No power-mining rate claimed for the bank loop.
		assertSame(iron, TrainingCatalog.byId(iron.id()).forStyle(TrainingStyle.PROFIT));
	}

	@Test
	public void bankingVariantCanResolveBackToTheXpRouteWithoutMutatingTheCatalog()
	{
		TrainingMethod banking = iron(AccountMode.F2P, TrainingStyle.PROFIT, 35);
		TrainingMethod dropping = banking.forStyle(TrainingStyle.FAST);
		assertNotEquals(banking.id(), dropping.id());
		assertEquals(1, dropping.loopFrom());
		assertTrue(dropping.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
		assertSame(banking, dropping.forStyle(TrainingStyle.PROFIT));
		assertTrue(dropping.xpPerHour() > 0);
	}

	@Test
	public void earlyProfitRecommendationsAndMemberOverridesAlsoBankIron()
	{
		TrainingMethod early = MethodSelector.select(TrainingCatalog.all(), Skill.MINING,
			AccountMode.F2P, TrainingStyle.PROFIT, 20, profile);
		assertEquals("Iron", early.name());
		assertEquals(0, early.loopFrom());
		assertFalse(early.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
		assertFalse(iron(AccountMode.P2P, TrainingStyle.PROFIT, 35).steps()
			.stream().anyMatch(step -> step.kind() == StepKind.DROP));
	}

	@Test
	public void manuallyChoosingFlyFishingUnderProfitKeepsTheFish()
	{
		TrainingMethod fishing = MethodSelector.alternatives(TrainingCatalog.all(), Skill.FISHING,
			AccountMode.F2P, TrainingStyle.PROFIT, 45, profile).stream()
			.filter(method -> method.name().equals("Trout and salmon")).findFirst().get();
		assertEquals(0, fishing.loopFrom());
		assertFalse(fishing.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
		assertTrue(fishing.steps().get(0).instruction().contains("Deposit the fish"));
	}
}
