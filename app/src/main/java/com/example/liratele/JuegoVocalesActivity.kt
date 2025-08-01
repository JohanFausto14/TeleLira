package com.example.liratele

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.example.liratele.ApiConfig.API_BASE_URL


class JuegoVocalesActivity : AppCompatActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var imgEmoji: TextView
    private lateinit var txtNombre: TextView
    private lateinit var layoutOpciones: LinearLayout
    private lateinit var txtPuntos: TextView
    private lateinit var layoutFeedback: TextView
    private lateinit var btnVolver: Button

    data class Item(val nombre: String, val emoji: String, val vocal: String)

    private val itemsFacil = listOf(
        Item("Águila", "🦅", "A"),
        Item("Elefante", "🐘", "E"),
        Item("Iguana", "🦎", "I"),
        Item("Oso", "🐻", "O"),
        Item("Avión", "✈", "A")
    )

    private val itemsMedio = listOf(
        Item("Armadillo", "🦫", "A"),
        Item("Erizo", "🦔", "E"),
        Item("Impala", "🦌", "I"),
        Item("Ornitorrinco", "🦫", "O"),
        Item("Unicornio", "🦄", "U")
    )

    private val itemsDificil = listOf(
        Item("Aguacate", "🥑", "A"),
        Item("Escorpión", "🦂", "E"),
        Item("Instrumento", "🎺", "I"),
        Item("Obelisco", "🏛", "O"),
        Item("Urogallo", "🐦", "U")
    )

    private var dificultad = 1
    private var puntos = 0
    private var preguntaActual = 0
    private lateinit var itemActual: Item
    private var itemsUsados = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_vocales)

        dificultad = intent.getIntExtra("dificultad", 1)

        imgEmoji = findViewById(R.id.imgEmoji)
        txtNombre = findViewById(R.id.txtNombre)
        layoutOpciones = findViewById(R.id.layoutOpciones)
        txtPuntos = findViewById(R.id.txtPuntos)
        layoutFeedback = findViewById(R.id.txtFeedback)
        btnVolver = findViewById(R.id.btnVolver)

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "ES")
            }
        }

        btnVolver.setOnClickListener {
            finish()
        }

        iniciarPregunta()
    }

    private fun iniciarPregunta() {
        val itemsDisponibles = getItemsPorNivel().filterNot { itemsUsados.contains(it.nombre) }

        if (itemsDisponibles.isEmpty() || preguntaActual >= getPreguntasPorNivel()) {
            guardarProgreso()
            mostrarFinJuego()
            return
        }

        layoutFeedback.text = ""
        layoutOpciones.removeAllViews()
        preguntaActual++

        itemActual = itemsDisponibles.random()
        itemsUsados.add(itemActual.nombre)

        imgEmoji.text = itemActual.emoji
        txtNombre.text = if (dificultad == 3) itemActual.nombre else ""

        tts.speak(itemActual.nombre, TextToSpeech.QUEUE_FLUSH, null, null)

        val opciones = generarOpciones(itemActual.vocal).shuffled()
        for (vocal in opciones) {
            val btn = Button(this).apply {
                text = vocal
                textSize = 24f
                setOnClickListener { verificarRespuesta(vocal) }
            }
            layoutOpciones.addView(btn)
        }

        txtPuntos.text = "⭐ Puntos: $puntos"
    }

    private fun verificarRespuesta(vocal: String) {
        if (vocal == itemActual.vocal) {
            puntos += dificultad * 20
            layoutFeedback.text = "✅ ¡Correcto!"
        } else {
            layoutFeedback.text = "❌ Era: ${itemActual.vocal}"
        }

        layoutFeedback.visibility = View.VISIBLE

        layoutOpciones.postDelayed({
            iniciarPregunta()
        }, 1500)
    }

    private fun generarOpciones(correcta: String): List<String> {
        val todas = listOf("A", "E", "I", "O", "U")
        return listOf(
            correcta,
            todas.random(),
            todas.random(),
            todas.random()
        )
    }

    private fun getItemsPorNivel(): List<Item> = when (dificultad) {
        1 -> itemsFacil
        2 -> itemsMedio
        3 -> itemsDificil
        else -> itemsFacil
    }

    private fun getPreguntasPorNivel(): Int = when (dificultad) {
        1 -> 5
        2 -> 7
        3 -> 10
        else -> 5
    }

    private fun guardarProgreso() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val childId = getSharedPreferences("prefs", MODE_PRIVATE).getString("id_niño", null)
                val token = getSharedPreferences("prefs", MODE_PRIVATE).getString("Token", null)
                if (childId != null && token != null) {
                    val json = """
                        {
                          "childId": "$childId",
                          "gameData": {
                            "gameName": "VocalesJuego",
                            "points": $puntos,
                            "levelsCompleted": $preguntaActual,
                            "highestDifficulty": "${getDificultadNombre()}",
                            "lastPlayed": "${Date()}"
                          },
                          "totalPoints": $puntos
                        }
                    """.trimIndent()

                    val url = java.net.URL("${API_BASE_URL}/child-progress")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", "Bearer $token")
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    conn.outputStream.write(json.toByteArray())
                    conn.outputStream.flush()
                    conn.inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getDificultadNombre(): String = when (dificultad) {
        1 -> "fácil"
        2 -> "medio"
        3 -> "difícil"
        else -> "fácil"
    }

    private fun mostrarFinJuego() {
        runOnUiThread {
            Toast.makeText(this, "Juego terminado. Puntos: $puntos", Toast.LENGTH_LONG).show()
            btnVolver.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
