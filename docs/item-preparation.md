# Preparation and route coverage — 25 October 2026

All **197 catalog methods across 24 skills** pass through the shared preparation policy. There are 53 F2P and 150 P2P entries, with six entries shared by both modes. Banking variants are additional views of those methods. [Every route, its inputs, player checks, inventory policy and progression rules](route-coverage.tsv) are listed separately.

## Shared behavior

The UI and membership lifecycle apply to every skill: guidance starts automatically, membership is detected with a manual F2P/P2P override, member skills are hidden in F2P mode, and Previous/Next/Bank sit below the steps. Instructions remain in the sidebar. The independent minimap marker works without the native hint arrow. Scene tiles/arrows, minimap, world map and sidebar direction share the resolved destination, including supply detours.

Required items, optional items and first-trip supplies have different roles. Optional food never causes a mandatory bank trip. First-trip fees, seeds and traps do not cause false depletion detours after preparation. If a guide is selected after paying, planting or deploying traps, its explicit Next instruction lets the player acknowledge that first-trip preparation. This acknowledgement does not verify deployed trap ownership, patch state or payment history.

An empty inventory at login can have no inventory container even when equipment is present. After a logged-in game tick has synchronized packets, an absent inventory is treated as empty, with 28 free slots; equipped tools still count. Before synchronization the guide waits, and logout/loading/hops reset that knowledge. No equipment cycling is needed.

Inventory and equipment are checked using unnoted item IDs or categories matched against actual item definitions, equipment slots and use levels. Pickaxes/axes can be carried or equipped; wielding Attack levels are not required for carried tools. Broken, noted and inactive variants do not satisfy the tool declaration. A usable tool is enough; purchasing an upgrade is optional.

Bank contents are unknown until observed in the current login. Positive quantities are cached and updated from bank-container events. Placeholders do not count. Logout, loading/hopping and account changes clear the cache. No bank data is persisted. A missing item is shown as Equip, Retrieve, Withdraw or Obtain. Known leprechaun tools are retrieved at a patch rather than bought again. Ultimate Ironman accounts receive sources rather than an impossible bank-stock check.

Missing recurring inputs pause training guidance and return to preparation while retaining the selected method. Partial bank stock is withdrawn before acquiring the remainder. GE destinations require a trading account and item definitions confirming tradeable alternatives; untradeable quest/activity items receive recovery sources. Construction can offer servant restocking without leaving the house. The Bank button remains available on every route, including courses with no original bank step.

## Skill families

| Skills | Detected preparation | Player checks retained |
| --- | --- | --- |
| Attack, Strength, Defence, Hitpoints | Equipped melee weapon and optional healing supplies; unarmed combat remains valid | Chosen XP attack style, combat setup, NMZ dream/payment |
| Ranged | Equipped ranged weapon, ammunition or a self-contained/thrown weapon, optional food | Ammunition compatibility/tier, remaining internal charges, attack style |
| Magic | Exact named-spell inputs, recommended standard combat cast from available supplies, equipped powered staffs, elemental staff/tome substitutes, combination runes, carried normal/divine rune pouches, dust protection | Selected spell/style, internal charges, splashing accuracy, NMZ preparation; spellbook mismatch is automatically flagged |
| Prayer | The route's bones and quantities | Hosted/lit altars, Wilderness transport and escape |
| Woodcutting, Mining | Usable axes/pickaxes and applicable food | Purchased unlocks, Volcanic entry/team preparation; Motherlode retains its hopper/sack sequence |
| Fishing | Nets/rods/harpoons and required feathers/bait | Ferry/transport choice, Tempoross supplies obtained inside the activity |
| Cooking, Firemaking | Raw ingredients, wines, logs/tinderbox; Wintertodt tools | Variable burns, Wintertodt warm clothing/healing and activity state |
| Crafting, Fletching | Exact recipe inputs and quantities, knives/chisels/needles/thread where required | Adaptive leather recipes accept usable soft/hard leather without a fixed XP forecast; darts require tips and feathers, no knife |
| Smithing | Bars and action quantities; cannonball moulds/steel; Blast Furnace ore and both glove sets | Furnace coffer/foreman and glove swaps; Foundry's 28 bar-equivalents, salvage/alloy choice and existing preform are explicit player checks |
| Runecraft | Essence, altar talisman/tiara, lava supplies, active Magic Imbue, binding necklace; GOTR pickaxe/chisel | Necklace charges, transports and in-minigame guardian equipment |
| Herblore | Exact potion combinations; usable grimy herbs | Variable herb choice and costs; Druidic Ritual is an eligibility gate |
| Construction | Saw/hammer, eight oak planks for a larder or six mahogany planks for a table; variable-route planks | Contract-specific tier/quantities/steel, house/hotspot/servant/nails and transport |
| Hunter | Snares, box traps, rental coins or an equipped falconer's glove | Number/ownership of deployed traps, rental already paid, empty equipment slots for falconry |
| Farming | Tools, usable seeds/saplings and starting quantities; leprechaun storage; optional compost | Full-run quantities, already-treated patches, bottomless bucket contents, growth timers, transports |
| Agility | Optional healing supplies; ordinary courses have no mandatory carried tool | Course/access unlocks and optional Sepulchre looting equipment; no trap prediction |
| Thieving | Optional healing supplies; equipped blackjack; antipoison for Plunder | Failure damage/healing, coin pouches, Plunder activity state |
| Slayer | Optional healing supplies | Actual task, task-specific weapon/protection/ammunition/runes/access; a selected master does not establish the assigned task |
| Sailing | Explicit declaration that installed facilities and assigned cargo are outside carried-item verification | Boat, facilities, selected task/cargo, trial/rank and destinations |

