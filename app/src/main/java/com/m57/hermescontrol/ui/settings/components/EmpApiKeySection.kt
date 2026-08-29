package com.m57.hermescontrol.ui.settings.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.m57.hermescontrol.ui.settings.SectionCard
import com.m57.hermescontrol.ui.settings.SettingsUiState
import com.m57.hermescontrol.ui.settings.SettingsViewModel

/**
 * 「总裁 API key」输入卡：填 emp-api 的 Bearer key，供首页驾驶舱调员工状态接口。
 * 输入后点保存 → 调 EmpApiClient.setApiKey（加密持久化到 EmpApiKeyStore）。
 */
@Composable
internal fun EmpApiKeySection(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
) {
    SectionCard {
        Text(
            text = "总裁 API key（驾驶舱数据）",
            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "用于首页驾驶舱读取员工状态；在云机接口配置里查看（emp-api Bearer key）。",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.empApiKey,
            onValueChange = viewModel::onEmpApiKeyChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API key") },
            placeholder = { Text("填写云机 emp-api 的 Bearer key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = viewModel::saveEmpApiKey,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.empApiKeySaved) "已保存 ✓" else "保存")
        }
        if (state.empApiKeySaved) {
            Text(
                text = "已保存。驾驶舱会用它请求员工状态。",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
