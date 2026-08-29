package com.m57.hermescontrol.ui.employeecard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m57.hermescontrol.data.model.EmployeeCard
import com.m57.hermescontrol.theme.DashboardInkPrimary
import com.m57.hermescontrol.theme.DashboardInkSecondary
import com.m57.hermescontrol.theme.DashboardInkWeak
import com.m57.hermescontrol.theme.DashboardWhite
import com.m57.hermescontrol.theme.StatusGreen
import com.m57.hermescontrol.theme.StatusGrey
import com.m57.hermescontrol.theme.StatusRed
import com.m57.hermescontrol.theme.StatusYellow
import com.m57.hermescontrol.ui.common.HermesScaffold
import com.m57.hermescontrol.ui.common.NavIcon

private fun statusColor(status: String): Color =
    when (status) {
        "blocked" -> StatusRed
        "busy" -> StatusYellow
        "idle" -> StatusGreen
        else -> StatusGrey
    }

@Composable
fun EmployeeCardScreen(
    name: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: EmployeeCardViewModel = viewModel { EmployeeCardViewModel(name) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HermesScaffold(
        modifier = modifier,
        title = { Text("员工名片", fontWeight = FontWeight.SemiBold) },
        navigationIcon = NavIcon.Back(onBack),
        drawerGesturesEnabled = false,
        onRefresh = { viewModel.load() },
        isRefreshing = state.isRefreshing,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.card == null -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.errorMessage != null && state.card == null -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(state.errorMessage.orEmpty(), color = DashboardInkSecondary)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.load(initial = true) }) { Text("重试") }
                    }
                }
                else -> {
                    state.card?.let { card -> CardContent(card) }
                }
            }
        }
    }
}

@Composable
private fun CardContent(card: EmployeeCard) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HeaderCard(card) }
        item { StatsRow(card) }
        if (card.skills.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "技能",
                        fontSize = 12.sp,
                        color = DashboardInkSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    card.skills.forEach { skill ->
                        Box(
                            Modifier
                                .background(statusColor(card.status).copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(skill, fontSize = 12.sp, color = statusColor(card.status))
                        }
                    }
                }
            }
        }
        if (card.blockedTasks.isNotEmpty() || card.activeTasks.isNotEmpty()) {
            item { Text("他的任务", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DashboardInkPrimary) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    card.blockedTasks.forEach { TaskRow(it.title, it.kindLabel, isBlocked = true) }
                    card.activeTasks.forEach { TaskRow(it.title, it.status, isBlocked = false) }
                }
            }
        }
        if (card.blockedTasks.isEmpty() && card.activeTasks.isEmpty()) {
            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun HeaderCard(card: EmployeeCard) {
    val color = statusColor(card.status)
    val avatarBg =
        card.color.takeIf {
            it.startsWith("#")
        }?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: color
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardWhite),
        border = BorderStroke(1.dp, Color(0xFFE1E3E8)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 大头像（首字母 + 状态色块）
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .background(avatarBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        card.name.take(1).uppercase(),
                        color = DashboardWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        card.title.ifBlank { card.name },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DashboardInkPrimary,
                    )
                    if (card.role.isNotBlank() && card.role != card.title) {
                        Text(card.role, fontSize = 13.sp, color = DashboardInkSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Box(
                            Modifier.size(
                                8.dp,
                            ).background(statusColor(card.status), androidx.compose.foundation.shape.CircleShape),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(card.statusLabel, fontSize = 12.sp, color = statusColor(card.status))
                    }
                }
            }
            if (card.signature.isNotBlank()) {
                Text(card.signature, fontSize = 13.sp, color = DashboardInkSecondary)
            }
            if (!card.enabled) {
                Text("已停用", fontSize = 12.sp, color = StatusGrey)
            }
        }
    }
}

@Composable
private fun StatsRow(card: EmployeeCard) {
    val s = card.stats
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCell("本周任务", s.weekTasks, Modifier.weight(1f))
        StatCell("本周完成", s.weekDone, Modifier.weight(1f))
        StatCell("本周卡住", s.weekBlocked, Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardWhite),
        border = BorderStroke(1.dp, Color(0xFFE1E3E8)),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("$value", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DashboardInkPrimary)
            Text(label, fontSize = 11.sp, color = DashboardInkWeak)
        }
    }
}

@Composable
private fun TaskRow(
    title: String,
    tag: String?,
    isBlocked: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardWhite),
        border = BorderStroke(1.dp, Color(0xFFE1E3E8)),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontSize = 13.sp, color = DashboardInkPrimary, modifier = Modifier.weight(1f), maxLines = 1)
            if (!tag.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(tag, fontSize = 11.sp, color = if (isBlocked) StatusRed else DashboardInkSecondary)
            }
        }
    }
}
