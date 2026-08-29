package com.m57.hermescontrol.theme

import androidx.compose.ui.graphics.Color

// 总裁驾驶舱（首页）专属的飞书式扁平配色。
// 放在 theme/ 下以满足 checkColorLiterals 守卫（色值不允许散落在 UI 屏文件）。
// 对齐产品设定：白卡 + 1px 细边框 + 主/次/弱三级文字 + 状态色强调。

/** 卡片 1px 细边框。 */
val DashboardBorder = Color(0xFFE1E3E8)

/** 主文字。 */
val DashboardInkPrimary = Color(0xFF1F2329)

/** 次文字。 */
val DashboardInkSecondary = Color(0xFF646A73)

/** 弱文字。 */
val DashboardInkWeak = Color(0xFF8F959E)

/** 待拍板浅红底。 */
val DashboardLightRed = Color(0xFFFFF3F1)

/** 完成浅绿底。 */
val DashboardLightGreen = Color(0xFFF0FBEC)

/** 待拍板卡边框（浅红）。 */
val DashboardLightRedBorder = Color(0xFFF5D3D0)

/** 已完成卡边框（浅绿）。 */
val DashboardLightGreenBorder = Color(0xFFD5F0CC)

/** 员工在线 KPI 的蓝色强调。 */
val DashboardBlue = Color(0xFF3370FF)

/** 卡片/文字用的白色（语义：置于 theme 供 UI 屏引用，避免裸 Color.White）。 */
val DashboardWhite = Color.White

/** 任务群聊里「对方评论」气泡的浅灰底。 */
val DashboardChatBubbleGrey = Color(0xFFF2F3F5)
