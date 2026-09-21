# World Music — Minecraft 26.3

本目录为 **Minecraft 26.3 / NeoForge 26.3.0.3-beta** 独立移植工程。播放规则、配置方式、自定义音乐和许可证说明见 [仓库首页](../../README.md)。

使用 Java 25 和全局 Gradle 9.6.1，在本目录执行 `gradle build`。请先将 `JAVA_HOME` 指向 Java 25 JDK。

- 安装包：`build/libs/world_music-26.3-1.0.1.jar`
- 源码包：`build/libs/world_music-26.3-1.0.1-sources.jar`
- 音乐打包：`powershell -ExecutionPolicy Bypass -File ./tools/Build-MusicPack.ps1`
- 资源包：`output/WorldMusicPack-26.3.zip`，格式 97.1。
- 默认配置：`config/world_music-client.toml`，与 26.1.2 版使用相同曲目 ID。

实际运行验证见 [VALIDATION.md](VALIDATION.md)。音乐不随源码分发，需自行放入本目录下的四个 `music` 子目录。
