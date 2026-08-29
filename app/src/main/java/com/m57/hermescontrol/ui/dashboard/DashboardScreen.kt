package com.m57.hermescontrol.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m57.hermescontrol.NavigationController
import com.m57.hermescontrol.data.model.DashboardEmployee
import com.m57.hermescontrol.data.model.DashboardResponse
import com.m57.hermescontrol.theme.DashboardBlue
import com.m57.hermescontrol.theme.DashboardBorder
import com.m57.hermescontrol.theme.DashboardInkPrimary
import com.m57.hermescontrol.theme.DashboardInkSecondary
import com.m57.hermescontrol.theme.DashboardInkWeak
import com.m57.hermescontrol.theme.DashboardLightGreen
import com.m57.hermescontrol.theme.DashboardLightGreenBorder
import com.m57.hermescontrol.theme.DashboardLightRed
import com.m57.hermescontrol.theme.DashboardLightRedBorder
import com.m57.hermescontrol.theme.DashboardWhite
import com.m57.hermescontrol.theme.StatusGreen
import com.m57.hermescontrol.theme.StatusGrey
import com.m57.hermescontrol.theme.StatusRed
import com.m57.hermescontrol.theme.StatusYellow
import com.m57.hermescontrol.ui.common.HermesScaffold
import com.m57.hermescontrol.ui.common.NavIcon

// 状态 → 颜色
private fun statusColor(status: String): Color =
    when (status) {
        "blocked" -> StatusRed
        "busy" -> StatusYellow
        "idle" -> StatusGreen
        else -> StatusGrey
    }

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: DashboardViewModel = viewModel { DashboardViewModel() },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HermesScaffold(
        modifier = modifier,
        title = { Text("驾驶舱", fontWeight = FontWeight.SemiBold) },
        navigationIcon = NavIcon.Menu { onOpenDrawer?.invoke() },
        onRefresh = { viewModel.load() },
        isRefreshing = state.isRefreshing,
        drawerGesturesEnabled = true,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.dashboard == null -> {
                    // 首屏加载
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.errorMessage != null && state.dashboard == null -> {
                    ErrorContent(state.errorMessage.orEmpty(), onRetry = { viewModel.load(initial = true) })
                }
                else -> {
                    val dashboard = state.dashboard
                    if (dashboard == null) {
                        ErrorContent("暂无数据", onRetry = { viewModel.load(initial = true) })
                    } else {
                        DashboardContent(
                            dashboard = dashboard,
                            onTaskClick = { taskId ->
                                NavigationController.navigateTo(com.m57.hermescontrol.TaskDetailKey(taskId))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, color = DashboardInkSecondary)
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = "重试", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("重试", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: DashboardResponse,
    onTaskClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ① KPI 4 格
        item {
            KpiGrid(dashboard)
        }

        // ② 需要拍板
        item {
            ApprovalSection(dashboard = dashboard, onTaskClick = onTaskClick)
        }

        // ③ 员工看板
        item {
            Text(
                "员工",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DashboardInkSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
        }
        items(dashboard.employees, key = { it.name }) { emp ->
            EmployeeRow(emp)
        }
    }
}

// ── ① KPI 4 格 ───────────────────────────────────────────────
@Composable
private fun KpiGrid(dashboard: DashboardResponse) {
    val kpi = dashboard.kpi
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 待拍板（浅红强调，首格占满前列）
            KpiCell(
                label = "待拍板",
                value = kpi.waiting_approval,
                icon = Icons.Filled.ErrorOutline,
                iconColor = StatusRed,
                valueColor = StatusRed,
                background = DashboardLightRed,
                modifier = Modifier.weight(1f),
            )
            KpiCell(
                label = "进行中",
                value = kpi.in_progress,
                icon = Icons.Filled.PlayCircle,
                iconColor = StatusYellow,
                valueColor = DashboardInkPrimary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCell(
                label = "今日完成",
                value = kpi.done_today,
                icon = Icons.Filled.CheckCircle,
                iconColor = StatusGreen,
                valueColor = DashboardInkPrimary,
                modifier = Modifier.weight(1f),
            )
            KpiCell(
                label = "员工在线",
                value = kpi.employees_online,
                icon = Icons.Filled.PeopleAlt,
                iconColor = DashboardBlue,
                valueColor = DashboardInkPrimary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KpiCell(
    label: String,
    value: Int,
    icon: ImageVector,
    iconColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    background: Color = DashboardWhite,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, DashboardBorder),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text("$value", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, fontSize = 12.sp, color = DashboardInkSecondary)
        }
    }
}

// ── ② 需要拍板 ────────────────────────────────────────────────
@Composable
private fun ApprovalSection(
    dashboard: DashboardResponse,
    onTaskClick: (String) -> Unit,
) {
    val tasks = dashboard.waitingApprovalTasks
    if (tasks.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = DashboardLightGreen),
            border = BorderStroke(1.dp, DashboardLightGreenBorder),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("都已拍板", color = StatusGreen, fontWeight = FontWeight.Medium)
            }
        }
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardLightRed),
        border = BorderStroke(1.dp, DashboardLightRedBorder),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "需要拍板 · ${tasks.size} 件",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusRed,
            )
            tasks.forEach { t ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onTaskClick(t.id) }
                            .padding(vertical = 4.dp),
                ) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = StatusRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, fontSize = 13.sp, color = DashboardInkPrimary, maxLines = 1)
                        Text(
                            buildString {
                                append(t.assignee ?: "未指派")
                                if (t.kindLabel != null) append(" · ${t.kindLabel}")
                            },
                            fontSize = 11.sp,
                            color = DashboardInkSecondary,
                        )
                    }
                }
            }
        }
    }
}

// ── ③ 员工看板 ────────────────────────────────────────────────
@Composable
private fun EmployeeRow(emp: DashboardEmployee) {
    val color = statusColor(emp.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardWhite),
        border = BorderStroke(1.dp, DashboardBorder),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 头像：首字母 + 状态色块
            Box(
                modifier = Modifier.size(40.dp).background(color = color, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    emp.name.take(1).uppercase(),
                    color = DashboardWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        emp.title?.takeIf { it.isNotBlank() } ?: emp.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DashboardInkPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(8.dp).background(color = color, shape = CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(emp.statusLabel, fontSize = 12.sp, color = color)
                }
                if (!emp.signature.isNullOrBlank()) {
                    Text(emp.signature, fontSize = 12.sp, color = DashboardInkSecondary, maxLines = 1)
                }
            }
            // 数据看板：本周任务 / 完成 / 卡住
            val stats = emp.stats
            if (stats != null) {
                Column(horizontalAlignment = Alignment.End) {
                    StatItem("本周", stats.weekTasks)
                    StatItem("完成", stats.weekDone)
                }
            } else {
                Text("—", color = DashboardInkWeak, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Int,
) {
    Text(
        "$label $value",
        fontSize = 11.sp,
        color = DashboardInkWeak,
    )
}
