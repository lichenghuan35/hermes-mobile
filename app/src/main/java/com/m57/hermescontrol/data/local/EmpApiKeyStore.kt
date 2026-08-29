package com.m57.hermescontrol.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * 加密存储「总裁 API key」（emp-api 的 Bearer key）。
 *
 * 用 EncryptedSharedPreferences（AES256-GCM + Keystore）保存，
 * 跟会话 cookie 同级安全，绝不明文落盘。
 *
 * 用法：app 启动时调 [initialize]（同 CookieManager），
 * 之后 [get]/[set] 即可（IO 需调用方自己切 Dispatchers.IO）。
 */
object EmpApiKeyStore {
    private const val PREFS_FILE = "hermes_emp_api_key"
    private const val KEY_STORE_KEY = "emp_api_key"

    private var prefs: SharedPreferences? = null

    /** 必须在读写前调用一次（传 applicationContext）。 */
    fun initialize(context: Context) {
        if (prefs != null) return
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs =
            EncryptedSharedPreferences.create(
                PREFS_FILE,
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
    }

    /** 读已保存的 emp-api Bearer key；未设置或为空返回 null。 */
    fun get(): String? = prefs?.getString(KEY_STORE_KEY, null)?.trim()?.takeIf { it.isNotBlank() }

    /** 保存（或清空，传 null/空）emp-api Bearer key。 */
    fun set(key: String?) {
        val p = prefs ?: return
        val value = key?.trim()?.takeIf { it.isNotBlank() }
        if (value == null) {
            p.edit().remove(KEY_STORE_KEY).apply()
        } else {
            p.edit().putString(KEY_STORE_KEY, value).apply()
        }
    }
}
