package com.levelinghelper.data;

import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.model.ItemCategory;
import com.levelinghelper.model.ItemRequirement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;

/** Minimum inputs for one cast; elemental staves/tomes and combination runes are interchangeable. */
final class MagicSupplies
{
	private static final int[] AIR_STAVES = {ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF,
		ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_STAFF, ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_STAFF};
	private static final int[] WATER_STAVES = {ItemID.STAFF_OF_WATER, ItemID.WATER_BATTLESTAFF, ItemID.MYSTIC_WATER_STAFF,
		ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF, ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_STAFF,
		ItemID.MYSTIC_STEAM_STAFF_12796, ItemID.TOME_OF_WATER};
	private static final int[] EARTH_STAVES = {ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF,
		ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF, ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF,
		ItemID.MYSTIC_LAVA_STAFF_21200, ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_STAFF, ItemID.TOME_OF_EARTH};
	private static final int[] FIRE_STAVES = {ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF,
		ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF, ItemID.MYSTIC_LAVA_STAFF_21200,
		ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_STAFF, ItemID.STEAM_BATTLESTAFF,
		ItemID.MYSTIC_STEAM_STAFF, ItemID.MYSTIC_STEAM_STAFF_12796, ItemID.TOME_OF_FIRE};

	static ItemRequirement rune(String spell, String element, int quantity)
	{
		int[] ids;
		int[] providers;
		switch (element)
		{
			case "Air": ids = new int[]{ItemID.AIR_RUNE, ItemID.MIST_RUNE, ItemID.DUST_RUNE, ItemID.SMOKE_RUNE}; providers = AIR_STAVES; break;
			case "Water": ids = new int[]{ItemID.WATER_RUNE, ItemID.MIST_RUNE, ItemID.MUD_RUNE, ItemID.STEAM_RUNE}; providers = WATER_STAVES; break;
			case "Earth": ids = new int[]{ItemID.EARTH_RUNE, ItemID.DUST_RUNE, ItemID.MUD_RUNE, ItemID.LAVA_RUNE}; providers = EARTH_STAVES; break;
			case "Fire": ids = new int[]{ItemID.FIRE_RUNE, ItemID.SUNFIRE_RUNE, ItemID.SMOKE_RUNE, ItemID.STEAM_RUNE, ItemID.LAVA_RUNE}; providers = FIRE_STAVES; break;
			case "Mind": ids = new int[]{ItemID.MIND_RUNE}; providers = new int[0]; break;
			case "Chaos": ids = new int[]{ItemID.CHAOS_RUNE}; providers = new int[0]; break;
			case "Death": ids = new int[]{ItemID.DEATH_RUNE}; providers = new int[0]; break;
			case "Blood": ids = new int[]{ItemID.BLOOD_RUNE}; providers = new int[0]; break;
			case "Body": ids = new int[]{ItemID.BODY_RUNE}; providers = new int[0]; break;
			case "Nature": ids = new int[]{ItemID.NATURE_RUNE}; providers = new int[0]; break;
			default: throw new IllegalArgumentException("Unknown rune: " + element);
		}
		return new ItemRequirement(spell + ": " + element + " runes", false, quantity, ids).providedBy(providers)
			.from("Buy runes from a rune shop or craft them. An equipped elemental staff or charged tome can supply its element.")
			.perAction(quantity);
	}

	static List<ItemRequirement> forRoute(String name, LevelLookup lookup)
	{
		List<ItemRequirement> inputs = new ArrayList<>();
		switch (name)
		{
			case "Ice Burst":
				inputs.add(rune(name, "Death", 2)); inputs.add(rune(name, "Chaos", 4)); inputs.add(rune(name, "Water", 4)); return inputs;
			case "Ice Barrage":
				inputs.add(rune(name, "Death", 4)); inputs.add(rune(name, "Blood", 2)); inputs.add(rune(name, "Water", 6)); return inputs;
			case "Curse splash":
				inputs.add(rune("Curse", "Body", 1)); inputs.add(rune("Curse", "Earth", 2)); inputs.add(rune("Curse", "Water", 3)); return inputs;
			case "High Alchemy":
				inputs.add(rune(name, "Nature", 1)); inputs.add(rune(name, "Fire", 5));
				inputs.add(new ItemRequirement("Yew longbows to alchemise", false, 1, ItemID.YEW_LONGBOW).perAction(1)
					.from("Fletch yew longbows or obtain them through trading or drops.")); return inputs;
			default:
				if (allowsPoweredStaff(name) && lookup.equippedMatches(ItemCategory.POWERED_STAFF))
				{
					return Collections.singletonList(ItemRequirement.matching("Charged powered staff", ItemRequirement.Container.EQUIPMENT,
						1, ItemCategory.POWERED_STAFF, "Charge and equip the powered staff you can use."));
				}
				return standard(name, lookup);
		}
	}

	static boolean allowsPoweredStaff(String route)
	{
		return route.equals("Combat spells") || route.equals("Gemstone Crab");
	}

	private static List<ItemRequirement> standard(String route, LevelLookup lookup)
	{
		int level = lookup.level(Skill.MAGIC);
		// Use a complete cast the player already has before recommending new runes.
		List<List<ItemRequirement>> options = new ArrayList<>();
		String[] elements = {"Air", "Water", "Earth", "Fire"};
		int[] levels = {1, 5, 9, 13, 17, 23, 29, 35, 41, 47, 53, 59};
		int[] airs = {1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4};
		int[] elemental = {0, 1, 2, 3, 0, 2, 3, 4, 0, 3, 4, 5};
		for (int i = 0; i < levels.length; i++)
		{
			if (levels[i] > level) continue;
			if (route.startsWith("Fire Strike") && i != 3) continue;
			if (route.equals("Wind Strike to Fire Strike") && i > 3) continue;
			String spell = (i % 4 == 0 ? "Wind" : elements[i % 4]) + (i < 4 ? " Strike" : i < 8 ? " Bolt" : " Blast");
			List<ItemRequirement> cast = new ArrayList<>();
			cast.add(rune(spell, "Air", airs[i]));
			if (elemental[i] > 0) cast.add(rune(spell, elements[i % 4], elemental[i]));
			cast.add(rune(spell, i < 4 ? "Mind" : i < 8 ? "Chaos" : "Death", 1));
			options.add(cast);
		}
		if (options.isEmpty()) return List.of(rune("Wind Strike", "Air", 1), rune("Wind Strike", "Mind", 1));
		for (int i = options.size() - 1; i >= 0; i--)
		{
			List<ItemRequirement> cast = options.get(i);
			if (cast.stream().allMatch(item -> java.util.Arrays.stream(item.equipmentProviders()).anyMatch(id -> lookup.providerUsable(id) && lookup.equippedCount(id) > 0)
				|| item.count(lookup::itemCount, lookup, lookup.membersWorld()) >= item.quantity())) return cast;
		}
		return options.get(options.size() - 1);
	}
}
