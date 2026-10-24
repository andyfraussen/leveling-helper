package com.levelinghelper.guide;

import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.HighlightMode;
import com.levelinghelper.model.StepKind;
import net.runelite.api.coords.WorldPoint;

public final class GuideSteps
{
	private static final String[] BANK_NAMES = {"banker", "bank booth", "bank chest", "deposit box"};

	private GuideSteps()
	{
	}

	public static GuideStep bank(String instruction, WorldPoint at)
	{
		return new GuideStep(
			instruction,
			StepKind.BANK,
			at,
			12,
			16,
			AdvanceWhen.BANK_CLOSED,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(BANK_NAMES),
			null);
	}

	public static GuideStep travel(String instruction, WorldPoint at, int radius, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAVEL,
			at,
			radius,
			Math.max(radius, 18),
			AdvanceWhen.NEAR_TARGET,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(names),
			null);
	}

	public static GuideStep train(String instruction, WorldPoint at, AdvanceWhen when, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAIN,
			at,
			12,
			20,
			when,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(names),
			null);
	}

	public static GuideStep trainWide(String instruction, WorldPoint at, int radius, AdvanceWhen when, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAIN,
			at,
			radius,
			radius,
			when,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(names),
			null);
	}

	public static GuideStep roam(String instruction, AdvanceWhen when, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAIN,
			null,
			0,
			0,
			when,
			HighlightMode.ANYWHERE,
			GuideStep.namesOf(names),
			null);
	}

	public static GuideStep untilEmpty(String instruction, WorldPoint at, String itemName, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAIN,
			at,
			12,
			18,
			AdvanceWhen.ITEM_GONE,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(names),
			itemName);
	}

	public static GuideStep drop(String instruction)
	{
		return new GuideStep(
			instruction,
			StepKind.DROP,
			null,
			0,
			0,
			AdvanceWhen.INVENTORY_HAS_SPACE,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(),
			null);
	}

	public static GuideStep hopper(String instruction, WorldPoint at, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAIN,
			at,
			10,
			16,
			AdvanceWhen.NEAR_AND_EMPTIED,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(names),
			null);
	}

	public static GuideStep note(String instruction)
	{
		return new GuideStep(
			instruction,
			StepKind.NOTE,
			null,
			0,
			0,
			AdvanceWhen.MANUAL,
			HighlightMode.NEAR_TARGET,
			GuideStep.namesOf(),
			null);
	}

	public static GuideStep find(String instruction, String... names)
	{
		return new GuideStep(
			instruction,
			StepKind.TRAVEL,
			null,
			0,
			0,
			AdvanceWhen.HIGHLIGHT_VISIBLE,
			HighlightMode.ANYWHERE,
			GuideStep.namesOf(names),
			null);
	}
}
