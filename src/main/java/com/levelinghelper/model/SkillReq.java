package com.levelinghelper.model;

import net.runelite.api.Skill;

public final class SkillReq
{
	private final Skill skill;
	private final int level;

	public SkillReq(Skill skill, int level)
	{
		this.skill = skill;
		this.level = level;
	}

	public Skill skill()
	{
		return skill;
	}

	public int level()
	{
		return level;
	}
}
