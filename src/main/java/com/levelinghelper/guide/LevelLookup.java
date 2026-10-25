package com.levelinghelper.guide;

import com.levelinghelper.model.ItemCategory;
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
	default boolean matches(ItemCategory category, int itemId) { return false; }
	default int extraItemCount(int itemId) { return 0; }
	default boolean providerUsable(int id) { return true; }
	default int storedItemCount(int id) { return 0; }
	default int itemCount(int id) { return 0; }
	default int equippedCount(int id) { return 0; }
	default boolean membersWorld() { return false; }
	default boolean equippedMatches(ItemCategory category) { return false; }
	default int varbit(int id) { return 0; }

}
