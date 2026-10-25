package com.levelinghelper.model;

import java.util.List;
import java.util.function.Function;
import com.levelinghelper.guide.LevelLookup;

/** Explicit catalog review result, including facts that require the player's confirmation. */
public final class PreparationPlan
{
	public final List<ItemRequirement> items;
	public final List<String> manualChecks;
	public final ResourceHandling handling;
	public final String noItemsReason;
	public final String localRestock;
	public boolean dynamicItems;
	public Function<LevelLookup, List<ItemRequirement>> resolveItems = lookup -> java.util.Collections.emptyList();
	public Function<LevelLookup, List<String>> setupIssues = lookup -> java.util.Collections.emptyList();

	public PreparationPlan(List<ItemRequirement> items, List<String> manualChecks, ResourceHandling handling,
		String noItemsReason, String localRestock)
	{
		this.items = List.copyOf(items);
		this.manualChecks = List.copyOf(manualChecks);
		this.handling = java.util.Objects.requireNonNull(handling);
		this.noItemsReason = java.util.Objects.requireNonNull(noItemsReason);
		this.localRestock = localRestock;
	}
}
