package com.m57.hermescontrol.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 员工名片聚合（/api/employees/{name}）：名片字段 + 状态 + 本周看板 + 活跃任务。 */
@Serializable
data class EmployeeCard(
    val name: String = "",
    val title: String = "",
    val role: String = "",
    val signature: String = "",
    val skills: List<String> = emptyList(),
    val enabled: Boolean = true,
    val color: String = "",
    val status: String = "",
    @SerialName("status_label") val statusLabel: String = "",
    val stats: EmployeeCardStats = EmployeeCardStats(),
    @SerialName("blocked_tasks") val blockedTasks: List<BlockedTaskItem> = emptyList(),
    @SerialName("active_tasks") val activeTasks: List<ActiveTaskItem> = emptyList(),
)

@Serializable
data class EmployeeCardStats(
    @SerialName("week_tasks") val weekTasks: Int = 0,
    @SerialName("week_done") val weekDone: Int = 0,
    @SerialName("week_blocked") val weekBlocked: Int = 0,
)
