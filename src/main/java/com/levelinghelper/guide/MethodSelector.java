package com.levelinghelper.guide;

import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Skill;

public final class MethodSelector
{
	private MethodSelector()
	{
	}

	public static TrainingMethod select(List<TrainingMethod> all, Skill skill, AccountMode mode, TrainingStyle style, int level, LevelLookup lookup)
	{
		List<TrainingMethod> pool = new ArrayList<TrainingMethod>();
		for (TrainingMethod method : all)
		{
			if (method.skill() != skill || !method.modes().contains(mode) || !method.containsLevel(level))
			{
				continue;
			}
			if (!method.requirementsMet(lookup))
			{
				continue;
			}
			pool.add(method);
		}
		TrainingMethod picked = best(pool, style);
		if (picked != null)
		{
			return picked;
		}
		for (TrainingStyle fallback : fallbacks(style))
		{
			picked = best(pool, fallback);
			if (picked != null)
			{
				return picked;
			}
		}
		return null;
	}

	public static List<TrainingMethod> alternatives(List<TrainingMethod> all, Skill skill, AccountMode mode, int level, LevelLookup lookup)
	{
		List<TrainingMethod> pool = new ArrayList<TrainingMethod>();
		for (TrainingMethod method : all)
		{
			if (method.skill() != skill || !method.modes().contains(mode) || !method.containsLevel(level))
			{
				continue;
			}
			if (!method.requirementsMet(lookup))
			{
				continue;
			}
			pool.add(method);
		}
		return pool;
	}

	private static TrainingMethod best(List<TrainingMethod> pool, TrainingStyle style)
	{
		TrainingMethod best = null;
		for (TrainingMethod method : pool)
		{
			if (!method.styles().contains(style))
			{
				continue;
			}
			if (best == null || better(method, best))
			{
				best = method;
			}
		}
		return best;
	}

	private static boolean better(TrainingMethod candidate, TrainingMethod current)
	{
		if (candidate.priority() != current.priority())
		{
			return candidate.priority() > current.priority();
		}
		if (candidate.minLevel() != current.minLevel())
		{
			return candidate.minLevel() > current.minLevel();
		}
		return candidate.xpPerHour() > current.xpPerHour();
	}

	private static TrainingStyle[] fallbacks(TrainingStyle style)
	{
		switch (style)
		{
			case FAST:
				return new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT};
			case AFK:
				return new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.PROFIT, TrainingStyle.FAST};
			case PROFIT:
				return new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.FAST};
			case BALANCED:
			default:
				return new TrainingStyle[]{TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT};
		}
	}
}
