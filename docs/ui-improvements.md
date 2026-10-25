# Automatic guidance and marker UI — 25 October 2026

Guidance starts for an eligible selected skill on plugin enable, login and selection changes. There are no Start/Stop controls. Goal completion and unavailable routes clear guidance; choosing an eligible route or raising the goal resumes it. Disable Leveling Helper to turn it off.

Membership days, with members-world availability as a fallback, select the account mode automatically. The manual F2P/P2P choice lasts until logout and survives world hops. F2P hides members skills; a hidden selected skill falls back to Woodcutting. The Match account button and routine disclaimer text are removed. Previous, Next and Bank sit below the step list. The floating guide box is removed, leaving the sidebar for instructions and rates.

The scene marker is now a small outlined downward arrow over the destination, with target highlighting retained. A separate overlay renders a compact direction arrow inside the minimap, including when the optional native hint arrow is disabled. The marker follows minimap rotation and zoom and clamps distant destinations inside the map. Hidden or restricted minimaps and targets on other planes are suppressed. The native arrow still yields to server/other-plugin arrows.

## Rules review

Checked against the current [RuneLite rejected features and API restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), [Plugin Hub review process](https://github.com/runelite/runelite/wiki/Plugin-Hub-Review), and [Jagex third-party client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1). These changes read account/game state and draw overlays or update this plugin's own sidebar/checklist. They add no game input, menu actions, walking, banking, camera changes, external programs, network access or boss-mechanic indicators. Restricted minimap states stay suppressed. Manual controls advance the guide only. This is a source review of this UI change, not RuneLite approval of a new release; Plugin Hub updates still require their review.

Quest Helper's [public implementation](https://github.com/Zoinkwiz/quest-helper) was consulted for the scene/minimap presentation pattern. No Quest Helper code or assets are bundled, and no quest functionality is added.

## Validation

Local build and all 108 tests pass against RuneLite 1.13.4. The refreshed `latest.release` build resolves to the same version. The sidebar was rendered with RuneLite's look and feel and visually inspected in both account modes. This also exposed and fixed initial HTML label rendering and unnecessary vertical expansion in the skill grid.

Regression coverage exercises automatic startup/resumption, membership detection on free/member worlds, manual overrides across ticks/hops, logout/reset, F2P skill fallback, goal completion, stale queued commands, and required-equipment highlights. Minimap rendering checks cover fixed and both resizable layouts, rotation, zoom, bounds, native-arrow independence, hidden/restricted maps and other-plane suppression. Scene rendering checks cover placement above a target, offscreen/other-plane suppression, disabled arrows and removal of the floating box. Sidebar checks cover skill filtering, controls and wrapped text.

Before release, verify in a logged-in client:

- Enable the plugin with a saved skill: guidance starts without another click.
- Log into F2P and P2P accounts; check the selected account mode and visible skill grid. Override the mode, hop, then log out/in and check detection resets.
- Select a skill, use Previous/Next/Bank below the steps, reach the goal, then raise it.
- With Native hint arrow disabled, follow a far-away target on the minimap in fixed and both resizable layouts; rotate and zoom. Approach it and check the scene arrow lands above the intended target.
- Hide the minimap or enter a restricted minimap state: no independent arrow should appear.
- Confirm another plugin/server hint arrow remains intact through step changes and disabling Leveling Helper.

Headless rendering and mocked regression tests do not replace these in-game checks. The previous computer smoke test covers the older release UI only.

The UI, membership filters and markers are shared by all 197 catalog methods across 24 skills. Scene tiles, minimap, optional native hints, sidebar direction and world-map destinations use the same resolved target, including preparation detours and any-bank fallbacks. A member-only route waits for a members world. See [the full preparation and route coverage](item-preparation.md).
