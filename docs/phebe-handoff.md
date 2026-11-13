# Phebe implementation and integration handoff

Implemented against the local baseline on 2026-11-11, on branch `phebe`.
The user's new Reset Plan ownership supersedes the older table in AGENTS.md for this work.
Phebe requested local commits, a push to `phebe`, and a reviewed pull request into `main`.
On 2026-11-13, main was merged into this branch without rewriting either member's commits.
No skeleton reset or direct changes to main were made.

## Implemented

- AssetLoader: validates paths, caches missing-image fallbacks, logs load failures once per path, disposes resources once.
- Creature: abstract interaction contract, proximity check, shared sprite-sheet animation.
- Nuno: retains Johndel's questions and one-time potion rewards, with greeting and progress accessors for the stage UI; Mangkukulam creates exactly one duel per encounter.
- Hud: Courage/Sanity text meters (red at 25 or below), coins, countdown, bounded item-name list.
- MenuText: shared screen projection and centered text that preserves the font's previous scale.
- CutScene: copied dialogue arrays, Enter/Space advancement, completion state, owned rendering resources and borrowed background textures.
- Draft intro, both flashbacks, serenade and ending text. Flashbacks play in their respective stages; serenade/ending play after winning the duel and reaching the exit.
- TulayStage: greet Nuno with E while nearby, answer with 1/2/3, earn one potion per correct answer, lose 10 Sanity on a wrong answer. All three correct answers and the exit are required.
- Tulay timer: 180 seconds, or 300 while carrying Malunggay Juice. Uses Johndel's Inventory.has(String).
- PotionDuel/PotionType: STUN beats DAMAGE; DAMAGE beats HEAL; HEAL beats STUN. The enemy telegraphs its next choice.
- MangkukulamStage: duel, result handling, flashback, serenade, ending, retry-compatible inventory use.
- Two playable 32-pixel Tiled maps with supplied background images, collision floor, coin and spawn/exit markers.
- Ten new unit tests, plus runtime smoke testing and captured screens.

## Team decisions to review

The log does not specify riddle wording, exact duel damage/health rules, or final dialogue.
These are explicit draft defaults, not claims about the approved story:

- Johndel's three food riddles are preserved: Sampalok, Atis, Kalabasa. Keys remain 1 Atis, 2 Sampalok, 3 Kalabasa; correct sequence is 2, 1, 3. Answers ignore case and surrounding whitespace.
- Rewards follow enum order: STUN, DAMAGE, HEAL.
- Dayo health = ceil((Courage + Sanity) / 50), minimum 1. Enemy health = 3.
- Winning a round deals 2 damage to the loser. Ties spend the potion without damage.
- At exhausted supplies, higher health wins; equal health loses. Entering with zero potions always loses.
- Enemy sequence is DAMAGE, HEAL, STUN and is shown before selection, making the counter choice learnable.
- Juice grants its time bonus by possession, without consumption.
- The maps use simple continuous collision floors, not collision inferred from every decorative pixel.
- Intro and Flashback 1 were unfinished in the plan. All dialogue is editable draft text in CutScene.

## Remaining integration owned by Jai

ForestGame's menu/newGame transition still starts Stage 1 directly. The intro is implemented but not connected there.
Keep player initialization in newGame and insert a CutScene before startStage(0), using:

```java
new CutScene(CutScene.intro(),
    getAssetLoader().loadImage("images/backgrounds/tulay.png"));
```

ForestGame should render that scene, check isFinished(), dispose the scene, then startStage(0) exactly once.
If the reset changes the GameStage API, preserve init(), update(float), handleInput(), begin(), retry(), reachedExit(),
protected player/game/timer, and the completion/failure flags or adapt these two subclasses in review.
Do not add another ending transition in ForestGame: Stage 5 already plays the serenade and ending before its completion callback.

Main now includes Johndel's Stage 1 pattern/rewards and Stage 2 chase/shop. Stage 3 remains a placeholder.
This is Phebe's implementation, not a claim that the whole campaign is finished.

## README/AGENTS changes proposed for Jai's integration PR

- Replace the old ownership tables with section 2 of the new Reset Plan.
- Describe Stages 4–5 as implemented; leave the other stage status accurate.
- Extend the existing map contract description: optional Tiled image layers are decorative; solid remains the collision source even when invisible.
- Add controls: E interact, 1/2/3 offering or potion, Enter/Space advance cutscenes.
- Include the screenshots and the testing results below.
- Acknowledge Codex/OpenAI help on this implementation alongside the existing Claude acknowledgement.
- Regenerate UML after the team agrees on added helper methods.
- Slides/template are a separate deliverable and have not been created by this coding task.

## Asset provenance

Backgrounds downloaded from the user's shared maps folder:
https://drive.google.com/drive/folders/10KASbSVwt08EQVJSqy1XAvYAjBGZe09f

- 04-tulay-ng-nuno.png -> src/assets/images/backgrounds/tulay.png
- 05-mangkukulam.png -> src/assets/images/backgrounds/mangkukulam.png
- 06-luningning-house.png -> src/assets/images/backgrounds/ending.png

