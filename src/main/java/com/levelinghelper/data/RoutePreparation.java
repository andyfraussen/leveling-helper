package com.levelinghelper.data;

import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.model.ItemCategory;
import com.levelinghelper.model.ItemRequirement;
import com.levelinghelper.model.PreparationPlan;
import com.levelinghelper.model.ResourceHandling;
import com.levelinghelper.model.StepKind;
import com.levelinghelper.model.TrainingMethod;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.gameval.VarbitID;

/** Every catalog family must explicitly declare items, external checks and inventory handling. */
final class RoutePreparation
{
	private RoutePreparation() { }

	static TrainingMethod review(TrainingMethod method)
	{
		List<ItemRequirement> inputs = new ArrayList<>();
		List<String> checks = new ArrayList<>();
		String noItems = "";
		String local = null;
		String name = method.name();
		ResourceHandling handling = ResourceHandling.PROCESS;
		switch (method.skill())
		{
			case ATTACK: case STRENGTH: case DEFENCE: case HITPOINTS:
				inputs.add(category("Equipped melee weapon (unarmed also works)", ItemCategory.MELEE_WEAPON, true, 1,
					"Equip your usable melee weapon, or train unarmed.").asOptional());
				inputs.add(food().asOptional());
				checks.add("Use a weapon you can wield, or unarmed combat, and select a combat style that trains " + method.skill().getName() + ".");
				combatChecks(method, checks);
				handling = ResourceHandling.MANUAL;
				break;
			case RANGED:
				inputs.add(category("Equipped ranged weapon", ItemCategory.RANGED_WEAPON, true, 1,
					"Obtain a bow, crossbow or thrown weapon you can wield, then equip it."));
				inputs.add(category("Compatible ammunition", ItemCategory.AMMUNITION, true, 1,
					"Equip ammunition compatible with your weapon. Charged self-contained weapons or thrown weapons supply their own shots.")
					.providedByCategory(ItemCategory.SELF_AMMO_WEAPON));
				inputs.add(food().asOptional());
				checks.add("Confirm the ammunition tier or weapon charges and choose the ranged attack style you want to train.");
				combatChecks(method, checks);
				handling = ResourceHandling.MANUAL;
				break;
			case MAGIC:
				inputs.add(food().asOptional());
				checks.add("Select the spell named in the detected supplies, or use your charged powered staff. Confirm your casting style.");
				if (name.equals("Curse splash")) checks.add("Check your equipment has sufficiently negative magic attack for splashing; do not rely on zero bonus.");
				combatChecks(method, checks);
				handling = ResourceHandling.MANUAL;
				break;
			case PRAYER:
				if (name.equals("Gilded altar")) checks.add("Check a lit gilded altar is available in your house or your chosen host's house.");
				if (name.equals("Chaos altar")) checks.add("Choose and check your Wilderness transport and escape supplies before departure; ordinary teleports do not work at the altar.");
				break;
			case WOODCUTTING:
				handling = ResourceHandling.BANK;
				if (name.equals("Sulliusceps")) inputs.add(food().asOptional());
				break;
			case MINING:
				handling = method.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP) ? ResourceHandling.BANK_OR_DROP
					: name.startsWith("Motherlode") ? ResourceHandling.PROCESS : name.equals("Volcanic Mine") ? ResourceHandling.MANUAL : ResourceHandling.BANK;
				if (name.equals("Volcanic Mine"))
				{
					inputs.add(food());
					checks.add("Pay the entry fee or confirm permanent access with Petrified Pete. Check food, prayer supplies and your team's preparation before entering.");
				}
				break;
			case FISHING:
				handling = name.equals("Tempoross") ? ResourceHandling.MANUAL
					: method.steps().stream().anyMatch(step -> step.kind() == StepKind.DROP) ? ResourceHandling.BANK_OR_DROP : ResourceHandling.BANK;
				if (name.equals("Lobsters") || name.equals("Swordfish")) checks.add("Bring ferry fare for both directions, or confirm an alternative transport route.");
				if (name.equals("Tempoross")) checks.add("Obtain ropes, buckets and a hammer from the minigame supply crates. These do not have to be bought or banked beforehand.");
				break;
			case COOKING: case FLETCHING: case CRAFTING:
				break; // Each recipe factory already declares its exact inputs and action quantities.
			case FIREMAKING:
				if (name.equals("Wintertodt"))
				{
					inputs.add(Tools.axe()); inputs.add(tool("Knife", ItemID.KNIFE)); inputs.add(tool("Tinderbox", ItemID.TINDERBOX));
					inputs.add(tool("Hammer", ItemID.HAMMER, ItemID.IMCANDO_HAMMER, ItemID.IMCANDO_HAMMER_OFFHAND));
					checks.add("Wear warm clothing and prepare the healing supplies appropriate to your Wintertodt warmth and Hitpoints. Check the in-game minigame HUD.");
					handling = ResourceHandling.MANUAL;
				}
				break;
			case HERBLORE:
				if (name.equals("Clean herbs")) inputs.add(category("Grimy herbs you can clean", ItemCategory.GRIMY_HERB, false, 1,
					"Grow, gather or obtain grimy herbs your Herblore level can clean."));
				break;
			case SMITHING:
				if (name.equals("Cannonballs"))
				{
					inputs.add(tool("Ammo mould", ItemID.AMMO_MOULD, ItemID.DOUBLE_AMMO_MOULD));
					inputs.add(Materials.supply("Steel bar", 1));
				}
				if (name.equals("Blast Furnace gold"))
				{
					inputs.add(material("Gold ore", 1, ItemID.GOLD_ORE));
					inputs.add(tool("Goldsmith gauntlets", ItemID.GOLDSMITH_GAUNTLETS).from("Recover the Family Crest gauntlets and have Avan in the Al Kharid mine change them to goldsmith gauntlets. These are untradeable."));
					inputs.add(tool("Ice gloves or smiths' gloves (i)", ItemID.ICE_GLOVES, ItemID.SMITHS_GLOVES_I).from("Obtain ice gloves from the Ice Queen, or combine them with smiths' gloves through Giants' Foundry rewards."));
					checks.add("Fund the Blast Furnace coffer and pay the foreman if required. Swap gauntlets for XP and cooling during the cycle.");
				}
				if (name.equals("Giants' Foundry"))
				{
					inputs.add(category("Foundry metal bars", ItemCategory.FOUNDRY_METAL, false, 1,
						"Use bars or eligible metal equipment at the Foundry crucible. A sword requires 28 bar-equivalents; the bank chest is beside the crucible.").asOptional());
					checks.add("Fill the crucible with 28 bar-equivalents of your chosen metal or alloy, or resume an existing preform. Use eligible metal equipment if preferred.");
					checks.add("Obtain a bucket of water at the Foundry or equip ice gloves/smiths' gloves (i) to pick up the preform. A carried hammer is not required.");
					handling = ResourceHandling.MANUAL;
					local = "Use the bank chest beside the Foundry crucible to replenish the chosen metal; resume your stored preform if one is active.";
				}
				break;
			case RUNECRAFT:
				if (name.equals("Guardians of the Rift"))
				{
					inputs.add(Tools.pickaxe()); inputs.add(tool("Chisel", ItemID.CHISEL));
					checks.add("Make or repair your guardian equipment inside the minigame. Mine guardian fragments there; ordinary banked essence is not required.");
					handling = ResourceHandling.MANUAL;
				}
				else if (name.equals("Lava runes"))
				{
					inputs.add(material("Pure essence", 1, ItemID.PURE_ESSENCE));
					inputs.add(material("Earth runes", 1, ItemID.EARTH_RUNE));
					inputs.add(new ItemRequirement("Fire altar access", false, 1, ItemID.FIRE_TALISMAN).providedBy(ItemID.FIRE_TIARA)
						.from("Use a fire talisman, equip a fire tiara, or confirm another unlocked entrance.").beforeDeparture());
					inputs.add(new ItemRequirement("Earth talisman or Magic Imbue", false, 1, ItemID.EARTH_TALISMAN)
						.unlessActive(VarbitID.MAGIC_IMBUE_ACTIVE).from("Bring an earth talisman per trip, or cast Magic Imbue."));
					inputs.add(tool("Binding necklace", ItemID.BINDING_NECKLACE).beforeDeparture());
					checks.add("Equip the binding necklace and check its remaining charges. Choose teleport jewellery or a walking route and bring enough earth runes for the essence you plan to combine.");
				}
				else
				{
					inputs.add(material("Unnoted essence", 1, ItemID.RUNE_ESSENCE, ItemID.PURE_ESSENCE));
					int tiara = name.startsWith("Air") ? ItemID.AIR_TIARA : name.startsWith("Earth") ? ItemID.EARTH_TIARA : name.startsWith("Fire") ? ItemID.FIRE_TIARA : ItemID.BODY_TIARA;
					int talisman = name.startsWith("Air") ? ItemID.AIR_TALISMAN : name.startsWith("Earth") ? ItemID.EARTH_TALISMAN : name.startsWith("Fire") ? ItemID.FIRE_TALISMAN : ItemID.BODY_TALISMAN;
					inputs.add(new ItemRequirement("Altar talisman or equipped tiara", false, 1, talisman).providedBy(tiara).beforeDeparture()
						.from("Bring the altar's talisman or equip its tiara. Mine essence through Aubury after Rune Mysteries."));
				}
				break;
			case CONSTRUCTION:
				inputs.add(tool("Saw", ItemID.SAW, ItemID.CRYSTAL_SAW));
				inputs.add(tool("Hammer", ItemID.HAMMER, ItemID.IMCANDO_HAMMER, ItemID.IMCANDO_HAMMER_OFFHAND));
				if (name.equals("Oak larders")) inputs.add(material("Oak planks", 8, ItemID.OAK_PLANK));
				else if (name.equals("Mahogany tables")) inputs.add(material("Mahogany planks", 6, ItemID.MAHOGANY_PLANK));
				else inputs.add(category("Planks for your chosen furniture or contract", ItemCategory.PLANK, false, 1,
					"Make or buy the plank type used by your selected furniture or contract.").perAction(1));
				if (name.equals("Mahogany Homes"))
				{
					inputs.add(material("Steel bars for the contract", 1, ItemID.STEEL_BAR).beforeDeparture().asOptional());
					checks.add("Check the contract's plank type, quantity and steel bars before departure. Choose your city transports; costs vary by contract.");
				}
				else
				{
					checks.add("Check your house, room/hotspot, Building mode and servant payment before starting. Bring nails for furniture that requires them.");
					local = "Ask your servant to bring the required planks, or use Bank to leave and restock. Keep the saw and hammer.";
				}
				break;
			case HUNTER:
				if (name.equals("Falconry"))
				{
					inputs.add(new ItemRequirement("Falcon rental coins", false, 500, ItemID.COINS_995).providedBy(ItemID.FALCONERS_GLOVE, ItemID.FALCONERS_GLOVE_10024).beforeDeparture().from("Obtain 500 coins for Matthias, or retrieve your already-rented falcon."));
					checks.add("If your falcon is already rented, retrieve it from Matthias. Leave your weapon, shield and gloves slots empty for falconry.");
				}
				else inputs.add(tool(name.contains("chinchompa") ? "Box traps" : "Bird snares",
					name.contains("chinchompa") ? ItemID.BOX_TRAP : ItemID.BIRD_SNARE).beforeDeparture());
				checks.add("Bring enough traps for your Hunter level. Retrieve deployed traps before leaving; trap ownership is not inferred from other players' traps.");
				handling = ResourceHandling.BANK;
				break;
			case FARMING:
				inputs.add(tool("Spade", ItemID.SPADE)); inputs.add(tool("Seed dibber", ItemID.SEED_DIBBER));
				inputs.add(tool("Rake", ItemID.RAKE).asOptional());
				if (name.equals("Allotments")) inputs.add(new ItemRequirement("Potato or onion seeds", false, 3, ItemID.POTATO_SEED, ItemID.ONION_SEED).beforeDeparture().from("Buy seeds from a seed stall or obtain them through Farming/Thieving."));
				else
				{
					inputs.add(category("Herb seeds you can plant", ItemCategory.HERB_SEED, false, 1, "Obtain herb seeds your Farming level can plant.").beforeDeparture());
					inputs.add(material("Ultracompost", 1, ItemID.ULTRACOMPOST).beforeDeparture().asOptional());
					checks.add("Use ultracompost, including a filled bottomless compost bucket or leprechaun stock, where appropriate. Compost already applied to a patch is checked in-game.");
					if (name.equals("Herb and tree run")) inputs.add(category("Unnoted saplings you can plant", ItemCategory.SAPLING, false, 1,
						"Grow seedlings into saplings or obtain saplings appropriate to your Farming level.").beforeDeparture());
				}
				checks.add("Check the patches, enough seeds/saplings and compost for the full run, growth timers and transports. Tools/compost may be retrieved from a tool leprechaun; check stored supplies there.");
				handling = ResourceHandling.MANUAL;
				break;
			case AGILITY:
				noItems = "This course has no mandatory carried training tool; quest/access gates are checked separately.";
				inputs.add(food().asOptional());
				if (name.equals("Hallowed Sepulchre")) checks.add("Confirm Darkmeyer access/disguise and the hallowed tools and looting supplies needed for the floors you choose. The guide does not predict traps.");
				handling = ResourceHandling.NONE;
				break;
			case THIEVING:
				inputs.add(food().asOptional());
				if (name.equals("Blackjacking")) inputs.add(new ItemRequirement("Equipped blackjack", true, 1,
					ItemID.OAK_BLACKJACK, ItemID.WILLOW_BLACKJACK, ItemID.MAPLE_BLACKJACK, ItemID.OAK_BLACKJACKO,
					ItemID.OAK_BLACKJACKD, ItemID.WILLOW_BLACKJACKO, ItemID.WILLOW_BLACKJACKD, ItemID.MAPLE_BLACKJACKO, ItemID.MAPLE_BLACKJACKD)
					.from("Buy a blackjack in Pollnivneach after The Feud."));
				if (name.equals("Pyramid Plunder")) inputs.add(new ItemRequirement("Antipoison", false, 1,
					ItemID.ANTIPOISON1, ItemID.ANTIPOISON2, ItemID.ANTIPOISON3, ItemID.ANTIPOISON4,
					ItemID.ANTIDOTE1, ItemID.ANTIDOTE2, ItemID.ANTIDOTE3, ItemID.ANTIDOTE4,
					ItemID.ANTIDOTE1_5958, ItemID.ANTIDOTE2_5956, ItemID.ANTIDOTE3_5954, ItemID.ANTIDOTE4_5952)
					.from("Make or obtain antipoison. A pharaoh's sceptre is optional transport."));
				checks.add("Check food and healing for the target's failure damage. Open coin pouches when necessary.");
				handling = name.equals("Blackjacking") || name.equals("Pyramid Plunder") ? ResourceHandling.MANUAL : ResourceHandling.BANK;
				break;
			case SLAYER:
				inputs.add(food().asOptional());
				checks.add("Get/read your actual task and check its weapon, protection, ammunition/runes and access requirements. The selected Slayer master does not determine task equipment.");
				handling = ResourceHandling.MANUAL;
				break;
			case SAILING:
				noItems = "Required ship facilities and task cargo are installed/assigned in the world, not verified from carried-item counts.";
				checks.add(name.equals("Shipwreck salvaging") ? "Check your skiff, fitted salvaging hook, access to the selected wreck and a salvage sorting station."
					: name.equals("Barracuda Trials") ? "Check your skiff, the trial/rank you can enter and the supplies assigned by that trial."
					: "Check your boat, task capacity and the assigned cargo and destination on the notice board.");
				handling = ResourceHandling.MANUAL;
				break;
			default: throw new IllegalArgumentException("No preparation policy for " + method.id());
		}
		PreparationPlan plan = new PreparationPlan(inputs, checks, handling, noItems, local);
		if (method.skill() == Skill.MAGIC)
		{
			plan.dynamicItems = true;
			plan.resolveItems = lookup -> MagicSupplies.forRoute(name, lookup);
			plan.setupIssues = lookup -> {
				int book = lookup.varbit(VarbitID.SPELLBOOK);
				boolean ancient = name.equals("Ice Burst") || name.equals("Ice Barrage");
				return ancient && book != 1 ? List.of("Switch to Ancient Magicks before casting " + name + ".")
					: !ancient && !(MagicSupplies.allowsPoweredStaff(name) && lookup.equippedMatches(ItemCategory.POWERED_STAFF)) && book != 0 ? List.of("Switch to the standard spellbook for the recommended spell.") : List.of();
			};
		}
		return method.reviewed(plan);
	}

	private static void combatChecks(TrainingMethod method, List<String> checks)
	{
		if (method.name().equals("Nightmare Zone")) checks.add("Confirm the unlocked dream, entry payment and absorption/overload supplies or an alternative food/prayer setup.");
	}

	private static ItemRequirement food()
	{
		return category("Healing food", ItemCategory.FOOD, false, 1, "Cook or obtain food suitable for your Hitpoints and the activity.");
	}

	private static ItemRequirement category(String label, ItemCategory category, boolean equipped, int quantity, String source)
	{
		return ItemRequirement.matching(label, equipped ? ItemRequirement.Container.EQUIPMENT : ItemRequirement.Container.INVENTORY,
			quantity, category, source);
	}

	private static ItemRequirement tool(String label, int... ids)
	{
		return new ItemRequirement(label, ItemRequirement.Container.EITHER, 1, ids).from("Obtain " + label + " from a suitable shop, source or activity supply crate.");
	}

	private static ItemRequirement material(String label, int quantity, int... ids)
	{
		return new ItemRequirement(label, false, quantity, ids).perAction(quantity).from("Gather, make or obtain " + label + " before training.");
	}
}
