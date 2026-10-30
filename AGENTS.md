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

| Owner | Classes |
|---|---|
| Jairus (@sjmbaldesco) | `ForestGame`, `Main`, `Player`, `TileMap` and collision, `GameStage` and stage transitions, `StageTimer`, `Checkpoint`, `MainMenuScreen`, `GameOverScreen` |
| Johndel (@jdvoid) | `Creature` hierarchy, `Item` hierarchy, `Inventory`, `Shop`, `InsufficientCoinsException`, `Riddle`, `PotionDuel`, `PotionType`, the mini-game logic inside the five stage subclasses |
| Phebe (@phiiibibii) | `CutScene` and dialogue, `Hud`, `AssetLoader`, Tiled maps and pixel art, input validation checks, tests and evidence, README, UML |

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

- Never push to `main`. Create a branch per task (e.g. `jairus/stage-timer-bonus`), open a pull request to `main`, and let another member review it before merging.
- One logical change per commit, with a clear imperative message. The instructor reviews each member's commit history, so commit as the member doing the work and avoid bulk or meaningless commits.
- Keep `README.md` (structure, OOP table, map contract) in sync when your change affects it.
