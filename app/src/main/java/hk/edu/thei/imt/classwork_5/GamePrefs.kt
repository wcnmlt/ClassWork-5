package hk.edu.thei.imt.classwork_5

import android.content.Context

/**
 * Small store for the account and the game settings.
 *
 * SharedPreferences keeps these values after the app is closed.
 * Bingo results are not stored here. Those go into SQLite.
 */
object GamePrefs {

    private const val FILE_NAME = "game_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_MAX_NUMBER = "max_number"
    private const val KEY_WIN_COUNT = "win_count"

    /** Opens the private preferences file for this app. */
    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** True after the first successful login has saved a username. */
    fun hasAccount(context: Context): Boolean {
        return !prefs(context).getString(KEY_USERNAME, "").isNullOrEmpty()
    }

    /** Saves the account created on the first login. */
    fun saveAccount(context: Context, username: String, password: String) {
        prefs(context).edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    /** Compares the typed username and password with the saved account. */
    fun checkLogin(context: Context, username: String, password: String): Boolean {
        val saved = prefs(context)
        return username == saved.getString(KEY_USERNAME, "") &&
            password == saved.getString(KEY_PASSWORD, "")
    }

    fun username(context: Context): String {
        return prefs(context).getString(KEY_USERNAME, "") ?: ""
    }

    /** Uses the username when the player has not saved a nickname yet. */
    fun nickname(context: Context): String {
        val saved = prefs(context).getString(KEY_NICKNAME, "")
        return if (saved.isNullOrBlank()) username(context) else saved
    }

    /**
     * The Bingo pool must contain at least five different numbers.
     * A missing or too-small value falls back to 25.
     */
    fun maxNumber(context: Context): Int {
        return prefs(context).getInt(KEY_MAX_NUMBER, 25).coerceIn(5, 100)
    }

    /** Saves the nickname and the largest number the Bingo pool may use. */
    fun saveConfig(context: Context, nickname: String, maxNumber: Int) {
        prefs(context).edit()
            .putString(KEY_NICKNAME, nickname)
            .putInt(KEY_MAX_NUMBER, maxNumber)
            .apply()
    }

    fun winCount(context: Context): Int {
        return prefs(context).getInt(KEY_WIN_COUNT, 0)
    }

    /** Called once when all five Bingo numbers have been drawn. */
    fun addWin(context: Context) {
        prefs(context).edit()
            .putInt(KEY_WIN_COUNT, winCount(context) + 1)
            .apply()
    }
}
