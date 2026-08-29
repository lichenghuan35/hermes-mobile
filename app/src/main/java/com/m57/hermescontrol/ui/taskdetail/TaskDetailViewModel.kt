package com.m57.hermescontrol.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m57.hermescontrol.data.model.TaskDetailResponse
import com.m57.hermescontrol.data.remote.EmpApiClient
import com.m57.hermescontrol.data.remote.NetworkResult
import com.m57.hermescontrol.data.remote.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 任务详情/群聊 UI 状态。 */
data class TaskDetailUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val detail: TaskDetailResponse? = null,
    val errorMessage: String? = null,
    // 评论输入
    val draft: String = "",
    // 拍板
    val isApproving: Boolean = false,
    val approvalDone: Boolean = false,
    val toastMessage: String? = null,
)

class TaskDetailViewModel(
    private val taskId: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        load(initial = true)
    }

    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = initial, isRefreshing = !initial, errorMessage = null) }
            val result =
                withContext(Dispatchers.IO) {
                    safeApiCall { EmpApiClient.empApi.getTaskDetail(taskId) }
                }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            detail = result.data,
                            errorMessage = null,
                        )
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "加载任务失败: ${result.error.message}",
                        )
                    }
                }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    /** 发一条评论到任务群聊。 */
    fun sendComment() {
        val draft = _uiState.value.draft.trim()
        if (draft.isEmpty()) return
        val taskId = this.taskId
        viewModelScope.launch {
            _uiState.update { it.copy(draft = "") }
            val result =
                withContext(Dispatchers.IO) {
                    safeApiCall {
                        EmpApiClient.empApi.postTaskComment(
                            taskId,
                            mapOf("author" to "boss", "body" to draft),
                        )
                    }
                }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(toastMessage = "已发送") }
                    load() // 刷新评论线程
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(toastMessage = "发送失败: ${result.error.message}", draft = draft)
                    }
                }
            }
        }
    }

    /** 总裁拍板：一句话 → 卡住解除变进行中。 */
    fun approve() {
        val taskId = this.taskId
        viewModelScope.launch {
            _uiState.update { it.copy(isApproving = true, approvalDone = false) }
            val result =
                withContext(Dispatchers.IO) {
                    safeApiCall { EmpApiClient.empApi.approveTask(taskId, mapOf("decision" to "")) }
                }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isApproving = false,
                            approvalDone = true,
                            toastMessage = "已拍板，任务解锁继续",
                        )
                    }
                    load() // 刷新（status 变化 + 拍板评论入线程）
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isApproving = false,
                            toastMessage = "拍板失败: ${result.error.message}",
                        )
                    }
                }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
