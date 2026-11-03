# Horror Mod (Fabric 1.20.1)

## Build
Install JDK 17 + Gradle 8.5 (or open the folder in IntelliJ IDEA), then:
    gradle build
The jar appears in build/libs/horrormod-1.0.0.jar -> put it in .minecraft/mods with Fabric API 0.92.2+1.20.1.

## Features
- The Stalker: tall dark mob. Freezes while anyone looks at it, sprints when you look away, its hit gives Darkness.
- Random scares in dark places / at night (every 30-120 s): footsteps behind you, whispers, doors, heartbeat + darkness, stalker spawn.
- Stalker Spawn Egg (Spawn Eggs tab).
- Test commands (OP): /horror scare footsteps|whisper|door|darkness|stalker
