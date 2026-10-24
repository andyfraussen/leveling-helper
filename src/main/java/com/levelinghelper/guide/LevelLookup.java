package com.levelinghelper.guide;

import net.runelite.api.Quest;
import net.runelite.api.Skill;

public interface LevelLookup
{
	int level(Skill skill);

	int combatLevel();

	default boolean questComplete(Quest quest)
	{
		return false;
	}

	default boolean allowWilderness()
	{
		return false;
	}
}
