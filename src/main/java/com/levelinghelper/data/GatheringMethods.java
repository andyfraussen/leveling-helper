package com.levelinghelper.data;

import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.RouteRisk;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.List;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

final class GatheringMethods
{
	private static final TrainingStyle[] ALL = {
		TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT
	};

	private GatheringMethods()
	{
	}

	static void register(List<TrainingMethod> out)
	{
		woodcutting(out);
		mining(out);
		fishing(out);
		hunter(out);
		farming(out);
	}

	private static void woodcutting(List<TrainingMethod> out)
	{
		chop(out, "Trees", 1, 14, AccountMode.F2P, ALL, 1, 25, 9000,
			"Chop normal trees around Lumbridge. They fall over after one log, so this is only the first few levels.",
			"Tree", Spots.LUMBRIDGE_BANK, Spots.LUMBRIDGE_TREES, true, "Tree");
		chop(out, "Oaks", 15, 29, AccountMode.F2P, ALL, 1, 37.5, 16000,
			"Oaks are the free-to-play step after normal trees. The ones south-west of Varrock west bank are next to a bank.",
			"Oak", Spots.VARROCK_WEST_BANK, Spots.VARROCK_OAKS, true, "Oak");
		chop(out, "Willows", 30, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 1, 67.5, 33000,
			"Draynor willows are the free-to-play Woodcutting training spot. The bank is a few steps north. Dark wizards between the trees attack players under combat level 15.",
			"Willow", Spots.DRAYNOR_BANK, Spots.DRAYNOR_WILLOWS, true, "Willow");
		chop(out, "Yews", 60, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 2, 175, 16000,
			"Yews are slower XP than willows and better money. These trees are south of Edgeville bank, outside the Wilderness.",
			"Yew", Spots.EDGEVILLE_BANK, Spots.EDGEVILLE_YEWS, true, "Yew");

		chop(out, "Trees", 1, 14, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 1, 25, 9000,
			"Normal trees only last until oaks. Members should still start here, or use quest XP if they have it.",
			"Tree", Spots.LUMBRIDGE_BANK, Spots.LUMBRIDGE_TREES, true, "Tree");
		chop(out, "Oaks", 15, 34, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 1, 37.5, 18000,
			"Oaks beside Varrock west bank, until teaks at 35.",
			"Oak", Spots.VARROCK_WEST_BANK, Spots.VARROCK_OAKS, true, "Oak");
		chop(out, "Teaks", 35, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.FAST}, 2, 85, 70000,
			"Teaks are the fast Woodcutting method when you are not tick-manipulating. The Isle of Souls has teaks and a bank chest, and the boat leaves from Port Sarim. Drop the logs, or bank them if you want the planks later.",
			"Teak", Spots.ISLE_OF_SOULS, Spots.ISLE_OF_SOULS, true, "Teak");
		chop(out, "Teaks", 35, 64, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED}, 2, 85, 55000,
			"Isle of Souls teaks are the balanced members method from 35 to 65. Take the boat from Port Sarim. There is a bank chest on the island.",
			"Teak", Spots.ISLE_OF_SOULS, Spots.ISLE_OF_SOULS, true, "Teak");
		chop(out, "Willows", 30, 44, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK}, 1, 67.5, 30000,
			"Draynor willows if you want a quieter members method before maples.",
			"Willow", Spots.DRAYNOR_BANK, Spots.DRAYNOR_WILLOWS, true, "Willow");
		chop(out, "Maples", 45, 59, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK}, 2, 100, 35000,
			"The maple trees are directly behind Seers' Village bank. A Camelot teleport puts you next to them.",
			"Maple tree", Spots.SEERS_BANK, Spots.SEERS_MAPLES, true, "Maple tree");
		chop(out, "Sulliusceps", 65, 89, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED}, 3, 0, 65000,
			"Sulliusceps in the Tar Swamp on Fossil Island. You need 65 Woodcutting and Bone Voyage. The mushroom moves after each cut. You also get fossils. This is more clicking than redwoods and less clicking than tick-manipulated teaks.",
			"Sulliuscep", Spots.MUSEUM_CAMP, Spots.SULLIUSCEPS, false, "Sulliuscep");
		chop(out, "Yews", 60, 74, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.PROFIT, TrainingStyle.AFK}, 2, 175, 18000,
			"Yews are the money-and-idle option from 60 until magic trees. Seers has yews south of the bank, and Edgeville's are outside the Wilderness.",
			"Yew", Spots.EDGEVILLE_BANK, Spots.EDGEVILLE_YEWS, true, "Yew");
		chop(out, "Magic trees", 75, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 4, 250, 18000,
			"Magic trees are the members money method. The Woodcutting Guild has several, a bank chest, and a invisible +7 Woodcutting boost. You need 60 Woodcutting to enter the guild and 75 to cut magics.",
			"Magic tree", Spots.REDWOODS, Spots.REDWOODS, true, "Magic tree");
		chop(out, "Redwoods", 90, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK}, 4, 380, 65000,
			"Redwoods in the Woodcutting Guild are the idle 90 to 99 method. The guild requires 60 Woodcutting. The redwood fence requires 90. There is a bank chest inside the guild. You almost never move.",
			"Redwood", Spots.REDWOODS, Spots.REDWOODS, true, "Redwood");
	}

	private static void chop(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String summary, String wiki, WorldPoint bank, WorldPoint trees, boolean bankLogs, String... highlights)
	{
		TrainingMethod.Builder builder = TrainingMethod.builder(out, Skill.WOODCUTTING, name)
			.item(Tools.axe())
			.itemPreparation(1)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(wiki)
			.opt("A better axe is the main upgrade. Rune is fine for a long time. A dragon axe special attack is a +3 boost.")
			.opt("Forestry events are optional. The official Forestry plugin tracks them. They are not required for this route.")
			.opt("Tick manipulation, such as 1.5-tick teaks, is much faster and much more clicking. This route does not use it.")
			.loop(0)
			.step(GuideSteps.bank("Prepare a usable axe and deposit the logs. The axe can be carried or equipped.", bank))
			.step(GuideSteps.travel("Walk to the trees. They are highlighted when you get close.", trees, 18, highlights));
		if (bankLogs)
		{
			builder.step(GuideSteps.train("Chop until the inventory is full, then the guide sends you back to the bank.", trees, AdvanceWhen.INVENTORY_FULL, highlights));
		}
		else
		{
			builder.step(GuideSteps.trainWide("Chop the sulliusceps and follow the mushroom when it moves. Bank fossils at the Museum Camp when you want to.", trees, 30, AdvanceWhen.MANUAL, highlights));
		}
		if (name.equals("Sulliusceps"))
		{
			builder.quest(Quest.BONE_VOYAGE).risk(RouteRisk.DANGEROUS);
		}
		builder.save();
	}

	private static void mining(List<TrainingMethod> out)
	{
		powerMine(out, "Copper and tin", 1, 14, AccountMode.F2P, ALL, 1, 17.5, 8000,
			"Power-mine copper and tin in the Lumbridge Swamp mine. Drop the ore. This is only until iron.",
			"Copper ore", Spots.LUMBRIDGE_SWAMP_MINE, "Rocks");
		powerMine(out, "Iron", 15, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 35, 42000,
			"Power-mining iron in the Al Kharid mine is the free-to-play Mining method. Drop the ore. Three rocks close together are the usual spot, in the north of the mine.",
			"Iron ore", Spots.AL_KHARID_MINE, "Rocks");
		powerMine(out, "Coal", 30, 54, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 2, 50, 18000,
			"Coal is worse XP than dropping iron and better money. The Al Kharid mine has coal rocks. Bank the ore.",
			"Coal", Spots.AL_KHARID_MINE, true, "Rocks");
		powerMine(out, "Mithril", 55, 69, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 3, 80, 14000,
			"Mithril is the free-to-play money step after coal. It is slower than iron. Bank the ore.",
			"Mithril ore", Spots.AL_KHARID_MINE, true, "Rocks");
		powerMine(out, "Adamantite", 70, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 4, 95, 12000,
			"Adamantite is the best free-to-play Mining money and poor XP. Iron is still the XP method. Bank the ore.",
			"Adamantite ore", Spots.AL_KHARID_MINE, true, "Rocks");

		powerMine(out, "Copper and tin", 1, 14, AccountMode.P2P, ALL, 1, 17.5, 8000,
			"Copper and tin until iron. Drop the ore.",
			"Copper ore", Spots.LUMBRIDGE_SWAMP_MINE, "Rocks");
		powerMine(out, "Iron", 15, 29, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 2, 35, 35000,
			"Power-mine iron at Al Kharid until Motherlode Mine at 30.",
			"Iron ore", Spots.AL_KHARID_MINE, "Rocks");
		powerMine(out, "Iron", 15, 49, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.FAST}, 2, 35, 40000,
			"Power-mine iron until Volcanic Mine at 50. Drop the ore.",
			"Iron ore", Spots.AL_KHARID_MINE, "Rocks");

		TrainingMethod.builder(out, Skill.MINING, "Motherlode Mine")
			.item(Tools.pickaxe())
			.itemPreparation(1)
			.levels(30, 91)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT)
			.priority(3)
			.xp(60, 35000)
			.summary("Motherlode Mine is the balanced members method. You mine pay-dirt, dump it in the hopper, and empty the sack at the chest. It is also profit. At 57, the upper level is available after paying 100 golden nuggets. The veins are through the Dwarven Mine east of Falador.")
			.wiki("Motherlode Mine")
			.opt("The upper level needs 57 Mining and a one-time payment of 100 golden nuggets. The Prospector kit is worth buying with golden nuggets.")
			.opt("Shooting stars are even more idle and less XP. A friends chat or the group star plugin finds the current star.")
			.opt("3-tick granite at the Bandit Camp quarry is the sweaty XP method. Bring waterskins or a Circlet of Water. The quarry is at the desert bandit camp.")
			.loop(2)
			.step(GuideSteps.travel("Enter the Dwarven Mine from Falador's east side, then the Motherlode Mine door.", Spots.FALADOR_EAST_BANK, 20))
			.step(GuideSteps.travel("Walk to the pay-dirt veins, hopper, and bank chest inside Motherlode.", Spots.MOTHERLODE, 20, "Ore vein", "Rocks", "Hopper", "Bank chest"))
			.step(GuideSteps.train("Mine pay-dirt until the inventory is full.", Spots.MOTHERLODE, AdvanceWhen.INVENTORY_FULL, "Ore vein", "Rocks"))
			.step(GuideSteps.hopper("Dump the pay-dirt in the hopper. Repair a broken wheel with a hammer. Empty the sack into the chest when it is full.", Spots.MOTHERLODE, "Hopper", "Sack", "Bank chest"))
			.save();

		TrainingMethod.builder(out, Skill.MINING, "Motherlode upper level")
			.item(Tools.pickaxe())
			.itemPreparation(1)
			.prepare("Buy upper-level access from Prospector Percy for 100 golden nuggets. Stay on the lower level until purchased.")
			.levels(57, 91)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT)
			.priority(2)
			.xp(60, 50000)
			.summary("The upper level requires 57 Mining and a one-time payment of 100 golden nuggets. Stay downstairs until you have purchased access. The upper hopper costs another 50 nuggets.")
			.wiki("Motherlode Mine")
			.opt("Amethyst at 92 is more idle and less XP, inside the Mining Guild.")
			.loop(2)
			.step(GuideSteps.travel("Enter Motherlode Mine through the Dwarven Mine east of Falador.", Spots.FALADOR_EAST_BANK, 20))
			.step(GuideSteps.travel("Climb to the upper level and mine the veins there.", Spots.MOTHERLODE, 25, "Ore vein"))
			.step(GuideSteps.train("Mine pay-dirt until the inventory is full.", Spots.MOTHERLODE, AdvanceWhen.INVENTORY_FULL, "Ore vein"))
			.step(GuideSteps.hopper("Dump the pay-dirt in the hopper and empty the sack when it is full.", Spots.MOTHERLODE, "Hopper", "Sack"))
			.save();

		TrainingMethod.builder(out, Skill.MINING, "Volcanic Mine")
			.item(Tools.pickaxe())
			.itemPreparation(1)
			.quest(Quest.BONE_VOYAGE)
			.risk(RouteRisk.DANGEROUS)
			.prepare("Check entry with Petrified Pete: each entry costs 30 numulites, or unlock permanent access for 3,000 numulites.")
			.levels(50, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(5)
			.xp(0, 70000)
			.summary("Volcanic Mine on Fossil Island is the fast Mining method that is still a team minigame rather than tick manipulation. You need 50 Mining and Bone Voyage. The rate depends on the team. Play on a dedicated world if you can.")
			.wiki("Volcanic Mine")
			.opt("3-tick granite at the quarry south of the Bandit Camp is higher XP if you want to click on a rhythm. It is in the desert, so bring water.")
			.loop(1)
			.step(GuideSteps.note("Complete Bone Voyage if you have not. Bring a pickaxe. Press Next when you are ready to go to Fossil Island."))
			.step(GuideSteps.travel("Enter the Volcanic Mine on the north-east of Fossil Island and join a team.", Spots.VOLCANIC_MINE, 18))
			.step(GuideSteps.train("Follow the mine's vents and chambers until the round ends, then start another.", Spots.VOLCANIC_MINE, AdvanceWhen.MANUAL))
			.save();

		TrainingMethod.builder(out, Skill.MINING, "Amethyst")
			.item(Tools.pickaxe())
			.itemPreparation(1)
			.levels(92, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT)
			.priority(6)
			.xp(240, 25000)
			.summary("Amethyst in the Mining Guild is the idle 92 to 99 method and it is profit. The guild requires 60 Mining, which you already have. The crystals are in the south of the guild. They do not deplete for a long time.")
			.wiki("Amethyst")
			.opt("Stay in Motherlode if you want more XP per hour and do not mind the hopper.")
			.loop(1)
			.step(GuideSteps.travel("Enter the Mining Guild from east Falador. The door requires 60 Mining.", Spots.FALADOR_EAST_BANK, 20))
			.step(GuideSteps.train("Mine the amethyst crystals in the south of the guild. Bank at the chest by the entrance when your inventory is full. Use the guild bank when full; this route stays on the training step.", Spots.AMETHYST, AdvanceWhen.MANUAL, "Amethyst crystals", "Crystals"))
			.save();
	}

	private static void powerMine(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String summary, String wiki, WorldPoint spot, String... highlights)
	{
		powerMine(out, name, min, max, mode, styles, priority, xpEach, xpHr, summary, wiki, spot, false, highlights);
	}

	private static void powerMine(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String summary, String wiki, WorldPoint spot, boolean bankOre, String... highlights)
	{
		WorldPoint bank = spot == Spots.LUMBRIDGE_SWAMP_MINE ? Spots.LUMBRIDGE_BANK : Spots.AL_KHARID_BANK;
		TrainingMethod.Builder builder = TrainingMethod.builder(out, Skill.MINING, name)
			.item(Tools.pickaxe())
			.itemPreparation(1)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(wiki)
			.opt(bankOre
				? "Use the best pickaxe you can. Banking is slower XP than dropping, which is why the XP route drops iron."
				: "Use the best pickaxe you can. Dropping is more XP than banking.")
			.loop(bankOre ? 0 : 1)
			.step(GuideSteps.bank(bankOre
				? "Prepare a usable pickaxe and deposit the ore."
				: "Prepare a usable pickaxe. Deposit ore if you are banking it.", bank))
			.step(GuideSteps.travel("Walk to the rocks.", spot, 15, highlights).objectIds(oreIds(name)))
			.step(GuideSteps.train("Mine until the inventory is full.", spot, AdvanceWhen.INVENTORY_FULL, highlights).objectIds(oreIds(name)));
		if (!bankOre)
		{
			builder.step(GuideSteps.drop("Drop the ore. Shift-click drop is in the game settings. The guide continues when the inventory has space again."));
			builder.banking("Mine " + name.toLowerCase(java.util.Locale.ROOT)
				+ " at the highlighted rocks. Keep the ore and deposit it at the bank when full, then return to the rocks.",
				GuideSteps.bank("Deposit the ore and keep your pickaxe. Close the bank when you have inventory space.", bank),
				GuideSteps.travel("Return to the highlighted rocks.", spot, 15, highlights).objectIds(oreIds(name)),
				GuideSteps.train("Mine until the inventory is full, then return to the bank.", spot, AdvanceWhen.INVENTORY_FULL, highlights).objectIds(oreIds(name)));
		}
		builder.save();
	}

	private static int[] oreIds(String name)
	{
		switch (name)
		{
			case "Iron": return new int[]{ObjectID.IRON_ROCKS, ObjectID.IRON_ROCKS_11365};
			case "Coal": return new int[]{ObjectID.COAL_ROCKS_11366, ObjectID.COAL_ROCKS_11367};
			case "Mithril": return new int[]{ObjectID.MITHRIL_ROCKS, ObjectID.MITHRIL_ROCKS_11373};
			case "Adamantite": return new int[]{ObjectID.ADAMANTITE_ROCKS, ObjectID.ADAMANTITE_ROCKS_11375};
			default: return new int[]{ObjectID.COPPER_ROCKS_10943, ObjectID.COPPER_ROCKS_11161, ObjectID.TIN_ROCKS_11360, ObjectID.TIN_ROCKS_11361};
		}
	}

	private static void fishing(List<TrainingMethod> out)
	{
		fish(out, "Shrimp and anchovies", 1, 19, AccountMode.F2P, ALL, 1, 10, 8000,
			"Net fishing at Draynor. Bring a small fishing net. Anchovies start at 15 and are caught at the same spot.",
			"Raw shrimps", Spots.DRAYNOR_BANK, "Withdraw a small fishing net.", Spots.DRAYNOR_FISHING, true);
		fish(out, "Trout and salmon", 20, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 50, 28000,
			"Fly fishing at Barbarian Village is the best free-to-play Fishing XP. Bring a fly fishing rod and feathers. Drop the fish, or bank them at Edgeville if you want to cook them.",
			"Raw trout", Spots.EDGEVILLE_BANK, "Withdraw a fly fishing rod and a few thousand feathers.", Spots.BARB_FISH, false);
		fish(out, "Lobsters", 40, 49, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 2, 90, 18000,
			"Lobsters at Musa Point. Bring a lobster pot. The boat from Port Sarim costs 30 coins each way. Bank at Draynor.",
			"Raw lobster", Spots.DRAYNOR_BANK, "Withdraw a lobster pot and at least 60 coins for the return boat trip.", Spots.MUSA_POINT, true);
		fish(out, "Swordfish", 50, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.PROFIT}, 3, 100, 20000,
			"Swordfish at Musa Point are the free-to-play money fish. Bring a harpoon. Bank via the boat to Port Sarim and the Draynor bank.",
			"Raw swordfish", Spots.DRAYNOR_BANK, "Withdraw a harpoon and coins for the Karamja boat.", Spots.MUSA_POINT, true);

		fish(out, "Shrimp", 1, 19, AccountMode.P2P, ALL, 1, 10, 8000,
			"Net fishing at Draynor until fly fishing. Members can skip some of this with Sea Slug and Fishing Contest.",
			"Raw shrimps", Spots.DRAYNOR_BANK, "Withdraw a small fishing net.", Spots.DRAYNOR_FISHING, true);
		fish(out, "Fly fishing", 20, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST}, 1, 50, 30000,
			"Fly fishing at Barbarian Village. This stays the recommendation only until you qualify for barbarian fishing or Tempoross.",
			"Raw trout", Spots.EDGEVILLE_BANK, "Withdraw a fly fishing rod and feathers.", Spots.BARB_FISH, false);
		TrainingMethod.builder(out, Skill.FISHING, "Barbarian fishing")
			.tool("Barbarian rod", ItemID.BARBARIAN_ROD, ItemID.PEARL_BARBARIAN_ROD)
			.supplies("Feathers, fishing bait, roe, caviar or fish offcuts", 1, ItemID.FEATHER, ItemID.FISHING_BAIT, ItemID.ROE, ItemID.CAVIAR, ItemID.FISH_OFFCUTS)
			.prepare("Ask Otto about heavy rod fishing before training. Keep bait when dropping fish.")
			.levels(48, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK)
			.priority(4)
			.req(Skill.AGILITY, 15)
			.req(Skill.STRENGTH, 15)
			.xp(50, 55000)
			.summary("Barbarian fishing at Otto's Grotto gives Fishing, Strength, and Agility XP. You need Barbarian Training started with Otto, 48 Fishing, 15 Agility, and 15 Strength. Use a heavy rod. Drop the leaping fish. Strength and Agility XP is awarded when catching the fish; a knife can turn fish into bait.")
			.wiki("Barbarian Fishing")
			.opt("Sea Slug and Fishing Contest are worth doing before this grind.")
			.opt("Tempoross is the minigame alternative and gives fish rewards. Switch the style to Fastest XP.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw a barbarian rod and plenty of feathers or fishing bait. Drop leftover fish, keeping the rod and bait.", Spots.EDGEVILLE_BANK))
			.step(GuideSteps.travel("Go to Otto's Grotto, north of the Barbarian Outpost. Talk to Otto once if you have not started Barbarian Training.", Spots.OTTOS, 15, "Fishing spot", "Otto Godblessed"))
			.step(GuideSteps.train("Fish the leaping fish. Drop them when the inventory is full. The guide returns you to dropping, not the bank.", Spots.OTTOS, AdvanceWhen.INVENTORY_FULL, "Fishing spot").npcIds(NpcID.FISHING_SPOT_1542))
			.step(GuideSteps.drop("Drop the leaping fish. Keep the rod and bait."))
			.save();
		TrainingMethod.builder(out, Skill.FISHING, "Tempoross")
			.tool("Harpoon", ItemID.HARPOON, ItemID.DRAGON_HARPOON, ItemID.DRAGON_HARPOON_OR, ItemID.INFERNAL_HARPOON, ItemID.INFERNAL_HARPOON_UNCHARGED)
			.levels(35, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(5)
			.xp(0, 50000)
			.summary("Tempoross is the Fishing minigame, boarded from the spirit anglers south of Al Kharid. It is better rewards than barbarian fishing and a solid XP rate. Solo games are more XP. Mass games are calmer. Bring a harpoon.")
			.wiki("Tempoross")
			.opt("Barbarian fishing is the method to use if you also want Strength and Agility XP.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw a harpoon or a dragon harpoon. Leave food behind. The minigame supplies the rest.", Spots.GE))
			.step(GuideSteps.travel("Talk to a spirit angler at the docks south of Al Kharid and board the boat.", Spots.TEMPOROSS, 18, "Spirit Angler"))
			.step(GuideSteps.train("Play Tempoross. Cook the harpoonfish on the shrine's fire when the energy is high, and douse fires when the spirit attacks. Start another game when you wash ashore.", Spots.TEMPOROSS, AdvanceWhen.MANUAL, "Spirit pool", "Fishing spot"))
			.save();
		fish(out, "Monkfish", 62, 81, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 120, 35000,
			"Monkfish at the Piscatoris Fishing Colony. Swan Song is required. They are quiet and decent food. Bank at the colony.",
			"Raw monkfish", Spots.PISCATORIS, "Withdraw a small fishing net.", Spots.PISCATORIS, true);
		TrainingMethod.builder(out, Skill.FISHING, "Anglerfish")
			.levels(82, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK, TrainingStyle.PROFIT)
			.priority(5)
			.xp(120, 23000)
			.summary("Fish anglerfish in Port Piscarilius, south of Tynan's Fishing Supplies. Bring a fishing rod and sandworms. Bank at Port Piscarilius. The fishing spot moves, so the guide finds its NPC instead of marking a fixed tile.")
			.wiki("Raw anglerfish")
			.tool("Fishing rod", ItemID.FISHING_ROD, ItemID.PEARL_FISHING_ROD)
			.supplies("Sandworms", 1, ItemID.SANDWORMS)
			.loop(0)
			.step(GuideSteps.anyBank("Withdraw a fishing rod and sandworms. Use the Port Piscarilius bank between inventories."))
			.step(GuideSteps.find("Travel to Port Piscarilius. Find the rod fishing spots south of Tynan's Fishing Supplies.", "Rod Fishing spot").npcIds(NpcID.ROD_FISHING_SPOT_6825))
			.step(GuideSteps.roam("Bait the highlighted anglerfish spot until your inventory is full, then bank in Port Piscarilius.", AdvanceWhen.INVENTORY_FULL, "Rod Fishing spot").npcIds(NpcID.ROD_FISHING_SPOT_6825))
			.save();
	}

	private static void fish(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String summary, String rawName, WorldPoint bank, String bankText, WorldPoint spot, boolean bankFish)
	{
		TrainingMethod.Builder builder = TrainingMethod.builder(out, Skill.FISHING, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(name)
			.opt("The best tool you can use is worth more than a slightly better spot.")
			.loop(bankFish ? 0 : 2)
			.step(GuideSteps.bank(bankText, bank))
			.step(GuideSteps.travel("Walk to the fishing spot.", spot, 18, "Fishing spot"));
		if (name.equals("Trout and salmon") || name.equals("Fly fishing"))
		{
			builder.tool("Fly fishing rod", ItemID.FLY_FISHING_ROD, ItemID.PEARL_FLY_FISHING_ROD)
				.item(Materials.supply("Feathers", 1)).itemPreparation(1);
		}
		else if (name.equals("Lobsters"))
		{
			builder.tool("Lobster pot", ItemID.LOBSTER_POT);
		}
		else if (name.equals("Swordfish"))
		{
			builder.tool("Harpoon", ItemID.HARPOON, ItemID.DRAGON_HARPOON);
		}
		else
		{
			builder.tool("Small fishing net", ItemID.SMALL_FISHING_NET).itemPreparation(1);
		}
		if (bankFish)
		{
			builder.step(GuideSteps.train("Fish until the inventory is full, then bank.", spot, AdvanceWhen.INVENTORY_FULL, "Fishing spot"));
		}
		else
		{
			builder.step(GuideSteps.train("Fish until the inventory is full.", spot, AdvanceWhen.INVENTORY_FULL, "Fishing spot"));
			builder.step(GuideSteps.drop("Drop the fish. Keep the rod and the feathers."));
			builder.banking("Fish at the highlighted spot. Keep the fish and deposit them at the bank when full, then return to the fishing spot.",
				GuideSteps.bank("Deposit the fish and keep your fishing tool and bait. Close the bank when you have inventory space.", bank),
				GuideSteps.travel("Return to the fishing spot.", spot, 18, "Fishing spot"),
				GuideSteps.train("Fish until the inventory is full, then return to the bank.", spot, AdvanceWhen.INVENTORY_FULL, "Fishing spot"));
		}
		if (name.equals("Monkfish"))
		{
			builder.quest(Quest.SWAN_SONG);
		}
		builder.save();
	}

	private static void hunter(List<TrainingMethod> out)
	{
		trap(out, "Crimson swifts", 1, 19, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 9000,
			"Lay bird snares on crimson swifts in the Feldip Hills, south of Castle Wars. Bring bird snares. This is the start of Hunter.",
			Spots.CASTLE_WARS, "Withdraw bird snares. A few dozen is enough to start.", Spots.FELDIP_SWIFTS, "Crimson swift", "Bird snare");
		trap(out, "Copper longtails", 20, 42, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 18000,
			"Travel to the Piscatoris hunter area and snare copper longtails. They are the next bird. Bank at Castle Wars if you need more snares.",
			Spots.CASTLE_WARS, "Withdraw bird snares.", Spots.PISCATORIS_HUNTER, "Copper longtail", "Bird snare");
		TrainingMethod.builder(out, Skill.HUNTER, "Falconry")
			.levels(43, 62)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK)
			.priority(3)
			.xp(0, 40000)
			.summary("Falconry is east of the Piscatoris hunter area. Talk to Matthias, borrow a gyr falcon, and catch spotted kebbits, then dark kebbits at 57. The falcon rental costs 500 coins. Bring a few hundred coins as the deposit.")
			.wiki("Falconry")
			.loop(1)
			.step(GuideSteps.bank("Withdraw 500 coins for the falcon deposit.", Spots.GE))
			.step(GuideSteps.travel("Go to the Piscatoris hunter area, then the falconry field. Talk to Matthias.", Spots.FALCONRY, 25, "Matthias", "Gyr Falcon"))
			.step(GuideSteps.train("Catch kebbits with the falcon until you want to return it. Spotted kebbits from 43, dark kebbits from 57.", Spots.FALCONRY, AdvanceWhen.MANUAL, "Spotted kebbit", "Dark kebbit", "Gyr Falcon"))
			.save();
		trap(out, "Red chinchompas", 63, 99, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK}, 4, 110000,
			"Box-trap red chinchompas in the Feldip Hills. They are the standard Hunter method and they make money. Bring box traps. A higher Hunter level fails less.",
			Spots.CASTLE_WARS, "Withdraw box traps.", Spots.FELDIP, "Red chinchompa", "Box trap");
		TrainingMethod.builder(out, Skill.HUNTER, "Black chinchompas")
			.risk(RouteRisk.WILDERNESS)
			.levels(73, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST, TrainingStyle.PROFIT)
			.priority(6)
			.xp(0, 160000)
			.summary("Black chinchompas are the fastest Hunter XP and good money. They are in level 32 Wilderness, in the hunting ground west of the Lava Maze. Other players can attack you. Bring only box traps. A royal seed pod only works at level 30 Wilderness or below. Run south before attempting to teleport and plan for teleblocks.")
			.wiki("Black chinchompa")
			.note("The rate assumes you are not interrupted. Deaths make it worse.")
			.opt("Red chinchompas are the safe version. Switch the style to Balanced.")
			.opt("Do a birdhouse run whenever the timer is up. It is the idle Hunter XP and it needs Bone Voyage plus clockwork.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw box traps only, plus one teleport you can afford to use. Leave the rest of your bank behind.", Spots.GE))
			.step(GuideSteps.travel("Go to the black chinchompa hunting area in level 32 Wilderness. This is dangerous.", new WorldPoint(3145, 3770, 0), 20, "Black chinchompa", "Box trap"))
			.step(GuideSteps.train("Lay the box traps and reset them when they fall. Run south to level 30 or below before using an eligible teleport. A teleblock can still prevent escape.", new WorldPoint(3145, 3770, 0), AdvanceWhen.MANUAL, "Black chinchompa", "Box trap"))
			.save();
	}

	private static void trap(List<TrainingMethod> out, String name, int min, int max, TrainingStyle[] styles, int priority, int xpHr, String summary, WorldPoint bank, String bankText, WorldPoint spot, String... highlights)
	{
		TrainingMethod.builder(out, Skill.HUNTER, name)
			.levels(min, max)
			.modes(AccountMode.P2P)
			.styles(styles)
			.priority(priority)
			.xp(0, xpHr)
			.summary(summary)
			.wiki(name)
			.opt("Birdhouse runs, after Bone Voyage, are the idle Hunter method. Do them between trap sessions. The Hunter Guild in Varlamore also offers rumours once you can start them.")
			.loop(1)
			.step(GuideSteps.bank(bankText, bank))
			.step(GuideSteps.travel("Walk to the hunting spot.", spot, 25, highlights))
			.step(GuideSteps.train("Lay and reset the traps. Press Bank when you need to empty the catch or restock traps.", spot, AdvanceWhen.MANUAL, highlights))
			.save();
	}

	private static void farming(List<TrainingMethod> out)
	{
		TrainingMethod.builder(out, Skill.FARMING, "Allotments")
			.levels(1, 14)
			.modes(AccountMode.P2P)
			.styles(ALL)
			.xp(0, 8000)
			.note("Farming XP per hour is XP divided by the minutes you actually spend, not the time the crops take to grow. A few thousand an hour of active time is normal this early.")
			.summary("Farming is a timed run. Rake and plant potatoes or onions, do something else while they grow, and come back. Use RuneLite's Time Tracking plugin for the timers. The Falador allotment is south of the city.")
			.wiki("Allotment patch")
			.opt("Fairytale I - Growing Pains unlocks magic secateurs later, which help herb runs. Do it when you can.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw a rake, a seed dibber, a spade, and potato or onion seeds.", Spots.GE))
			.step(GuideSteps.travel("The Falador allotment is south of Falador. Rake the weeds and plant the seeds.", Spots.FALADOR_EAST_BANK, 30))
			.step(GuideSteps.note("Wait for the crops to grow, then harvest them and plant again. Press Next to start the next run. Train another skill while you wait."))
			.save();

		TrainingMethod.builder(out, Skill.FARMING, "Herb and tree run")
			.levels(15, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.PROFIT)
			.priority(3)
			.xp(0, 80000)
			.note("Most of this XP happens in a few minutes of planting, then you wait. Trees are the XP. Herbs are the profit. The hourly figure is active time, not time spent logged off.")
			.summary("Do a tree run and a herb run, then leave. Plant the best tree, fruit tree, and herb you can. Ultracompost on herbs. Magic secateurs help once you have Fairytale I. The patches below are the easy ones. Add Troll Stronghold, Weiss, Harmony, and Civitas illa Fortis when you have the teleports.")
			.wiki("Herb run")
			.opt("Hardwood trees on Fossil Island and calquat trees are extra XP once a day or so.")
			.opt("Tithe Farm is active Farming XP if you do not want to wait on crops. It is in Hosidius.")
			.opt("The Farming Guild middle tier at 65 has an herb patch and contracts. Protect herbs with ultracompost; this patch is not disease-free.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw a spade, seed dibber, rake, the best seeds you can plant, ultracompost for the herbs, and teleports. Keep saplings unnoted for planting.", Spots.GE))
			.step(GuideSteps.travel("Falador: the allotment and herb patch are south of the east bank. Rake, plant, and compost.", Spots.FALADOR_EAST_BANK, 25))
			.step(GuideSteps.travel("Catherby: the patches are just north of the bank. A Camelot teleport is the usual arrival.", Spots.CATHERBY_BANK, 20))
			.step(GuideSteps.travel("Ardougne: the herb patch is north of the north bank.", Spots.ARDOUGNE_NORTH_BANK, 25))
			.step(GuideSteps.travel("Canifis: Priest in Peril is required. The patch is west of the bank, through the gate.", Spots.CANIFIS_BANK, 25))
			.step(GuideSteps.travel("Hosidius: the patch is south of the town. Xeric's talisman is the teleport.", Spots.HOSIDIUS, 25))
			.step(GuideSteps.travel("Farming Guild, once you have 65 Farming: plant and compost the herb patch inside. Skip this step with Next if you cannot enter yet.", Spots.FARMING_GUILD, 20))
			.step(GuideSteps.note("Plant any tree patches you brought saplings for, then stop. Come back when Time Tracking says the patches are grown. Herbs are about 80 minutes. Trees are hours."))
			.save();

		TrainingMethod.builder(out, Skill.FARMING, "Herb run")
			.levels(9, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK)
			.priority(2)
			.xp(0, 30000)
			.summary("Farming is not an idle skill you sit at. The idle version is: do the herb run, log out or train something else, and come back when the herbs have grown. Guam is level 9. Ranarr, snapdragon, and toadflax are the usual profit herbs. Check the seed price first.")
			.wiki("Herb run")
			.loop(0)
			.step(GuideSteps.note("Use the Balanced herb and tree run for the actual patch order. This style is a reminder that you should not stand at a patch and wait. Press Next after each run and go do another skill."))
			.save();
	}
}
