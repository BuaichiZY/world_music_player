# World Music Player

![World Music icon](artwork/world_music-icon-64.png)

A client-side world music mod for Minecraft NeoForge, with separate playlists for morning, noon, evening, and midnight, each supporting up to 10 tracks. **Only one randomly selected track plays during each time period. Once it finishes, the music stays silent until the next period.**

## Supported Versions

| Minecraft | Minimum NeoForge Version | Source Directory | Resource Pack Format |
| --- | --- | --- | --- |
| 26.1.2 | 26.1.2.94 | Repository root | 84–2147483647 |
| 26.2 | 26.2.0.67 | [port/26.2](port/26.2) | 84–2147483647 |
| 26.3 | 26.3.0.3-beta | [ports/26.3](ports/26.3) | 84–2147483647 |

The current mod version is **1.0.1**. The three projects build independently, and their mod JARs are not interchangeable between Minecraft versions. The minimum NeoForge versions have been tested; later builds have not been individually verified.

## Features

- Separate playlists for four time periods, supporting individual vanilla tracks and custom Ogg Vorbis music from resource packs.
- When a new period begins, any unfinished track from the previous period stops, and a new track is selected.
- Once a track has played, editing the playlist, reloading resources, or changing dimensions does not grant another playback during the same period. Leaving and rejoining starts a new session, and the current period is evaluated again.
- Replaces Overworld music by default, with an option to make other dimensions follow the Overworld clock.
- If no track has played yet and the playlist is empty or contains no valid tracks, you can choose to use vanilla music or remain silent.
- Respects the game's master and music volume settings without affecting jukeboxes or other sound effects.

| Period | Game Ticks | In-Game Time | Real-Time Duration at Normal Speed |
| --- | --- | --- | --- |
| Morning | 0–5999 | 06:00–12:00 | 5 minutes |
| Noon | 6000–11999 | 12:00–18:00 | 5 minutes |
| Evening | 12000–17999 | 18:00–00:00 | 5 minutes |
| Midnight | 18000–23999 | 00:00–06:00 | 5 minutes |

Sleeping, time commands, and changes to the game clock's rate affect when periods change.

## Building

Requires **Java 25 JDK** and a global installation of **Gradle 9.6.1**. Set `JAVA_HOME` to your Java 25 installation directory, then use `gradle --version` to verify the JVM. The project does not include a Gradle Wrapper or automatically download a JDK. The first build requires internet access to download dependencies.

```powershell
# Minecraft 26.1.2
gradle build

# Minecraft 26.2
gradle -p port/26.2 build

# Minecraft 26.3
gradle -p ports/26.3 build
```

Alternatively, run `powershell -ExecutionPolicy Bypass -File ./build-local.ps1` from the appropriate project directory.

Build outputs are placed in each project's `build/libs/` directory. Install `world_music-26.1.2-1.0.1.jar`, `world_music-26.2-1.0.1.jar`, or `world_music-26.3-1.0.1.jar`; JARs with the `-sources` suffix contain source code.

## Installation and Configuration

Place the mod JAR for your Minecraft version in the client's `mods` folder. No server installation is required. Edit playlists through **World Music → Config** in the mod list, or run `/worldmusic config` while in a world.

The configuration file is `config/world_music-client.toml` inside your game instance. Without custom music, the default playlists reference 12 vanilla tracks already included in the game.

```toml
enabled = true
overworldOnly = true
emptyUsesVanilla = true
morning = ["world_music:vanilla.minecraft", "world_music:vanilla.sweden"]
noon = ["world_music:vanilla.living_mice"]
evening = ["world_music:vanilla.danny"]
midnight = ["world_music:vanilla.dry_hands"]
```

See [sounds.json](src/main/resources/assets/world_music/sounds.json) for other built-in single-track references. Vanilla sound events such as `minecraft:music.game` may themselves contain multiple randomly selected tracks.

| Client Command | Function |
| --- | --- |
| `/worldmusic` or `/worldmusic status` | Show the current period and playback status |
| `/worldmusic config` | Open the configuration screen |
| `/worldmusic next` | Stop and skip music for the current period, then wait for the next period |
| `/worldmusic reload` | Recheck playlists without resetting the playback allowance already used |

Server operator permissions are not required. The old `gapSeconds` setting is no longer used.

## Custom Music

**This repository does not include user music, Minecraft audio files, or generated music ZIPs.** Place Ogg Vorbis `.ogg` files in the target version's `music/morning`, `music/noon`, `music/evening`, and `music/midnight` folders, with up to 10 tracks per folder. Then run the following from that project directory:

```powershell
powershell -ExecutionPolicy Bypass -File ./tools/Build-MusicPack.ps1
```

The following files are generated in `output/`:

- `WorldMusicPack.zip` (universal; declares compatibility with Minecraft 26.1.2 and later): place it in the game's `resourcepacks` folder and enable it. No extraction is needed.
- `world_music-client.toml`: close the game, then copy this file into `config`. Back up your existing configuration before replacing it.
- `track-mapping.txt`: maps original filenames to sound event IDs.

Filenames containing Chinese characters and spaces are supported. Tracks are sorted by filename and assigned IDs such as `world_music:morning_1`. MP3, WAV, and Ogg Opus files must first be converted to Ogg Vorbis; simply changing the extension will not work. Press F3+T to reload resources after replacing an installed resource pack.

## Development Checks

- `gradle verifyMusicLogic`: regression checks for period boundaries, day rollover, random selection, and one playback per period.
- `gradle runClient`: launch the development client.
- `gradle runClientSmoke`: launch an actual client to verify loading, default audio, and configuration, then exit automatically.
- `gradle runClientGameplay`: automatically create a separate test world to verify track switching, silence after a track ends naturally, and playback resuming in the next period.

The 26.3 project uses the same tasks. Add `-p ports/26.3` or run them from that directory. The test mod is excluded from release JARs. See the [26.1.2 validation record](VALIDATION.md) and [26.3 validation record](ports/26.3/VALIDATION.md). Compatibility with other mods that control music requires additional testing in your target modpack.

Use `-p port/26.2` for the 26.2 project; see [26.2 validation](port/26.2/VALIDATION.md). All pack builders declare resource formats 84 through 2147483647. This does not guarantee compatibility with future format changes or change each mod JAR's Minecraft version requirement.

## License

The source code is licensed under the [MIT License](LICENSE). Third-party audio added by users is not distributed as part of this repository.
