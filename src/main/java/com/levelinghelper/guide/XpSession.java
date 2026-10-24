package com.levelinghelper.guide;

import java.util.function.LongSupplier;
import net.runelite.api.Skill;

public final class XpSession
{
	private Skill skill;
	private int startXp;
	private int lastXp;
	private int gained;
	private long startMs;
	private final LongSupplier clock;

	public XpSession()
	{
		this(() -> System.nanoTime() / 1_000_000L);
	}

	public XpSession(LongSupplier clock)
	{
		this.clock = clock;
	}

	public void start(Skill skill, int currentXp)
	{
		this.skill = skill;
		this.startXp = currentXp;
		this.lastXp = currentXp;
		this.gained = 0;
		this.startMs = clock.getAsLong();
	}

	public void stop()
	{
		skill = null;
		gained = 0;
		startXp = 0;
		lastXp = 0;
	}

	public boolean active()
	{
		return skill != null;
	}

	public Skill skill()
	{
		return skill;
	}

	public int gained()
	{
		return gained;
	}

	public void onXp(Skill changed, int xp)
	{
		if (skill == null || changed != skill)
		{
			return;
		}
		if (xp > lastXp)
		{
			gained += xp - lastXp;
			lastXp = xp;
		}
		else if (xp < lastXp)
		{
			// An account change or XP reset must never be counted as negative training.
			start(changed, xp);
		}
	}

	public double xpPerHour()
	{
		if (skill == null || gained <= 0)
		{
			return 0;
		}
		long elapsed = clock.getAsLong() - startMs;
		if (elapsed < 15_000L)
		{
			return 0;
		}
		return gained * 3_600_000.0 / elapsed;
	}

	public int startXp()
	{
		return startXp;
	}
}
