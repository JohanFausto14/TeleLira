package com.example.liratele

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FormarPalabrasActivity : AppCompatActivity() {

    private val palabraCorrecta = "LIRA"
    private lateinit var letrasDesordenadas: LinearLayout
    private lateinit var zonaRespuesta: LinearLayout
    private lateinit var verificarBtn: Button
    private lateinit var hintButton: Button
    private lateinit var resetButton: Button
    private lateinit var hintText: TextView

    private var showHint = false
    private val hints = mapOf(
        "LIRA" to "Instrumento musical de cuerda"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formar_palabras)

        letrasDesordenadas = findViewById(R.id.letrasDesordenadas)
        zonaRespuesta = findViewById(R.id.zonaRespuesta)
        verificarBtn = findViewById(R.id.verificarBtn)
        hintButton = findViewById(R.id.hintButton)
        resetButton = findViewById(R.id.resetButton)
        hintText = findViewById(R.id.hintText)

        setupGame()

        verificarBtn.setOnClickListener { verificarRespuesta() }
        hintButton.setOnClickListener { toggleHint() }
        resetButton.setOnClickListener { resetGame() }
    }

    private fun setupGame() {
        letrasDesordenadas.removeAllViews()
        zonaRespuesta.removeAllViews()

        palabraCorrecta.toList().shuffled().forEach { letra ->
            letrasDesordenadas.addView(crearLetraView(letra.toString()))
        }

        for (i in palabraCorrecta.indices) {
            zonaRespuesta.addView(crearEspacioDestino())
        }

        showHint = false
        hintText.visibility = View.GONE
        hintText.text = hints[palabraCorrecta]
    }

    private fun crearLetraView(letra: String): TextView {
        return TextView(this).apply {
            text = letra
            textSize = 36f
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#FF5722")) // Naranja
            setTextColor(Color.WHITE)

            setOnLongClickListener { view ->
                val data = ClipData.newPlainText("letter", letra)
                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDragAndDrop(data, shadowBuilder, view, 0)
                view.visibility = View.INVISIBLE
                true
            }

            setOnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        setBackgroundColor(Color.parseColor("#FF9800")) // Naranja claro
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        setBackgroundColor(Color.parseColor("#FF5722")) // Naranja
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        if (v is TextView && v.parent == letrasDesordenadas) {
                            val originalLetter = v.text.toString()
                            val draggedLetter = event.clipData.getItemAt(0).text.toString()
                            v.text = draggedLetter
                            (event.localState as? TextView)?.text = originalLetter
                        }
                        setBackgroundColor(Color.parseColor("#FF5722"))
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        setBackgroundColor(Color.parseColor("#FF5722"))
                        if (!event.result) {
                            (event.localState as? TextView)?.visibility = View.VISIBLE
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun crearEspacioDestino(): TextView {
        return TextView(this).apply {
            text = ""
            textSize = 36f
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#9E9E9E")) // Gris
            setTextColor(Color.BLACK)

            setOnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> true
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        setBackgroundColor(Color.parseColor("#BDBDBD")) // Gris claro
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        setBackgroundColor(Color.parseColor("#9E9E9E"))
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        if (text.isEmpty()) {
                            text = event.clipData.getItemAt(0).text.toString()
                            (event.localState as? TextView)?.visibility = View.INVISIBLE
                        }
                        setBackgroundColor(Color.parseColor("#9E9E9E"))
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        setBackgroundColor(Color.parseColor("#9E9E9E"))
                        if (!event.result) {
                            (event.localState as? TextView)?.visibility = View.VISIBLE
                        }
                        true
                    }
                    else -> false
                }
            }

            setOnClickListener {
                if (text.isNotEmpty()) {
                    returnLetterToPool(it as TextView)
                }
            }
        }
    }

    private fun returnLetterToPool(slot: TextView) {
        val letter = slot.text.toString()
        slot.text = ""

        for (i in 0 until letrasDesordenadas.childCount) {
            val view = letrasDesordenadas.getChildAt(i) as TextView
            if (view.visibility == View.INVISIBLE && view.text == letter) {
                view.visibility = View.VISIBLE
                break
            }
        }
    }

    private fun verificarRespuesta() {
        val respuesta = StringBuilder().apply {
            for (i in 0 until zonaRespuesta.childCount) {
                append((zonaRespuesta.getChildAt(i) as TextView).text.toString())
            }
        }.toString()

        if (respuesta == palabraCorrecta) {
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Intenta de nuevo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleHint() {
        showHint = !showHint
        hintText.visibility = if (showHint) View.VISIBLE else View.GONE
    }

    private fun resetGame() {
        setupGame()
    }
}