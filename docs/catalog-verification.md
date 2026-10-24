# Catalog verification

Reviewed on 24 October 2026 against RuneLite 1.13.4. The catalog is curated guidance, not a pathfinder. Hourly rates remain estimates. NPC/object IDs identify a resource; they do not establish a walkable route to it.

## Verified corrections

| Activity | Correction | Reference |
| --- | --- | --- |
| Dust devils | Equip a facemask or Slayer helmet, including cosmetic/imbued variants. Ancient staff ownership does not supply spell runes. | [Dust devils](https://oldschool.runescape.wiki/w/Dust_devils), [Slayer helmet](https://oldschool.runescape.wiki/w/Slayer_helmet) |
| Ice Burst | Requires 70 Magic and Desert Treasure I, rather than 62 Magic. | [Ice Burst](https://oldschool.runescape.wiki/w/Ice_Burst) |
| Anglerfish | Land fishing is in Port Piscarilius, south of Tynan's Fishing Supplies, using a rod and sandworms. Match NPC 6825 rather than a Fossil Island tile. | [Anglerfish fishing spot](https://oldschool.runescape.wiki/w/Rod_Fishing_spot_%28anglerfish%29) |
| Barbarian fishing | Requires bait as well as the rod. Catching gives the secondary XP; cutting supplies bait. | [Barbarian Training](https://oldschool.runescape.wiki/w/Barbarian_Training), [Barbarian fishing spot](https://oldschool.runescape.wiki/w/Fishing_spot_%28barbarian%29) |
| Mining rocks | Match ore-specific IDs, excluding other ores and depleted rocks with the same display name. | [Copper rocks](https://oldschool.runescape.wiki/w/Copper_rocks), [Iron rocks](https://oldschool.runescape.wiki/w/Iron_rocks), RuneLite `ObjectID` |
| Motherlode upper floor | Requires 57 Mining and purchased access for 100 golden nuggets. It stays a manually selectable alternative until the player chooses it; the lower floor remains eligible. | [Motherlode Mine](https://oldschool.runescape.wiki/w/Motherlode_Mine) |
| Blackjacking | Use bearded bandits at 45, unbearded bandits at 55, Menaphite Thugs at 65. | [Thieving training](https://oldschool.runescape.wiki/w/Thieving_training), [Menaphite Thug](https://oldschool.runescape.wiki/w/Menaphite_Thug) |
| Blast Furnace | Equip goldsmith gauntlets for the XP drop, then switch to ice gloves for collection. The gloves cannot be worn together. | [Smithing training](https://oldschool.runescape.wiki/w/Pay-to-play_Smithing_training) |
| Chaos altar | Burning amulet's Chaos Temple destination is a different temple. Use Lava Maze and walk west. Ordinary teleports cannot be used at the altar's Wilderness level. | [Burning amulet](https://oldschool.runescape.wiki/w/Burning_amulet), [Chaos Temple church](https://oldschool.runescape.wiki/w/Chaos_Temple_%28church%29) |

Quest completion and the Wilderness preference are selection requirements. Item checks apply to the requirements declared by each method; they are not a complete inventory planner. A bank step waits for declared equipment and supplies before automatically advancing. Missing mandatory equipped protection suppresses training highlights. Manual Next remains available.

Some unlocks require more than quest completion. Purchased Motherlode access, starting Barbarian Training, Volcanic Mine Kudos/camp rewards, available Nightmare Zone bosses, and selected spellbook remain explicit preparation checks for the player. The panel labels these under Before training. It does not claim to have verified those unlocks automatically.

Town landmarks and manual minigame steps still need logged-in acceptance testing. The anglerfish route deliberately finds the moving NPC instead of inventing another fixed coordinate.

## Adding or changing a route

1. Check the current game source for level, quest, location, equipment, supplies and risk. Record the reference here.
2. Add quest gates and risk metadata. Keep an accessible fallback when possible.
3. Prefer resource IDs when names overlap. Check depleted/transformed objects and moving NPCs.
4. Test every automatic transition, including actions occurring between game ticks.
5. Run `./gradlew check`. Test the actual route in the developer client before claiming in-game acceptance.

## Manual acceptance still required

- Start/stop, change account mode and choose an unsupported skill: old markers must disappear.
- Run a bank → travel → train → drop/bank loop and drop before the next tick.
- Follow the corrected anglerfish route and confirm rod/bait checks and matching moving NPCs.
- With dust-devil protection missing, check the warning, bank latch and absence of training highlights; equip it and recheck.
- Rotate the camera and resize the client: the edge arrow must follow the destination and stay in the game viewport.
- Enable native hint arrows alongside another plugin that uses them: existing external arrows must be preserved.
- Log out, switch account, reconnect and disable/re-enable the plugin: session counters must start fresh and stale callbacks must do nothing.