A Ready row confirms its listed item, not the player's full activity setup. Every external condition is labelled **Player check**. The guide does not infer hidden world state or promise that a flexible contract, assigned Slayer task or minigame loadout is fully verified.

## Inventory and style rules

All routes declare whether inventory contents are collected for banking, dropped or banked according to style, processed, or managed manually. **Better profit** converts every collecting drop route into a deposit/return loop, including manual alternatives and barbarian fishing. The dropdown labels converted routes `(banking)`. It does not reuse power-training XP/hour estimates for a bank loop.

A full gathering inventory must be deposited before the bank loop resumes. Motherlode goes to the hopper, recipes continue processing, and combat/minigames do not become generic bank loops just because food fills the inventory. Other styles retain the route's explicit XP/drop behavior.

Fixed-XP recipes estimate material quantities to the next level, separately from the minimum needed to begin. Variable XP, burns, thread consumption, already-supplied elemental runes and variable activity materials are not presented as exact forecasts.

Previous/Next/Bank only update this plugin's checklist. No purchase, withdrawal, equip action, spell selection, item interaction or movement is automated. Item sources are instructions, not recursive crafting or obstacle-aware pathfinding.

## Local evidence and acceptance

`JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew build --offline --no-daemon` passes against RuneLite 1.13.4 with **108 tests**. Tests exercise all 197 route policies, all style variants, and **19,008 skill/membership/style/level combinations**, including manual alternatives. All collecting bank loops are exercised with full inventory, unsuccessful banking and deposited stock. The matrix is generated by `CatalogCoverageTest` and copied to `docs/route-coverage.tsv`.

Runtime regressions cover initial empty inventories with worn tools, absent inventory nodes after consumption, synchronization reset, bank closing with empty inventory, tool ownership, exact recipe quantities, secondary depletion, optional food, deployed traps, first-trip acknowledgement, leprechaun retrieval, servant restocking, elemental providers, pouch presence, spell recommendations/book mismatch, untradeable recovery, member-world gating, forced bank visits, protection removal, account/cache reset, UI lifecycle and shared markers. Sidebar renders are saved in `build/reports/`.

These are local data, mocked lifecycle and headless rendering checks. Live acceptance remains: test representative F2P/P2P bank/drop loops; a full Motherlode inventory; both ingredients of a production recipe; normal/divine pouch and elemental staff use; Hunter after setting traps; leprechaun retrieval; servant replenishment; untradeable item recovery; and a member route after hopping from a free world. Do not treat this matrix as 197 routes individually played in-game or Plugin Hub approval.

References: [RuneLite restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), [Jagex guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), [Giants' Foundry](https://oldschool.runescape.wiki/w/Giants%27_Foundry), [Volcanic Mine](https://oldschool.runescape.wiki/w/Volcanic_Mine), [goldsmith gauntlets](https://oldschool.runescape.wiki/w/Goldsmith_gauntlets). See [the UI/rules review](ui-improvements.md).
