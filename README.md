# Leveling Helper

Step-by-step skill training for RuneLite, with a checklist, world highlights, banking routes and XP estimates. Choose a skill, training style and goal; guidance starts automatically.

The catalog contains **197 methods across all 24 skills**, with free-to-play and members routes. The plugin reads game state and displays guidance. You perform every game action yourself.

## Getting started

Open the Leveling Helper sidebar and log in:

1. Check **Account**. Membership is detected automatically; you can override Free-to-play or Members for the current login.
2. Choose a training style and level goal.
3. Pick a skill. The guide selects an eligible method for your level and shows its preparation and steps.
4. Follow the highlighted destination. Use **Previous**, **Next** and **Bank** below the checklist when you want to change steps yourself.

Members skills are hidden in Free-to-play mode. A manual account choice survives world hops and resets on logout. A members-only route waits until you are on a members world.

| Style | Focus |
| --- | --- |
| Balanced | A practical balance of XP, cost and effort |
| Fastest XP | Faster training, often with greater cost or attention |
| More AFK | Less frequent interaction where suitable methods exist |
| Better profit | Keeping resources and favoring money-making methods |

Use **Other methods at this level** to choose an alternative. Your selected style still applies: under **Better profit**, choosing Iron instead of the recommended Coal gives a mine → bank → return loop. Collecting alternatives that normally drop their output use a banking variant, labelled **(banking)**, including barbarian fishing.

## Tools, supplies and banking

The sidebar checks the items declared by the selected route against your inventory and equipment:

| Status | What to do |
| --- | --- |
| Ready | The listed item is available in the required container |
| Equip | Equip an item already in your inventory |
| Retrieve | Retrieve detected stored tools from a tool leprechaun |
| Withdraw | Withdraw supplies found in your last bank check |
| Obtain | Follow the item's shop, gathering, crafting or recovery source |

A usable pickaxe or axe can be carried or equipped. Owning a suitable tool avoids an unnecessary bank visit; the guide does not require buying the most expensive upgrade. Recipes check their required combinations and minimum action quantities, such as dart tips plus feathers or five bars for a platebody.

Bank stock is **unknown until you open a bank**. Observed quantities are kept for the current login, updated as the bank changes and cleared on logout, loading/world hops or account changes. Placeholders do not count as supplies. GE acquisition guidance requires a trading account and confirmed tradeable alternatives; untradeable equipment receives recovery instructions. Ironman accounts receive item sources, and Ultimate Ironman accounts are not asked to check bank stock.

When recurring supplies run out, the guide pauses training guidance and offers preparation or restocking while retaining your method. Construction can offer servant replenishment. Optional supplies and first-trip items are handled separately, so optional food or deployed traps do not repeatedly send you to the bank.

A full gathering inventory follows the selected banking or dropping route. Activity-specific steps stay intact: Motherlode uses its hopper/sack sequence, and production recipes continue processing their inputs. **Bank** can request a bank visit on any route, including routes without an original bank step.

### What still needs a player check

A **Ready** item row confirms that item, rather than the entire activity setup. Conditions the guide cannot establish are labelled **Player check**, including:

- Contract-specific materials, quantities and destinations.
- Your assigned Slayer task and its equipment requirements.
- Internal weapon charges, ammunition compatibility and chosen combat style.
- Minigame entry payments, team preparation and supplies obtained inside the activity.
- Deployed trap ownership, patch state, full farming-run supplies and transport choices.

Quest completion and declared skill/combat requirements gate route eligibility. Some purchased unlocks and access conditions still need checking in-game. Wilderness routes require enabling **Include Wilderness routes** in the plugin settings. Quest walkthroughs are outside the current training catalog.

See the [preparation coverage and limitations](docs/item-preparation.md), the [complete route matrix](docs/route-coverage.tsv), and the [catalog verification checklist](docs/catalog-verification.md).

## Highlights and progression

Instructions stay in the sidebar. Destination tiles and targets can be highlighted, a compact downward arrow marks visible targets, and an independent minimap marker points toward distant destinations. There is no floating instruction box.

**Target arrows** controls the scene arrow and minimap marker. The optional **Native hint arrow** is off by default; the independent minimap marker works without it. Native hints yield to existing server or other-plugin arrows. Hidden or restricted minimaps remain hidden.

Steps can advance when you arrive, close the bank after preparing, fill the inventory or finish the watched input. Activity steps that need a manual decision remain on the checklist until you advance them. Guidance resumes automatically after logging in or changing to an eligible selection, and clears at the goal or when no route is available. Disable the plugin to turn guidance off.

## XP and progress

The sidebar shows XP to your next level and goal, your measured session XP/hour, and the next method you unlock. Fixed-XP recipes also show estimated actions and materials needed. Typical training rates and time estimates are approximate; your session rate includes travel and banking.

Banking variants do not reuse power-training XP/hour estimates. Variable recipes, burns and other uncertain consumption are not presented as exact material forecasts. Reconnects and world hops start a fresh session-rate baseline.

## Run locally

Install **JDK 17** and use the included Gradle wrapper:

```bash
./gradlew run
```

This opens a developer RuneLite client with Leveling Helper loaded from the current checkout. Log in and open its sidebar to test. For Jagex account setup, follow [RuneLite's instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

After changing Java sources, restart the developer client to load the new build. Running from this checkout does not update an installed Plugin Hub release.

On macOS with Homebrew's JDK 17, you can select it explicitly:

```bash
JAVA_HOME="$(brew --prefix openjdk@17)" ./gradlew run
```

### Build and test

```bash
./gradlew build
```

The plugin targets Java 11 bytecode. Local validation against RuneLite **1.13.4** passes **102 tests**, including all catalog policies, banking loops, supply depletion, membership handling and marker/UI behavior. Selection checks cover **19,008 skill/account-mode/style/level combinations**.

CI checks RuneLite `1.13.4` and `latest.release`. To check the fixed compatibility target locally:

```bash
./gradlew check -PruneLiteVersion=1.13.4
```

Headless rendering and mocked game-state checks do not replace testing routes in a logged-in client. See the [UI and rules review](docs/ui-improvements.md) for the remaining live acceptance checks. Plugin Hub review and publication are separate from a local build or Git push.
