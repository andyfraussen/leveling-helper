package com.levelinghelper.guide;

import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.StepKind;
import com.levelinghelper.model.TrainingMethod;
import net.runelite.api.coords.WorldPoint;

public final class GuideController
{
	private TrainingMethod method;
	private int stepIndex;
	private boolean armed;
	private GuideTick lastTick;

	public void start(TrainingMethod method)
	{
		this.method = method;
		this.stepIndex = 0;
		this.armed = false;
		this.lastTick = null;
	}

	public void stop()
	{
		method = null;
		stepIndex = 0;
		armed = false;
		lastTick = null;
	}

	public boolean running()
	{
		return method != null;
	}

	public TrainingMethod method()
	{
		return method;
	}

	public int stepIndex()
	{
		return stepIndex;
	}

	public GuideStep current()
	{
		if (method == null || method.steps().isEmpty())
		{
			return null;
		}
		return method.steps().get(stepIndex);
	}

	public boolean tick(GuideTick tick, boolean autoAdvance, int defaultRadius)
	{
		observe(tick);
		if (method == null || !autoAdvance)
		{
			return false;
		}
		GuideStep step = current();
		if (step == null)
		{
			return false;
		}
		if (!shouldAdvance(step, tick, defaultRadius))
		{
			return false;
		}
		advance();
		return true;
	}

	/** Observe completion prerequisites even when automatic progression is disabled. */
	public void observe(GuideTick tick)
	{
		lastTick = tick;
		GuideStep step = current();
		if (step == null)
		{
			return;
		}
		switch (step.advance())
		{
			case BANK_CLOSED:
				armed |= tick.bankOpen();
				break;
			case INVENTORY_HAS_SPACE:
			case NEAR_AND_EMPTIED:
				armed |= tick.inventoryFull();
				break;
			case ITEM_GONE:
				armed |= tick.inventoryKnown() && !tick.watchedItemGone();
				break;
			default:
				break;
		}
	}

	public void advance()
	{
		if (method == null || method.steps().isEmpty())
		{
			return;
		}
		armed = false;
		int next = stepIndex + 1;
		if (next >= method.steps().size())
		{
			next = Math.max(0, Math.min(method.loopFrom(), method.steps().size() - 1));
		}
		stepIndex = next;
		// The full inventory that completed training also arms the following drop/hopper step.
		if (lastTick != null && lastTick.inventoryFull()
			&& (current().advance() == com.levelinghelper.model.AdvanceWhen.INVENTORY_HAS_SPACE
				|| current().advance() == com.levelinghelper.model.AdvanceWhen.NEAR_AND_EMPTIED))
		{
			armed = true;
		}
	}

	public void back()
	{
		if (method == null)
		{
			return;
		}
		armed = false;
		if (stepIndex > 0)
		{
			stepIndex--;
		}
	}

	public void jumpToBank()
	{
		if (method == null)
		{
			return;
		}
		for (int i = 0; i < method.steps().size(); i++)
		{
			if (method.steps().get(i).kind() == StepKind.BANK)
			{
				stepIndex = i;
				armed = false;
				return;
			}
		}
	}

	private boolean shouldAdvance(GuideStep step, GuideTick tick, int defaultRadius)
	{
		switch (step.advance())
		{
			case NEAR_TARGET:
				return near(tick.player(), step.target(), step.arrivalRadius(defaultRadius));
			case BANK_CLOSED:
				if (tick.bankOpen())
				{
					armed = true;
					return false;
				}
				return armed && tick.prepared() && tick.inventoryKnown();
			case INVENTORY_FULL:
				return tick.inventoryFull();
			case INVENTORY_HAS_SPACE:
				if (!tick.inventoryKnown())
				{
					return false;
				}
				if (tick.inventoryFull())
				{
					armed = true;
					return false;
				}
				return armed;
			case NEAR_AND_EMPTIED:
				if (tick.inventoryFull())
				{
					armed = true;
				}
				return armed
					&& tick.inventoryKnown()
					&& !tick.inventoryFull()
					&& near(tick.player(), step.target(), step.arrivalRadius(defaultRadius));
			case ITEM_GONE:
				return armed && tick.inventoryKnown() && tick.watchedItemGone();
			case HIGHLIGHT_VISIBLE:
				return tick.highlightVisible();
			case MANUAL:
			default:
				return false;
		}
	}

	static boolean near(WorldPoint player, WorldPoint target, int radius)
	{
		if (player == null || target == null)
		{
			return false;
		}
		if (player.getPlane() != target.getPlane())
		{
			return false;
		}
		int dx = Math.abs(player.getX() - target.getX());
		int dy = Math.abs(player.getY() - target.getY());
		return Math.max(dx, dy) <= radius;
	}
}
