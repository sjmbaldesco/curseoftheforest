# Curse of the Forest agent instructions

## Layout and commands
- The Gradle root is `src/`, with `core` game code, `lwjgl3` desktop launcher, and `assets` resources.
- Use JDK 21. Base package: `com.cupids.curseoftheforest`.
- From `src/` on Windows: `./gradlew.bat lwjgl3:run` launches the menu; `./gradlew.bat test` runs the tests; `./gradlew.bat build` builds both modules.
- On macOS/Linux use `./gradlew` instead of `./gradlew.bat`.
- The baseline has 10 tests. Report actual test counts and failures; do not claim a check passed unless it ran.
- Use LibGDX only, desktop LWJGL3 only, and simple tile collision. Do not introduce Box2D.

## Tiled map contract
- Tiles are 32 by 32 pixels.
- Tile layer `solid`: any tile blocks movement.
- Object layer `coins`: rectangle objects represent coins.
- Object layer `markers`: rectangles named `spawn` and `exit`.
- Preserve this contract; see `src/assets/maps/test.tmx`.

## Ownership
- Jairus: `ForestGame`, `Player`, `TileMap`, `GameStage`, `StageTimer`, main-menu and game-over screens and their helpers.
- Johndel: `Inventory`; `creatures/`, `items/`, `minigames/` classes (including `Shop`, `InsufficientCoinsException`, `Riddle`, `PotionDuel`, `PotionType`); mini-game mechanics in the five concrete stages.
- Phebe: `AssetLoader`, `CutScene`, `Hud`, Tiled maps and pixel art, UML, tests, README and presentation.
- Identify the requesting member before editing owned files. This setup is for Johndel.
- Never rewrite another member's classes. Ask for explicit permission before editing files outside the requesting member's ownership; ask about ambiguous ownership first.
- Leave `TODO(name)` stubs untouched unless the requesting member owns that class and the requested change calls for implementing them.
- Do not change shared base classes to make an owned feature easier. Explain any needed integration change first.

## Workflow and explanations
- Work on a feature branch, commit there, and open a PR. Never push directly to `main` or `master`.
- If a direct push to a protected branch is requested later, stop and ask for explicit confirmation; normal work must still use a PR.
- Root agent instructions are shared setup documentation authorized by the setup checklist. Future changes to shared instructions require an explicit request.
- Explain every change in plain language: what changed, why, and how it works. Each member must be able to defend their own code in the Q&A.
- Run relevant checks and report results and limitations before requesting review. Keep changes small and preserve unrelated work.
- These instructions guide the agent; they do not configure app approval settings or enforce GitHub branch protection.
