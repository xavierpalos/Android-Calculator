package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var swDegrees: SwitchCompat

    private var currentInput = ""          // número que se está escribiendo
    private var accumulator: Double? = null // resultado acumulado
    private var pendingOp: Char? = null     // operación pendiente
    private var justEvaluated = false       // true tras "=" o tras sin/cos/tan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        swDegrees = findViewById(R.id.swDegrees)

        val digits = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        for ((id, d) in digits) {
            findViewById<Button>(id).setOnClickListener { onDigit(d) }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener { onDot() }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperator('+') }
        findViewById<Button>(R.id.btnSub).setOnClickListener { onOperator('-') }
        findViewById<Button>(R.id.btnMul).setOnClickListener { onOperator('*') }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { onOperator('/') }

        findViewById<Button>(R.id.btnEq).setOnClickListener { onEquals() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }

        findViewById<Button>(R.id.btnSin).setOnClickListener { onTrig("sin") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { onTrig("cos") }
        findViewById<Button>(R.id.btnTan).setOnClickListener { onTrig("tan") }
    }

    // ---------- Entrada de números ----------

    private fun onDigit(d: String) {
        if (justEvaluated) {
            currentInput = ""
            justEvaluated = false
        }
        currentInput = if (currentInput == "0") d else currentInput + d
        tvDisplay.text = currentInput
    }

    private fun onDot() {
        if (justEvaluated) {
            currentInput = ""
            justEvaluated = false
        }
        if (currentInput.contains('.')) return
        currentInput = if (currentInput.isEmpty()) "0." else "$currentInput."
        tvDisplay.text = currentInput
    }

    // ---------- Operaciones ----------

    private fun onOperator(op: Char) {
        val value = currentInput.toDoubleOrNull()
        if (value != null) {
            val result = if (accumulator != null && pendingOp != null) {
                calculate(accumulator!!, value, pendingOp!!)
            } else value
            if (result == null) { showError(); return }
            accumulator = result
            tvDisplay.text = format(result)
            currentInput = ""
        } else if (accumulator == null) {
            return // no hay nada con lo que operar
        }
        pendingOp = op
        justEvaluated = false
    }

    private fun onEquals() {
        val value = currentInput.toDoubleOrNull()
        val acc = accumulator
        val op = pendingOp
        if (value == null || acc == null || op == null) return

        val result = calculate(acc, value, op)
        if (result == null) { showError(); return }

        accumulator = null
        pendingOp = null
        currentInput = format(result)
        justEvaluated = true
        tvDisplay.text = currentInput
    }

    private fun onTrig(fn: String) {
        // Se aplica al número en pantalla
        val value = currentInput.toDoubleOrNull() ?: accumulator ?: 0.0
        val rad = if (swDegrees.isChecked) Math.toRadians(value) else value

        val result = when (fn) {
            "sin" -> sin(rad)
            "cos" -> cos(rad)
            else -> {
                if (abs(cos(rad)) < 1e-12) { showError(); return } // tan indefinida
                tan(rad)
            }
        }
        if (result.isNaN() || result.isInfinite()) { showError(); return }

        // Redondeo para evitar cosas como sin(180°) = 1.2E-16
        val rounded = Math.round(result * 1e10) / 1e10
        currentInput = format(rounded)
        justEvaluated = true
        tvDisplay.text = currentInput
    }

    private fun calculate(a: Double, b: Double, op: Char): Double? = when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b == 0.0) null else a / b
        else -> null
    }

    // ---------- Utilidades ----------

    private fun clearAll() {
        currentInput = ""
        accumulator = null
        pendingOp = null
        justEvaluated = false
        tvDisplay.text = "0"
    }

    private fun showError() {
        clearAll()
        tvDisplay.text = "Error"
    }

    private fun format(x: Double): String {
        if (x == floor(x) && abs(x) < 1e15) return x.toLong().toString()
        return BigDecimal(x.toString()).stripTrailingZeros().toPlainString()
    }
}