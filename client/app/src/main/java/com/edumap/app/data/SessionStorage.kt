package com.edumap.app.data

import android.content.Context

class SessionStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(session: StudentSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_LOGIN, session.login)
            .putString(KEY_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .apply()
    }

    fun load(): StudentSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val login = prefs.getString(KEY_LOGIN, null) ?: return null
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        return StudentSession(
            userId = userId,
            login = login,
            accessToken = token,
            refreshToken = refreshToken
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "edumap_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_LOGIN = "login"
        const val KEY_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
