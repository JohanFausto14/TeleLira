package com.example.liratele

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.liratele.ApiConfig.API_BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class JuegoVocalesActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var imgEmoji: TextView
    private lateinit var txtNombre: TextView
    private lateinit var layoutOpciones: LinearLayout
    private lateinit var txtPuntos: TextView
    private lateinit var txtFeedback: TextView
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
    private val itemsUsados = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_vocales)

        dificultad = intent.getIntExtra("dificultad", 1)
        imgEmoji = findViewById(R.id.imgEmoji)
        txtNombre = findViewById(R.id.txtNombre)
        layoutOpciones = findViewById(R.id.layoutOpciones)
        txtPuntos = findViewById(R.id.txtPuntos)
        txtFeedback = findViewById(R.id.txtFeedback)
        btnVolver = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() }

        tts = TextToSpeech(this, this)
        mostrarPreguntaInicial()
    }

    private fun mostrarPreguntaInicial() {
        iniciarPregunta()
    }

    private fun iniciarPregunta() {
        val disponibles = getItemsPorNivel().filterNot { itemsUsados.contains(it.nombre) }

        if (disponibles.isEmpty() || preguntaActual >= getPreguntasPorNivel()) {
            guardarProgreso()
            mostrarFin()
            return
        }

        txtFeedback.text = ""
        txtFeedback.visibility = View.INVISIBLE
        layoutOpciones.removeAllViews()
        preguntaActual++

        itemActual = disponibles.random()
        itemsUsados.add(itemActual.nombre)

        imgEmoji.text = itemActual.emoji
        txtNombre.text = if (dificultad == 3) itemActual.nombre else ""

        tts.speak(itemActual.nombre, TextToSpeech.QUEUE_FLUSH, null, null)

        val opciones = generarOpciones(itemActual.vocal).shuffled()
        opciones.forEach { vocal ->
            val btn = Button(this).apply {
                text = vocal
                textSize = 24f
                setOnClickListener { verificarRespuesta(vocal, this) }
            }
            layoutOpciones.addView(btn)
        }
        txtPuntos.text = "⭐ Puntos: $puntos"
    }

    private fun verificarRespuesta(vocal: String, btn: Button) {
        if (vocal == itemActual.vocal) {
            puntos += dificultad * 20
            txtFeedback.text = "✅ ¡Correcto! +${dificultad * 20}"
        } else {
            txtFeedback.text = "❌ Era: ${itemActual.vocal}"
        }
        txtFeedback.visibility = View.VISIBLE
        btn.postDelayed({ iniciarPregunta() }, 1500)
    }

    private fun generarOpciones(correcta: String): List<String> {
        val todas = listOf("A", "E", "I", "O", "U")
        return (listOf(correcta) + todas.shuffled().filter { it != correcta }.take(3)).shuffled()
    }

    private fun getItemsPorNivel(): List<Item> = when (dificultad) {
        1 -> itemsFacil
        2 -> itemsMedio
        else -> itemsDificil
    }

    private fun getPreguntasPorNivel(): Int = when (dificultad) {
        1 -> 5
        2 -> 7
        else -> 10
    }

    private fun guardarProgreso() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
                val childId = prefs.getString("id_niño", null)
                val token = prefs.getString("Token", null)
                if (childId != null && token != null) {
                    val json = """
                        {
                          "childId":"$childId",
                          "gameData":{
                            "gameName":"VocalesJuego",
                            "points":$puntos,
                            "levelsCompleted":$preguntaActual,
                            "highestDifficulty":"${if(dificultad==1) "fácil" else if(dificultad==2) "medio" else "difícil"}",
                            "lastPlayed":"${Date()}"
                          },
                          "totalPoints":$puntos
                        }
                    """.trimIndent()
                    val url = URL("$API_BASE_URL/child-progress")
                    (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        outputStream.write(json.toByteArray())
                        outputStream.flush()
                        inputStream.close()
                    }
                }
            } catch (_: Exception) { /* ignora fallos durante desarrollo */ }
        }
    }

    private fun mostrarFin() {
        runOnUiThread {
            txtFeedback.text = "Juego terminado\nPuntos: $puntos"
            txtFeedback.visibility = View.VISIBLE
            btnVolver.visibility = View.VISIBLE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
