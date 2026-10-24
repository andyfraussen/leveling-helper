package com.levelinghelper.model;

import java.util.Collections;
import java.util.List;
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
		this.instruction = instruction;
		this.kind = kind;
		this.target = target;
		this.arrivalRadius = arrivalRadius;
		this.highlightRadius = highlightRadius;
		this.advance = advance;
		this.highlightMode = highlightMode;
		this.names = names;
		this.watchedItem = watchedItem;
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
		return arrivalRadius > 0 ? arrivalRadius : fallback;
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
				cleaned.add(name.toLowerCase());
			}
		}
		return Collections.unmodifiableList(cleaned);
	}
}
