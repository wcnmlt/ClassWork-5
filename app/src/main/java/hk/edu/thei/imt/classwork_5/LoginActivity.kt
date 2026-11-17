package hk.edu.thei.imt.classwork_5

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * First screen of the app.
 *
 * The first login creates the account and stores it in SharedPreferences.
 * Later logins must match that username and password before the Bingo game opens.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvLoginMessage = findViewById<TextView>(R.id.tvLoginMessage)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        if (!GamePrefs.hasAccount(this)) {
            tvLoginMessage.text = getString(R.string.login_first_hint)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                tvLoginMessage.text = getString(R.string.login_empty)
                return@setOnClickListener
            }

            // No saved account yet, so this login becomes the account.
            if (!GamePrefs.hasAccount(this)) {
                GamePrefs.saveAccount(this, username, password)
                openGame()
                return@setOnClickListener
            }

            if (GamePrefs.checkLogin(this, username, password)) {
                openGame()
            } else {
                tvLoginMessage.text = getString(R.string.login_failed)
            }
        }
    }

    /** Closes the login screen so the Back key does not return to it from the game. */
    private fun openGame() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
