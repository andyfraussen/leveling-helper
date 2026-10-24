package com.levelinghelper.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.coords.WorldPoint;

public final class GuideStep
{
	private final String instruction;
	private final StepKind kind;
	private final WorldPoint target;
	private final int arrivalRadius;
	private final int highlightRadius;
	private final AdvanceWhen advance;
	private final HighlightMode highlightMode;
	private final List<String> names;
	private final String watchedItem;
	private final Set<Integer> objectIds;
	private final Set<Integer> npcIds;

	public GuideStep(
		String instruction,
		StepKind kind,
		WorldPoint target,
		int arrivalRadius,
		int highlightRadius,
		AdvanceWhen advance,
		HighlightMode highlightMode,
		List<String> names,
		String watchedItem)
	{
		this(instruction, kind, target, arrivalRadius, highlightRadius, advance, highlightMode,
			names, watchedItem, Collections.emptySet(), Collections.emptySet());
	}

	private GuideStep(String instruction, StepKind kind, WorldPoint target, int arrivalRadius,
		int highlightRadius, AdvanceWhen advance, HighlightMode highlightMode, List<String> names,
		String watchedItem, Set<Integer> objectIds, Set<Integer> npcIds)
	{
		this.instruction = instruction;
		this.kind = kind;
		this.target = target;
		this.arrivalRadius = arrivalRadius;
		this.highlightRadius = highlightRadius;
		this.advance = advance;
		this.highlightMode = highlightMode;
		this.names = List.copyOf(names);
		this.watchedItem = watchedItem;
		this.objectIds = Set.copyOf(objectIds);
		this.npcIds = Set.copyOf(npcIds);
	}

	public String instruction()
	{
		return instruction;
	}

	public StepKind kind()
	{
		return kind;
	}

	public WorldPoint target()
	{
		return target;
	}

	public int arrivalRadius(int fallback)
	{
		return arrivalRadius > 0 ? Math.min(arrivalRadius, fallback) : fallback;
	}

	public int highlightRadius()
	{
		return highlightRadius > 0 ? highlightRadius : 18;
	}

	public AdvanceWhen advance()
	{
		return advance;
	}

	public HighlightMode highlightMode()
	{
		return highlightMode;
	}

	public List<String> names()
	{
		return names;
	}

	public String watchedItem()
	{
		return watchedItem;
	}

	public boolean hasTargets()
	{
		return !names.isEmpty() || !objectIds.isEmpty() || !npcIds.isEmpty();
	}

	public boolean matchesObject(int id, String name)
	{
		return !objectIds.isEmpty() ? objectIds.contains(id) : npcIds.isEmpty() && matchesName(name);
	}

	public boolean matchesNpc(int id, String name)
	{
		return !npcIds.isEmpty() ? npcIds.contains(id) : objectIds.isEmpty() && matchesName(name);
	}

	private boolean matchesName(String name)
	{
		return name != null && names.contains(name.replaceAll("<[^>]*>", "").trim().toLowerCase(Locale.ROOT));
	}

	public GuideStep objectIds(int... ids)
	{
		return new GuideStep(instruction, kind, target, arrivalRadius, highlightRadius, advance,
			highlightMode, names, watchedItem, ids(ids), npcIds);
	}

	public GuideStep onAnyPlane()
	{
		return new GuideStep(instruction, kind, target, arrivalRadius, highlightRadius, advance,
			HighlightMode.NEAR_TARGET_ANY_PLANE, names, watchedItem, objectIds, npcIds);
	}

	public GuideStep npcIds(int... ids)
	{
		return new GuideStep(instruction, kind, target, arrivalRadius, highlightRadius, advance,
			highlightMode, names, watchedItem, objectIds, ids(ids));
	}

	private static Set<Integer> ids(int[] ids)
	{
		if (Arrays.stream(ids).anyMatch(id -> id < 0))
		{
			throw new IllegalArgumentException("Target IDs must be nonnegative");
		}
		return Arrays.stream(ids).boxed().collect(Collectors.toSet());
	}

	public static List<String> namesOf(String... names)
	{
		if (names == null || names.length == 0)
		{
			return Collections.emptyList();
		}
		java.util.ArrayList<String> cleaned = new java.util.ArrayList<String>(names.length);
		for (String name : names)
		{
			if (name != null && !name.isEmpty())
			{
				cleaned.add(name.trim().toLowerCase(Locale.ROOT));
			}
		}
		return Collections.unmodifiableList(cleaned);
	}
}
