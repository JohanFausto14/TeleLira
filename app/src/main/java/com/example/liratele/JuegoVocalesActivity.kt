package com.example.liratele

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var layoutFinal: LinearLayout
    private lateinit var txtTituloFinal: TextView
    private lateinit var txtPuntuacionFinal: TextView
    private lateinit var txtMensajeFinal: TextView
    private lateinit var btnJugarNuevo: Button
    private lateinit var btnCambiarDificultad: Button
    private lateinit var btnVolverInicio: Button

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
    private val TOTAL_PREGUNTAS = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_vocales)
        initViews()
        setupListeners()
        tts = TextToSpeech(this, this)
        mostrarPreguntaInicial()
    }

    private fun initViews() {
        dificultad = intent.getIntExtra("dificultad", 1)
        imgEmoji = findViewById(R.id.imgEmoji)
        txtNombre = findViewById(R.id.txtNombre)
        layoutOpciones = findViewById(R.id.layoutOpciones)
        txtPuntos = findViewById(R.id.txtPuntos)
        txtFeedback = findViewById(R.id.txtFeedback)
        layoutFinal = findViewById(R.id.layoutFinal)
        txtTituloFinal = findViewById(R.id.txtTituloFinal)
        txtPuntuacionFinal = findViewById(R.id.txtPuntuacionFinal)
        txtMensajeFinal = findViewById(R.id.txtMensajeFinal)
        btnJugarNuevo = findViewById(R.id.btnJugarNuevo)
        btnCambiarDificultad = findViewById(R.id.btnCambiarDificultad)
        btnVolverInicio = findViewById(R.id.btnVolverInicio)
    }

    private fun setupListeners() {
        btnJugarNuevo.setOnClickListener { reiniciarJuego() }
        btnCambiarDificultad.setOnClickListener { cambiarDificultad() }
        btnVolverInicio.setOnClickListener { volverASeleccionJuegos() }
    }

    private fun reiniciarJuego() {
        puntos = 0
        preguntaActual = 0
        itemsUsados.clear()
        layoutFinal.visibility = View.GONE
        txtPuntos.visibility = View.VISIBLE
        imgEmoji.visibility = View.VISIBLE
        layoutOpciones.visibility = View.VISIBLE
        txtFeedback.visibility = View.INVISIBLE
        mostrarPreguntaInicial()
    }

    private fun cambiarDificultad() {
        val intent = Intent(this, SeleccionDificultadActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun volverASeleccionJuegos() {
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun mostrarPreguntaInicial() {
        iniciarPregunta()
    }

    private fun iniciarPregunta() {
        val disponibles = getItemsPorNivel().filterNot { itemsUsados.contains(it.nombre) }

        if (disponibles.isEmpty() || preguntaActual >= TOTAL_PREGUNTAS) {
            guardarProgreso()
            mostrarFin()
            return
        }

        resetUIForNewQuestion()
        preguntaActual++
        setupCurrentItem(disponibles.random())
        setupOptionsButtons()
    }

    private fun resetUIForNewQuestion() {
        txtFeedback.text = ""
        txtFeedback.visibility = View.INVISIBLE
        layoutOpciones.removeAllViews()
    }

    private fun setupCurrentItem(item: Item) {
        itemActual = item
        itemsUsados.add(itemActual.nombre)
        imgEmoji.text = itemActual.emoji
        txtNombre.text = if (dificultad == 3) itemActual.nombre else ""
        tts.speak(itemActual.nombre, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun setupOptionsButtons() {
        val opciones = generarOpciones(itemActual.vocal)
        val screenWidth = resources.displayMetrics.widthPixels
        val buttonWidth = (screenWidth * 0.45).toInt()
        val buttonHeight = (60 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for (i in 0..1) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, if (i == 0) 0 else margin, 0, 0)
                }
                weightSum = 2f
            }

            for (j in 0..1) {
                val index = i * 2 + j
                if (index < opciones.size) {
                    val btn = createOptionButton(opciones[index], buttonWidth, buttonHeight, margin)
                    row.addView(btn)
                }
            }
            container.addView(row)
        }

        layoutOpciones.addView(container)
        actualizarPuntos()
    }

    private fun createOptionButton(vocal: String, width: Int, height: Int, margin: Int): Button {
        return Button(this).apply {
            text = vocal
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.btn_vocal_selector)
            setOnClickListener { verificarRespuesta(vocal) }
            layoutParams = LinearLayout.LayoutParams(width, height).apply {
                setMargins(margin, 0, margin, 0)
                weight = 1f
            }
        }
    }

    private fun verificarRespuesta(vocal: String) {
        if (vocal == itemActual.vocal) {
            puntos += calcularPuntosPorAcierto()
            txtFeedback.text = "✅ ¡Correcto! +${calcularPuntosPorAcierto()} puntos"
            txtFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct_green))
        } else {
            txtFeedback.text = "❌ Incorrecto"
            txtFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong_red))
            layoutOpciones.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        actualizarPuntos()
        txtFeedback.visibility = View.VISIBLE
        layoutOpciones.postDelayed({ iniciarPregunta() }, 1500)
    }

    private fun calcularPuntosPorAcierto(): Int {
        return when (dificultad) {
            1 -> 20
            2 -> 30
            else -> 50
        }
    }

    private fun actualizarPuntos() {
        txtPuntos.text = "Puntos: $puntos"
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

    private fun guardarProgreso() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
                val childId = prefs.getString("idNino", null)
                val token = prefs.getString("token", null)
                val puntosTotales = prefs.getInt("puntosTotales", 0) + puntos
                prefs.edit().putInt("puntosTotales", puntosTotales).apply()

                // Log para depuración
                android.util.Log.d("JuegoVocales", "Guardando progreso -> childId: $childId, token: $token, puntos: $puntos, total: $puntosTotales")

                if (childId.isNullOrEmpty() || token.isNullOrEmpty()) {
                    android.util.Log.e("JuegoVocales", "No se pudo guardar: childId o token vacío")
                    return@launch
                }

                val json = """
            {
                "childId": "$childId",
                "gameData": {
                    "gameName": "VocalesJuego",
                    "points": $puntos,
                    "levelsCompleted": $TOTAL_PREGUNTAS,
                    "highestDifficulty": "${getDifficultyString()}",
                    "lastPlayed": "${Date()}"
                },
                "totalPoints": $puntosTotales
            }
            """.trimIndent()

                android.util.Log.d("JuegoVocales", "JSON enviado: $json")

                val url = URL("${ApiConfig.API_BASE_URL}/child-progress")
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.use { os ->
                        os.write(json.toByteArray())
                        os.flush()
                    }
                }

                android.util.Log.d("JuegoVocales", "Respuesta servidor: ${connection.responseCode} - ${connection.responseMessage}")

            } catch (e: Exception) {
                android.util.Log.e("JuegoVocales", "Error al guardar progreso", e)
            }
        }
    }

    private fun getDifficultyString(): String {
        return when (dificultad) {
            1 -> "fácil"
            2 -> "medio"
            else -> "difícil"
        }
    }

    private fun mostrarFin() {
        runOnUiThread {
            txtPuntos.visibility = View.GONE
            imgEmoji.visibility = View.GONE
            layoutOpciones.visibility = View.GONE
            txtFeedback.visibility = View.GONE
            txtTituloFinal.text = "# Juego Completado!"
            txtPuntuacionFinal.text = "Puntuación final: $puntos puntos"
            val porcentajeAciertos = (puntos.toDouble() / (TOTAL_PREGUNTAS * calcularPuntosPorAcierto())) * 100
            txtMensajeFinal.text = when {
                porcentajeAciertos == 100.0 -> "¡Perfecto! 🎉"
                porcentajeAciertos >= 80.0 -> "¡Excelente trabajo! 👍"
                porcentajeAciertos >= 50.0 -> "¡Buen intento! 😊"
                else -> "¡Sigue practicando! 💪"
            }
            layoutFinal.visibility = View.VISIBLE
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
