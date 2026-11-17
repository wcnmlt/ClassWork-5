package hk.edu.thei.imt.classwork_5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Bingo game screen.
 *
 * A round deals five different numbers. Draw keeps calling a new number
 * from 1 up to the configured maximum. A called number that is on the card
 * is marked. The round is won when all five numbers have been called.
 * The five numbers, the username, the round number and the system time
 * are written to SQLite when the round starts.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvPlayer: TextView
    private lateinit var tvRound: TextView
    private lateinit var numberViews: List<TextView>
    private lateinit var tvCalled: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvHistory: TextView
    private lateinit var btnDraw: Button
    private lateinit var database: BingoDbHelper

    /** The five numbers dealt for the current round. */
    private val card = mutableListOf<Int>()

    /** Every number already called in this round, including misses. */
    private val called = mutableSetOf<Int>()

    /** Upper end of the number pool. It comes from Configuration and is at least 5. */
    private var maxNumber = 25

    private var roundNumber = 1
    private var bingo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvPlayer = findViewById(R.id.tvPlayer)
        tvRound = findViewById(R.id.tvRound)
        numberViews = listOf(
            findViewById(R.id.tvNum1),
            findViewById(R.id.tvNum2),
            findViewById(R.id.tvNum3),
            findViewById(R.id.tvNum4),
            findViewById(R.id.tvNum5)
        )
        tvCalled = findViewById(R.id.tvCalled)
        tvMessage = findViewById(R.id.tvMessage)
        tvHistory = findViewById(R.id.tvHistory)
        btnDraw = findViewById(R.id.btnDraw)
        database = BingoDbHelper(this)

        findViewById<Button>(R.id.btnMeal).setOnClickListener {
            startActivity(Intent(this, MealActivity::class.java))
        }
        findViewById<Button>(R.id.btnConfig).setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // After Bingo, the same button starts the next round instead of drawing.
        btnDraw.setOnClickListener {
            if (bingo) {
                startRound()
            } else {
                drawOneNumber()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showPlayer()
        val savedMax = GamePrefs.maxNumber(this)
        // A new maximum changes the pool, so the current card is no longer valid.
        if (card.isEmpty() || savedMax != maxNumber) {
            maxNumber = savedMax
            startRound()
        }
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    /** Nickname and win count are kept in SharedPreferences from the earlier practice. */
    private fun showPlayer() {
        tvPlayer.text = getString(
            R.string.player_status,
            GamePrefs.nickname(this),
            GamePrefs.winCount(this)
        )
    }

    /**
     * Deals five different numbers, saves that round, and resets the draw button.
     * Numbers are chosen from 1 to maxNumber so five unique values always exist.
     */
    private fun startRound() {
        maxNumber = GamePrefs.maxNumber(this)
        card.clear()
        called.clear()
        bingo = false

        val dealt = (1..maxNumber).shuffled(Random).take(CARD_SIZE).sorted()
        card.addAll(dealt)

        val username = GamePrefs.username(this)
        roundNumber = database.nextRoundNumber(username)
        val timeText = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        database.insertRound(
            username = username,
            roundNumber = roundNumber,
            bingoNumbers = card.joinToString(", "),
            systemTime = timeText
        )

        btnDraw.text = getString(R.string.bingo_draw)
        tvRound.text = getString(R.string.bingo_round, roundNumber)
        tvCalled.text = getString(R.string.bingo_called_none)
        tvMessage.text = getString(R.string.bingo_start)
        showCard()
        showHistory()
        showPlayer()
    }

    /**
     * Calls one number that has not been called yet.
     * If it is one of the five card numbers, the card marks it.
     * When every card number has been called, the player wins.
     */
    private fun drawOneNumber() {
        val remaining = (1..maxNumber).filter { it !in called }
        if (remaining.isEmpty()) {
            tvMessage.text = getString(R.string.bingo_pool_empty)
            return
        }

        val number = remaining.random()
        called.add(number)
        tvCalled.text = getString(R.string.bingo_called, number)

        val matched = card.all { it in called }
        if (matched) {
            bingo = true
            GamePrefs.addWin(this)
            showPlayer()
            btnDraw.text = getString(R.string.bingo_next)
            tvMessage.text = getString(R.string.bingo_win)
        } else if (number in card) {
            tvMessage.text = getString(R.string.bingo_hit, number)
        } else {
            tvMessage.text = getString(R.string.bingo_miss, number)
        }
        showCard()
    }

    /** A matched number fills its tile. The others stay white. */
    private fun showCard() {
        numberViews.forEachIndexed { index, tile ->
            val number = card.getOrNull(index)
            tile.text = number?.toString().orEmpty()
            val hit = number != null && number in called
            tile.setBackgroundResource(
                if (hit) R.drawable.bg_number_hit else R.drawable.bg_number_tile
            )
            tile.setTextColor(getColor(if (hit) R.color.white else R.color.bingo_ink))
        }
    }

    /** Shows the latest saved rows: username is implied, plus round, five numbers and time. */
    private fun showHistory() {
        val rows = database.latestRounds(GamePrefs.username(this), 3)
        if (rows.isEmpty()) {
            tvHistory.text = getString(R.string.bingo_empty_history)
            return
        }
        tvHistory.text = rows.joinToString("\n") { row ->
            getString(
                R.string.bingo_history_line,
                row.roundNumber,
                row.bingoNumbers,
                row.systemTime
            )
        }
    }

    companion object {
        private const val CARD_SIZE = 5
    }
}
