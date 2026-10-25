package com.levelinghelper.data;

import com.levelinghelper.model.ItemRequirement;
import net.runelite.api.ItemID;

/** Explicit unnoted recipe inputs. Unknown names fail catalog construction rather than guessing. */
final class Materials
{
	private Materials() { }

	static ItemRequirement supply(String name, int quantity)
	{
		return new ItemRequirement(name, false, quantity, id(name)).from(source(name))
			.perAction(name.equals("Thread") ? 0 : quantity);
	}

	private static int id(String name)
	{
		switch (name)
		{
			case "Logs": return ItemID.LOGS;
			case "Oak logs": return ItemID.OAK_LOGS;
			case "Willow logs": return ItemID.WILLOW_LOGS;
			case "Maple logs": return ItemID.MAPLE_LOGS;
			case "Yew logs": return ItemID.YEW_LOGS;
			case "Magic logs": return ItemID.MAGIC_LOGS;
			case "Raw shrimps": return ItemID.RAW_SHRIMPS;
			case "Raw trout": return ItemID.RAW_TROUT;
			case "Raw salmon": return ItemID.RAW_SALMON;
			case "Raw lobster": return ItemID.RAW_LOBSTER;
			case "Raw swordfish": return ItemID.RAW_SWORDFISH;
			case "Raw monkfish": return ItemID.RAW_MONKFISH;
			case "Raw shark": return ItemID.RAW_SHARK;
			case "Bronze dart tip": return ItemID.BRONZE_DART_TIP;
			case "Iron dart tip": return ItemID.IRON_DART_TIP;
			case "Mithril dart tip": return ItemID.MITHRIL_DART_TIP;
			case "Adamant dart tip": return ItemID.ADAMANT_DART_TIP;
			case "Rune dart tip": return ItemID.RUNE_DART_TIP;
			case "Leather": return ItemID.LEATHER;
			case "Green dragon leather": return ItemID.GREEN_DRAGON_LEATHER;
			case "Blue dragon leather": return ItemID.BLUE_DRAGON_LEATHER;
			case "Red dragon leather": return ItemID.RED_DRAGON_LEATHER;
			case "Black dragon leather": return ItemID.BLACK_DRAGON_LEATHER;
			case "Uncut sapphire": return ItemID.UNCUT_SAPPHIRE;
			case "Uncut emerald": return ItemID.UNCUT_EMERALD;
			case "Uncut ruby": return ItemID.UNCUT_RUBY;
			case "Uncut diamond": return ItemID.UNCUT_DIAMOND;
			case "Uncut dragonstone": return ItemID.UNCUT_DRAGONSTONE;
			case "Bronze bar": return ItemID.BRONZE_BAR;
			case "Iron bar": return ItemID.IRON_BAR;
			case "Steel bar": return ItemID.STEEL_BAR;
			case "Mithril bar": return ItemID.MITHRIL_BAR;
			case "Adamantite bar": return ItemID.ADAMANTITE_BAR;
			case "Guam potion (unf)": return ItemID.GUAM_POTION_UNF;
			case "Tarromin potion (unf)": return ItemID.TARROMIN_POTION_UNF;
			case "Harralander potion (unf)": return ItemID.HARRALANDER_POTION_UNF;
			case "Ranarr potion (unf)": return ItemID.RANARR_POTION_UNF;
			case "Irit potion (unf)": return ItemID.IRIT_POTION_UNF;
			case "Kwuarm potion (unf)": return ItemID.KWUARM_POTION_UNF;
			case "Snapdragon potion (unf)": return ItemID.SNAPDRAGON_POTION_UNF;
			case "Dwarf weed potion (unf)": return ItemID.DWARF_WEED_POTION_UNF;
			case "Toadflax potion (unf)": return ItemID.TOADFLAX_POTION_UNF;
			case "Torstol potion (unf)": return ItemID.TORSTOL_POTION_UNF;
			case "Eye of newt": return ItemID.EYE_OF_NEWT;
			case "Limpwurt root": return ItemID.LIMPWURT_ROOT;
			case "Chocolate dust": return ItemID.CHOCOLATE_DUST;
			case "Snape grass": return ItemID.SNAPE_GRASS;
			case "Red spiders' eggs": return ItemID.RED_SPIDERS_EGGS;
			case "Wine of zamorak": return ItemID.WINE_OF_ZAMORAK;
			case "Crushed nest": return ItemID.CRUSHED_NEST;
			case "Super attack (4)": return ItemID.SUPER_ATTACK4;
			case "Super strength (4)": return ItemID.SUPER_STRENGTH4;
			case "Super defence (4)": return ItemID.SUPER_DEFENCE4;
			case "Big bones": return ItemID.BIG_BONES;
			case "Dragon bones": return ItemID.DRAGON_BONES;
			case "Feathers": return ItemID.FEATHER;
			case "Grapes": return ItemID.GRAPES;
			case "Jug of water": return ItemID.JUG_OF_WATER;
			case "Thread": return ItemID.THREAD;
			case "Rune essence": return ItemID.RUNE_ESSENCE;
			case "Pure essence": return ItemID.PURE_ESSENCE;
			default: throw new IllegalArgumentException("No item ID declared for " + name);
		}
	}

	private static String source(String name)
	{
		if (name.equals("Big bones"))
		{
			return "Kill hill giants for big bones; a brass key opens the Edgeville Dungeon entrance.";
		}
		if (name.equals("Dragon bones"))
		{
			return "Obtain dragon bones from adult dragons; choose a dragon you can fight safely.";
		}
		if (name.equals("Logs") || name.endsWith(" logs"))
		{
			return "Chop the matching trees with a usable axe.";
		}
		if (name.endsWith("dart tip"))
		{
			return "Smith dart tips from the matching bars after The Tourist Trap, or obtain them as drops.";
		}
		if (name.equals("Feathers"))
		{
			return "Collect feathers from chickens or buy them from a fishing shop.";
		}
		if (name.startsWith("Raw "))
		{
			return "Catch the matching fish with the Fishing skill.";
		}
		if (name.contains("essence"))
		{
			return "Mine essence via Aubury after Rune Mysteries, or obtain pure essence from drops.";
		}
		if (name.equals("Thread"))
		{
			return "Buy thread from a crafting shop, such as Dommik's in Al Kharid.";
		}
		return "Gather or make " + name.toLowerCase(java.util.Locale.ROOT) + "; check its Wiki page for sources.";
	}
}
