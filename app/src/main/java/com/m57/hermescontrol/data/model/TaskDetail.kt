package com.m57.hermescontrol.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 任务群聊的评论。 */
@Serializable
data class TaskComment(
    val id: Long = 0,
    val author: String = "",
    val body: String = "",
    @SerialName("created_at") val createdAt: Long = 0,
)

/** 任务详情（房间视图）。 */
@Serializable
data class TaskDetail(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val assignee: String? = null,
    val status: String = "",
    @SerialName("status_label") val statusLabel: String = "",
    val kind: String? = null,
    @SerialName("kind_label") val kindLabel: String = "",
    val priority: Int = 0,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("started_at") val startedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null,
)

/** 任务详情响应 = 任务 + 评论线程 + 是否可拍板。 */
@Serializable
data class TaskDetailResponse(
    val task: TaskDetail = TaskDetail(),
    val comments: List<TaskComment> = emptyList(),
    @SerialName("can_approve") val canApprove: Boolean = false,
)

/** 拍板结果。 */
@Serializable
data class ApproveResult(
    val ok: Boolean = false,
    @SerialName("task_id") val taskId: String = "",
    val status: String? = null,
    val decision: String = "",
    val comment: String = "",
)
