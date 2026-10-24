package com.levelinghelper.data;

import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.util.List;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

final class SupportMethods
{
	private static final TrainingStyle[] ROOF = {
		TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT
	};

	private SupportMethods()
	{
	}

	static void register(List<TrainingMethod> out)
	{
		agility(out);
		thieving(out);
		sailing(out);
	}

	private static void agility(List<TrainingMethod> out)
	{
		roof(out, "Draynor rooftop", 1, 29, 1, 9000, Spots.DRAYNOR_ROOF,
			"Draynor Village rooftop is the course from level 1. Marks of grace spawn on the roofs. Turn on RuneLite's Agility plugin to see the marks and your lap count. The start is the rough wall on the north-east house.");
		roof(out, "Varrock rooftop", 30, 39, 1, 13000, Spots.VARROCK_ROOF,
			"Varrock rooftop starts on the clothes shop. It is the course from 30 to 40.");
		roof(out, "Canifis rooftop", 40, 49, 1, 19000, Spots.CANIFIS_ROOF,
			"Canifis rooftop is the best source of marks of grace for a long time. Priest in Peril is required. The start is the tree by the pub. You fail less as your level climbs, so people often stay past 50 if they want marks.");
		roof(out, "Falador rooftop", 50, 59, 1, 28000, Spots.FALADOR_ROOF,
			"Falador rooftop starts on the wall south of the east bank.");
		roof(out, "Seers' Village rooftop", 60, 69, 1, 45000, Spots.SEERS_ROOF,
			"Seers' Village rooftop starts beside the bank. The hard Kandarin Diary Camelot teleport puts you on the roof and makes this course much better. Do the diary when you can.");
		roof(out, "Pollnivneach rooftop", 70, 79, 1, 52000, Spots.POLLNIVNEACH_ROOF,
			"Pollnivneach rooftop. Bring water, or wear a Circlet of Water, because the town is in the desert. The hard Desert Diary removes the heat.");
		roof(out, "Rellekka rooftop", 80, 89, 1, 58000, Spots.RELLEKKA_ROOF,
			"Rellekka rooftop starts on the rough wall in the south of town.");
		roof(out, "Ardougne rooftop", 90, 99, 1, 62000, Spots.ARDOUGNE_ROOF,
			"Ardougne rooftop is the course to 99 if you are not running the Hallowed Sepulchre. The start is the wooden beam by the north bank.");

		TrainingMethod.builder(out, Skill.AGILITY, "Wilderness course")
			.levels(52, 71)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(5)
			.xp(0, 45000)
			.summary("The Wilderness Agility Course is faster than the Falador and Seers rooftops and it is in the Wilderness. Other players can attack you. Bring only a knife or nothing. A teleport out is worth more than food. The course is north of the Mage Arena, just past the gate.")
			.wiki("Wilderness Agility Course")
			.note("Rooftops are the safe version. Switch the style to Balanced if you do not want Wilderness risk.")
			.loop(1)
			.step(GuideSteps.bank("Empty your inventory of anything you would miss. Bring a teleport.", Spots.GE))
			.step(GuideSteps.travel("Enter the Wilderness Agility Course. This is dangerous.", Spots.WILDERNESS_COURSE, 15, "Obstacle pipe"))
			.step(GuideSteps.trainWide("Run laps. Leave if another player shows up.", Spots.WILDERNESS_COURSE, 40, AdvanceWhen.MANUAL, "Obstacle pipe", "Rocks", "Log balance", "Stepping stone"))
			.save();

		TrainingMethod.builder(out, Skill.AGILITY, "Hallowed Sepulchre")
			.levels(72, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(8)
			.xp(0, 75000)
			.summary("The Hallowed Sepulchre in Darkmeyer is the fastest Agility. Sins of the Father is required. Floor 1 opens at 52, but the course is at its best from 72, then 82, then 92 as the lower floors open. It is dangerous inside the minigame and safe from other players. Bring the hallowed tools you have unlocked.")
			.wiki("Hallowed Sepulchre")
			.note("The rate climbs when you stop failing and when the higher floors unlock. 75k is a learning rate, not a perfected one.")
			.opt("Ardougne rooftop is the calm alternative. Switch the style to Balanced.")
			.loop(1)
			.step(GuideSteps.note("Complete Sins of the Father if you have not. Darkmeyer is hostile until you have the vyre disguise from that quest. Press Next when you can enter."))
			.step(GuideSteps.travel("Enter the Hallowed Sepulchre in Darkmeyer.", Spots.SEPULCHRE, 15))
			.step(GuideSteps.train("Run the floors you have unlocked. Loot the coffins if you want the rewards. Start another run when you leave.", Spots.SEPULCHRE, AdvanceWhen.MANUAL))
			.save();
	}

	private static void roof(List<TrainingMethod> out, String name, int min, int max, int priority, int xpHr, WorldPoint start, String summary)
	{
		TrainingMethod.builder(out, Skill.AGILITY, name)
			.levels(min, max)
			.modes(AccountMode.P2P)
			.styles(ROOF)
			.priority(priority)
			.xp(0, xpHr)
			.note("Rooftop rates assume you are not failing often. Failing gets rarer as you pass the course's level.")
			.summary(summary + " Agility is never idle. Rooftops are the repetitive version, and the marks of grace buy amylase and stamina.")
			.wiki(name)
			.opt("The Colossal Wyrm course in Varlamore is another rooftop-style course if you want a change of scenery. Basic is lower level and advanced is better.")
			.opt("Graceful is worth getting. Wear it while you train so the marks keep coming and your weight stays low.")
			.loop(1)
			.step(GuideSteps.travel("Go to the start of the course.", start, 12, "Rough wall", "Ladder", "Tree"))
			.step(GuideSteps.trainWide("Run laps from the start. Pick up marks of grace. RuneLite's Agility plugin counts the laps.", start, 45, AdvanceWhen.MANUAL,
				"Rough wall", "Tightrope", "Narrow wall", "Gap", "Crate", "Clothes line", "Ledge", "Edge", "Pole-vault", "Jump", "Log balance"))
			.save();
	}

	private static void thieving(List<TrainingMethod> out)
	{
		steal(out, "Men and women", 1, 4, ALL_SAFE, 1, 5000, Spots.LUMBRIDGE_BANK, Spots.LUMBRIDGE_SQUARE,
			"Pickpocket men and women in Lumbridge. This lasts a few minutes. Have some food, or just die and come back. The damage is tiny.",
			"Pickpocket the men and women around Lumbridge castle.",
			"Man", "Woman");
		steal(out, "Baker's stall", 5, 19, ALL_SAFE, 1, 15000, Spots.ARDOUGNE_SOUTH_BANK, Spots.ARDOUGNE_MARKET,
			"Steal from the baker's stall in East Ardougne. The guards will attack you if they see it. Stand on the stall's safe side, or bank the cakes.",
			"Steal from the baker's stall in the Ardougne market.",
			"Baker's stall");
		steal(out, "Silk stall", 20, 24, ALL_SAFE, 2, 25000, Spots.ARDOUGNE_SOUTH_BANK, Spots.ARDOUGNE_MARKET,
			"Steal silk in East Ardougne. It is a short step before the Hosidius fruit stall.",
			"Steal from the silk stall.",
			"Silk stall");
		steal(out, "Fruit stall", 25, 44, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK, TrainingStyle.FAST}, 2, 40000, Spots.HOSIDIUS, Spots.FRUIT_STALL,
			"Steal from the Hosidius fruit stall. The bank is in the same square. Empty the inventory when it fills with fruit.",
			"Steal fruit. Bank it or drop it when you are full.",
			"Fruit Stall");
		TrainingMethod.builder(out, Skill.THIEVING, "Master Farmers")
			.levels(38, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.PROFIT)
			.priority(4)
			.xp(0, 50000)
			.summary("Pickpocket master farmers for seeds. The one in Draynor is the usual spot. Seed rates get much better as your Thieving level climbs, and the Rogue outfit helps. This is the profit method. It is not the fastest XP.")
			.wiki("Master Farmer")
			.opt("A seed box holds the seeds so you bank less often.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw food. Dodgy necklaces help later. Deposit seeds when you come back.", Spots.DRAYNOR_BANK))
			.step(GuideSteps.travel("The Draynor master farmer is just north of the market.", Spots.DRAYNOR_FARMER, 12, "Master Farmer"))
			.step(GuideSteps.train("Pickpocket the master farmer. Eat when your hitpoints get low. Bank when you are out of food or full of seeds.", Spots.DRAYNOR_FARMER, AdvanceWhen.MANUAL, "Master Farmer"))
			.save();
		steal(out, "Ardougne knights", 55, 99, new TrainingStyle[]{TrainingStyle.BALANCED, TrainingStyle.AFK}, 3, 60000, Spots.ARDOUGNE_NORTH_BANK, Spots.ARDOUGNE_KNIGHT,
			"Pickpocket knights of Ardougne. Coin pouches stack until you have 28, then you must open them. At higher levels you fail less and this becomes the calm method. Bring food at 55. The knight by the north side of the market is the usual one. The medium Ardougne Diary makes them stop teleporting away when you are caught, which is a large upgrade.",
			"Pickpocket the knight. Open coin pouches before you hit 28 of them. Eat when you need to.",
			"Knight of Ardougne");
		TrainingMethod.builder(out, Skill.THIEVING, "Blackjacking")
			.levels(45, 70)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(5)
			.xp(0, 140000)
			.summary("Blackjack Menaphite Thugs in Pollnivneach. The Feud is required, and you need a blackjack from the general store or from the bandits. Lure, knock out, pickpocket, and repeat. It is fast and it is miserable. Bring wines or jugs of wine to heal.")
			.wiki("Blackjack")
			.note("A focused blackjack rate. Most people do not hold the perfect rate for long.")
			.opt("Ardougne knights are the calm alternative from 55.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw jugs of wine and a blackjack. Complete The Feud if you have not.", Spots.GE))
			.step(GuideSteps.travel("Go to Pollnivneach. The thugs are in the centre of town.", Spots.POLLNIVNEACH, 15, "Menaphite Thug"))
			.step(GuideSteps.train("Lure, knock out, and pickpocket. Heal with the wine. This is the whole method.", Spots.POLLNIVNEACH, AdvanceWhen.MANUAL, "Menaphite Thug"))
			.save();
		TrainingMethod.builder(out, Skill.THIEVING, "Pyramid Plunder")
			.levels(71, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(6)
			.xp(0, 180000)
			.summary("Pyramid Plunder in Sophanem is the fast Thieving method from 71, and it gets better at 81 and 91 as the later rooms open. Bring antipoison, food, and a pharaoh's sceptre if you have one. Loot the urns and the chests in the highest room you can open.")
			.wiki("Pyramid Plunder")
			.note("The rate depends on which room you loot. Later rooms are more XP.")
			.loop(1)
			.step(GuideSteps.bank("Withdraw antipoison, food, and a sceptre if you have one.", Spots.GE))
			.step(GuideSteps.travel("Enter Pyramid Plunder in Sophanem. A pharaoh's sceptre teleports there.", Spots.PYRAMID_PLUNDER, 15, "Guardian Mummy"))
			.step(GuideSteps.train("Loot the highest room you can, then start another run when the timer ends.", Spots.PYRAMID_PLUNDER, AdvanceWhen.MANUAL))
			.save();
	}

	private static final TrainingStyle[] ALL_SAFE = {
		TrainingStyle.BALANCED, TrainingStyle.FAST, TrainingStyle.AFK, TrainingStyle.PROFIT
	};

	private static void steal(List<TrainingMethod> out, String name, int min, int max, TrainingStyle[] styles, int priority, int xpHr, WorldPoint bank, WorldPoint spot, String summary, String action, String... highlights)
	{
		TrainingMethod.builder(out, Skill.THIEVING, name)
			.levels(min, max)
			.modes(AccountMode.P2P)
			.styles(styles)
			.priority(priority)
			.xp(0, xpHr)
			.summary(summary)
			.wiki(name)
			.opt("Food and, later, dodgy necklaces cover the failures. The Rogue outfit from the Rogues' Den gives double loot.")
			.loop(0)
			.step(GuideSteps.bank("Withdraw food if you are being hit. Deposit the loot.", bank))
			.step(GuideSteps.travel("Walk to the target.", spot, 15, highlights))
			.step(GuideSteps.train(action, spot, AdvanceWhen.INVENTORY_FULL, highlights))
			.save();
	}

	private static void sailing(List<TrainingMethod> out)
	{
		TrainingMethod.builder(out, Skill.SAILING, "Port tasks")
			.levels(1, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.BALANCED, TrainingStyle.PROFIT, TrainingStyle.AFK)
			.xp(0, 35000)
			.note("Courier tasks are the medium Sailing method. The rate climbs with your boat and with how many tasks you can hold: 2 at level 7, 3 at 28, 4 at 56, and 5 at 84. This number is only a mid-level orientation.")
			.summary("Complete the Pandemonium quest first. Will and Anne are on the Port Sarim docks, and the quest gives you a raft. After that, take courier tasks from a port notice board, load the cargo, sail it to the destination, and unload it. You can hold more tasks at once as you level. Sea charting tasks are once each and are worth doing as you pass them.")
			.wiki("Port task")
			.opt("Sea charting is a pile of one-off XP. Do every chart you can while you are in the area.")
			.opt("A skiff at 15 and a sloop at 50 change what you can carry and where you can dock.")
			.opt("Deep sea trawling is the money activity. It is weak Sailing XP on purpose. Do port tasks or trials when you want levels.")
			.loop(1)
			.step(GuideSteps.travel("If you still need the quest, talk to Will and Anne on the Port Sarim docks. Otherwise go to The Pandemonium and board your boat from the gangplank.", Spots.PORT_SARIM, 20, "Will", "Anne", "Gangplank"))
			.step(GuideSteps.travel("Take a courier task from a notice board. The Pandemonium has one.", Spots.PANDEMONIUM, 20, "Notice board"))
			.step(GuideSteps.train("Load the cargo, sail to the port on the task, and unload it. Trim the sails when the wind gusts. Take another task when you are done.", Spots.PANDEMONIUM, AdvanceWhen.MANUAL, "Cargo", "Notice board"))
			.save();

		TrainingMethod.builder(out, Skill.SAILING, "Shipwreck salvaging")
			.levels(15, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.AFK)
			.priority(4)
			.xp(0, 25000)
			.note("Salvaging is the idle method. The rate depends on the wreck tier, your hook, and whether a crewmate is doing the work. Higher wrecks are better.")
			.summary("From 15 you can use a skiff and a salvaging hook. Sail up to a shipwreck, deploy the hook, and collect salvage. Sort it at a salvaging station in port, or build a station on the boat. This is the method to use when you want Sailing XP while you look away. A crewmate can salvage for you once you have the facility and the crew.")
			.wiki("Shipwreck salvaging")
			.opt("Port tasks are more XP for the same boat. Trials are more XP again, and they need your attention.")
			.loop(1)
			.step(GuideSteps.note("Build a salvaging hook on your skiff if you have not. Buy the skiff at 15 if you are still on the raft. Press Next when the hook is fitted."))
			.step(GuideSteps.travel("Board at The Pandemonium and sail to a shipwreck you can salvage.", Spots.PANDEMONIUM, 20, "Gangplank"))
			.step(GuideSteps.train("Deploy the hook beside the wreck. Sort the salvage when you return to port.", Spots.PANDEMONIUM, AdvanceWhen.MANUAL))
			.save();

		TrainingMethod.builder(out, Skill.SAILING, "Barracuda Trials")
			.levels(30, 99)
			.modes(AccountMode.P2P)
			.styles(TrainingStyle.FAST)
			.priority(6)
			.xp(0, 70000)
			.note("Trials are the fast Sailing method and the rate depends on the trial, the rank, and how clean your lines are. Tempor Tantrum at 30, Jubbly Jive at 55, Gwenith Glide at 72. Use the hardest rank you can finish.")
			.summary("Barracuda Trials are races. You need a skiff. Tempor Tantrum unlocks at 30, Jubbly Jive at 55, and Gwenith Glide at 72. Each has Swordfish, Shark, and Marlin ranks. Sail the course, pick up the supplies, and deliver the rum before the clock runs out. This is the fastest Sailing and it is the most clicking.")
			.wiki("Barracuda Trials")
			.opt("Do the new trial as soon as it unlocks. A lower trial stops being the best XP once the next one is available.")
			.loop(1)
			.step(GuideSteps.travel("Board your skiff at The Pandemonium and talk to the Barracudas to start the best trial you have unlocked.", Spots.PANDEMONIUM, 20, "Gangplank"))
			.step(GuideSteps.train("Run the trial. Start the next rank when you can finish it cleanly. Press Next if you need to resupply the boat.", Spots.PANDEMONIUM, AdvanceWhen.MANUAL))
			.save();
	}
}
