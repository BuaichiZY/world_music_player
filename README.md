# World Music Player

![World Music 图标](artwork/world_music-icon-64.png)

Minecraft NeoForge 客户端世界音乐模组：早晨、中午、晚上和午夜各有独立歌单，每组最多 10 首。**每个时段只随机播放一首，结束后保持安静，下一时段再选曲。**

## 支持版本

| Minecraft | NeoForge 最低版本 | 源码目录 | 资源包格式 |
| --- | --- | --- | --- |
| 26.1.2 | 26.1.2.94 | 仓库根目录 | 84–2147483647 |
| 26.2 | 26.2.0.67 | [port/26.2](port/26.2) | 84–2147483647 |
| 26.3 | 26.3.0.3-beta | [ports/26.3](ports/26.3) | 84–2147483647 |

模组版本为 **1.0.1**。三个工程独立构建，安装包不可跨 Minecraft 版本使用。最低版本已实际测试，后续 NeoForge 构建未逐个验证。

## 功能

- 四时段独立歌单，支持原版单曲和资源包中的自定义 Ogg Vorbis 音乐。
- 进入下一时段时，停止尚未播完的旧曲目并选择新曲目。
- 已播放后，修改歌单、重载资源或切换维度不会增加本时段播放次数。退出后重新进入作为新会话重新判断。
- 默认替换主世界音乐；可让其他维度跟随主世界时钟。
- 尚未播放时遇到空歌单或全部无效的歌单，可恢复原版音乐或保持安静。
- 音量服从游戏的主音量与音乐设置，不影响唱片机和其他音效。

| 时段 | 游戏刻数 | 游戏内时间 | 正常速度下现实持续时间 |
| --- | --- | --- | --- |
| 早晨 | 0–5999 | 06:00–12:00 | 5 分钟 |
| 中午 | 6000–11999 | 12:00–18:00 | 5 分钟 |
| 晚上 | 12000–17999 | 18:00–00:00 | 5 分钟 |
| 午夜 | 18000–23999 | 00:00–06:00 | 5 分钟 |

睡觉、时间指令和游戏时间流速会影响切换时机。

## 构建

需要 **Java 25 JDK** 与全局 **Gradle 9.6.1**。先将 `JAVA_HOME` 指向自己的 Java 25 安装目录，用 `gradle --version` 确认 JVM。工程不附带 Gradle Wrapper，不自动下载 JDK；首次构建需要联网获取依赖。

```powershell
# Minecraft 26.1.2
gradle build

# Minecraft 26.2
gradle -p port/26.2 build

# Minecraft 26.3
gradle -p ports/26.3 build
```

也可在相应工程目录运行 `powershell -ExecutionPolicy Bypass -File ./build-local.ps1`。

输出位于各工程的 `build/libs/`：`world_music-26.1.2-1.0.1.jar`、`world_music-26.2-1.0.1.jar` 或 `world_music-26.3-1.0.1.jar` 为安装包，带 `-sources` 的 JAR 为源码包。

## 安装和配置

将对应版本安装包放入客户端 `mods` 文件夹。服务器无需安装。通过模组列表 **World Music → 配置**，或世界内的 `/worldmusic config` 编辑歌单。

配置文件：游戏实例的 `config/world_music-client.toml`。未添加自定义音乐时默认引用游戏已有的 12 首原版音乐。

```toml
enabled = true
overworldOnly = true
emptyUsesVanilla = true
morning = ["world_music:vanilla.minecraft", "world_music:vanilla.sweden"]
noon = ["world_music:vanilla.living_mice"]
evening = ["world_music:vanilla.danny"]
midnight = ["world_music:vanilla.dry_hands"]
```

其他内置单曲见 [sounds.json](src/main/resources/assets/world_music/sounds.json)。`minecraft:music.game` 等原版声音事件自身可能包含多首随机曲目。

| 客户端指令 | 功能 |
| --- | --- |
| `/worldmusic` 或 `/worldmusic status` | 查看当前时段和播放状态 |
| `/worldmusic config` | 打开配置界面 |
| `/worldmusic next` | 停止并跳过本时段，等待下个时段 |
| `/worldmusic reload` | 重新检查歌单，不重置已使用的播放次数 |

无需服务器 OP 权限。旧版 `gapSeconds` 设置已不再使用。

## 自定义音乐

**仓库不包含用户音乐、Minecraft 音频或打包后的音乐 ZIP。** 在目标版本工程的 `music/morning`、`music/noon`、`music/evening`、`music/midnight` 文件夹中放入 Ogg Vorbis `.ogg`，每组最多 10 首，然后在该工程目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File ./tools/Build-MusicPack.ps1
```

输出位于 `output/`：

- `WorldMusicPack.zip`（通用版，声明兼容 Minecraft 26.1.2 及以上）：放入游戏 `resourcepacks` 并启用，无需解压。
- `world_music-client.toml`：关闭游戏后复制到 `config`。替换前备份已有配置。
- `track-mapping.txt`：原文件名与声音事件 ID 对照表。

支持中文及空格文件名，按文件名排序分配 `world_music:morning_1` 等 ID。MP3、WAV、Ogg Opus 需要先转换成 Ogg Vorbis，不能只修改扩展名。替换已安装资源包后按 F3+T 重载。

## 开发验证

- `gradle verifyMusicLogic`：时段边界、跨天、随机选择及每时段一次播放的回归检查。
- `gradle runClient`：开发客户端。
- `gradle runClientSmoke`：实际客户端加载、默认音频和配置验证，完成后自动退出。
- `gradle runClientGameplay`：自动新建独立测试世界，验证切歌、自然结束后静音、下一时段恢复。

26.3 使用相同任务，加上 `-p ports/26.3` 或在其目录执行。测试模组不会进入发布 JAR。记录见 [26.1.2 验证](VALIDATION.md) 和 [26.3 验证](ports/26.3/VALIDATION.md)。其他接管音乐的模组需在目标整合包额外验证。

26.2 工程使用 `-p port/26.2`，验证记录见 [26.2 验证](port/26.2/VALIDATION.md)。所有工程的打包工具统一声明最低资源格式 84、最高 2147483647；该声明不保证未来格式变更后仍兼容，也不改变模组 JAR 的版本要求。

## 许可证

源码采用 [MIT License](LICENSE)。自行添加的第三方音频不属于本仓库分发内容。
