package com.levelinghelper.model;

import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.guide.GuideSteps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.runelite.api.Quest;
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
	private final List<Quest> quests;
	private final List<ItemRequirement> items;
	private final List<String> preparation;
	private final RouteRisk risk;
	private final int preparationFreeSlots;
	private TrainingMethod bankingVariant;
	private TrainingMethod sourceMethod;
	private boolean preparationReviewed;
	private List<String> manualChecks = Collections.emptyList();
	private ResourceHandling resourceHandling;
	private String noItemsReason = "";
	private String localRestock;
	private java.util.function.Function<LevelLookup, List<ItemRequirement>> resolveItems = lookup -> Collections.emptyList();
	private java.util.function.Function<LevelLookup, List<String>> setupIssues = lookup -> Collections.emptyList();

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
		int loopFrom,
		List<Quest> quests,
		List<ItemRequirement> items,
		List<String> preparation,
		RouteRisk risk, int preparationFreeSlots)
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
		this.quests = List.copyOf(quests);
		this.items = List.copyOf(items);
		this.preparation = List.copyOf(preparation);
		this.risk = risk;
		this.preparationFreeSlots = preparationFreeSlots;
	}

	private TrainingMethod(TrainingMethod source, String summary, List<GuideStep> bankingSteps)
	{
		this(source.id + ":bank", source.skill, source.name, source.modes, source.styles,
			source.minLevel, source.maxLevel, source.priority, source.combatRequirement, source.requirements,
			source.xpEach, 0,
			"Bank trips reduce the training rate. Your session rate includes time spent travelling and banking.",
			summary, source.wikiTitle, Collections.singletonList("Keep the resources and deposit them at the highlighted bank when full."),
			List.copyOf(bankingSteps), 0, source.quests, source.items, source.preparation, source.risk, source.preparationFreeSlots);
		this.sourceMethod = source;
		this.preparationReviewed = source.preparationReviewed;
		this.manualChecks = source.manualChecks;
		this.resourceHandling = ResourceHandling.BANK;
		this.noItemsReason = source.noItemsReason;
		this.localRestock = source.localRestock;
		this.resolveItems = source.resolveItems;
		this.setupIssues = source.setupIssues;
	}

	public List<ItemRequirement> resolvedItems(LevelLookup lookup)
	{
		List<ItemRequirement> all = new ArrayList<>(items);
		all.addAll(resolveItems.apply(lookup));
		return List.copyOf(all);
	}

	public List<String> setupIssues(LevelLookup lookup) { return List.copyOf(setupIssues.apply(lookup)); }
	public boolean preparationReviewed() { return preparationReviewed; }
	public List<String> manualChecks() { return manualChecks; }
	public ResourceHandling resourceHandling() { return resourceHandling; }
	public String noItemsReason() { return noItemsReason; }
	public String localRestock() { return localRestock; }

	public TrainingMethod reviewed(PreparationPlan plan)
	{
		List<ItemRequirement> inputs = new ArrayList<>(items);
		for (ItemRequirement input : plan.items)
		{
			if (inputs.stream().noneMatch(existing -> existing.label().equals(input.label()))) inputs.add(input);
		}
		if (inputs.isEmpty() && plan.noItemsReason.isEmpty() && !plan.dynamicItems)
		{
			throw new IllegalArgumentException("Unreviewed item preparation: " + id);
		}
		List<GuideStep> route = new ArrayList<>(steps);
		int loop = loopFrom;
		boolean needsBank = inputs.stream().anyMatch(item -> !item.optional())
			|| plan.dynamicItems;
		if (needsBank && route.stream().noneMatch(step -> step.kind() == StepKind.BANK))
		{
			route.add(0, GuideSteps.anyBank("Prepare the required tools and supplies, then close the bank."));
			loop++;
		}
		if (plan.handling == ResourceHandling.BANK)
		{
			for (int i = route.size() - 1; i >= 0; i--)
			{
				GuideStep step = route.get(i);
				if (step.kind() == StepKind.TRAIN)
				{
					route.set(i, step.describedAs(step.instruction() + " Bank the collected resources when full.", AdvanceWhen.INVENTORY_FULL));
					break;
				}
			}
			for (int i = 0; i < route.size(); i++) if (route.get(i).kind() == StepKind.BANK) { loop = i; break; }
		}
		int freeSlots = preparationFreeSlots >= 0 ? preparationFreeSlots
			: plan.handling == ResourceHandling.BANK || plan.handling == ResourceHandling.BANK_OR_DROP ? 1 : 0;
		TrainingMethod copy = new TrainingMethod(id, skill, name, modes, styles, minLevel, maxLevel, priority,
			combatRequirement, requirements, xpEach, xpPerHour, rateNote, summary, wikiTitle, optimizations,
			List.copyOf(route), loop, quests, inputs, preparation, risk, freeSlots);
		copy.preparationReviewed = true;
		List<String> checks = new ArrayList<>(manualChecks);
		checks.addAll(preparation);
		checks.addAll(plan.manualChecks);
		copy.manualChecks = List.copyOf(new java.util.LinkedHashSet<>(checks));
		copy.resourceHandling = plan.handling;
		copy.noItemsReason = plan.noItemsReason;
		copy.localRestock = plan.localRestock;
		copy.resolveItems = plan.resolveItems;
		copy.setupIssues = plan.setupIssues;
		if (bankingVariant != null)
		{
			copy.bankingVariant = new TrainingMethod(copy, bankingVariant.summary, bankingVariant.steps);
		}
		else if (route.stream().anyMatch(step -> step.kind() == StepKind.DROP))
		{
			List<GuideStep> bankRoute = new ArrayList<>();
			GuideStep bank = route.stream().filter(step -> step.kind() == StepKind.BANK).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("A collecting route needs a bank destination: " + id));
			bankRoute.add(bank.describedAs("Deposit collected resources and keep the required tools and supplies.", AdvanceWhen.BANK_CLOSED));
			for (GuideStep step : route)
			{
				if (step.kind() == StepKind.BANK || step.kind() == StepKind.DROP) continue;
				bankRoute.add(step.kind() == StepKind.TRAIN
					? step.describedAs("Continue " + name + ". Bank collected resources when your inventory is full.", AdvanceWhen.INVENTORY_FULL) : step);
			}
			copy.bankingVariant = new TrainingMethod(copy, "Follow the highlighted " + name + " route. Keep the resources and bank them when full.", bankRoute);
		}
		return copy;
	}

	/** A resource choice keeps the selected style's banking behavior, including manual selections. */
	public TrainingMethod forStyle(TrainingStyle style)
	{
		if (sourceMethod != null)
		{
			return sourceMethod.forStyle(style);
		}
		return style == TrainingStyle.PROFIT && bankingVariant != null ? bankingVariant : this;
	}

	/** -1 leaves manual/special preparation intact; otherwise item checks can skip a bank visit. */
	public int preparationFreeSlots()
	{
		return preparationFreeSlots;
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
		return EnumSet.copyOf(modes);
	}

	public EnumSet<TrainingStyle> styles()
	{
		return EnumSet.copyOf(styles);
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
			return quests.isEmpty() && requirements.isEmpty() && combatRequirement == 0 && risk != RouteRisk.WILDERNESS;
		}
		if (risk == RouteRisk.WILDERNESS && !lookup.allowWilderness())
		{
			return false;
		}
		for (Quest quest : quests)
		{
			if (!lookup.questComplete(quest))
			{
				return false;
			}
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

	public List<String> unmetRequirements(LevelLookup lookup)
	{
		List<String> missing = new ArrayList<>();
		if (lookup == null)
		{
			return missing;
		}
		if (combatRequirement > lookup.combatLevel())
		{
			missing.add("Combat " + combatRequirement);
		}
		for (SkillReq requirement : requirements)
		{
			if (lookup.level(requirement.skill()) < requirement.level())
			{
				missing.add(requirement.skill().getName() + " " + requirement.level());
			}
		}
		for (Quest quest : quests)
		{
			if (!lookup.questComplete(quest))
			{
				missing.add(quest.getName());
			}
		}
		if (risk == RouteRisk.WILDERNESS && !lookup.allowWilderness())
		{
			missing.add("Enable Include Wilderness routes to accept PvP risk");
		}
		return List.copyOf(missing);
	}

	public List<Quest> quests()
	{
		return quests;
	}

	public List<ItemRequirement> items()
	{
		return items;
	}

	public List<String> preparation()
	{
		return preparation;
	}

	public RouteRisk risk()
	{
		return risk;
	}

	@Override
	public String toString()
	{
		return name + (sourceMethod == null ? "" : " (banking)") + "  " + minLevel + "-" + maxLevel;
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
		private final List<Quest> quests = new ArrayList<>();
		private final List<ItemRequirement> items = new ArrayList<>();
		private final List<String> preparation = new ArrayList<>();
		private RouteRisk risk = RouteRisk.SAFE;
		private int preparationFreeSlots = -1;
		private String bankingSummary;
		private List<GuideStep> bankingSteps;

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

		public Builder quest(Quest quest)
		{
			quests.add(quest);
			return this;
		}

		public Builder equipment(String label, int... ids)
		{
			items.add(new ItemRequirement(label, true, 1, ids));
			return this;
		}

		public Builder supplies(String label, int quantity, int... ids)
		{
			items.add(new ItemRequirement(label, false, quantity, ids));
			return this;
		}

		public Builder tool(String label, int... ids)
		{
			items.add(new ItemRequirement(label, ItemRequirement.Container.EITHER, 1, ids));
			return this;
		}

		public Builder toolFrom(String label, String source, int... ids)
		{
			items.add(new ItemRequirement(label, ItemRequirement.Container.EITHER, 1, ids).from(source));
			return this;
		}

		public Builder equipmentFrom(String label, String source, int... ids)
		{
			items.add(new ItemRequirement(label, true, 1, ids).from(source));
			return this;
		}

		public Builder item(ItemRequirement item)
		{
			items.add(item);
			return this;
		}

		public Builder itemPreparation(int freeSlots)
		{
			if (freeSlots < 0 || freeSlots > 28)
			{
				throw new IllegalArgumentException("Invalid free slot requirement");
			}
			preparationFreeSlots = freeSlots;
			return this;
		}

		public Builder prepare(String instruction)
		{
			preparation.add(instruction);
			return this;
		}

		public Builder risk(RouteRisk risk)
		{
			this.risk = risk;
			return this;
		}

		public Builder step(GuideStep step)
		{
			steps.add(step);
			return this;
		}

		public Builder banking(String summary, GuideStep... steps)
		{
			if (summary.isEmpty() || steps.length < 2 || steps[0].kind() != StepKind.BANK
				|| Arrays.stream(steps).anyMatch(step -> step.kind() == StepKind.DROP))
			{
				throw new IllegalArgumentException("A banking variant needs a bank loop without dropping");
			}
			bankingSummary = summary;
			bankingSteps = List.copyOf(Arrays.asList(steps));
			return this;
		}

		public void save()
		{
			if (min < 1 || max > 99 || min > max || steps.isEmpty()
				|| loop < 0 || loop >= steps.size() || xpPerHour <= 0 || xpEach < 0 || summary.isEmpty()
				|| preparationFreeSlots >= 0 && items.isEmpty())
			{
				throw new IllegalArgumentException("Invalid training method: " + name);
			}
			EnumSet<AccountMode> modeSet = modes == null ? EnumSet.of(AccountMode.P2P) : EnumSet.copyOf(modes);
			EnumSet<TrainingStyle> styleSet = styles == null ? EnumSet.of(TrainingStyle.BALANCED) : EnumSet.copyOf(styles);
			int loopIndex = steps.isEmpty() ? 0 : Math.max(0, Math.min(loop, steps.size() - 1));
			TrainingMethod method = new TrainingMethod(
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
				loopIndex, quests, items, preparation, risk, preparationFreeSlots);
			if (bankingSteps != null)
			{
				method.bankingVariant = new TrainingMethod(method, bankingSummary, bankingSteps);
			}
			dest.add(method);
		}
	}
}
