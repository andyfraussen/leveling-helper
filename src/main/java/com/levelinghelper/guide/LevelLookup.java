package com.levelinghelper.guide;

import net.runelite.api.Skill;

public interface LevelLookup
{
	int level(Skill skill);

	int combatLevel();
}
