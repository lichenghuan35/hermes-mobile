package com.m57.hermescontrol.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m57.hermescontrol.data.model.DashboardResponse
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

/** 首页=总裁驾驶舱 UI 状态。 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboard: DashboardResponse? = null,
    val errorMessage: String? = null,
    val toastMessage: String? = null,
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load(initial = true)
    }

    /** 首屏加载 / 下拉刷新。 */
    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = initial, isRefreshing = !initial, errorMessage = null) }
            val result =
                withContext(Dispatchers.IO) {
                    safeApiCall { EmpApiClient.empApi.dashboard() }
                }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            dashboard = result.data,
                            errorMessage = null,
                        )
                    }
                }
                is NetworkResult.Failure -> {
                    val msg =
                        if (!EmpApiClient.hasApiKey()) {
                            "尚未配置总裁 API key，请到 设置→连接→总裁 API key 填写后返回。"
                        } else {
                            "加载驾驶舱失败: ${result.error.message}"
                        }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = msg,
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
