package com.m57.hermescontrol.data.remote

import com.m57.hermescontrol.data.local.AuthManager
import com.m57.hermescontrol.data.local.EmpApiKeyStore
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * emp-api（自建员工状态接口）客户端单例。
 *
 * 与主 [ApiClient] 完全隔离：
 *  - baseUrl = dashboard base url 后拼 `/emp-api/`（nginx 反代前缀）
 *  - 鉴权 = 独立 Bearer API_SERVER_KEY（不是 dashboard 的 cookie / token）
 *
 * API key 通过 [setApiKey] 注入并持久化到 [EmpApiKeyStore]（EncryptedSharedPreferences，
 * AES256-GCM）。key 为空时请求不带鉴权头（后端 401），驾驶舱提示先填 key。
 */
object EmpApiClient {
    @Volatile
    private var apiKey: String? = EmpApiKeyStore.get()

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var service: EmpApiService? = null

    /** 注入 emp-api 的 Bearer key，并加密持久化。key 为空时请求不带鉴权头。 */
    fun setApiKey(key: String?) {
        val trimmed = key?.trim()?.takeIf { it.isNotBlank() }
        apiKey = trimmed
        EmpApiKeyStore.set(trimmed)
        rebuild()
    }

    /** 当前是否已配置 emp-api key（用于驾驶舱提示）。 */
    fun hasApiKey(): Boolean = !apiKey.isNullOrBlank()

    /** 当前 service 实例。首次调用时构建。 */
    val empApi: EmpApiService
        get() {
            return service ?: synchronized(this) {
                service ?: buildService().also { service = it }
            }
        }

    /** 强制重建（base url / key 变化后调用）。 */
    fun rebuild() {
        synchronized(this) {
            retrofit = null
            service = null
        }
    }

    /** 拼 emp-api base url：dashboard base 后追加 `emp-api/`。 */
    private fun empApiBaseUrl(base: String): String {
        var b = base.trim()
        if (!b.endsWith("/")) b += "/"
        return b + "emp-api/"
    }

    private fun buildService(): EmpApiService {
        val key = apiKey
        val bearerInterceptor =
            Interceptor { chain ->
                val request = chain.request()
                if (key.isNullOrBlank()) {
                    chain.proceed(request)
                } else {
                    chain.proceed(
                        request.newBuilder()
                            .addHeader("Authorization", "Bearer $key")
                            .build(),
                    )
                }
            }

        val okHttp =
            OkHttpProvider.base.newBuilder()
                .addInterceptor(bearerInterceptor)
                .build()

        val base = AuthManager.getBaseUrl()
        val rf =
            Retrofit.Builder()
                .baseUrl(empApiBaseUrl(base))
                .client(okHttp)
                .addConverterFactory(OkHttpProvider.json.asConverterFactory("application/json".toMediaType()))
                .build()
                .also { retrofit = it }
        return rf.create(EmpApiService::class.java)
    }
}
