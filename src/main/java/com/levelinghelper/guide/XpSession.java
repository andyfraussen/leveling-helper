package com.levelinghelper.guide;

import net.runelite.api.Skill;

public final class XpSession
{
	private Skill skill;
	private int startXp;
	private int lastXp;
	private int gained;
	private long startMs;

	public void start(Skill skill, int currentXp)
	{
		this.skill = skill;
		this.startXp = currentXp;
		this.lastXp = currentXp;
		this.gained = 0;
		this.startMs = System.currentTimeMillis();
	}

	public void stop()
	{
		skill = null;
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
	}

	public double xpPerHour()
	{
		if (skill == null || gained <= 0)
		{
			return 0;
		}
		long elapsed = System.currentTimeMillis() - startMs;
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
