package hk.edu.thei.imt.classwork_5

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Configuration screen.
 *
 * The player can change the nickname and the largest number used by Bingo.
 * The largest number must be from 5 to 100, because each round needs five different numbers.
 * Saved values are written to SharedPreferences and read again when the game resumes.
 */
class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val tvConfigUser = findViewById<TextView>(R.id.tvConfigUser)
        val etNickname = findViewById<EditText>(R.id.etNickname)
        val etMaxNumber = findViewById<EditText>(R.id.etMaxNumber)
        val tvWinCount = findViewById<TextView>(R.id.tvWinCount)
        val tvConfigMessage = findViewById<TextView>(R.id.tvConfigMessage)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnReturn = findViewById<Button>(R.id.btnConfigReturn)

        tvConfigUser.text = getString(R.string.config_user, GamePrefs.username(this))
        etNickname.setText(GamePrefs.nickname(this))
        etMaxNumber.setText(GamePrefs.maxNumber(this).toString())
        tvWinCount.text = getString(R.string.config_wins, GamePrefs.winCount(this))

        btnSave.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val maxNumber = etMaxNumber.text.toString().trim().toIntOrNull()

            if (nickname.isEmpty() || maxNumber == null || maxNumber !in 5..100) {
                tvConfigMessage.text = getString(R.string.config_invalid)
                return@setOnClickListener
            }

            GamePrefs.saveConfig(this, nickname, maxNumber)
            tvConfigMessage.text = getString(R.string.config_saved)
        }

        // finish() closes this screen and shows the Bingo game again.
        btnReturn.setOnClickListener {
            finish()
        }
    }
}
