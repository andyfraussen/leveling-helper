package com.levelinghelper.data;

import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

final class CombatMethods
{
	private static final String COMBAT_NOTE = "Combat XP per hour depends on your weapon and bonuses. This is a rough mid-gear average. The session rate is your real number.";
	private static final TrainingStyle[] ALL = {
		TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT
	};

	private CombatMethods()
	{
	}

	static void register(List<TrainingMethod> out)
	{
		Skill[] melee = {Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS, Skill.RANGED};
		for (Skill skill : melee)
		{
			freeCombat(out, skill);
			memberCombat(out, skill);
		}
		freeMagic(out);
		memberMagic(out);
		prayer(out);
		slayer(out);
	}

	private static void freeCombat(List<TrainingMethod> out, Skill skill)
	{
		fight(out, skill, "Cows", 1, 19, AccountMode.F2P, ALL, 1, 12000,
			"Kill the cows east of Lumbridge. Bury the bones if you want a little Prayer XP, and bank the hides if you want Crafting later.",
			"Cow",
			Spots.LUMBRIDGE_BANK,
			gear(skill) + " Lumbridge bank is upstairs in the castle.",
			Spots.LUMBRIDGE_COWS,
			"Walk to the cow field east of Lumbridge castle.",
			"Cow");
		fight(out, skill, "Hill giants", 20, 39, AccountMode.F2P, ALL, 1, 22000,
			"Hill giants are the standard free-to-play step after cows. They drop big bones and limpwurts. Bring food.",
			"Hill giant",
			Spots.EDGEVILLE_BANK,
			gear(skill) + " Withdraw food. Deposit the bones and loot when you come back.",
			Spots.HILL_GIANTS,
			"Climb down the trapdoor in the building south of Edgeville bank, then walk to the hill giants.",
			"Hill Giant");
		fight(out, skill, "Ogresses", 40, 99, AccountMode.F2P, ALL, 1, 30000,
			"Ogresses in the Corsair Cove Resource Area are the best free-to-play combat training that also makes money. They hit hard, so bring food and decent armour.",
			"Ogress Warrior",
			Spots.DRAYNOR_BANK,
			gear(skill) + " Withdraw a good stack of food. Bank the rune drops when you return.",
			Spots.OGRESSES,
			"Go to Corsair Cove, enter the Resource Area dungeon, and fight the ogresses. The cove is west of Port Sarim.",
			"Ogress Warrior", "Ogress Shaman");
	}

