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
- Each stage owner builds that stage end to end, including its Tiled map in `src/assets/maps/`.
- Jairus: `ForestGame`, `Player`, `TileMap`, `GameStage`, `StageTimer`, main-menu and game-over screens and their helpers; Stage 3 (`GubatStage`, `Tikbalang`, `GlowingBush`).
- Johndel: `Inventory`; `items/` classes, `Shop`, `InsufficientCoinsException`; creatures `LolaPacita`, `DuwendeTonyo`, `Wakwak`; mini-game mechanics in Stages 1–2.
- Phebe: `AssetLoader`, `CutScene`, `Hud`, the abstract `Creature` base, Stage 4 (`TulayStage`, `Nuno`, `Riddle`) and Stage 5 (`MangkukulamStage`, `Mangkukulam`, `PotionDuel`, `PotionType`), pixel art, UML, tests, README and presentation.
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
# AGENTS.md

Instructions for AI coding agents (Codex, Claude, and others) working in this repo. Read `README.md` for the game itself.

Curse of the Forest is the OOP 101 final project of Team CUPIDS (BatStateU, CS-2103): a 2D platformer in Java + LibGDX. The instructor allows AI use, but every member must be able to explain their code in the Q&A, so clarity beats cleverness.

## Commands

The Gradle project lives in `src/`, not the repo root. Requires JDK 21.

```bash
cd src
./gradlew lwjgl3:run     # run the game (Windows: gradlew.bat lwjgl3:run)
./gradlew test           # JUnit 5 tests in core/
./gradlew lwjgl3:jar     # runnable jar in lwjgl3/build/libs/
```

Run `./gradlew test` before every commit; it must pass.

## Layout

```
src/
├── core/       game code, base package com.cupids.curseoftheforest
│   ├── ForestGame, AssetLoader
│   ├── player/      Player, Inventory
│   ├── stages/      GameStage + 5 stages, StageTimer, TileMap, Checkpoint
│   ├── screens/     MainMenuScreen, GameOverScreen, CutScene, Hud, MenuText
│   ├── items/       Item, Weapon, Consumable, Accessory, Potion
│   ├── creatures/   Interactable, Creature + creatures, GlowingBush
│   └── minigames/   Shop, InsufficientCoinsException, Riddle, PotionDuel, PotionType
│   (tests in core/src/test/java, same packages)
├── lwjgl3/     desktop launcher: com.cupids.curseoftheforest.lwjgl3.Main
└── assets/     maps/ (Tiled .tmx), images/
docs/           UML class diagram (PDF)
screenshots/
```

New classes go in the sub-package that matches the UML in `docs/`. Do not add packages or classes outside the UML without the team agreeing first.

## Hard constraints

- LibGDX 1.13.1 core + LWJGL3 desktop backend only. No Box2D, no Ashley, no extra libraries. Desktop only (no Android, iOS, or web modules).
- Collision is simple axis-by-axis tile collision in `Player`/`TileMap`. Do not replace it with a physics engine.
- Java 21, plain Java style. Keep the OOP structure visible: abstract `GameStage`, `Item`, `Creature`; interface `Interactable`; subclasses override `init()`, `update()`, `handleInput()`, `use()`, `interact()`.
- Use exceptions for invalid actions and missing assets instead of crashing (e.g. `InsufficientCoinsException`, `AssetLoader` placeholder fallback).

## Tiled map contract

Every `.tmx` in `src/assets/maps/` must have:

- tile layer `solid`: any tile there blocks movement
- object layer `coins`: rectangles, one per coin
- object layer `markers`: rectangles named `spawn` and `exit`
- 32 px tiles

`src/assets/maps/test.tmx` is the reference. If a stage needs a new layer, add it to this list and to `README.md`.

## Class ownership

Each member must defend their own classes, so respect ownership.

Each stage owner also makes that stage's Tiled map.

| Owner | Classes |
|---|---|
| Jairus (@sjmbaldesco) | `ForestGame`, `Main`, `Player`, `TileMap` and collision, `GameStage` and stage transitions, `StageTimer`, `Checkpoint`, `MainMenuScreen`, `GameOverScreen`, Stage 3 `GubatStage` + `Tikbalang` + `GlowingBush` |
| Johndel (@jdvoid) | `Item` hierarchy, `Inventory`, `Shop`, `InsufficientCoinsException`, `LolaPacita`, `DuwendeTonyo`, `Wakwak`, the mini-game logic in Stages 1–2 (`SimulaStage`, `PalengkeStage`), item effects (`use()` overrides) |
| Phebe (@phiiibibii) | `Creature` (abstract base), Stage 4 `TulayStage` + `Nuno` + `Riddle`, Stage 5 `MangkukulamStage` + `Mangkukulam` + `PotionDuel` + `PotionType`, `CutScene` and dialogue, `Hud`, `AssetLoader`, pixel art, input validation checks, tests and evidence, README, UML |

Rules:

- Only change classes owned by the member you are working for. For anyone else's class, describe the suggested change (in the PR description or a comment) instead of rewriting it.
- `TODO(Name)` marks work that belongs to that person. Do not fill in another member's `TODO` unless that member asks.
- Stubs that are not done yet throw `UnsupportedOperationException("TODO(Name)")`; keep that pattern for new stubs.
- If unsure who owns something, ask instead of guessing.

## Explain every change

For each change, state in the PR description (and briefly in your reply):

1. What changed and why.
2. The OOP concept involved (inheritance, abstraction, encapsulation, polymorphism, interfaces, exceptions, collections) and where it shows in the code.
3. One question the instructor might ask about it, with a short answer.

Add short comments only where the reason is not obvious, matching the existing comment density.

## Branches, commits, PRs

**AI agents never push.** Do not run `git push`, open or merge pull requests, create tags, or change branches on GitHub on your own. Make your changes in the working tree (and local commits, if the member asks), then stop and tell the member what to review. The member in charge of the task reads the change, commits it as themselves, and pushes it. This keeps every commit in the history one the member has checked and can explain.

The only exception is the Claude assistant in Jai's own project workspace (repo owner): it may push task branches and open pull requests, but never pushes to `main` and never merges. Jai or a reviewer merges. No other agent may claim this exception.

- Never push to `main`. Create a branch per task (e.g. `jairus/stage-timer-bonus`), open a pull request to `main`, and let another member review it before merging.
- One logical change per commit, with a clear imperative message. The instructor reviews each member's commit history, so commit as the member doing the work and avoid bulk or meaningless commits.
- Keep `README.md` (structure, OOP table, map contract) in sync when your change affects it.
