package com.m57.hermescontrol.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** KPI 4 格（首页驾驶舱最顶上）。 */
@Serializable
data class DashboardKpi(
    val waiting_approval: Int = 0, // 待拍板（首页红点）
    val in_progress: Int = 0, // 进行中
    val done_today: Int = 0, // 今日完成
    val employees_online: Int = 0, // 员工在线
)

/** 需要拍板清单里的一条（卡住任务）。 */
@Serializable
data class WaitingApprovalTask(
    val id: String = "",
    val title: String = "",
    val assignee: String? = null,
    val status: String = "",
    val kind: String? = null,
    @SerialName("kind_label") val kindLabel: String? = null,
    val reason: String? = null,
    @SerialName("created_at") val createdAt: Long? = null,
)

/** 每员工数据看板（本周任务/完成/卡住/在线）。 */
@Serializable
data class EmployeeStats(
    @SerialName("week_tasks") val weekTasks: Int = 0,
    @SerialName("week_done") val weekDone: Int = 0,
    @SerialName("week_blocked") val weekBlocked: Int = 0,
    val online: Boolean = false,
)

/** 员工卡住任务条目。 */
@Serializable
data class BlockedTaskItem(
    val id: String = "",
    val title: String = "",
    val kind: String? = null,
    @SerialName("kind_label") val kindLabel: String? = null,
    val reason: String? = null,
)

/** 员工活跃任务条目。 */
@Serializable
data class ActiveTaskItem(
    val id: String = "",
    val title: String = "",
    val status: String = "",
    val running: Boolean = false,
    val alive: Boolean = true,
)

/**
 * 驾驶舱里一个员工的整体状态。
 * 名片字段（title/role/signature/skills）来自 /profile 接口，可能缺省，都做默认兜底。
 */
@Serializable
data class DashboardEmployee(
    val name: String = "",
    @SerialName("on_disk") val onDisk: Boolean = true,
    val status: String = "", // blocked/busy/idle/offline
    @SerialName("status_label") val statusLabel: String = "",
    @SerialName("blocked_count") val blockedCount: Int = 0,
    @SerialName("blocked_tasks") val blockedTasks: List<BlockedTaskItem> = emptyList(),
    @SerialName("active_count") val activeCount: Int = 0,
    @SerialName("active_tasks") val activeTasks: List<ActiveTaskItem> = emptyList(),
    val stats: EmployeeStats? = null,
    // 名片字段（可能缺省）
    val title: String? = null,
    val role: String? = null,
    val signature: String? = null,
    val skills: List<String>? = null,
)

/** 驾驶舱聚合根响应。 */
@Serializable
data class DashboardResponse(
    @SerialName("generated_at") val generatedAt: Long = 0,
    val kpi: DashboardKpi = DashboardKpi(),
    @SerialName("waiting_approval_tasks") val waitingApprovalTasks: List<WaitingApprovalTask> = emptyList(),
    val employees: List<DashboardEmployee> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(),
)

@Serializable
data class DashboardSummary(
    val blocked: Int = 0,
    val busy: Int = 0,
    val idle: Int = 0,
    val offline: Int = 0,
)

/** 员工名片配置。 */
@Serializable
data class EmployeeProfile(
    val name: String = "",
    val title: String = "",
    val role: String = "",
    val signature: String = "",
    val skills: List<String> = emptyList(),
    val enabled: Boolean = true,
    val color: String = "",
    val editable: Boolean = true,
)
