# Reset Plan and Progress Log

Team CUPIDS · Curse of the Forest · OOP 101 (CS-2103)
Started 2026-11-11 · Lock-in day **Sat 2026-11-14** · Final: finals week **Dec 7–13, 2026**

> **Status: PLAN ONLY.** Nothing in `main` has been reset, deleted or force-pushed. Step 1 of the reset waits for Jai's approval (see [Open questions](#6-open-questions-for-jai)).

---

## 1. Reset approach

### Where the repo stands (checked 2026-11-11, `main` at `e93ba4d`)

- 7 commits, all from the Oct 23 baseline and PR #1 (AGENTS.md). **No commits from Johndel or Phebe yet.**
- ~1,630 lines of game code. About 900 of them are Jai's classes, fully working (`Player` 250, `GameStage` 190, `TileMap` 167, `ForestGame` 122, screens, `StageTimer`, `Checkpoint`), with 10 passing JUnit tests.
- Johndel's and Phebe's classes are 8–40 line stubs or minimal versions (`TODO(Johndel)` / `TODO(Phebe)`).
- The infrastructure is the part worth keeping regardless: Gradle 8.14 + LibGDX 1.13.1 + Java 21 under `src/`, the LWJGL3 launcher, package layout matching the UML, the Tiled map contract and `test.tmx`.

So the imbalance is real: one member's code exists and works, the other two have nothing to show in the commit history. The guidelines grade **individual contribution and commit history**, and every member must defend their own code in the Q&A.

### Options

| | Option | What it costs | What it fixes | Risk |
|---|---|---|---|---|
| A | **Full reset** (wipe `src/`, regenerate with gdx-liftoff) | Redo Gradle setup, launcher, map contract, tests. ~1 day lost on plumbing nobody gets graded for. | Everyone starts equal. | Highest. Burns Saturday on setup instead of game code. |
| B | **Rebalance only** (keep all code, reassign remaining work) | Nothing now. | Future work is even. | Jai's finished classes stay authored by the AI baseline, and whoever inherits `Player`/`GameStage` has nothing of their own to show. Jai is left with little coding. |
| C | **Skeleton reset** (keep infrastructure, turn every class back into a UML stub, reassign, each owner reimplements) | ~900 lines rewritten, but the tagged baseline is a working reference, so it is fast. | Even split **and** each member's commits show their own classes. | Low. Done through a normal PR; old code stays reachable by tag. |

### Recommendation: Option C, done non-destructively

A full reset is not efficient: the Gradle/LibGDX setup is the slowest, least-graded part and it already works. Option B is the cheapest, but it does not fix the real problem (commit evidence and ownership). Option C keeps what is expensive and ungraded, and resets what is graded.

**If the only goal is an even split of the remaining work and commit history does not matter to you, pick B instead.** That is the open question that decides it.

**Safe procedure (after Jai approves):**

