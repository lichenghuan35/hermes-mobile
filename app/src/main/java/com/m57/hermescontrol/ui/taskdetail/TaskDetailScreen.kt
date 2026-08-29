package com.m57.hermescontrol.ui.taskdetail

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m57.hermescontrol.data.model.TaskComment
import com.m57.hermescontrol.data.model.TaskDetailResponse
import com.m57.hermescontrol.theme.DashboardBorder
import com.m57.hermescontrol.theme.DashboardChatBubbleGrey
import com.m57.hermescontrol.theme.DashboardInkPrimary
import com.m57.hermescontrol.theme.DashboardInkSecondary
import com.m57.hermescontrol.theme.DashboardLightRed
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
        "busy", "running" -> StatusYellow
        "idle" -> StatusGreen
        "done", "ready" -> StatusGreen
        else -> StatusGrey
    }

@Composable
fun TaskDetailScreen(
    taskId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: TaskDetailViewModel = viewModel { TaskDetailViewModel(taskId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HermesScaffold(
        modifier = modifier,
        title = { Text("任务 · 群聊", fontWeight = FontWeight.SemiBold) },
        navigationIcon = NavIcon.Back(onBack),
        drawerGesturesEnabled = false,
        onRefresh = { viewModel.load() },
        isRefreshing = state.isRefreshing,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.detail == null -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.errorMessage != null && state.detail == null -> {
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
                    val detail = state.detail
                    if (detail == null) {
                        Text("暂无数据", color = DashboardInkSecondary, modifier = Modifier.align(Alignment.Center))
                    } else {
                        DetailBody(
                            detail = detail,
                            draft = state.draft,
                            isApproving = state.isApproving,
                            approvalDone = state.approvalDone,
                            onDraftChange = viewModel::onDraftChange,
                            onSend = viewModel::sendComment,
                            onApprove = viewModel::approve,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBody(
    detail: TaskDetailResponse,
    draft: String,
    isApproving: Boolean,
    approvalDone: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onApprove: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // 任务头 + 评论线程（可滚动，占剩余空间）
        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { TaskHeader(detail) }
            item { Spacer(Modifier.height(2.dp)) }
            items(detail.comments, key = { it.id }) { comment ->
                CommentRow(comment)
            }
            if (detail.comments.isEmpty()) {
                item {
                    Text(
                        "还没有评论。这是任务群聊 / 拍板房间。",
                        fontSize = 12.sp,
                        color = DashboardInkSecondary,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        // 固定在底部：拍板按钮 + 评论输入
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (detail.canApprove) {
                OutlinedButton(
                    onClick = onApprove,
                    enabled = !isApproving,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, StatusRed),
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Filled.CheckCircle, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (approvalDone) "已拍板 ✓" else "拍板：卡住解除",
                        color = StatusRed,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Text(
                    "状态：${detail.task.statusLabel.ifBlank { detail.task.status }}",
                    fontSize = 11.sp,
                    color = DashboardInkSecondary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("评论 / 拍板意见…") },
                    maxLines = 3,
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.FilledIconButton(onClick = onSend) {
                    Icon(Icons.Filled.ErrorOutline, "发送", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun TaskHeader(detail: TaskDetailResponse) {
    val t = detail.task
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DashboardWhite),
        border = BorderStroke(1.dp, DashboardBorder),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(t.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DashboardInkPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(statusColor(t.status), CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    t.statusLabel.ifBlank { t.status },
                    fontSize = 12.sp,
                    color = statusColor(t.status),
                )
                t.assignee?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.width(12.dp))
                    Text("负责人：$it", fontSize = 12.sp, color = DashboardInkSecondary)
                }
            }
            if (detail.canApprove && t.kind != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = StatusRed, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t.kindLabel, fontSize = 11.sp, color = StatusRed, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Text("等拍板", fontSize = 11.sp, color = StatusRed)
                }
            }
            if (!t.body.isNullOrBlank()) {
                Text(t.body, fontSize = 13.sp, color = DashboardInkSecondary)
            }
        }
    }
}

@Composable
private fun CommentRow(comment: TaskComment) {
    val isBoss = comment.author == "boss"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isBoss) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .background(
                    if (isBoss) DashboardLightRed else DashboardChatBubbleGrey,
                    RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(comment.author, fontSize = 11.sp, color = if (isBoss) StatusRed else DashboardInkSecondary)
            Text(comment.body, fontSize = 13.sp, color = DashboardInkPrimary)
        }
    }
}
