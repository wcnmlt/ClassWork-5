package hk.edu.thei.imt.classwork_5

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite database for one Bingo round.
 *
 * Each row stores who played, which round it was, the five Bingo numbers
 * dealt on that round, and the phone time when the round started.
 * The database file is bingo.db and it stays on the device after the app closes.
 */
class BingoDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    /**
     * Creates the table the first time the database is opened.
     * id is only used by SQLite to tell rows apart.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USERNAME TEXT NOT NULL, " +
                "$COL_ROUND INTEGER NOT NULL, " +
                "$COL_NUMBERS TEXT NOT NULL, " +
                "$COL_TIME TEXT NOT NULL)"
        )
    }

    /**
     * If the table design changes later, drop the old table and create it again.
     * This project only has version 1, so this method is not used yet.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * The next round for this username is one more than the highest round already saved.
     * A player with no rows starts at round 1.
     */
    fun nextRoundNumber(username: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT MAX($COL_ROUND) FROM $TABLE_NAME WHERE $COL_USERNAME = ?",
            arrayOf(username)
        )
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                return it.getInt(0) + 1
            }
        }
        return 1
    }

    /**
     * Inserts one Bingo round.
     * bingoNumbers is the five numbers joined by commas, for example "4, 9, 12, 18, 23".
     * systemTime is already formatted text, for example "2026-11-17 11:20:00".
     */
    fun insertRound(username: String, roundNumber: Int, bingoNumbers: String, systemTime: String) {
        val values = ContentValues()
        values.put(COL_USERNAME, username)
        values.put(COL_ROUND, roundNumber)
        values.put(COL_NUMBERS, bingoNumbers)
        values.put(COL_TIME, systemTime)
        writableDatabase.insert(TABLE_NAME, null, values)
    }

    /**
     * Reads the newest rows for this username so the game screen can show them.
     * The list is newest first.
     */
    fun latestRounds(username: String, limit: Int): List<BingoRecord> {
        val records = mutableListOf<BingoRecord>()
        val cursor = readableDatabase.rawQuery(
            "SELECT $COL_USERNAME, $COL_ROUND, $COL_NUMBERS, $COL_TIME " +
                "FROM $TABLE_NAME WHERE $COL_USERNAME = ? " +
                "ORDER BY $COL_ID DESC LIMIT ?",
            arrayOf(username, limit.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                records.add(
                    BingoRecord(
                        username = it.getString(0),
                        roundNumber = it.getInt(1),
                        bingoNumbers = it.getString(2),
                        systemTime = it.getString(3)
                    )
                )
            }
        }
        return records
    }

    companion object {
        private const val DATABASE_NAME = "bingo.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "bingo_record"
        const val COL_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_ROUND = "round_number"
        const val COL_NUMBERS = "bingo_numbers"
        const val COL_TIME = "system_time"
    }
}

/**
 * One saved Bingo round, matching the four fields required by the assignment.
 */
data class BingoRecord(
    val username: String,
    val roundNumber: Int,
    val bingoNumbers: String,
    val systemTime: String
)