1. Tag the current state: `git tag baseline-2026-10-23 e93ba4d` and push the tag. Nothing is lost; anyone can `git checkout baseline-2026-10-23` to read the old code.
2. On branch `jairus/skeleton-reset`, keep `src/` build files, launcher, assets, `test.tmx`, `AGENTS.md`, `docs/`. Replace each class body with its UML signatures and `throw new UnsupportedOperationException("TODO(Name)")` for the **new** owner. Keep the base `ForestGame` boot so the app still launches to a blank menu.
3. Existing tests (`PlayerMovementTest`, `PlayerStatsTest`, `StageTimerTest`) are kept as **acceptance tests**, marked `@Disabled("TODO(Name): enable when implemented")`. Each owner re-enables theirs; they are the definition of done.
4. Update the ownership table in `AGENTS.md` and `README.md` in the same PR.
5. PR into `main`, reviewed by Johndel or Phebe, merged normally. **No force-push, no branch deletion, `main` history preserved.**
6. Log the change in the vault decision log (draft in [section 7](#7-decision-log-draft)) after Jai approves.

---

## 2. Rebalanced ownership

Principle: each member owns **one abstract hierarchy + one support piece + full stages (mini-game, creature, map)**, so each can demonstrate inheritance, abstraction, polymorphism and exceptions in their own code. Effort is in points (1 small, 2 medium, 3 large).

| | **Jai** (@sjmbaldesco) | **Johndel** (@jdvoid) | **Phebe** (@phiiibibii) |
|---|---|---|---|
| Abstract hierarchy | `GameStage` (abstract, extends `ScreenAdapter`) — 2 | `Item` → `Weapon`, `Consumable`, `Accessory`, `Potion` — 2 | `Creature` (abstract) base — 1 |
| Core / support | `ForestGame`, `Main`, stage transitions — 2; `TileMap` + collision lookups — 2; `StageTimer`, `Checkpoint` — 1; `MainMenuScreen`, `GameOverScreen` — 1 | `Player` (movement, jump, stats, coins) — 3; `Inventory` — 1; `Shop` + `InsufficientCoinsException` + shop screen — 2 | `AssetLoader` (try-catch fallback) — 1; `Hud` + `MenuText` — 2; `CutScene` + intro, 2 flashbacks, serenade, ending — 3 |
| Stages (mini-game + creatures + Tiled map) | **Stage 3 `GubatStage`**: clue search, `Tikbalang`, `GlowingBush`, `Interactable` — 3 | **Stage 1 `SimulaStage`**: pattern game, `LolaPacita` — 2; **Stage 2 `PalengkeStage`**: Wakwak chase, `DuwendeTonyo`, `Wakwak` — 3 | **Stage 4 `TulayStage`**: `Riddle`, `Nuno` — 2; **Stage 5 `MangkukulamStage`**: `PotionDuel`, `PotionType`, `Mangkukulam` — 3 |
| OOP concepts they defend | abstraction, polymorphism (`update`/`render`/`isComplete` per stage), interface | encapsulation (private stats, `spendCoins`), inheritance + overridden `use(Player)`, custom exception, `ArrayList` | inheritance + overridden `interact()`, exception handling, enum, arrays |
| Shared work | Tileset + collision layer, README, UML regen, coordination | Dayo sprite, test evidence + screenshots | Slides (everyone writes their own slide), dialogue text formatting |
| **Total** | **11 + coordination and reviews** | **13** | **12** |

Jai's coding load is lighter than before (no `Player`, one stage) to account for coordination and reviewing. Every stage owner makes their own stage's `.tmx` and creature sprites, following the map contract in `AGENTS.md`.

**Interfaces that cross owners** (freeze these on Saturday morning, change only by PR with the other owner's review):

- `Player.update(float delta, TileMap map)` → calls `map.isSolid(col, row)` (Johndel → Jai)
- `Player.spendCoins(int)` / `getCoins()` / `getInventory()` (Shop, Hud, stages → Johndel)
- `Item.use(Player)`; `Inventory.has(String)` (stages check Agimat, Malunggay Juice → Johndel)
- `Creature.interact(Player)`; `Interactable` (Phebe base, used by all stage owners)
- `AssetLoader.loadImage(String)` (everyone → Phebe)
- `GameStage` abstract methods `init()`, `update(float)`, `handleInput()`, `isComplete()` (all stage owners → Jai)
- `CutScene(lines, background)` + `isFinished()` (Jai's transitions → Phebe)

---

## 3. Schedule

Working back from Dec 7 (earliest finals day): **feature freeze Nov 28**, **code freeze Dec 5**.

### Prep, Nov 11–13

| Date | Who | Task |
|---|---|---|
| Wed 11 | Jai | Review this plan, answer the open questions, approve A/B/C. Send the ownership table to the team. |
| Wed 11 | Johndel, Phebe | Confirm Saturday hours and whether in person or online. |
| Thu 12 | Jai (+ Claude) | Push tag `baseline-2026-10-23`; open the skeleton-reset PR. |
| Thu 12 | All | Clone, open `src/` in IntelliJ, run `gradlew.bat lwjgl3:run` and `gradlew.bat test` on **Windows**. Set `git config user.name/user.email` to your own GitHub account. Read `AGENTS.md` and your classes in the UML (`docs/`). |
| Fri 13 | Johndel or Phebe | Review and merge the skeleton PR. |
| Fri 13 | All | Each member writes the method list for their classes (from the UML) and reads the old implementation of anything they now own at the tag. |
| Fri 13 | Phebe + Jai | Story text for the intro and Flashback 1 (still unfinished; blocks cutscenes). |

### Lock-in day, Sat Nov 14 (assumed 09:00–21:00 Asia/Manila; adjust once confirmed)

**Goal by 21:00:** menu → a stage on `test.tmx` with Dayo moving and colliding, HUD showing Courage/Sanity/coins, game over and retry working, a shop purchase that can throw `InsufficientCoinsException`, the intro cutscene playing. Every member has merged at least two PRs of their own.

| Time | Jai | Johndel | Phebe |
|---|---|---|---|
| 09:00–09:30 | **Kickoff (all):** confirm ownership, freeze the cross-owner interfaces above, create branches. | | |
| 09:30–12:00 | `TileMap` (load, `isSolid`, render) + `GameStage` loop | `Player` movement, jump, collision via `TileMap` (re-enable `PlayerMovementTest`) | `AssetLoader` with fallback, `Creature` base, `Interactable` agreed with Jai |
| 12:00–13:00 | Lunch | | |
| 13:00–13:45 | **Integration 1 (all):** PRs reviewed by rotation, merge order Jai → Johndel → Phebe, run the game together. | | |
| 13:45–16:30 | `ForestGame` flow, `MainMenuScreen`, `GameOverScreen`, `StageTimer`, `Checkpoint` retry | `Player` stats (re-enable `PlayerStatsTest`), `Inventory`, `Item` hierarchy | `Hud` meters, `CutScene` + intro scene |
| 16:30–17:15 | **Integration 2 (all):** merge, full run from menu to game over and retry. | | |
| 17:15–18:00 | Dinner | | |
| 18:00–20:15 | `GubatStage` skeleton: timer, `GlowingBush` | `Shop` + `InsufficientCoinsException` + shop screen | `Riddle` + `TulayStage` skeleton |
| 20:15–21:00 | **Integration 3 + retro (all):** merge, update the checkboxes below, agree next week's targets. | | |

Rules for the day: pull `main` after each integration; if blocked on another person's class for more than 15 minutes, use a temporary stub in your own branch and say so in the PR.

### After Saturday

| Window | Target |
|---|---|
| Nov 15–21 | Each owner finishes their first stage's mini-game on its real `.tmx` (Jai Gubat, Johndel Simula, Phebe Tulay). Item-dependent timers (Agimat, Malunggay Juice). Mid-week sync (Wed). |
| Nov 22–28 | Remaining stages (Johndel Palengke, Phebe Mangkukulam), potions from riddles feed the duel, flashbacks/serenade/ending. **Nov 28: feature freeze, full play-through possible.** |
| Nov 29–Dec 5 | Testing and bug fixes, exception checks, art polish, README, UML regen, screenshots, slides. Claude rubric review on Dec 2. **Dec 5: code freeze.** |
| Dec 6 | Rehearse the 10-minute presentation; each member rehearses Q&A on their own classes. |
| Dec 7–13 | Present (exact date TBD). |

---

## 4. Integration and git workflow

`AGENTS.md` (merged in PR #1) applies. In short:

- **Never push to `main`.** One branch per task: `jairus/tilemap`, `johndel/player-movement`, `phebe/hud`.
- **Commit as yourself** (your own git name and email). One logical change per commit, imperative message. AI help is allowed, but the commit is yours and you must be able to explain it.
- **Review rotation:** Jai's PRs → Johndel reviews; Johndel's → Phebe; Phebe's → Jai. The reviewer runs `gradlew test` and the game before approving.
- `./gradlew test` must pass before every PR.
- Do not edit another owner's class. Suggest the change in a PR comment, or ask in the group chat.
- Hotspot files (`ForestGame` stage list, `AGENTS.md`, `README.md`): Jai merges these; others send their changes via PR.
- Each PR description: what changed, the OOP concept, one likely instructor question with its answer.

---

## 5. Progress

Update by ticking boxes in a PR (or at each integration on Saturday).

### Reset

- [x] Jai approves option C, skeleton reset (2026-11-11)
- [ ] Tag `baseline-2026-10-23` pushed
- [ ] Skeleton-reset PR opened
- [ ] Skeleton-reset PR reviewed and merged
- [ ] Decision log entry written to the vault
- [ ] All three ran the project on Windows

### Jai

- [ ] `TileMap` (load, `isSolid`, render)
- [ ] `GameStage` frame loop, drain hooks, win/lose
- [ ] `StageTimer` (re-enable `StageTimerTest`)
- [ ] `Checkpoint` + retry
- [ ] `ForestGame`, `Main`, stage transitions with cutscenes
- [ ] `MainMenuScreen`, `GameOverScreen`
- [ ] `GubatStage`: clue search, `Tikbalang`, `GlowingBush`, `Interactable`, Weapon `cutSpeed`
- [ ] Agimat timer bonus (90 s / 180 s)
- [ ] Shared tileset; `gubat.tmx`
- [ ] README, UML regenerated

### Johndel

- [ ] `Player` movement and collision (re-enable `PlayerMovementTest`)
- [ ] `Player` stats, coins, `spendCoins` (re-enable `PlayerStatsTest`)
- [ ] `Inventory`
- [ ] `Item`, `Weapon`, `Consumable`, `Accessory`, `Potion`
- [ ] `Shop`, `InsufficientCoinsException`, shop screen
- [ ] `SimulaStage`: pattern game, `LolaPacita`, `simula.tmx`
- [ ] `PalengkeStage`: Wakwak chase, `DuwendeTonyo`, `Wakwak`, `palengke.tmx`
- [ ] Dayo sprite
- [ ] Test evidence and screenshots

### Phebe

- [ ] `AssetLoader` with fallback on missing files
- [ ] `Creature` base
- [ ] `Hud` (Courage, Sanity, coins, items), `MenuText`
- [ ] `CutScene` + intro
- [ ] Flashbacks 1–2, serenade, ending
- [ ] `TulayStage`: `Riddle`, `Nuno`, Malunggay Juice timer bonus, potions per correct riddle, `tulay.tmx`
- [ ] `MangkukulamStage`: `PotionDuel`, `PotionType`, `Mangkukulam`, `mangkukulam.tmx`
- [ ] Slides (template + own slides)

### Log

| Date | Entry |
|---|---|
| 2026-11-11 | Plan drafted. Jai chose option C (skeleton reset). Awaiting answers on Saturday hours, ownership and go-ahead for the tag and skeleton PR. |

---

## 6. Open questions for Jai

1. **Why reset?** If it is only to even out the remaining work, Option B is enough. If it is also so each member's commits and Q&A cover their own classes, Option C. Which one?
2. **Saturday hours and format.** Is 09:00–21:00 right? In person or online? Are Johndel and Phebe available the whole day?
3. **New ownership.** Is Johndel OK owning `Player`? Is Phebe OK coding two stages (Tulay, Mangkukulam) on top of cutscenes, instead of mainly art and docs as in the proposal?
4. **Proposal section VI** lists the old roles. Does the instructor need to know about the reassignment, or is the final README enough?
5. **Exact presentation date** in Dec 7–13?
6. **Story text** for the intro and Flashback 1: who writes it, and by when?
7. May Claude push the tag and open the skeleton-reset PR on Thursday once you approve?

---

## 7. Decision log draft

For the vault (`Efforts/On/OOP 101 - Curse of the Forest.md`), to be written only after Jai approves:

> **2026-11-1x · Skeleton reset and rebalanced ownership.** Baseline tagged `baseline-2026-10-23`. Infrastructure kept; every class returned to a UML stub and reassigned so each member owns one hierarchy and whole stages: Jai = engine (`ForestGame`, `GameStage`, `TileMap`, `StageTimer`, `Checkpoint`, screens) + Stage 3; Johndel = `Player`, `Inventory`, `Item` hierarchy, `Shop` + Stages 1–2; Phebe = `Creature` base, `AssetLoader`, `Hud`, `CutScene` + Stages 4–5. Reason: even workload and individual commit evidence. Lock-in day Nov 14; feature freeze Nov 28; code freeze Dec 5.
