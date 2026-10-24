# Leveling Helper

A RuneLite plugin that trains skills the way Quest Helper walks quests.

You pick free-to-play or members, pick a skill, and pick a style: balanced, fastest XP, more AFK, or better profit. The side panel then gives the current method for your level, the bank trip, the walk, and the thing to click. The destination tile is marked, the NPC or object is highlighted, and an arrow points at it when it is off screen.

It also shows:

- XP to the next level and to your goal
- actions left, when the action has a fixed XP value
- a typical XP per hour for the method
- your session XP per hour once you have been gaining XP for a few seconds
- a rough time to goal at both rates
- the next method you unlock
- optimizations such as quest XP, a better tool, or a faster alternative

Steps advance on their own when you arrive, close the bank, fill the inventory, or empty the item the step is watching. Nothing in the plugin clicks, types, or moves your character.

## What already exists

These are useful, and they are not this:

| Plugin | What it does |
| --- | --- |
| [Quest Helper](https://runelite.net/plugin-hub/) | Step-by-step quests, not skill training |
| [Efficient Skilling](https://github.com/DapperMickie/EfficientSkillingPlugin) | Builds a level-by-level plan. It does not walk you there or run the bank loop |
| Skill Calculator | How many actions a method takes. No route |
| XP Tracker | Your live XP per hour. No route |
| Guide Overlay | Turns a wiki page you pick into a checklist |

Use Efficient Skilling if you only want a plan. Use this if you want the plan standing in the world with you.

## Run it

You need a JDK 17 or newer. The plugin itself is compiled for Java 11, which is what RuneLite plugins use.

```bash
./gradlew test
./gradlew run
```

`test` checks the routes, the XP maths, and the step logic. `run` opens a developer RuneLite with this plugin loaded. Log in with a Jagex account using the steps at https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts

Then open the sidebar icon and:

1. Choose Free-to-play or Members. **Match account** reads your membership.
2. Choose a style and a goal.
3. Choose a skill.
4. Press **Start guiding** and follow the highlighted step.
5. Use **Next**, **Back**, and **Bank** if you want to move the checklist yourself.

Rates are typical figures for ordinary gear, without tick manipulation. They are labelled as typical. The session rate is the one measured from your XP drops. Locations come from OSRS Wiki map points. A few training tiles are the landmark next to the real object, and the object highlight takes over once you are close.

This is not a bot. Do not expect it to play the account.
