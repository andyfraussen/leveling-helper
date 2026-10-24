package com.levelinghelper.guide;

import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;

public final class GuideSnapshot
{
	private final boolean loggedIn;
	private final boolean membersWorld;
	private final Skill skill;
	private final AccountMode mode;
	private final TrainingStyle style;
	private final int goal;
	private final int level;
	private final int xp;
	private final boolean running;
	private final TrainingMethod method;
	private final int stepIndex;
	private final int sessionXp;
	private final double sessionPerHour;
	private final String distanceText;
	private final TrainingMethod upcoming;
	private final List<TrainingMethod> alternatives;
	private final String status;

	public GuideSnapshot(
		boolean loggedIn,
		boolean membersWorld,
		Skill skill,
		AccountMode mode,
		TrainingStyle style,
		int goal,
		int level,
		int xp,
		boolean running,
		TrainingMethod method,
		int stepIndex,
		int sessionXp,
		double sessionPerHour,
		String distanceText,
		TrainingMethod upcoming,
		List<TrainingMethod> alternatives,
		String status)
	{
		this.loggedIn = loggedIn;
		this.membersWorld = membersWorld;
		this.skill = skill;
		this.mode = mode;
		this.style = style;
		this.goal = goal;
		this.level = level;
		this.xp = xp;
		this.running = running;
		this.method = method;
		this.stepIndex = stepIndex;
		this.sessionXp = sessionXp;
		this.sessionPerHour = sessionPerHour;
		this.distanceText = distanceText;
		this.upcoming = upcoming;
		this.alternatives = alternatives == null ? Collections.<TrainingMethod>emptyList() : alternatives;
		this.status = status;
	}

	public boolean loggedIn()
	{
		return loggedIn;
	}

	public boolean membersWorld()
	{
		return membersWorld;
	}

	public Skill skill()
	{
		return skill;
	}

	public AccountMode mode()
	{
		return mode;
	}

	public TrainingStyle style()
	{
		return style;
	}

	public int goal()
	{
		return goal;
	}

	public int level()
	{
		return level;
	}

	public int xp()
	{
		return xp;
	}

	public boolean running()
	{
		return running;
	}

	public TrainingMethod method()
	{
		return method;
	}

	public int stepIndex()
	{
		return stepIndex;
	}

	public int sessionXp()
	{
		return sessionXp;
	}

	public double sessionPerHour()
	{
		return sessionPerHour;
	}

	public String distanceText()
	{
		return distanceText;
	}

	public TrainingMethod upcoming()
	{
		return upcoming;
	}

	public List<TrainingMethod> alternatives()
	{
		return alternatives;
	}

	public String status()
	{
		return status;
	}

	public boolean atGoal()
	{
		return loggedIn && level >= goal;
	}
}
