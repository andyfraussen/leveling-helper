# Computer smoke test — 24 October 2026

Tested the local RuneLite 1.13.4 developer client through Computer on macOS, logged in on a free-to-play world. The test character had 34 Mining and an empty inventory, standing in Al Kharid bank.

The developer JAR was packaged under ignored `build/macos/LevelingHelperDev.app` with JDK 17 `jpackage` so Computer could attach to a macOS application. This is a local test launcher, not a Plugin Hub release.

## Observed results

| Check | Result |
| --- | --- |
| Open plugin sidebar while stopped | Mining statistics and Iron recommendation displayed; no guide overlay. |
| Start Mining | Stop guiding and step controls appeared; overlay showed step 1/4; local bankers and bank booths were highlighted. |
| Next from bank | Step 2/4 selected; bank highlights disappeared; destination read 126 tiles northeast; direction arrow appeared. |
| Bank control | Returned to bank preparation step. This checks guide state only, not a banking transaction. |
| Select Agility while guiding in F2P mode | Members-only explanation appeared; old method, overlay, arrow and highlights disappeared. |
| Return to Mining after that selection | Guidance resumed automatically. See issue below. |
| Stop guiding | Step controls, overlay and highlights disappeared; Start guiding returned. |
| Goal equals current level | Goal 34 displayed the already-at-goal message and 0 XP remaining. Clicking Start did not activate guidance. |
| Restore goal | Goal 99 restored; guide remained stopped. |
| Match account while stopped | F2P mode remained selected and guidance remained stopped. |

## Issues reproduced

1. **Sidebar text clips at the right edge.** Long introduction, status, route summary and step text wrap beyond the visible sidebar width. `LevelingHelperPanel.html` fixes the HTML body at 190 CSS pixels while `WrapLabel` measures against the parent width; the rendered width needs to follow the available content width.
2. **Unsupported selection leaves a hidden start intent.** Reproduce: start Mining in F2P mode, choose Agility, then choose Mining. Agility shows Start guiding and clears the guide, but Mining immediately starts again. `guidePaused` remains false while no valid method exists. Either pause when selection becomes unavailable or explicitly represent the suspended state and keep a usable Stop control.

Both findings were fixed in the subsequent publication preparation. Unavailable routes now pause guidance until an explicit restart. Sidebar HTML now wraps to the available content width, including panel insets. Regression tests cover unsupported-selection/restart behavior and wrapping at 160, 205 and 280 pixels with 13, 18 and 24 point fonts. A headless Swing render was visually checked. These fixes have not been retested through live game input.

## Limits

This was an in-game UI smoke test, not acceptance of every training route. No full mining/inventory/drop loop, moving fishing NPC, protective-equipment check, native-arrow conflict, logout/account-switch cycle or members route was exercised. Camera input did not visibly rotate the camera, so camera-aware arrow behavior remains unverified here. Direct goal text entry could not be exercised reliably through Computer; the spinner accessibility actions worked.

Final state: guide stopped, Mining / Fastest XP / Free-to-play / goal 99, character still in the bank, inventory unchanged.
