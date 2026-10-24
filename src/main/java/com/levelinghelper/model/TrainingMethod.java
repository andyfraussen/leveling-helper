package com.levelinghelper.model;

import com.levelinghelper.guide.LevelLookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.runelite.api.Skill;

public final class TrainingMethod
{
	private final String id;
	private final Skill skill;
	private final String name;
	private final EnumSet<AccountMode> modes;
	private final EnumSet<TrainingStyle> styles;
	private final int minLevel;
	private final int maxLevel;
	private final int priority;
	private final int combatRequirement;
	private final List<SkillReq> requirements;
	private final double xpEach;
	private final int xpPerHour;
	private final String rateNote;
	private final String summary;
	private final String wikiTitle;
	private final List<String> optimizations;
	private final List<GuideStep> steps;
	private final int loopFrom;

	private TrainingMethod(
		String id,
		Skill skill,
		String name,
		EnumSet<AccountMode> modes,
		EnumSet<TrainingStyle> styles,
		int minLevel,
		int maxLevel,
		int priority,
		int combatRequirement,
		List<SkillReq> requirements,
		double xpEach,
		int xpPerHour,
		String rateNote,
		String summary,
		String wikiTitle,
		List<String> optimizations,
		List<GuideStep> steps,
		int loopFrom)
	{
		this.id = id;
		this.skill = skill;
		this.name = name;
		this.modes = modes;
		this.styles = styles;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.priority = priority;
		this.combatRequirement = combatRequirement;
		this.requirements = requirements;
		this.xpEach = xpEach;
		this.xpPerHour = xpPerHour;
		this.rateNote = rateNote;
		this.summary = summary;
		this.wikiTitle = wikiTitle;
		this.optimizations = optimizations;
		this.steps = steps;
		this.loopFrom = loopFrom;
	}

	public String id()
	{
		return id;
	}

	public Skill skill()
	{
		return skill;
	}

	public String name()
	{
		return name;
	}

	public EnumSet<AccountMode> modes()
	{
		return modes;
	}

	public EnumSet<TrainingStyle> styles()
	{
		return styles;
	}

	public int minLevel()
	{
		return minLevel;
	}

	public int maxLevel()
	{
		return maxLevel;
	}

	public int priority()
	{
		return priority;
	}

	public int combatRequirement()
	{
		return combatRequirement;
	}

	public List<SkillReq> requirements()
	{
		return requirements;
	}

	public double xpEach()
	{
		return xpEach;
	}

	public int xpPerHour()
	{
		return xpPerHour;
	}

	public String rateNote()
	{
		return rateNote;
	}

	public String summary()
	{
		return summary;
	}

	public String wikiTitle()
	{
		return wikiTitle;
	}

	public List<String> optimizations()
	{
		return optimizations;
	}

	public List<GuideStep> steps()
	{
		return steps;
	}

	public int loopFrom()
	{
		return loopFrom;
	}

	public boolean containsLevel(int level)
	{
		return level >= minLevel && level <= maxLevel;
	}

	public boolean requirementsMet(LevelLookup lookup)
	{
		if (lookup == null)
		{
			return true;
		}
		if (combatRequirement > 0 && lookup.combatLevel() < combatRequirement)
		{
			return false;
		}
		for (SkillReq requirement : requirements)
		{
			if (lookup.level(requirement.skill()) < requirement.level())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString()
	{
		return name + "  " + minLevel + "-" + maxLevel;
	}

	public String styleLabel()
	{
		StringBuilder builder = new StringBuilder();
		for (TrainingStyle style : TrainingStyle.values())
		{
			if (!styles.contains(style))
			{
				continue;
			}
			if (builder.length() > 0)
			{
				builder.append(", ");
			}
			builder.append(style);
		}
		return builder.toString();
	}

	public static Builder builder(List<TrainingMethod> dest, Skill skill, String name)
	{
		return new Builder(dest, skill, name);
	}

	public static final class Builder
	{
		private final List<TrainingMethod> dest;
		private final Skill skill;
		private final String name;
		private EnumSet<AccountMode> modes;
		private EnumSet<TrainingStyle> styles;
		private int min = 1;
		private int max = 99;
		private int priority;
		private int combat;
		private final List<SkillReq> reqs = new ArrayList<SkillReq>();
		private double xpEach;
		private int xpPerHour = 10000;
		private String note = "Typical rate for this level range in ordinary gear, without tick manipulation. Your session rate is the one that matters.";
		private String summary = "";
		private String wiki;
		private final List<String> opts = new ArrayList<String>();
		private final List<GuideStep> steps = new ArrayList<GuideStep>();
		private int loop;

		private Builder(List<TrainingMethod> dest, Skill skill, String name)
		{
			this.dest = dest;
			this.skill = skill;
			this.name = name;
		}

		public Builder levels(int min, int max)
		{
			this.min = min;
			this.max = max;
			return this;
		}

		public Builder modes(AccountMode... modes)
		{
			this.modes = EnumSet.copyOf(Arrays.asList(modes));
			return this;
		}

		public Builder styles(TrainingStyle... styles)
		{
			this.styles = EnumSet.copyOf(Arrays.asList(styles));
			return this;
		}

		public Builder xp(double each, int perHour)
		{
			this.xpEach = each;
			this.xpPerHour = perHour;
			return this;
		}

		public Builder note(String note)
		{
			this.note = note;
			return this;
		}

		public Builder summary(String summary)
		{
			this.summary = summary;
			return this;
		}

		public Builder wiki(String wiki)
		{
			this.wiki = wiki;
			return this;
		}

		public Builder opt(String opt)
		{
			opts.add(opt);
			return this;
		}

		public Builder priority(int priority)
		{
			this.priority = priority;
			return this;
		}

		public Builder combat(int combat)
		{
			this.combat = combat;
			return this;
		}

		public Builder req(Skill skill, int level)
		{
			reqs.add(new SkillReq(skill, level));
			return this;
		}

		public Builder loop(int index)
		{
			this.loop = index;
			return this;
		}

		public Builder step(GuideStep step)
		{
			steps.add(step);
			return this;
		}

		public void save()
		{
			EnumSet<AccountMode> modeSet = modes == null ? EnumSet.of(AccountMode.P2P) : EnumSet.copyOf(modes);
			EnumSet<TrainingStyle> styleSet = styles == null ? EnumSet.of(TrainingStyle.BALANCED) : EnumSet.copyOf(styles);
			int loopIndex = steps.isEmpty() ? 0 : Math.max(0, Math.min(loop, steps.size() - 1));
			dest.add(new TrainingMethod(
				skill.name() + ":" + modeSet + ":" + min + "-" + max + ":" + name,
				skill,
				name,
				modeSet,
				styleSet,
				min,
				max,
				priority,
				combat,
				Collections.unmodifiableList(new ArrayList<SkillReq>(reqs)),
				xpEach,
				xpPerHour,
				note,
				summary,
				wiki,
				Collections.unmodifiableList(new ArrayList<String>(opts)),
				Collections.unmodifiableList(new ArrayList<GuideStep>(steps)),
				loopIndex));
		}
	}
}
