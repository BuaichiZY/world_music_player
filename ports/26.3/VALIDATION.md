# Minecraft 26.3 移植验证

日期：2026-09-20。World Music 1.0.1，Minecraft 26.3，NeoForge 26.3.0.3-beta。

## 移植内容

- 独立工程位于 `world_music/ports/26.3`，原 26.1.2 工程保留。
- 使用本机全局 Gradle 9.6.1、Java 25、ModDevGradle 2.0.147。
- 游戏版本限定 `[26.3]`，NeoForge 依赖范围 `[26.3.0.3-beta,26.4)`。
- 适配 26.3 的 `Minecraft.gui` 界面管理入口：配置界面打开、当前界面及加载遮罩检查。
- 保留每个时段只随机播放一首、结束后静音、下一时段再播放的调度规则。
- 自定义资源包更新至 Minecraft 26.3 的 97.1 格式。

## 已通过

执行 `gradle build runClientGameplay -PverifyMusicPack=true --console=plain`，最终 `BUILD SUCCESSFUL`。

1. Java 25 编译、音乐逻辑回归检查、安装包与源码包生成。
2. 实际启动 Minecraft 26.3 / NeoForge 26.3.0.3-beta 客户端，加载模组与配置。
3. 12 个默认原版曲目引用可解析；空歌单与 10 首歌单有效；配置 UI 限制最多 10 首；非法 ID 被拒绝。
4. 启用 `WorldMusicPack-26.3.zip` 后，40 个自定义音乐事件全部加载成功。
5. 独立单人世界中，在 0、6000、12000、18000、24000 刻验证时段切换与实际音频通道，旧曲目正常停止。
6. 手动停止已选曲目不会在同一时段再播；短音频自然结束后没有第二首或原版音乐接续。随后修改歌单仍保持安静，到下一时段恢复播放。
7. 资源包 ZIP 完整性通过，40 个音频文件与音乐目录原文件逐字节一致，四组各 10 首，所有配置 ID 与流式声音条目一致。
8. 发布 JAR 版本约束正确，字节码为 Java 25（major 69），不包含测试模组或音频文件。

## 结果位置

- `run/gameplay/smoke-result.txt`：客户端加载与配置验证。
- `run/gameplay/gameplay-result.txt`：实际播放行为验证。
- `run/gameplay/logs/latest.log`：包含 `WORLD_MUSIC_PACK_PASS` 与各时段音频启动记录。
- `build/libs/`：可安装 JAR、源码 JAR 与 SHA-256 校验文件。
- `output/`：26.3 资源包、配套配置、曲目映射及校验文件。

复现资源包检查时，需将生成的 ZIP 放入 `run/gameplay/resourcepacks` 并在该测试实例启用；省略 `-PverifyMusicPack=true` 可仅运行不依赖用户音乐的基础游戏测试。

验证限于上述 NeoForge 构建与独立客户端；未逐个验证后续构建及其他同时修改音乐的模组。音频引擎状态已检查，未进行逐首人工听感评价。
