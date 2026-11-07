# Pinbar

把坐标「钉」在原版定位栏（Locator Bar）上的 Fabric 客户端模组。

- **Minecraft**：26.3
- **加载器**：Fabric Loader 0.20.1+
- **依赖**：Fabric API 0.165.x+26.3
- **Java**：25
- **版本**：0.1.0

## 功能

```text
/pin <x> <y> <z>
/pin overworld|nether|end <x> <y> <z>
```

示例：

```text
/pin 120 ~ -340
/pin nether 120 ~ -340
```

- 坐标支持绝对值和 `~` 相对值（如 `~`、`~5`、`~-3.5`）。
- `y` 轴只用于记录坐标，不影响定位栏的指向；实际显示高度跟随玩家视平线，避免出现上下箭头。
- 只保留**一个**坐标标记；再次执行 `/pin` 会覆盖旧标记。
- 当玩家进入以 `(x, z)` 为中心的 **3×3 方块区域**时，标记自动取消，并在动作栏提示。
- **不持久化**：退出游戏/客户端后标记消失。
- **跨维度不显示**：标记只在其所属维度显示；进入其他维度时隐藏，回到该维度后重新显示。
- 纯客户端：服务器不需要安装，其他玩家也看不到你的标记。

## 安装

1. 安装 Fabric Loader 0.20.1+ 与 Fabric API 0.165.x+26.3。
2. 把 `build/libs/pinbar-0.1.0.jar` 放进 `.minecraft/mods/`。
3. 启动 26.3 的 Fabric 配置。

## 构建

需要 JDK 25：

```bash
./gradlew build
```

产物：`build/libs/pinbar-0.1.0.jar`。

> 本项目使用 Fabric Loom 的 `splitEnvironmentSourceSets()`，客户端代码在 `src/client/java`。

## 实现要点

- `/pin` 通过 Fabric API 的**客户端指令 API**注册，不会发送到服务器。
- 标记本身是一个本地 `TrackedWaypoint`，直接放进 `ClientWaypointManager`，因此复用原版定位栏的渲染、距离缩放和方向计算。
- 自定义图标通过资源包机制提供：
  - `assets/pinbar/waypoint_style/pin.json`
  - `assets/pinbar/textures/gui/sprites/hud/locator_bar_dot/pin.png`
- 定位栏的显示优先级由原版决定：经验条/跳跃载具信息可能短暂优先于定位栏。

## 已知限制

- 定位栏只在有 waypoint 时显示；原版经验条在特定时刻会优先显示。
- 标记只对当前客户端可见，不能分享给其他玩家。
- 不提供配置文件；如果需要持久化或多标记，需要额外实现。
