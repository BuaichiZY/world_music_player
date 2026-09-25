# World Music — Minecraft 26.2

本目录为 **Minecraft 26.2 / NeoForge 26.2.0.67+** 独立移植工程，MODID 为 `world_music`，模组版本为 1.0.1。
播放规则、配置及许可证说明见 [仓库首页](../../README.md)。

使用本机 Java 25 和全局 Gradle；先设置 `JAVA_HOME`，在本目录执行 `gradle build`，或在仓库根目录执行 `gradle -p port/26.2 build`。

- 安装包：`build/libs/world_music-26.2-1.0.1.jar`
- 源码包：`build/libs/world_music-26.2-1.0.1-sources.jar`
- 打包音乐：`powershell -ExecutionPolicy Bypass -File ./tools/Build-MusicPack.ps1`
- 通用资源包：`output/WorldMusicPack.zip`，声明资源格式 84–2147483647，即 Minecraft 26.1.2 及以上。

音乐不随源码分发，将自己的 Ogg Vorbis 音乐放入 `music` 下四个时段目录即可，每组最多十首。
兼容声明不保证未来资源格式变更后仍可用；模组本身限定 Minecraft 26.2。

实际客户端与播放验证见 [VALIDATION.md](VALIDATION.md)。
