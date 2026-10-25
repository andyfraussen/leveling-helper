package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.*;
import com.levelinghelper.model.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class CatalogCoverageTest
{
	private LevelLookup profile(int level, AccountMode mode)
	{
		return new LevelLookup()
		{
			public int level(Skill skill) { return level; }
			public int combatLevel() { return 126; }
			public boolean questComplete(Quest quest) { return true; }
			public boolean allowWilderness() { return true; }
			public boolean membersWorld() { return mode == AccountMode.P2P; }
		};
	}

	@Test
	public void everyCatalogRouteHasExplicitPreparationAndInventoryPolicy()
	{
		assertEquals(197, TrainingCatalog.all().size());
		assertEquals(24, TrainingCatalog.all().stream().map(TrainingMethod::skill).distinct().count());
		for (TrainingMethod method : TrainingCatalog.all())
		{
			assertTrue(method.id(), method.preparationReviewed());
			assertNotNull(method.id(), method.resourceHandling());
			assertTrue(method.id(), method.preparationFreeSlots() >= 0);
			List<ItemRequirement> inputs = method.resolvedItems(profile(method.minLevel(), AccountMode.P2P));
			assertTrue(method.id(), !inputs.isEmpty() || !method.noItemsReason().isEmpty());
			if (inputs.stream().noneMatch(item -> !item.optional()))
			{
				assertTrue(method.id(), !method.manualChecks().isEmpty() || !method.noItemsReason().isEmpty());
			}
			for (ItemRequirement item : inputs)
			{
				assertFalse(method.id() + item.label(), item.source().isEmpty());
				assertTrue(method.id(), item.quantity() > 0);
			}
			for (TrainingStyle style : TrainingStyle.values())
			{
				TrainingMethod variant = method.forStyle(style);
				assertTrue(variant.id(), variant.preparationReviewed());
				assertEquals(method.manualChecks(), variant.manualChecks());
				assertTrue(variant.id(), variant.loopFrom() >= 0 && variant.loopFrom() < variant.steps().size());
				if (style == TrainingStyle.PROFIT)
				{
					assertFalse(variant.id(), variant.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
					assertFalse(variant.id(), variant.steps().stream().anyMatch(step -> step.instruction().toLowerCase().matches(".*\\bdrop (?:the |your |leftover )?(?:ore|fish|resources|inventory)\\b.*")));
				}
				if (variant.resourceHandling() == ResourceHandling.BANK)
				{
					assertEquals(variant.id(), StepKind.BANK, variant.steps().get(variant.loopFrom()).kind());
					assertTrue(variant.id(), variant.preparationFreeSlots() > 0);
					assertTrue(variant.id(), variant.steps().stream().anyMatch(step -> step.kind() == StepKind.TRAIN && step.advance() == AdvanceWhen.INVENTORY_FULL));
				}
			}
		}
	}

	@Test
	public void everySkillModeStyleAndLevelPreservesEligibilityAndBanking()
	{
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL) continue;
			for (AccountMode mode : AccountMode.values()) for (TrainingStyle style : TrainingStyle.values()) for (int level = 1; level <= 99; level++)
			{
				LevelLookup lookup = profile(level, mode);
				TrainingMethod selected = MethodSelector.select(TrainingCatalog.all(), skill, mode, style, level, lookup);
				List<TrainingMethod> alternatives = MethodSelector.alternatives(TrainingCatalog.all(), skill, mode, style, level, lookup);
				if (selected != null) assertTrue(selected.id(), alternatives.contains(selected));
				for (TrainingMethod method : alternatives)
				{
					assertEquals(method.id(), skill, method.skill());
					assertTrue(method.id(), method.modes().contains(mode));
					assertTrue(method.id(), method.containsLevel(level));
					assertTrue(method.id(), method.requirementsMet(lookup));
					assertFalse(method.id(), mode == AccountMode.F2P && method.skill().isMembers());
					if (style == TrainingStyle.PROFIT) assertFalse(method.id(), method.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP));
				}
			}
		}
	}

	@Test
	public void noDeclaredRequiredInputIsInventedFromAnEmptyInventoryOrUnknownBank()
	{
		SupplyTracker tracker = new SupplyTracker();
		for (TrainingMethod method : TrainingCatalog.all()) for (TrainingStyle style : TrainingStyle.values())
		{
			for (SupplyTracker.Stock stock : tracker.check(method.forStyle(style), SupplyTrackerTest.container(), SupplyTrackerTest.container(), profile(99, AccountMode.P2P), true))
			{
				assertFalse(method.id() + stock.requirement.label(), stock.ready());
				assertFalse(method.id(), stock.bankKnown);
				assertFalse(method.id(), stock.absentFromBank());
			}
		}
	}

	@Test
	public void fullInventoryBankLoopsWaitForSpaceAndReturnToTheSelectedRoute()
	{
		for (TrainingMethod source : TrainingCatalog.all()) for (TrainingStyle style : TrainingStyle.values())
		{
			TrainingMethod method = source.forStyle(style);
			if (method.resourceHandling() != ResourceHandling.BANK) continue;
			GuideController controller = new GuideController();
			controller.start(method);
			while (controller.current().kind() != StepKind.TRAIN) controller.advance();
			controller.tick(new GuideTick(null, false, true, false, false, true, true), true, 10);
			assertEquals(method.id(), StepKind.BANK, controller.current().kind());
			controller.tick(new GuideTick(null, true, true, false, false, false, true), true, 10);
			controller.tick(new GuideTick(null, false, true, false, false, false, true), true, 10);
			assertEquals(method.id(), StepKind.BANK, controller.current().kind());
			assertTrue(method.id(), controller.tick(new GuideTick(null, false, false, false, false, true, true), true, 10));
			assertSame(method, controller.method());
			assertNotEquals(method.id(), StepKind.BANK, controller.current().kind());
		}
	}

	@Test
	public void writeRouteMatrix() throws Exception
	{
		List<String> rows = new ArrayList<>();
		rows.add("skill\tname\tmodes\tlevels\titems\tmanual checks\thandling\tsteps");
		for (TrainingMethod method : TrainingCatalog.all())
		{
			rows.add(method.skill() + "\t" + method.name() + "\t" + method.modes() + "\t" + method.minLevel() + "-" + method.maxLevel()
				+ "\t" + method.resolvedItems(profile(method.minLevel(), AccountMode.P2P)).stream()
					.map(item -> item.label() + (item.optional() ? " (optional)" : item.departureOnly() ? " (first trip)" : ""))
					.collect(Collectors.joining("; "))
				+ "\t" + String.join("; ", method.manualChecks()) + "\t" + method.resourceHandling()
				+ "\t" + method.steps().stream().map(step -> step.kind() + ":" + step.advance()).collect(Collectors.joining("; ")));
		}
		Files.createDirectories(Path.of("build/reports"));
		Files.write(Path.of("build/reports/catalog-coverage.tsv"), rows);
	}
}
