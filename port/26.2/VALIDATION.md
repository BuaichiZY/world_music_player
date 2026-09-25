# Minecraft 26.2 验证记录

环境：2026-09-25，本机全局 Gradle 9.6.1，Eclipse Adoptium Java 25，NeoForge 26.2.0.67。

## 构建与依赖

- `gradle build compileSmokeJava --console=plain --info` 构建成功。
- 实际编译、运行基线为 NeoForge 26.2.0.67。
- 发布 JAR 内声明 NeoForge `[26.2.0.67,)`，Minecraft `[26.2]`。
- MODID 为 `world_music`，模组版本为 1.0.1。
- 发布 JAR 不包含开发测试模组和用户音乐。
- 沿用 26.3 工程的音乐逻辑；26.2 接口编译兼容，无须改变播放行为。

## 逻辑检查

`verifyMusicLogic` 已通过：时段边界、随机选曲、最多十首、每时段仅一次播放及跨日重置。

## 客户端与资源包

执行 `gradle runClientGameplay -PverifyMusicPack=true --console=plain`，使用独立测试存档。
结果保存在 `run/gameplay/smoke-result.txt`、`run/gameplay/gameplay-result.txt`，日志为 `build/gameplay.log`。

客户端初始加载检查已通过：12 个默认声音、空歌单、十首歌单、界面数量限制和无效 ID 校验。
日志确认通用资源包内全部 40 个自定义音乐事件加载成功。
实际世界播放检查已通过，任务以 `BUILD SUCCESSFUL` 结束并自动关闭客户端：四个时段与跨日播放、切换时段停止旧曲、曲目停止后不重播、两首短曲只随机播放一首、自然结束后保持安静、修改歌单不补发播放次数、进入下一时段恢复播放。

通用打包工具输出 `WorldMusicPack.zip`，资源声明 `min_format = 84`、`max_format = 2147483647`。
ZIP 内实际包含 40 个 Ogg 文件；已检查生成的元数据。
此范围声明兼容 Minecraft 26.1.2 及以上格式，不改变模组自身的游戏版本要求；未来格式变化仍可能需要适配。

后续 NeoForge 版本允许加载，但未逐个实测。
