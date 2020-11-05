package com.example.socialmediaappv2.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context: Context) {
        private val prefsName = "scaPrefs"
        private val sharedPref: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        companion object Images {
            var imagesTaken = 0
        }

        fun save(key: String, value: String) {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun save(key: String, value: Int) {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun save(key: String, value: Boolean) {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun getString(key: String): String? {
            return sharedPref.getString(key, null)
        }

        fun getInt(key: String): Int {
            return sharedPref.getInt(key, 0)
        }

        fun getBoolean(key: String): Boolean {
            return sharedPref.getBoolean(key, false)
        }

        fun clearData() {
            val editor: SharedPreferences.Editor = sharedPref.edit()

            editor.clear()
            editor.apply()
        }

        fun clearData(key: String) {
            val editor: SharedPreferences.Editor = sharedPref.edit()

            editor.remove(key)
            editor.apply()
        }
}