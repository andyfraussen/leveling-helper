package com.levelinghelper;

import com.levelinghelper.guide.*;
import com.levelinghelper.model.*;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import static org.junit.Assert.*;

public class GuideControllerRegressionTest
{
	private final WorldPoint at = new WorldPoint(100, 100, 0);

	private GuideController guide(GuideStep... steps)
	{
		List<TrainingMethod> methods = new ArrayList<>();
		TrainingMethod.Builder builder = TrainingMethod.builder(methods, Skill.MINING, "Regression")
			.summary("Exercise the route state machine");
		for (GuideStep step : steps) builder.step(step);
		builder.save();
		GuideController controller = new GuideController();
		controller.start(methods.get(0));
		return controller;
	}

	private GuideTick tick(boolean bank, boolean full, boolean gone, boolean prepared)
	{
		return new GuideTick(at, bank, full, gone, false, prepared);
	}

	@Test
	public void droppingBeforeTheFollowingTickDoesNotStall()
	{
		GuideController controller = guide(GuideSteps.train("Mine", at, AdvanceWhen.INVENTORY_FULL), GuideSteps.drop("Drop"));
		assertTrue(controller.tick(tick(false, true, false, true), true, 10));
		assertEquals(1, controller.stepIndex());
		assertTrue(controller.tick(tick(false, false, false, true), true, 10));
		assertEquals(0, controller.stepIndex());
	}

	@Test
	public void hopperRemembersFullInventoryWhenItIsEmptiedBeforeTheNextTick()
	{
		GuideController controller = guide(GuideSteps.train("Mine", at, AdvanceWhen.INVENTORY_FULL), GuideSteps.hopper("Deposit", at));
		controller.tick(tick(false, true, false, true), true, 10);
		assertTrue(controller.tick(tick(false, false, false, true), true, 10));
	}

	@Test
	public void bankRequiresAnObservedOpenAndTheRequiredSupplies()
	{
		GuideController controller = guide(GuideSteps.bank("Withdraw", at), GuideSteps.note("Ready"));
		assertFalse(controller.tick(tick(false, false, false, true), true, 10));
		assertFalse(controller.tick(tick(true, false, false, false), true, 10));
		assertFalse(controller.tick(tick(false, false, false, false), true, 10));
		assertTrue(controller.tick(tick(false, false, false, true), true, 10));
	}

	@Test
	public void missingMaterialsAreNotMistakenForACompletedInventory()
	{
		GuideController controller = guide(GuideSteps.untilEmpty("Process", at, "Grapes"), GuideSteps.note("Finished"));
		assertFalse(controller.tick(tick(false, false, true, true), true, 10));
		controller.observe(tick(false, false, false, true));
		assertTrue(controller.tick(tick(false, false, true, true), true, 10));
	}

	@Test
	public void manualPauseStillObservesBankStateAndBackClearsTheLatch()
	{
		GuideController controller = guide(GuideSteps.bank("Bank", at), GuideSteps.note("Ready"));
		assertFalse(controller.tick(tick(true, false, false, true), false, 10));
		assertTrue(controller.tick(tick(false, false, false, true), true, 10));
		controller.back();
		assertFalse(controller.tick(tick(false, false, false, true), true, 10));
	}

	@Test
	public void arrivalSettingCapsTheRouteRadiusAndHonorsThePlane()
	{
		GuideController controller = guide(GuideSteps.travel("Walk", at, 18), GuideSteps.note("Arrived"));
		assertFalse(controller.tick(new GuideTick(at.dx(8), false, false, false, false), true, 3));
		assertFalse(controller.tick(new GuideTick(at.dz(1), false, false, false, false), true, 3));
		assertTrue(controller.tick(new GuideTick(at.dx(2), false, false, false, false), true, 3));
	}

	@Test
	public void unavailableInventoryCannotArmMaterialCompletionOrFinishDropping()
	{
		GuideController controller = guide(GuideSteps.untilEmpty("Process", at, "Grapes"), GuideSteps.note("Finished"));
		assertFalse(controller.tick(new GuideTick(at, false, false, false, false, true, false), true, 10));
		assertFalse(controller.tick(tick(false, false, true, true), true, 10));
		controller = guide(GuideSteps.train("Mine", at, AdvanceWhen.INVENTORY_FULL), GuideSteps.drop("Drop"));
		controller.tick(tick(false, true, false, true), true, 10);
		assertFalse(controller.tick(new GuideTick(at, false, false, false, false, true, false), true, 10));
		assertTrue(controller.tick(tick(false, false, false, true), true, 10));
	}
}
