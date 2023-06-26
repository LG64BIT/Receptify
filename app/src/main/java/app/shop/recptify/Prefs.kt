package app.shop.recptify

import android.content.Context
import android.content.SharedPreferences

class Prefs private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var email: String?
        get() = sharedPreferences.getString(KEY_EMAIL, null)
        set(value) = sharedPreferences.edit().putString(KEY_EMAIL, value).apply()

    var username: String?
        get() = sharedPreferences.getString(KEY_USERNAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_USERNAME, value).apply()

    companion object {

        private const val PREFS_NAME = "MyPrefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "password"

        @Volatile
        private var instance: Prefs? = null

        fun getInstance(context: Context): Prefs {
            return instance ?: synchronized(this) {
                instance ?: Prefs(context).also { instance = it }
            }
        }
    }
}