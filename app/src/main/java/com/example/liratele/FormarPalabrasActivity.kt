package com.example.liratele

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class FormarPalabrasActivity : AppCompatActivity() {

    private val palabraCorrecta = "LIRA"
    private lateinit var letrasDesordenadas: LinearLayout
    private lateinit var zonaRespuesta: LinearLayout
    private lateinit var verificarBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formar_palabras)

        letrasDesordenadas = findViewById(R.id.letrasDesordenadas)
        zonaRespuesta = findViewById(R.id.zonaRespuesta)
        verificarBtn = findViewById(R.id.verificarBtn)

        val letras = palabraCorrecta.toList().shuffled()

        letras.forEach { letra ->
            val letraView = crearLetraView(letra.toString())
            letrasDesordenadas.addView(letraView)
        }

        // Crear espacios vacíos para la respuesta
        for (i in palabraCorrecta.indices) {
            val espacio = crearEspacioDestino()
            zonaRespuesta.addView(espacio)
        }

        verificarBtn.setOnClickListener {
            verificarRespuesta()
        }
    }

    private fun crearLetraView(letra: String): TextView {
        val textView = TextView(this)
        textView.text = letra
        textView.textSize = 24f
        textView.setPadding(20, 20, 20, 20)
        textView.setBackgroundResource(android.R.color.holo_blue_light)
        textView.setOnLongClickListener {
            val data = ClipData.newPlainText("", letra)
            val shadowBuilder = View.DragShadowBuilder(it)
            it.startDrag(data, shadowBuilder, it, 0)
            true
        }
        return textView
    }

    private fun crearEspacioDestino(): TextView {
        val espacio = TextView(this)
        espacio.text = ""
        espacio.textSize = 24f
        espacio.setBackgroundResource(android.R.color.darker_gray)
        espacio.setPadding(20, 20, 20, 20)

        espacio.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val letraView = event.localState as View
                    val parent = letraView.parent as ViewGroup
                    parent.removeView(letraView)

                    (v as TextView).text = (letraView as TextView).text
                    letraView.visibility = View.GONE
                    true
                }
                else -> true
            }
        }

        return espacio
    }

    private fun verificarRespuesta() {
        val respuesta = StringBuilder()
        for (i in 0 until zonaRespuesta.childCount) {
            val letra = (zonaRespuesta.getChildAt(i) as TextView).text.toString()
            respuesta.append(letra)
        }

        if (respuesta.toString() == palabraCorrecta) {
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show()
            // Regresar al menú
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Intenta de nuevo", Toast.LENGTH_SHORT).show()
        }
    }
}
