package com.example.myapplication.auth

import android.content.Context

object AuthManager {

    private const val PREF_NAME = "auth_pref"
    private const val KEY_LOGIN = "isLoggedIn"

    fun setLoggedIn(context: Context, value: Boolean) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putBoolean(KEY_LOGIN, value).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY_LOGIN, false)
    }

    fun logout(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putBoolean(KEY_LOGIN, false).apply()
    }
}
