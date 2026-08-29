package com.m57.hermescontrol.ui.employeecard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m57.hermescontrol.data.model.EmployeeCard
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

/** 员工名片 UI 状态。 */
data class EmployeeCardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val card: EmployeeCard? = null,
    val errorMessage: String? = null,
)

class EmployeeCardViewModel(
    private val name: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmployeeCardUiState())
    val uiState: StateFlow<EmployeeCardUiState> = _uiState.asStateFlow()

    init {
        load(initial = true)
    }

    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = initial, isRefreshing = !initial, errorMessage = null) }
            val result =
                withContext(Dispatchers.IO) {
                    safeApiCall { EmpApiClient.empApi.getEmployeeCard(name) }
                }
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, card = result.data, errorMessage = null)
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            card = null,
                            errorMessage = "加载名片失败: ${result.error.message}",
                        )
                    }
                }
            }
        }
    }
}