	private static void memberCombat(List<TrainingMethod> out, Skill skill)
	{
		fight(out, skill, "Sand crabs", 1, 99, AccountMode.P2P,
			new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.PROFIT}, 2, 45000,
			"Sand crabs have low defence and several spawn together on the Hosidius coast. Aggro drops off after about 10 minutes: run a short distance away and come back. Combat training itself does not make money. Do it on a Slayer task when you want drops.",
			"Sand crab",
			Spots.HOSIDIUS,
			gear(skill) + " The Hosidius bank is in the town square. A Xeric's talisman teleport saves the walk.",
			Spots.SAND_CRABS,
			"Walk south from Hosidius to the beach with the sand crabs.",
			"Sand Crab");
		fight(out, skill, "Sand crabs", 1, 39, AccountMode.P2P,
			new TrainingStyle[]{TrainingStyle.FAST}, 2, 30000,
			"Use sand crabs until you can reach Fossil Island. Reset aggro by running away for a minute when they stop attacking.",
			"Sand crab",
			Spots.HOSIDIUS,
			gear(skill),
			Spots.SAND_CRABS,
			"Walk south from Hosidius to the sand crab beach.",
			"Sand Crab");
		fight(out, skill, "Ammonite crabs", 40, 99, AccountMode.P2P,
			new TrainingStyle[]{TrainingStyle.FAST}, 3, 65000,
			"Ammonite crabs on the north coast of Fossil Island give full combat XP and a long aggro timer. You need Bone Voyage. This is faster attention than the Gemstone Crab, which only gives 87.5% XP.",
			"Ammonite Crab",
			Spots.MUSEUM_CAMP,
			gear(skill) + " Bank at the Museum Camp on Fossil Island.",
			Spots.AMMONITE_CRABS,
			"From the Museum Camp, walk to the ammonite crabs on the northern coast.",
			"Ammonite Crab");
		fight(out, skill, "Nightmare Zone", 70, 99, AccountMode.P2P,
			new TrainingStyle[]{TrainingStyle.FAST}, 5, 90000,
			"Nightmare Zone in Yanille is the strong XP method once you have a few quest bosses unlocked and absorption potions from the reward shop. Use overloads if you have Herblore for them. Stay on ammonite crabs until the dream is actually faster for your gear.",
			"Nightmare Zone",
			Spots.NMZ,
			gear(skill) + " Withdraw absorption potions, and overloads if you use them. Dominic's dream is inside the arena.",
			Spots.NMZ,
			"Enter Nightmare Zone in Yanille and start a customizable rumble of bosses you have unlocked.",
			"Dominic Onion");
		gemstone(out, skill);
	}

	private static void gemstone(List<TrainingMethod> out, Skill skill)
	{
		TrainingMethod.builder(out, skill, "Gemstone Crab")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK)
			.priority(4)
			.xp(0, 40000)
			.note(COMBAT_NOTE + " The crab gives 87.5% of normal combat XP and stays for about 10 minutes.")
			.summary("The Gemstone Crab in the Tlati Rainforest is the modern AFK combat method. Complete Children of the Sun to reach Varlamore. The crab moves between three mines. A crawl-through cave at each mine follows it. You do not need to switch prayers or dodge mechanics. Just attack it.")
			.wiki("Gemstone Crab")
			.opt("Sand crabs and ammonite crabs give the full XP rate if you would rather reset aggro than chase the crab.")
			.opt("This guide never tells you where to stand or which prayer to use. It only marks the crab.")
			.loop(2)
			.step(GuideSteps.note("Complete Children of the Sun if you have not. Then travel to the Tlati Rainforest mines in eastern Varlamore. Press Next once you are in Varlamore."))
			.step(GuideSteps.find("Walk until the Gemstone Crab is on screen. If it has burrowed, crawl through the cave to the next mine.", "Gemstone Crab"))
			.step(GuideSteps.roam(gear(skill) + " Attack the Gemstone Crab. When it burrows, follow it through the crawl-through cave. This step stays up while you train.", AdvanceWhen.MANUAL, "Gemstone Crab"))
			.save();
	}

	private static void freeMagic(List<TrainingMethod> out)
	{
		spell(out, "Wind Strike to Fire Strike", 1, 12, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.PROFIT},
			"Cast the best strike spell you can. A staff of air, then water, then earth, then fire, saves the elemental runes. Mind runes are the other cost.",
			Spots.LUMBRIDGE_BANK, Spots.LUMBRIDGE_COWS, "Cow", "Fight the cows east of Lumbridge.");
		spell(out, "Fire Strike", 13, 39, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.PROFIT},
			"Fire Strike is the free-to-play spell you will use for a long time. Equip a staff of fire and bring mind runes.",
			Spots.EDGEVILLE_BANK, Spots.HILL_GIANTS, "Hill Giant", "Climb down the Edgeville trapdoor and Fire Strike the hill giants.");
		spell(out, "Fire Strike ogresses", 40, 99, AccountMode.F2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.PROFIT},
			"Keep using Fire Strike. Ogresses drop runes, so this is the free-to-play magic training that pays some of the cost back.",
			Spots.DRAYNOR_BANK, Spots.OGRESSES, "Ogress Warrior", "Go to Corsair Cove, enter the Resource Area, and cast Fire Strike.");
		TrainingMethod.builder(out, Skill.MAGIC, "Curse splash")
			.levels(19, 99)
			.modes(AccountMode.F2P)
			.styles(TrainingStyle.AFK)
			.xp(29, 15000)
			.note("Splashing is slow. The rate assumes you are actually splashing Curse, not hitting the monster.")
			.summary("Curse grants Magic XP even when the spell misses. Wear armour with no positive magic attack so you never hit, and fight something that cannot kill you. Free-to-play does not have a perfect splash spot. Fire Strike on ogresses is the practical alternative if this is awkward.")
			.wiki("Curse")
			.opt("Switch the style to Balanced for Fire Strike, which is much faster.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw body and legs with negative or zero magic attack, plus mind and body runes for Curse. A staff is optional.", Spots.LUMBRIDGE_BANK))
			.step(GuideSteps.train("Cast Curse and wait. If the monster can kill you, eat or pick a weaker one. Press Bank when you need more runes.", Spots.LUMBRIDGE_COWS, AdvanceWhen.MANUAL, "Cow", "Chicken"))
			.save();
	}

	private static void memberMagic(List<TrainingMethod> out)
	{
		spell(out, "Combat spells", 1, 99, AccountMode.P2P, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.FAST},
			"Cast the best combat spell you can afford on sand crabs. Upgrade the spell when you unlock it. A matching elemental staff saves runes. At 62 Magic, with 65 Slayer and Desert Treasure I, switch stays on this method only if you cannot burst yet.",
			Spots.HOSIDIUS, Spots.SAND_CRABS, "Sand Crab", "Train on the sand crabs south of Hosidius.");
		TrainingMethod.builder(out, Skill.MAGIC, "Ice Burst")
			.levels(62, 93)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(6)
			.req(Skill.SLAYER, 65)
			.xp(0, 180000)
			.note("Burst XP per hour depends on how many dust devils you hit with each cast. This is a normal Catacombs rate, not a perfect one.")
			.summary("Ice Burst in the Catacombs of Kourend is the usual fast Magic method. You need Desert Treasure I for Ancient Magicks, 65 Slayer, death runes, chaos runes, and water runes. Stand so one cast hits several dust devils. Bring prayer potions or protect from melee.")
			.wiki("Ice Burst")
			.opt("Monkey Madness II unlocks maniacal monkeys in Kruk's Dungeon. They are the other standard burst spot and do not need a Slayer level.")
			.opt("Ice Barrage at 94 is the same trip with a stronger spell.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw an ancient staff or the runes for Ice Burst, prayer potions, and food. Nothing else is required.", Spots.GE))
			.step(GuideSteps.travel("Enter the Catacombs of Kourend through the hole in the centre of Great Kourend, by the statue. Dust devils are in the south-east wing.", Spots.CATACOMBS, 15))
			.step(GuideSteps.roam("Burst the dust devils. This step stays up while you train. Bank when you run out of prayer potions or runes.", AdvanceWhen.MANUAL, "Dust devil"))
			.save();
		TrainingMethod.builder(out, Skill.MAGIC, "Ice Barrage")
			.levels(94, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(7)
			.req(Skill.SLAYER, 65)
			.xp(0, 280000)
			.note("A normal Catacombs barrage rate. Monkey spots can be higher.")
			.summary("Ice Barrage is the same dust devil trip as Ice Burst, with the stronger spell. You still need Ancient Magicks and 65 Slayer.")
			.wiki("Ice Barrage")
			.opt("Maniacal monkeys after Monkey Madness II are the other standard barrage monster.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw barrage runes, an ancient staff if you use one, prayer potions, and food.", Spots.GE))
			.step(GuideSteps.travel("Enter the Catacombs of Kourend at the hole by the statue in the centre of Great Kourend.", Spots.CATACOMBS, 15))
			.step(GuideSteps.roam("Barrage the dust devils until your supplies run out, then press Bank.", AdvanceWhen.MANUAL, "Dust devil"))
			.save();
		gemstone(out, Skill.MAGIC);
		TrainingMethod.builder(out, Skill.MAGIC, "High Alchemy")
			.levels(55, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.PROFIT)
			.priority(5)
			.xp(65, 70000)
			.summary("High Alchemy is 65 XP per cast and can profit. Equip a fire staff or a tome of fire, bring nature runes, and alch an item whose high-alch price beats the Grand Exchange price plus the nature rune. Yew longbows are the default item in this loop. Alch something else if the margin is better, and press Next when that inventory is done.")
			.wiki("High Level Alchemy")
			.note("About 65 XP a cast. The hourly rate is for steady alching, not tick-perfect alching.")
			.opt("Stun-alching is faster and much more clicking. This loop is the normal alch.")
			.opt("Check today's prices before buying thousands of bows. Margins move.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw nature runes, equip a fire staff, and withdraw yew longbows. Leave one inventory of bows unnoted.", Spots.GE))
			.step(GuideSteps.untilEmpty("Cast High Alchemy on the yew longbows. When they are gone, the guide sends you back to the bank.", Spots.GE, "Yew longbow"))
			.save();
	}

	private static void prayer(List<TrainingMethod> out)
	{
		TrainingMethod.builder(out, Skill.PRAYER, "Bury big bones")
			.levels(1, 99)
			.modes(AccountMode.F2P)
			.styles(ALL)
			.xp(15, 45000)
			.summary("Free-to-play Prayer is burying bones. Big bones are 15 XP and are the ones worth using. Buy them at the Grand Exchange, or kill hill giants and bury the drops. There is no gilded altar or chaos altar in free-to-play.")
			.wiki("Big bones")
			.opt("The Restless Ghost gives a small amount of early Prayer XP.")
			.opt("Killing the hill giants yourself is slower Prayer XP and saves the cost of the bones.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw a full inventory of big bones. Buy more at the Grand Exchange if you are out.", Spots.GE))
			.step(GuideSteps.untilEmpty("Bury the big bones. Stand just outside the booth so the next bank trip is one click.", Spots.GE, "Big bones"))
			.save();

		TrainingMethod.builder(out, Skill.PRAYER, "Gilded altar")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED)
			.xp(252, 350000)
			.note("Dragon bones are 72 XP when buried and 252 XP on a lit gilded altar. The rate assumes a hosted house and steady clicks.")
			.summary("Use dragon bones on a gilded altar with both burners lit. You need 75 Construction yourself, or a host. Rimmington is the usual meeting point. Bring marrentill only if you are lighting your own burners. A host's altar is already lit.")
			.wiki("Gilded altar")
			.opt("Big bones are the cheaper version of this exact trip, at 52.5 XP each instead of 252.")
			.opt("The chaos altar is faster and is in the Wilderness. Switch the style to Fastest XP if you accept that risk.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw unnoted dragon bones. Leave a few spaces if you also need marrentill for your own burners.", Spots.GE))
			.step(GuideSteps.travel("Go to the Rimmington house portal. Use the house advert and join a host who is offering a gilded altar.", Spots.RIMMINGTON_PORTAL, 15, "Portal"))
			.step(GuideSteps.note("Use the dragon bones on the gilded altar until the inventory is empty, then leave the house. Press Next to bank again."))
			.save();

		TrainingMethod.builder(out, Skill.PRAYER, "Chaos altar")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.xp(252, 450000)
			.note("The chaos altar averages better than a gilded altar because bones are sometimes saved. Deaths and running lower the real rate.")
			.summary("The chaos altar in level 38 Wilderness is the fastest Prayer training. Other players can attack you here. A burning amulet teleports to the Chaos Temple. Bring only dragon bones and the teleport. Do not bring armour you mind losing. Use the bones on the altar.")
			.wiki("Chaos altar")
			.opt("Turn on player indicators before you go, and hop if the world is busy.")
			.opt("Big bones work too and risk less money per trip.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw three or four inventories of dragon bones only if you accept losing some. For the trip itself, take one inventory and a burning amulet. Leave everything else in the bank.", Spots.GE))
			.step(GuideSteps.travel("Teleport with the burning amulet to the Chaos Temple, or walk to the level 38 Wilderness altar west of the Lava Maze. This is dangerous.", Spots.CHAOS_ALTAR, 12))
			.step(GuideSteps.untilEmpty("Use the dragon bones on the chaos altar. If another player attacks you, teleport or die. The bones are the only thing you should lose.", Spots.CHAOS_ALTAR, "Dragon bones", "Chaos altar"))
			.save();

		TrainingMethod.builder(out, Skill.PRAYER, "Bury dragon bones")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK, TrainingStyle.PROFIT)
			.xp(72, 100000)
			.summary("Burying dragon bones is the simple, safe Prayer method. It is much slower than an altar. Prayer does not make a profit. Big bones are the cheaper bury if you care about cost more than XP.")
			.wiki("Dragon bones")
			.opt("Switch to Balanced for a gilded altar, or Fastest XP for the chaos altar, when you want the real rates.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw dragon bones. Buy them on the Grand Exchange if the bank is empty.", Spots.GE))
			.step(GuideSteps.untilEmpty("Bury the dragon bones beside the Grand Exchange.", Spots.GE, "Dragon bones"))
			.save();
	}

	private static void slayer(List<TrainingMethod> out)
	{
		master(out, "Turael", 1, 0, 0, 1, Spots.TURAEL,
			"Turael in Burthorpe assigns the easiest tasks and gives no Slayer points. Use him below 20 combat, and later when you want to cancel a miserable task for free.",
			"Games necklace to the Burthorpe Games Room, then walk east to Turael's house.",
			"Talk to Turael and kill whatever he assigns. Open the in-game Slayer counter, or RuneLite's Slayer plugin, for the monster and the count. Come back when the task is done.");
		master(out, "Mazchna", 2, 20, 0, 2, Spots.MAZCHNA,
			"Mazchna in Canifis is the master from 20 combat. Priest in Peril is required. He gives 6 points a task.",
			"Reach Canifis through Paterdomus after Priest in Peril. Mazchna is in the middle of the town.",
			"Get a task from Mazchna and complete it. Skip tasks you cannot finish. The Slayer plugin shows where the monster lives.");
		master(out, "Vannaka", 3, 40, 0, 3, Spots.VANNAKA,
			"Vannaka is in the Edgeville Dungeon and takes tasks from 40 combat. He gives 8 points.",
			"Climb down the trapdoor south of Edgeville bank and walk to Vannaka.",
			"Get a task and finish it. Bank food at Edgeville between trips.");
		master(out, "Chaeldar", 4, 70, 0, 4, Spots.CHAELDAR,
			"Chaeldar is in Zanaris and takes tasks from 70 combat. Lost City is required. She gives 10 points.",
			"Enter Zanaris through the fairy ring in the Lumbridge shed, or the shed itself after Lost City. Chaeldar is by the bank.",
			"Get a task from Chaeldar and complete it.");
		master(out, "Konar quo Maten", 5, 75, 0, 5, Spots.KONAR,
			"Konar on Mount Karuulm takes tasks from 75 combat and gives the most points of the normal masters, plus brimstone keys. She tells you where to kill the monster. Use her when you want points. Use Nieve or Duradel when you want faster tasks.",
			"Climb Mount Karuulm in Kebos. Konar is at the peak.",
			"Get a task. Kill the monster only in the location she names, or the kill will not count.");
		master(out, "Nieve", 6, 85, 0, 6, Spots.NIEVE,
			"Nieve, or Steve after Monkey Madness II, is in the Tree Gnome Stronghold and takes tasks from 85 combat. This is the best task list before Duradel. She gives 12 points, or 15 with the elite Western Provinces Diary.",
			"Nieve is in a cave on the west side of the Tree Gnome Stronghold, south of the bank.",
			"Get a task and complete it. Cannon or burst tasks are the fast ones. Press Next and talk to her again when the counter hits zero.");
		master(out, "Duradel", 8, 100, 50, 8, Spots.DURADEL,
			"Duradel in Shilo Village is the usual final master. You need 100 combat, 50 Slayer, and Shilo Village. He gives 15 points. Kuradal replaces him during While Guthix Sleeps.",
			"Travel to Shilo Village and go upstairs to Duradel.",
			"Get a task and finish it. This is the master to use for a long Slayer grind.");
	}

	private static void master(List<TrainingMethod> out, String name, int priority, int combat, int slayer, int pointsStyle, WorldPoint at, String summary, String travel, String train)
	{
		TrainingStyle[] styles = pointsStyle >= 5 && name.startsWith("Konar")
			? new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.PROFIT}
			: ALL;
		TrainingMethod.Builder builder = TrainingMethod.builder(out, Skill.SLAYER, name)
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(styles)
			.priority(priority)
			.combat(combat)
			.xp(0, 20000)
			.note("Slayer XP per hour is the XP of the task you were given, not a property of the master. This figure is only a placeholder. Watch the session rate.")
			.summary(summary)
			.wiki(name)
			.opt("Krystilia in the Edgeville jail assigns Wilderness tasks and gives 25 points. Every monster is in the Wilderness.")
			.opt("Turael can replace a bad task from another master with an easy one. That does not spend points, and it resets your task streak.")
			.opt("Every 10th and 50th task gives bonus points. Do those at Konar or Duradel.")
			.loop(1)
			.step(GuideSteps.travel(travel, at, 12, name))
			.step(GuideSteps.train(train, at, AdvanceWhen.MANUAL, name));
		if (slayer > 0)
		{
			builder.req(Skill.SLAYER, slayer);
		}
		builder.save();
	}

	private static void fight(List<TrainingMethod> out, Skill skill, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, int priority, int xpHr, String summary, String wiki, WorldPoint bank, String bankText, WorldPoint spot, String travel, String... highlights)
	{
		TrainingMethod.builder(out, skill, name)
			.levels(min, max)
			.modes(mode)
			.styles(styles)
			.priority(priority)
			.xp(0, xpHr)
			.note(COMBAT_NOTE)
			.summary(summary)
			.wiki(wiki)
			.opt(questOpt(skill))
			.loop(0)
			.step(GuideSteps.bank(bankText, bank))
			.step(GuideSteps.travel(travel, spot, 15, highlights))
			.step(GuideSteps.train("Keep fighting until you need to bank food or loot. A full inventory sends you back to the bank step.", spot, AdvanceWhen.INVENTORY_FULL, highlights))
			.save();
	}

	private static void spell(List<TrainingMethod> out, String name, int min, int max, AccountMode mode, TrainingStyle[] styles, String summary, WorldPoint bank, WorldPoint spot, String highlight, String travel)
	{
		fight(out, Skill.MAGIC, name, min, max, mode, styles, 1, 35000, summary, "Fire Strike", bank,
			"Withdraw mind runes and equip a staff of the matching element. Bring a little food.",
			spot, travel, highlight);
	}

	private static String gear(Skill skill)
	{
		switch (skill)
		{
			case RANGED:
				return "Withdraw your best bow or crossbow, ammunition, and a little food. Use Rapid.";
			case HITPOINTS:
				return "Withdraw your best weapon. Hitpoints XP is the damage you deal, on any style.";
			case ATTACK:
				return "Withdraw your best melee weapon and a little food. Set the style to Accurate.";
			case STRENGTH:
				return "Withdraw your best melee weapon and a little food. Set the style to Aggressive.";
			case DEFENCE:
				return "Withdraw your best melee weapon, armour, and food. Set the style to Defensive.";
			default:
				return "Withdraw your best weapon and a little food.";
		}
	}

	private static String questOpt(Skill skill)
	{
		switch (skill)
		{
			case ATTACK:
			case STRENGTH:
				return "Waterfall Quest gives about 13,750 Attack XP and 13,750 Strength XP. Do it before you grind the early levels.";
			case HITPOINTS:
				return "Witch's House gives about 6,325 Hitpoints XP.";
			case DEFENCE:
				return "Dragon Slayer I and several other quests reward Defence XP. They are worth doing before a long Defence grind.";
			case RANGED:
				return "Dwarf Cannon is members content and unlocks the cannon, which speeds up a lot of later combat and Slayer.";
			default:
				return "Quest XP rewards skip the slowest early levels. Check the wiki for this skill before you settle in.";
		}
	}
}
