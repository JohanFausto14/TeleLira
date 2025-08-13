package com.example.liratele

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CuentosDivertidosActivity : AppCompatActivity() {

    // Datos completos de los cuentos con redacción mejorada
    private val stories = listOf(
        Story(
            "El León y el Ratón",
            listOf(
                "Un león descansaba plácidamente bajo la sombra de un árbol, cuando un ratón travieso comenzó a corretear sobre él y lo despertó.",
                "El león, molesto, atrapó al ratón entre sus garras, pero decidió perdonarlo tras la promesa de que algún día le devolvería el favor.",
                "Tiempo después, el león quedó atrapado en una red. El ratón, recordando su promesa, roía las cuerdas hasta liberarlo."
            ),
            listOf(
                Question("¿Quién despertó al león?", listOf("Un ratón", "Un elefante", "Un pájaro"), "Un ratón", 10),
                Question("¿Cómo ayudó el ratón al león?", listOf("Rompió las cuerdas con sus dientes", "Pidió ayuda a otros animales", "Le dio agua"), "Rompió las cuerdas con sus dientes", 15)
            )
        ),
        Story(
            "La Tortuga y la Liebre",
            listOf(
                "Una liebre veloz se burlaba constantemente de la lentitud de una tortuga.",
                "Cansada de las burlas, la tortuga retó a la liebre a una carrera. La liebre aceptó confiada en su rapidez.",
                "Durante la carrera, la liebre decidió descansar y se quedó dormida. La tortuga, con paso firme, llegó a la meta primero."
            ),
            listOf(
                Question("¿Por qué perdió la liebre la carrera?", listOf("Se durmió en el camino", "Corrió demasiado lento", "Se perdió en el bosque"), "Se durmió en el camino", 10),
                Question("¿Qué enseñanza nos deja la historia?", listOf("La constancia vence a la velocidad", "Siempre hay que correr rápido", "Hay que ser astuto"), "La constancia vence a la velocidad", 15)
            )
        ),
        Story(
            "El Zorro y las Uvas",
            listOf(
                "Un zorro hambriento vio unas uvas jugosas colgando de una parra muy alta y quiso alcanzarlas.",
                "Saltó una y otra vez, pero no logró tocarlas. Cansado, decidió rendirse.",
                "Mientras se alejaba, murmuró que seguramente esas uvas estaban agrias."
            ),
            listOf(
                Question("¿Por qué no alcanzó el zorro las uvas?", listOf("Estaban muy altas", "Eran pocas", "Eran verdes"), "Estaban muy altas", 10),
                Question("¿Qué hizo el zorro al final?", listOf("Se fue diciendo que estaban agrias", "Intentó más veces", "Pidió ayuda"), "Se fue diciendo que estaban agrias", 10)
            )
        )
    )

    private var currentStoryIndex = 0
    private var currentPage = 0
    private var currentQuestion = 0
    private var score = 0
    private var correctAnswers = 0
    private var questionsAnswered = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuentos_divertidos)

        setupViews()
        showStoriesList()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.backButton).setOnClickListener {
            when {
                findViewById<LinearLayout>(R.id.questionsContainer).visibility == View.VISIBLE -> showStory()
                findViewById<LinearLayout>(R.id.storyContainer).visibility == View.VISIBLE -> showStoriesList()
                else -> finish()
            }
        }
    }

    private fun showStoriesList() {
        resetGameState()
        updateScore()

        findViewById<LinearLayout>(R.id.storyContainer).visibility = View.GONE
        findViewById<LinearLayout>(R.id.questionsContainer).visibility = View.GONE

        val container = findViewById<LinearLayout>(R.id.storiesContainer).apply {
            removeAllViews()

            stories.forEachIndexed { index, story ->
                val button = Button(this@CuentosDivertidosActivity).apply {
                    text = story.title
                    setBackgroundColor(0xFFFF9800.toInt()) // Botón naranja
                    setTextColor(0xFFFFFFFF.toInt())
                    setOnClickListener {
                        currentStoryIndex = index
                        showStory()
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 16) }
                }
                addView(button)
            }
            visibility = View.VISIBLE
        }
    }

    private fun showStory() {
        val story = stories[currentStoryIndex]

        findViewById<LinearLayout>(R.id.storiesContainer).visibility = View.GONE
        findViewById<LinearLayout>(R.id.questionsContainer).visibility = View.GONE

        val storyText = findViewById<TextView>(R.id.storyText)
        val storyImage = findViewById<ImageView>(R.id.storyImage)

        storyText.text = story.content[currentPage]

        val imageName = when (currentStoryIndex) {
            0 -> when (currentPage) {
                0 -> "uno"
                1 -> "dos"
                2 -> "tres"
                else -> null
            }
            1 -> when (currentPage) {
                0 -> "cuatro"
                1 -> "cinco"
                2 -> "seis"
                else -> null
            }
            2 -> when (currentPage) {
                0, 1 -> "siete"
                2 -> "ocho"
                else -> null
            }
            else -> null
        }

        if (imageName != null) {
            val resId = resources.getIdentifier(imageName, "drawable", packageName)
            if (resId != 0) {
                storyImage.setImageResource(resId)
                storyImage.visibility = View.VISIBLE
            } else {
                storyImage.visibility = View.GONE
            }
        } else {
            storyImage.visibility = View.GONE
        }

        findViewById<Button>(R.id.nextButton).apply {
            text = if (currentPage < story.content.size - 1) "Siguiente" else "Preguntas"
            setBackgroundColor(0xFFFF9800.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener {
                if (currentPage < story.content.size - 1) {
                    currentPage++
                    showStory()
                } else {
                    showQuestion()
                }
            }
        }

        findViewById<LinearLayout>(R.id.storyContainer).visibility = View.VISIBLE
    }

    private fun showQuestion() {
        val story = stories[currentStoryIndex]
        val question = story.questions[currentQuestion]

        findViewById<LinearLayout>(R.id.storyContainer).visibility = View.GONE
        findViewById<LinearLayout>(R.id.storiesContainer).visibility = View.GONE
        findViewById<ImageView>(R.id.storyImage).visibility = View.GONE

        val questionText = findViewById<TextView>(R.id.questionText)
        questionText.text = question.text

        findViewById<LinearLayout>(R.id.optionsContainer).apply {
            removeAllViews()
            question.options.forEach { option ->
                val button = Button(this@CuentosDivertidosActivity).apply {
                    text = option
                    setBackgroundColor(0xFFFF9800.toInt())
                    setTextColor(0xFFFFFFFF.toInt())
                    setOnClickListener {
                        checkAnswer(option, question.correctAnswer, question.points)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 16) }
                }
                addView(button)
            }
        }

        findViewById<LinearLayout>(R.id.questionsContainer).visibility = View.VISIBLE
    }

    private fun checkAnswer(selectedAnswer: String, correctAnswer: String, points: Int) {
        questionsAnswered++
        if (selectedAnswer == correctAnswer) {
            correctAnswers++
            score += points
            Toast.makeText(this, "¡Correcto! +$points puntos", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show()
        }
        updateScore()
        currentQuestion++
        if (currentQuestion < stories[currentStoryIndex].questions.size) {
            showQuestion()
        } else {
            mostrarResumenFinal()
        }
    }

    private fun mostrarResumenFinal() {
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val puntosTotalesGuardados = prefs.getInt("puntosTotales", 0)
        val puntosTotalesActualizados = puntosTotalesGuardados + score
        prefs.edit().putInt("puntosTotales", puntosTotalesActualizados).apply()

        guardarProgreso()
        Handler(Looper.getMainLooper()).postDelayed({
            showStoriesList()
        }, 2000)
    }

    private fun guardarProgreso() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
                val childId = prefs.getString("idNino", null)
                val token = prefs.getString("token", null)
                val puntosTotalesGuardados = prefs.getInt("puntosTotales", 0)

                if (childId.isNullOrEmpty() || token.isNullOrEmpty()) {
                    Log.e("CuentosDivertidos", "No se pudo guardar: childId o token vacío")
                    return@launch
                }

                val json = """
                {
                    "childId": "$childId",
                    "gameData": {
                        "gameName": "CuentosDivertidos",
                        "points": $score,
                        "questionsAnswered": $questionsAnswered,
                        "correctAnswers": $correctAnswers,
                        "lastPlayed": "${Date()}"
                    },
                    "totalPoints": $puntosTotalesGuardados
                }
                """.trimIndent()

                Log.d("CuentosDivertidos", "JSON enviado: $json")

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
                Log.d("CuentosDivertidos", "Respuesta servidor: ${connection.responseCode} - ${connection.responseMessage}")
            } catch (e: Exception) {
                Log.e("CuentosDivertidos", "Error al guardar progreso", e)
            }
        }
    }

    private fun updateScore() {
        findViewById<TextView>(R.id.scoreText).text = "Puntos: $score"
    }

    private fun resetGameState() {
        currentPage = 0
        currentQuestion = 0
        score = 0
        correctAnswers = 0
        questionsAnswered = 0
    }

    data class Story(val title: String, val content: List<String>, val questions: List<Question>)
    data class Question(val text: String, val options: List<String>, val correctAnswer: String, val points: Int)
}
