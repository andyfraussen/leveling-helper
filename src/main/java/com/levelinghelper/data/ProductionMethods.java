package com.levelinghelper.data;

import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.RouteRisk;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.List;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

final class ProductionMethods
{
	private static final TrainingStyle[] ALL = {
		TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT
	};

	private ProductionMethods()
	{
	}

	static void register(List<TrainingMethod> out)
	{
		cooking(out);
		firemaking(out);
		crafting(out);
		smithing(out);
		runecraft(out);
		fletching(out);
		herblore(out);
		construction(out);
	}

	private static void cooking(List<TrainingMethod> out)
	{
		cook(out, "Shrimps", 1, 14, AccountMode.F2P, ALL, 1, 30, 35000, "Raw shrimps",
			"Cook shrimps on the Al Kharid range. Buy them at the Grand Exchange if you do not have any. You will burn some until your level is higher. A range burns less than a fire.");
		cook(out, "Trout", 15, 24, AccountMode.F2P, ALL, 1, 70, 70000, "Raw trout",
			"Cook trout on the Al Kharid range. The bank is just south of the palace.");
		cook(out, "Salmon", 25, 39, AccountMode.F2P, ALL, 1, 90, 90000, "Raw salmon",
			"Cook salmon. Move to lobsters at 40 if you want less burning and better food.");
		cook(out, "Lobsters", 40, 44, AccountMode.F2P, ALL, 1, 120, 120000, "Raw lobster",
			"Cook lobsters on the Al Kharid range.");
		cook(out, "Swordfish", 45, 99, AccountMode.F2P, ALL, 2, 140, 140000, "Raw swordfish",
			"Swordfish are the free-to-play cooking method to 99. You keep burning some of them until the high 80s. Cook on a range, not a fire.");

		cookAt(out, "Shrimps", 1, 34, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 30, 40000, "Raw shrimps",
			"Cook at the Hosidius kitchen. The bank chest is in the same room, and the range burns less than a normal range. Use the best raw fish you can until wines at 35.",
			Spots.HOSIDIUS_KITCHEN, Spots.HOSIDIUS_KITCHEN);
		wines(out);
		cookAt(out, "Monkfish", 62, 79, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 150, 150000, "Raw monkfish",
			"Monkfish at the Hosidius kitchen are the quieter cooking method. Wines are much more XP. Check the Grand Exchange if you want the fish to profit.",
			Spots.HOSIDIUS_KITCHEN, Spots.HOSIDIUS_KITCHEN);
		cookAt(out, "Sharks", 80, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.AFK, TrainingStyle.PROFIT}, 4, 210, 170000, "Raw shark",
			"Sharks at the Hosidius kitchen. You stop burning them well before 99 if you use a range. Cooking gauntlets from Family Crest lower the burn level.",
			Spots.HOSIDIUS_KITCHEN, Spots.HOSIDIUS_KITCHEN);
	}

	private static void wines(List<TrainingMethod> out)
	{
		TrainingMethod.builder(out, Skill.COOKING, "Jugs of wine")
			.levels(35, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.FAST)
			.priority(5)
			.xp(200, 400000)
			.note("Successful wines give 200 XP. Fermentation can fail below 68 Cooking; failed wines give no XP. The rate is for normal clicking at the Grand Exchange. Faster clicking is higher.")
			.summary("Use grapes on jugs of water. Buy both at the Grand Exchange. This is the members cooking method from 35 to 99. It usually costs money. Cook fish instead if you want profit or something quieter.")
			.wiki("Jug of wine")
			.opt("Cook's Assistant is a small amount of early Cooking XP and is required for later quests.")
			.opt("The Hosidius kitchen is the AFK-style alternative: cook a full inventory of fish, bank, repeat.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw 14 grapes and 14 jugs of water. Buy more on the Grand Exchange if you are out.", Spots.GE))
			.step(GuideSteps.untilEmpty("Use the grapes on the jugs of water. Empty the finished wines at the bank.", Spots.GE, "Grapes"))
			.save();
	}

	private static void cook(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String raw, String summary)
	{
		cookAt(out, name, min, max, mode, styles, priority, xpEach, xpHr, raw, summary, Spots.AL_KHARID_BANK, Spots.AL_KHARID_RANGE);
	}

	private static void cookAt(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String raw, String summary, WorldPoint bank, WorldPoint range)
	{
		TrainingMethod.builder(out, Skill.COOKING, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(name)
			.opt("Buy the raw food at the Grand Exchange. The bank step is the range's nearest bank.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw the raw food. Deposit the cooked food.", bank))
			.step(GuideSteps.travel("Walk to the range.", range, 12, "Range", "Cooking range"))
			.step(GuideSteps.untilEmpty("Cook the food on the range. Burnt food can be dropped.", range, raw, "Range"))
			.save();
	}

	private static void firemaking(List<TrainingMethod> out)
	{
		burn(out, "Logs", 1, 14, AccountMode.F2P, ALL, 1, 40, 40000, "Logs");
		burn(out, "Oak logs", 15, 29, AccountMode.F2P, ALL, 1, 60, 65000, "Oak logs");
		burn(out, "Willow logs", 30, 59, AccountMode.F2P, ALL, 1, 90, 100000, "Willow logs");
		burn(out, "Yew logs", 60, 99, AccountMode.F2P, ALL, 2, 202.5, 180000, "Yew logs");

		burn(out, "Logs", 1, 14, AccountMode.P2P, ALL, 1, 40, 40000, "Logs");
		burn(out, "Oak logs", 15, 29, AccountMode.P2P, ALL, 1, 60, 65000, "Oak logs");
		burn(out, "Willow logs", 30, 49, AccountMode.P2P, ALL, 1, 90, 110000, "Willow logs");
		TrainingMethod.builder(out, Skill.FIREMAKING, "Wintertodt")
			.levels(50, 99)
			.modes(AccountMode.P2P)
			.styles(ALL)
			.priority(5)
			.xp(0, 220000)
			.note("Mass games are calmer and a bit less XP. Solo games are more. Warm clothing and your Hitpoints level change how much you eat.")
			.summary("Wintertodt is the members Firemaking method from 50. A games necklace teleports to the camp. Bring food, an axe, a knife, a hammer, and a tinderbox. Four pieces of warm clothing reduce the cold. Fletch the bruma roots into kindling, feed the brazier, and heal when the cold hits you. You also get loot.")
			.wiki("Wintertodt")
			.opt("Lighting magic or redwood logs in a line at the Grand Exchange is the alternative if you do not want the minigame. It burns the logs.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw food, an axe, a knife, a hammer, and a tinderbox. Wear warm clothing if you have it.", Spots.GE))
			.step(GuideSteps.travel("Teleport with a games necklace to Wintertodt, or walk to the camp. The bank chest is just inside the doors.", Spots.WINTERTODT_BANK, 20, "Bank chest", "Doors of Dinh"))
			.step(GuideSteps.train("Subdue the Wintertodt. Chop the bruma roots, fletch them, and feed a brazier. Start another game when it ends.", Spots.WINTERTODT, AdvanceWhen.MANUAL, "Bruma roots", "Brazier"))
			.save();
	}

	private static void burn(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String item)
	{
		TrainingMethod.builder(out, Skill.FIREMAKING, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary("Light " + item.toLowerCase() + " in a line just south of the Grand Exchange, so the fires do not block the bankers. Bring a tinderbox. Buy the logs on the Grand Exchange.")
			.wiki(name)
			.loop(0)
			.step(GuideSteps.bank("Withdraw a tinderbox and an inventory of " + item.toLowerCase() + ".", Spots.GE))
			.step(GuideSteps.untilEmpty("Use a clear east-west line south of the exchange to light logs. Lighting a fire moves you west; avoid blocking the bankers.", Spots.GE, item))
			.save();
	}

	private static void crafting(List<TrainingMethod> out)
	{
		craft(out, "Leather", 1, 99, AccountMode.F2P, ALL, 1, 25, 20000, "Leather",
			"Free-to-play Crafting is leather. Buy leather, a needle, and thread. Make the highest leather item you can: gloves at 1, then boots, cowl, vambraces, bodies at 14, chaps at 18, and hardleather bodies at 28. Do it at the Grand Exchange.");
		craft(out, "Leather", 1, 19, AccountMode.P2P, ALL, 1, 25, 25000, "Leather",
			"Leather until you can cut gems. Needle and thread stay in the inventory. Members move on to gems quickly.");
		craft(out, "Sapphires", 20, 26, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 50, 120000, "Uncut sapphire",
			"Cut uncut sapphires at the Grand Exchange. A chisel stays in the inventory.");
		craft(out, "Emeralds", 27, 33, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 67.5, 140000, "Uncut emerald",
			"Cut uncut emeralds.");
		craft(out, "Rubies", 34, 42, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK}, 2, 85, 160000, "Uncut ruby",
			"Cut uncut rubies.");
		craft(out, "Diamonds", 43, 62, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 2, 107.5, 180000, "Uncut diamond",
			"Cut uncut diamonds. Check the margin. If another gem profits more today, cut that and press Next when the inventory is done.");
		craft(out, "Dragonstones", 55, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.FAST}, 4, 137.5, 250000, "Uncut dragonstone",
			"Cutting dragonstones is the fast, expensive Crafting method. Only buy what you can afford to lose on the margin.");
		craft(out, "Green d'hide bodies", 63, 70, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 186, 180000, "Green dragon leather",
			"Green dragonhide bodies. You need a needle, thread, and green dragon leather. Often cheaper XP than cutting dragonstones.");
		craft(out, "Blue d'hide bodies", 71, 76, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 210, 190000, "Blue dragon leather",
			"Blue dragonhide bodies.");
		craft(out, "Red d'hide bodies", 77, 83, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 234, 200000, "Red dragon leather",
			"Red dragonhide bodies.");
		craft(out, "Black d'hide bodies", 84, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 258, 210000, "Black dragon leather",
			"Black dragonhide bodies are the usual balanced Crafting method to 99.");
	}

	private static void craft(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String item, String summary)
	{
		TrainingMethod.builder(out, Skill.CRAFTING, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary + " Crafting is click-heavy. There is no truly idle version.")
			.wiki(name)
			.opt("Check the Grand Exchange margin before you buy a large stack. Fast methods are often a loss.")
			.opt("Battlestaves at 66, if you have orbs, are another standard method and sometimes profit.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw the materials. Keep your needle, thread, or chisel in the inventory.", Spots.GE))
			.step(GuideSteps.untilEmpty("Make the items, then bank them.", Spots.GE, item))
			.save();
	}

	private static void smithing(List<TrainingMethod> out)
	{
		smith(out, "Bronze", 1, 17, 6.2, 20000, "Bronze bar", "Smith bronze items at the Varrock west anvil. Buy bronze bars. Doric's Quest gives 1,300 Smithing XP and is free-to-play.");
		smith(out, "Bronze platebodies", 18, 32, 62.5, 50000, "Bronze bar", "Smith bronze platebodies. They are 62.5 XP each.");
		smith(out, "Iron platebodies", 33, 47, 125, 90000, "Iron bar", "Smith iron platebodies. Buying the bars avoids the 50% iron smelting failure, which you cannot remove in free-to-play.");
		smith(out, "Steel platebodies", 48, 67, 187.5, 140000, "Steel bar", "Smith steel platebodies.");
		smith(out, "Mithril platebodies", 68, 87, 250, 180000, "Mithril bar", "Smith mithril platebodies.");
		smith(out, "Adamant platebodies", 88, 99, 312.5, 220000, "Adamantite bar", "Smith adamant platebodies to 99. Rune platebodies are level 99, so they are not the training method.");

		smithMembers(out, "Bronze", 1, 14, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 6.2, 30000, "Bronze bar",
			"Smith bronze at the Varrock west anvil until Giants' Foundry at 15. Knight's Sword gives about 12,725 Smithing XP and skips this on a members account.");
		TrainingMethod.builder(out, Skill.SMITHING, "Giants' Foundry")
			.quest(Quest.SLEEPING_GIANTS)
			.levels(15, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.PROFIT)
			.priority(4)
			.xp(0, 180000)
			.summary("Giants' Foundry, east of Al Kharid, is the balanced Smithing method. You alloy two metals, pour the mould, and hammer the sword. It uses fewer bars than smithing platebodies and pays you in foundry rewards. Bring a hammer. The preform storage lets you bank less often once you unlock it.")
			.wiki("Giants' Foundry")
			.opt("Use the best metal pair the founder recommends for your level. Higher metals are more XP.")
			.opt("Knight's Sword is about 12,725 Smithing XP. Do it early.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw a hammer and the two types of bar you plan to alloy. The foundry tells you the useful pairs.", Spots.GE))
			.step(GuideSteps.travel("Go to Giants' Foundry, east of Al Kharid.", Spots.GIANTS_FOUNDRY, 18, "Kovac"))
			.step(GuideSteps.train("Commission a sword, pour the crucible, and work the preform. Hand it in, then start another. Press Bank when you need bars.", Spots.GIANTS_FOUNDRY, AdvanceWhen.MANUAL, "Crucible", "Mould jig"))
			.save();
		TrainingMethod.builder(out, Skill.SMITHING, "Blast Furnace gold")
			.quest(Quest.FAMILY_CREST)
			.prepare("Start The Giant Dwarf to enter Keldagrim. Use a Blast Furnace world and fund the coffer; under 60 Smithing also pay the foreman.")
			.prepare("Wear goldsmith gauntlets until the XP drop. Swap to ice gloves to collect bars, then swap back. These share the same equipment slot.")
			.levels(40, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(6)
			.xp(56.2, 300000)
			.note("Gold at the Blast Furnace is 56.2 XP per ore with goldsmith gauntlets. The rate assumes you keep the furnace moving.")
			.summary("Blast Furnace gold is the fast Smithing method. You want goldsmith gauntlets from Family Crest, ice gloves or the smiths' gloves, and a coal bag is optional for other bars. Pay the foreman if you are under 60 Smithing. Take the minecart from the Grand Exchange to Keldagrim.")
			.wiki("Blast Furnace")
			.opt("Steel or mithril at the Blast Furnace is often better money and less XP than gold.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw gold ore. Bring goldsmith gauntlets and ice gloves. Equip the goldsmith gauntlets for the XP drop, then swap to ice gloves to collect bars. Bring coins for the foreman if you are under 60.", Spots.GE))
			.step(GuideSteps.travel("Take the minecart to Keldagrim and enter the Blast Furnace.", Spots.BLAST_FURNACE, 20, "Conveyor belt"))
			.step(GuideSteps.train("Put gold ore on the conveyor, cool the bars, and collect them from the dispenser. Repeat until the ore is gone, then press Bank.", Spots.BLAST_FURNACE, AdvanceWhen.MANUAL, "Conveyor belt", "Dispenser"))
			.save();
		TrainingMethod.builder(out, Skill.SMITHING, "Cannonballs")
			.quest(Quest.DWARF_CANNON)
			.levels(35, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK)
			.priority(3)
			.xp(25.5, 15000)
			.summary("Cannonballs at the Edgeville furnace are the idle Smithing method. You need the ammo mould from Dwarf Cannon, and steel bars. It is about four cannonballs a bar and very slow XP. Smith them while you do something else.")
			.wiki("Cannonball")
			.opt("Giants' Foundry is far more XP if you are willing to pay attention.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw an ammo mould and steel bars.", Spots.EDGEVILLE_BANK))
			.step(GuideSteps.travel("Use the Edgeville furnace, next to the bank.", Spots.EDGEVILLE_FURNACE, 10, "Furnace"))
			.step(GuideSteps.untilEmpty("Smith cannonballs. Each bar takes a while. Bank when the bars are gone.", Spots.EDGEVILLE_FURNACE, "Steel bar", "Furnace"))
			.save();
	}

	private static void smith(List<TrainingMethod> out, String name, int min, int max, double xpEach, int xpHr, String bar, String summary)
	{
		smithAtAnvil(out, name, min, max, ALL, 1, xpEach, xpHr, bar, summary + " The anvil is south of Varrock west bank.", AccountMode.F2P, AccountMode.P2P);
	}

	private static void smithMembers(List<TrainingMethod> out, String name, int min, int max, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String bar, String summary)
	{
		smithAtAnvil(out, name, min, max, styles, priority, xpEach, xpHr, bar, summary, AccountMode.P2P);
	}

	private static void smithAtAnvil(List<TrainingMethod> out, String name, int min, int max, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String bar, String summary, AccountMode... modes)
	{
		TrainingMethod.builder(out, Skill.SMITHING, name)
			.levels(min, max)
			.modes(modes)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(name)
			.loop(0)
			.step(GuideSteps.bank("Withdraw " + bar.toLowerCase() + "s and a hammer.", Spots.VARROCK_WEST_BANK))
			.step(GuideSteps.travel("Walk to the anvil just south-west of the bank.", Spots.VARROCK_ANVIL, 12, "Anvil"))
			.step(GuideSteps.untilEmpty("Smith the items until the bars are gone.", Spots.VARROCK_ANVIL, bar, "Anvil"))
			.save();
	}

	private static void runecraft(List<TrainingMethod> out)
	{
		altar(out, "Air runes", 1, 8, AccountMode.F2P, ALL, 1, 5, 8000, Spots.AIR_RUINS, "Air tiara",
			"Craft air runes. Wear an air tiara so the inventory can be all essence. Aubury in Varrock east will teleport you to the essence mine.");
		altar(out, "Earth runes", 9, 13, AccountMode.F2P, ALL, 1, 6.5, 10000, Spots.EARTH_RUINS, "Earth tiara",
			"Earth runes. The ruins are north-east of Varrock.");
		altar(out, "Fire runes", 14, 19, AccountMode.F2P, ALL, 1, 7, 12000, Spots.FIRE_RUINS, "Fire tiara",
			"Fire runes. The ruins are north-east of Al Kharid.");
		altar(out, "Body runes", 20, 99, AccountMode.F2P, ALL, 2, 7.5, 14000, Spots.BODY_RUINS, "Body tiara",
			"Body runes are the free-to-play Runecraft method to 99, and they are also the money method. The ruins are south of the Monastery.");

		altar(out, "Air runes", 1, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 5, 10000, Spots.AIR_RUINS, "Air tiara",
			"Air runes until Guardians of the Rift. Use pure essence. Complete Temple of the Eye when you can.");
		TrainingMethod.builder(out, Skill.RUNECRAFT, "Guardians of the Rift")
			.quest(Quest.TEMPLE_OF_THE_EYE)
			.levels(27, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT)
			.priority(4)
			.xp(0, 45000)
			.summary("Guardians of the Rift is the members Runecraft method. Complete Temple of the Eye, then enter from the Wizard's Tower basement. Mine fragments, craft runes at the altars the portal opens, and charge the guardian. You also earn the Raiments outfit. The minigame gets better as your level unlocks more altars.")
			.wiki("Guardians of the Rift")
			.opt("Ourania altar, west of the Castle Wars battlefield, is a simple essence run if you want a break from the minigame.")
			.opt("From 77, blood runes in Arceuus are the idle option: mine dense essence at the Dark Altar, chisel it, and craft at the blood altar.")
			.loop(1)
			.step(GuideSteps.note("Complete Temple of the Eye if you have not. Bring a pickaxe and a chisel. Press Next when that is done."))
			.step(GuideSteps.travel("Go to the Wizard's Tower south of Draynor, climb to the basement, and enter the rift.", Spots.WIZARDS_TOWER, 15))
			.step(GuideSteps.train("Play a game of Guardians of the Rift. Start another when it ends.", Spots.WIZARDS_TOWER, AdvanceWhen.MANUAL, "Guardian remains", "Portal"))
			.save();
		TrainingMethod.builder(out, Skill.RUNECRAFT, "Lava runes")
			.levels(23, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(6)
			.xp(0, 60000)
			.note("A relaxed lava-rune rate. The duel-ring loop is faster if you click harder, and this guide does not time the clicks for you.")
			.summary("Lava runes are the fast Runecraft method. Use earth runes on the fire altar with a binding necklace, plus an earth talisman for each trip unless Magic Imbue is active. A ring of dueling to the Emir's Arena, or Castle Wars, and back to the altar, is the usual loop. Bring pure essence. It costs money.")
			.wiki("Lava rune")
			.opt("Magic Imbue removes the need to carry the talisman combination. It is a Lunar spell.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw pure essence, earth runes, a binding necklace, an earth talisman (unless using Magic Imbue), and a ring of dueling. Wear a fire tiara if you have one.", Spots.GE))
			.step(GuideSteps.travel("Go to the fire altar ruins north-east of Al Kharid.", Spots.FIRE_RUINS, 12, "Mysterious ruins"))
			.step(GuideSteps.note("Enter the ruins, use the earth runes on the altar, and teleport back with the dueling ring. Press Next to bank and repeat."))
			.save();
	}

	private static void altar(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, double xpEach, int xpHr, WorldPoint ruins, String tiara, String summary)
	{
		String essence = mode == AccountMode.F2P ? "Rune essence" : "Pure essence";
		TrainingMethod.builder(out, Skill.RUNECRAFT, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.summary(summary)
			.wiki(name)
			.opt("A tiara in the head slot holds more essence than a talisman in the inventory. Use the talisman on the altar once to make the tiara.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw " + essence.toLowerCase() + " and wear a " + tiara.toLowerCase() + ". Aubury, in the rune shop by Varrock east bank, teleports you to the essence mine.", Spots.VARROCK_EAST_BANK))
			.step(GuideSteps.travel("Go to the mysterious ruins. The tiara lets you enter.", ruins, 12, "Mysterious ruins"))
			.step(GuideSteps.untilEmpty("Enter the ruins and craft the runes at the altar, then leave and bank.", null, essence, "Altar"))
			.save();
	}

	private static void fletching(List<TrainingMethod> out)
	{
		fletch(out, "Arrow shafts", 1, 9, ALL, 1, 5, 20000, "Logs",
			"Use a knife on logs to cut arrow shafts. This is only the first few levels.");
		fletch(out, "Longbows", 10, 24, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 10, 80000, "Logs",
			"Cut logs into longbows (u). String them afterwards if the bow string margin is good.");
		fletch(out, "Oak longbows", 25, 39, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 25, 120000, "Oak logs",
			"Cut oak logs into oak longbows (u).");
		fletch(out, "Willow longbows", 40, 54, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 1, 41.5, 160000, "Willow logs",
			"Cut willow longbows (u).");
		fletch(out, "Maple longbows", 55, 69, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 2, 58.3, 200000, "Maple logs",
			"Cut maple longbows (u). Stringing maple longbows is a common profit check.");
		fletch(out, "Yew longbows", 70, 84, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 2, 75, 230000, "Yew logs",
			"Cut yew longbows (u).");
		fletch(out, "Magic longbows", 85, 99, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT}, 3, 91.5, 260000, "Magic logs",
			"Cut magic longbows (u). This is the quiet fletching method to 99.");
		fletch(out, "Mithril darts", 52, 66, new TrainingStyle[]{TrainingStyle.FAST}, 4, 0, 400000, "Mithril dart tip",
			"Add feathers to mithril dart tips. Darts are the fast Fletching method. Buy the tips and the feathers.");
		fletch(out, "Adamant darts", 67, 80, new TrainingStyle[]{TrainingStyle.FAST}, 5, 0, 550000, "Adamant dart tip",
			"Add feathers to adamant dart tips.");
		fletch(out, "Rune darts", 81, 99, new TrainingStyle[]{TrainingStyle.FAST}, 6, 0, 750000, "Rune dart tip",
			"Add feathers to rune dart tips. Dragon darts at 95 are faster and more expensive.");
		fletch(out, "Iron darts", 22, 51, new TrainingStyle[]{TrainingStyle.FAST}, 3, 0, 250000, "Iron dart tip",
			"Add feathers to iron dart tips until mithril darts at 52. Steel darts at 37 are a small upgrade if the tips are cheap.");
		fletch(out, "Bronze darts", 10, 21, new TrainingStyle[]{TrainingStyle.FAST}, 2, 0, 150000, "Bronze dart tip",
			"Add feathers to bronze dart tips.");
	}

	private static void fletch(List<TrainingMethod> out, String name, int min, int max, TrainingStyle[] styles, int priority, double xpEach, int xpHr, String item, String summary)
	{
		TrainingMethod.builder(out, Skill.FLETCHING, name)
			.levels(min, max)
			.modes(AccountMode.P2P)
			.styles(styles)
			.priority(priority)
			.xp(xpEach, xpHr)
			.note(xpEach <= 0
				? "Darts are a small amount of XP each. The hourly figure is a normal clicking pace, not a record."
				: "Typical rate while making these at the Grand Exchange.")
			.summary(summary + " Fletching is members only. Do it at the Grand Exchange.")
			.wiki(name)
			.opt("Stringing bows is a second action of the same XP and is often where the profit is. Check the price of the unstrung bow against the strung bow and the bow string.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw the materials. Keep a knife in the inventory if you are cutting logs.", Spots.GE))
			.step(GuideSteps.untilEmpty("Make the items until the materials in this step are gone.", Spots.GE, item))
			.save();
	}

	private static void herblore(List<TrainingMethod> out)
	{
		potion(out, "Attack potions", 3, 11, 25, "Guam potion (unf)", "Make attack potions: use an eye of newt on a guam potion (unf).");
		potion(out, "Strength potions", 12, 25, 50, "Tarromin potion (unf)", "Make strength potions with limpwurt roots.");
		potion(out, "Energy potions", 26, 37, 67.5, "Harralander potion (unf)", "Make energy potions with chocolate dust.");
		potion(out, "Prayer potions", 38, 44, 87.5, "Ranarr potion (unf)", "Make prayer potions with snape grass. These are often expensive XP.");
		potion(out, "Super attack", 45, 54, 100, "Irit potion (unf)", "Make super attack potions with eyes of newt.");
		potion(out, "Super strength", 55, 62, 125, "Kwuarm potion (unf)", "Make super strength potions with limpwurt roots.");
		potion(out, "Super restores", 63, 71, 142.5, "Snapdragon potion (unf)", "Make super restores with red spiders' eggs.");
		potion(out, "Ranging potions", 72, 80, 162.5, "Dwarf weed potion (unf)", "Make ranging potions with wine of zamorak.");
		potion(out, "Saradomin brews", 81, 89, 180, "Toadflax potion (unf)", "Make Saradomin brews with crushed bird nests.");
		potion(out, "Super combat", 90, 99, 150, "Torstol potion (unf)", "Make super combat potions. You need a torstol potion (unf) plus super attack, super strength, and super defence.");

		TrainingMethod.builder(out, Skill.HERBLORE, "Clean herbs")
			.quest(Quest.DRUIDIC_RITUAL)
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.PROFIT)
			.priority(2)
			.xp(0, 20000)
			.summary("Cleaning grimy herbs is often profit and poor XP. You can clean herbs above your potion level. Do it at the Grand Exchange, and check that the clean herb sells for more than the grimy one before you buy a big stack. Complete Druidic Ritual or Herblore is locked.")
			.wiki("Grimy guam leaf")
			.opt("Making potions is the XP. Cleaning herbs is the money.")
			.loop(0)
			.step(GuideSteps.note("Complete Druidic Ritual if Herblore is still locked. Press Next after that."))
			.step(GuideSteps.bank("Withdraw grimy herbs.", Spots.GE))
			.step(GuideSteps.note("Clean the herbs and bank them. Press Next when the inventory is done."))
			.save();
	}

	private static void potion(List<TrainingMethod> out, String name, int min, int max, double xpEach, String unfinished, String summary)
	{
		TrainingMethod.builder(out, Skill.HERBLORE, name)
			.quest(Quest.DRUIDIC_RITUAL)
			.levels(min, max)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK)
			.xp(xpEach, 250000)
			.note("Potion XP per hour is mostly how fast you click and bank. Herblore is not idle. This is a normal pace.")
			.summary(summary + " Complete Druidic Ritual first or you cannot train the skill. Buy the unfinished potions and the secondary at the Grand Exchange. Herblore is usually a loss. Clean herbs if you want profit.")
			.wiki(name)
			.opt("Mastering Mixology, on the coast south of Aldarin, is the herblore minigame. It spends pastes instead of a single potion and is worth learning once you want a change from combining.")
			.opt("Make the potion with the best XP for the money you can spend. The one named here is the straightforward choice, not always the cheapest.")
			.loop(1)
			.step(GuideSteps.note("If you have not completed Druidic Ritual, do that first. Press Next when Herblore is unlocked."))
			.step(GuideSteps.bank(name.equals("Super combat")
				? "Withdraw 7 torstol potions (unf), 7 super attack (4), 7 super strength (4), and 7 super defence (4)."
				: "Withdraw 14 " + unfinished.toLowerCase() + " and 14 of the secondary ingredient.", Spots.GE))
			.step(GuideSteps.untilEmpty("Combine the potions.", Spots.GE, unfinished))
			.save();
	}

	private static void construction(List<TrainingMethod> out)
	{
		TrainingMethod.builder(out, Skill.CONSTRUCTION, "Mahogany Homes")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.PROFIT)
			.xp(0, 120000)
			.summary("Mahogany Homes is the cheaper, calmer Construction method. Talk to Amy in Falador, just west of the east bank. She gives you a contract in a city. Bring a saw, a hammer, teleports, and the plank type for your tier. Build what the contract asks, talk to the homeowner, and take another contract. Higher contracts need higher Construction and better planks.")
			.wiki("Mahogany Homes")
			.opt("Oak larders and mahogany tables in your own house are faster and much more expensive. Switch the style to Fastest XP.")
			.opt("A servant in your house is what makes the fast method bearable. A demon butler carries planks.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw a saw, a hammer, the right planks, steel bars if the contract needs them, and teleports to Falador, Varrock, Ardougne, and Hosidius.", Spots.GE))
			.step(GuideSteps.travel("Talk to Amy in Falador, west of the east bank, and take a contract.", Spots.AMY, 12, "Amy"))
			.step(GuideSteps.note("Go to the city on the contract, build the furniture, and talk to the homeowner. Press Next and return to Amy, or take the next contract from the notice."))
			.save();

		TrainingMethod.builder(out, Skill.CONSTRUCTION, "Oak larders")
			.levels(33, 51)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(4)
			.xp(480, 350000)
			.note("Oak larders are 480 XP and cost 8 oak planks. A butler changes the rate more than the object does.")
			.summary("Build oak larders in your player-owned house. Call the butler to fetch more planks from the bank. Rimmington is the usual portal because the house advert and the shop are there. You need enough planks and servant money.")
			.wiki("Oak larder")
			.loop(0)
			.step(GuideSteps.bank("Withdraw oak planks and coins for the butler. Bring a saw and hammer.", Spots.GE))
			.step(GuideSteps.travel("Go to the Rimmington house portal and enter your house.", Spots.RIMMINGTON_PORTAL, 12, "Portal"))
			.step(GuideSteps.note("Build oak larders, remove them, and have the butler bring more planks. Press Next when you need to bank again."))
			.save();

		TrainingMethod.builder(out, Skill.CONSTRUCTION, "Mahogany tables")
			.levels(52, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(5)
			.xp(840, 700000)
			.note("Mahogany tables are 840 XP and cost 6 mahogany planks. Butler speed decides the real rate.")
			.summary("Build mahogany tables the same way as oak larders. This is the fast, expensive Construction method from 52. Mahogany Homes is the version that does not empty the bank as quickly.")
			.wiki("Mahogany table")
			.loop(0)
			.step(GuideSteps.bank("Withdraw mahogany planks and butler money.", Spots.GE))
			.step(GuideSteps.travel("Enter your house from the Rimmington portal.", Spots.RIMMINGTON_PORTAL, 12, "Portal"))
			.step(GuideSteps.note("Build and remove mahogany tables. Send the butler for planks. Press Next to restock."))
			.save();

		TrainingMethod.builder(out, Skill.CONSTRUCTION, "Wooden furniture")
			.levels(1, 32)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.xp(0, 30000)
			.summary("Before oak larders at 33, build the highest wooden or oak furniture your level allows, or just do Mahogany Homes. Homes is the better default. This fast route is only here so the early levels have a house method.")
			.wiki("Construction")
			.loop(0)
			.step(GuideSteps.bank("Withdraw planks, a saw, and a hammer.", Spots.GE))
			.step(GuideSteps.travel("Enter your house at Rimmington.", Spots.RIMMINGTON_PORTAL, 12, "Portal"))
			.step(GuideSteps.note("Build the best furniture you can, remove it, and repeat. Press Next to bank."))
			.save();
	}
}
