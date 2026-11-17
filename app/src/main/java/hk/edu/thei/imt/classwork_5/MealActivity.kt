package hk.edu.thei.imt.classwork_5

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Meal screen kept from the previous practice.
 *
 * The player can tick one or both customers and choose one meal.
 * Confirm writes the result. Return closes this screen and goes back to Bingo.
 */
class MealActivity : AppCompatActivity() {

    private lateinit var cbCustomer1: CheckBox
    private lateinit var cbCustomer2: CheckBox
    private lateinit var rgMeals: RadioGroup
    private lateinit var btnConfirm: Button
    private lateinit var tvResult: TextView
    private lateinit var btnReturn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)

        cbCustomer1 = findViewById(R.id.cbCustomer1)
        cbCustomer2 = findViewById(R.id.cbCustomer2)
        rgMeals = findViewById(R.id.rgMeals)
        btnConfirm = findViewById(R.id.btnConfirm)
        tvResult = findViewById(R.id.tvResult)
        btnReturn = findViewById(R.id.btnReturn)

        btnConfirm.setOnClickListener {
            showSelection()
        }

        btnReturn.setOnClickListener {
            finish()
        }
    }

    /**
     * Builds the result sentence.
     * Checkboxes may both be ticked. The radio group allows only one meal.
     * If a customer or a meal is missing, the result stays "Please select item:".
     */
    private fun showSelection() {
        val customers = mutableListOf<String>()
        if (cbCustomer1.isChecked) {
            customers.add(getString(R.string.customer_1))
        }
        if (cbCustomer2.isChecked) {
            customers.add(getString(R.string.customer_2))
        }

        val meal = when (rgMeals.checkedRadioButtonId) {
            R.id.rbBreakfast -> getString(R.string.breakfast)
            R.id.rbLunch -> getString(R.string.lunch)
            R.id.rbDinner -> getString(R.string.dinner)
            else -> null
        }

        if (customers.isEmpty() || meal == null) {
            tvResult.text = getString(R.string.please_select_item)
            return
        }

        val who = customers.joinToString(" and ")
        tvResult.text = getString(R.string.meal_result, who, meal)
    }
}
