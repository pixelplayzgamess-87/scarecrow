# Scarecrow (Forge 1.20.1 + GeckoLib)

## What's included

- Full Java source for a GeckoLib-animated hostile entity (`com.CodDev.scarecrow`), mod id `scarecrow`.
- Your uploaded `scarecrow_geo.json`, `scarecrow_animation.json`, and `chase.ogg`, wired into the correct resource paths.
- A generated placeholder texture at `src/main/resources/assets/scarecrow/textures/entity/scarecrow.png` (128x128, burlap-colored). **You did not upload a texture**, so replace this file with your real one before playing — keep the same path and it will just work.
- Commands, sounds, and block-breaking AI as described below.

## Commands

All require operator permission (level 2), like `/summon`.

- `/scarecrow pose1 [x y z]` — spawns a motionless scarecrow holding the `pose_1` pose. Plays an ambient noise on spawn. Disappears (teleport sound + smoke) the instant a player looks directly at it.
- `/scarecrow pose2 [x y z]` — same, using the `pose_2` pose.
- `/scarecrow pose3 [x y z]` — same, using the `Pose_3` pose.
- `/scarecrow chase [x y z]` — spawns a dormant scarecrow playing `pose_1_idle`. It stays still until a player looks directly at it, then it screeches (Enderman-stare sound), starts the chase music (your `chase.ogg`, looping, follows the entity), and sprints at whoever looked at it using the `run`/`walk` animations.
- `/scarecrow instant [x y z]` — spawns already hunting the nearest player, no look-trigger needed. Same chase music/behavior as above.

Any scarecrow in a hunting state (`chase` after being triggered, or `instant`) will break blocks in its path if it's been stuck for more than ~0.6s, respects the `mobGriefing` game rule, and won't touch blocks that are unbreakable or have a block entity (chests, furnaces, etc).

## Sounds used

No royalty-free scarecrow-specific SFX could be sourced offline, so this uses vanilla sound events chosen to fit the theme (all real Minecraft sounds, no extra files needed):

- Ambient/idle: `entity.vex.ambient` (faint, eerie)
- "Noticed you" screech: `entity.enderman.stare`
- Disappear: `entity.enderman.teleport`
- Chase music: your `chase.ogg`, registered as `scarecrow:chase`, streamed and looped, following the entity, stopped automatically when it dies or despawns.

Swap any of these in `ScarecrowEntity.java` (`SoundEvents.XXX`) for your own custom sounds later — just drop the `.ogg` into `assets/scarecrow/sounds/` and add an entry to `sounds.json`.

## Building — please read

This project **could not be compiled in the sandbox that generated it** — the build requires network access to Minecraft Forge's and GeckoLib's Maven repositories, which the sandbox does not have. So instead of a broken half-built jar, you're getting the complete, ready-to-compile source tree. On your own machine, with normal internet access, this is a completely standard Forge 1.20.1 MDK project:

1. Make sure you have **JDK 17** installed.
2. This zip does **not** include `gradle/wrapper/gradle-wrapper.jar` (a binary file the sandbox couldn't fetch). Either:
   - Open the project folder directly in **IntelliJ IDEA** with the Minecraft Development plugin, or **Eclipse** — both will offer to import/sync the Gradle project and fetch everything automatically, no jar needed, **or**
   - If you have Gradle installed locally, run `gradle wrapper --gradle-version 7.6.1` once inside the project folder to generate the missing wrapper jar, then use `./gradlew` from then on.
3. Run `./gradlew build` (or `gradlew.bat build` on Windows). The first run downloads Forge's MCP mappings and GeckoLib and will take a few minutes.
4. The compiled mod jar will be in `build/libs/scarecrow-1.0.0.jar`.
5. To test in-game directly: `./gradlew runClient`.

Forge version pinned: `1.20.1-47.2.0`. GeckoLib version pinned: `4.4.9` (from the official GeckoLib Cloudsmith maven, already configured in `build.gradle`). Both are well-tested, widely used versions for 1.20.1 — bump them in `gradle.properties`/`build.gradle` if you'd like something newer.

## Notes / things you may want to tune

- Look-detection angle/range (how precisely a player has to stare, and from how far) is in `ScarecrowEntity#getDirectWatcher`.
- Attributes (health, speed, damage, follow range) are in `ScarecrowEntity#createAttributes`.
- Block-breaking speed/threshold is in `ScarecrowBreakBlockGoal`.