Characters: the user already supplied local copies under ../character asset/.
Copied nuno and mangkukulam unchanged into src/assets/images/characters/ with .png extensions.
Both are 320x64 strips with five 64x64 frames.
The provided Drive parent links to the same named character collection:
https://drive.google.com/drive/folders/1UCXvQO9lO_AbGIaHyHxg0ntFAbaF63D9

Existing untracked forest-cast/movement assets were left untouched.
Do not add those unrelated files accidentally when staging this work.

## Validation

2026-11-13 after integrating main at 5c185a4:
- Fresh Java 21 build: PASS, 20 tests, 0 failures; desktop jar built successfully.
- Used ../.tools/verify-merge.gradle to direct build output to a fresh directory because the existing OneDrive build folder could not be cleaned.
- LWJGL3 smoke test: PASS, including Stage 1/2 screen-space overlays after camera movement, Johndel's riddle sequence, single potion rewards, retry, duel and ending.
- Updated screenshots/phebe-tulay.png and screenshots/phebe-duel.png; added screenshots/phebe-merged-simula.png for overlay compatibility.

2026-11-11, Windows, Temurin JDK 21.0.13:
- gradlew.bat test lwjgl3:jar --no-daemon: PASS, 19 tests, 0 failures.
- Invisible LWJGL3 desktop smoke test: PASS. Actual maps/images/fonts loaded and rendered.
- Smoke scenarios: missing-image cache; valid image after failure; wrong riddle penalty; three rewards;
  retry restores Sanity and removes rewards; two winning duel rounds consume two potions;
  serenade and ending finish before completion callback.
- Screenshots: screenshots/phebe-tulay.png and screenshots/phebe-duel.png.

The local default Java was version 26; a workspace-local JDK 21 and Gradle cache were downloaded into ../.tools/.
Normal project requirement remains JDK 21. No global Java setting was changed.

Run from src/ with JAVA_HOME set to your JDK 21:
```powershell
.\gradlew.bat test
.\gradlew.bat lwjgl3:run
```

Manual play:
1. Complete Stage 1's pattern, Stage 2's chase/shop, and cross Stage 3 to reach Tulay.
2. Advance the flashback with Enter. Walk near Nuno; E starts the encounter.
3. Choose offerings with 1/2/3. Try one incorrect answer, then solve all three.
4. Cross to the right exit, advance Flashback 2, approach the witch and press E.
5. Counter the displayed enemy potion using 1/2/3. Walk to the exit after winning.
6. Advance the serenade and ending. Also verify a loss and R retry.
7. Check the 300-second timer with Malunggay Juice bought in Stage 2.

## Main integration resolution (2026-11-13)

- Nuno keeps the main-branch riddle/reward model; Tulay delegates to offer() instead of awarding duplicate potions.
- Riddle keeps constructor validation and whitespace/case-insensitive answers.
- Tulay keeps Phebe's real map, animated sprite, flashback, nearby interaction, wrong-answer penalty, and exit goal.
- Hud leaves the batch in screen space as Johndel's earlier stages expect. Its beginWorld() method restores the saved camera for Phebe's creature sprites.
- Inventory text moves to the bottom to avoid Stage 1's dialogue bubble.
- Original Phebe commits and all incoming main commits remain in history; use a merge commit when reviewing the PR to retain them.

## PR description / OOP defense

Problem: Phebe's newly assigned classes were stubs or minimal baseline implementations.
Result: Stage 4 now produces potions through riddles, Stage 5 consumes them in a playable duel, and UI/story/asset support is implemented.

| Change | Concept and purpose | Likely instructor question and answer |
| --- | --- | --- |
| Creature, Nuno, Mangkukulam | Abstraction, inheritance, polymorphism: common rendering/proximity with different interact(Player) behavior | Why abstract? A generic creature has no single meaningful interaction; subclasses supply it. |
| TulayStage, MangkukulamStage | Inheritance: reuse GameStage physics, coins, checkpoint and failure handling | Why override update()? Each stage has different goals while sharing the frame loop. |
| Riddle | Encapsulation: private immutable question and answer | Why normalize input? Case and surrounding spaces should not make a correct food answer fail. |
| PotionType, PotionDuel | Enum prevents arbitrary potion types; private state validates each round | Why remove the potion from inventory too? The duel and later gameplay must agree that it was consumed. |
| CutScene | Arrays and encapsulation: copy dialogue and expose controlled advancement | Why clone the array? Callers cannot change the active story through their original array reference. |
| AssetLoader | Exception handling and shared resource ownership | Why fallback? Missing art should be visible and logged without preventing play. |
| Hud, MenuText | Encapsulation and reusable presentation | Why screen coordinates? Stats should remain fixed while the world camera follows Dayo. |
| Maps and image strips | Data-driven level layout and shared rendering | Why hide solid? Collision still reads tile occupancy while background art supplies the visible floor. |

Before submitting, Phebe should review each change, run the game, practice these answers, and acknowledge AI assistance.
Review the local commits as Phebe, then push and request Jai's review.
