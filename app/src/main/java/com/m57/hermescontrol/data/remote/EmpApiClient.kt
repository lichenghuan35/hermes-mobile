package com.m57.hermescontrol.data.remote

import com.m57.hermescontrol.data.local.AuthManager
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
 * 不要在 APK 里硬编码 API key —— key 由「我的/设置」页运行时填写，
 * 通过 [setApiKey] 注入后 [rebuild]，存进 EncryptedSharedPreferences（后续接）。
 */
object EmpApiClient {
    @Volatile
    private var apiKey: String? = null

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var service: EmpApiService? = null

    /** 运行时注入 emp-api 的 Bearer key。key 为空时请求不带鉴权头（后端 401）。 */
    fun setApiKey(key: String?) {
        apiKey = key?.trim()?.takeIf { it.isNotBlank() }
        rebuild()
    }

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
