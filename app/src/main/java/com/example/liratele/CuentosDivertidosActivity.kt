package com.example.liratele

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CuentosDivertidosActivity : AppCompatActivity() {

    // Datos completos de los cuentos
    private val stories = listOf(
        Story(
            "El León y el Ratón",
            listOf(
                "Un león dormía cuando un ratón lo despertó corriendo sobre él.",
                "El león atrapó al ratón pero lo dejó ir cuando prometió ayudarlo.",
                "Días después, el ratón salvó al león royendo las cuerdas de una red."
            ),
            listOf(
                Question("¿Quién despertó al león?", listOf("Elefante", "Ratón", "Pájaro"), "Ratón", 10),
                Question("¿Cómo ayudó el ratón?", listOf("Royó redes", "Llamó ayuda", "Trajo agua"), "Royó redes", 15)
            )
        ),
        Story(
            "La Tortuga y la Liebre",
            listOf(
                "Una liebre veloz se burlaba de una tortuga por su lentitud.",
                "La tortuga desafió a la liebre a una carrera.",
                "La liebre, confiada, se durmió y la tortuga ganó."
            ),
            listOf(
                Question("¿Por qué perdió la liebre?", listOf("Se durmió", "Corrió lento", "Se perdió"), "Se durmió", 10),
                Question("¿Qué enseñanza deja?", listOf("Constancia", "Velocidad", "Astucia"), "Constancia", 15)
            )
        ),
        Story(
            "El Zorro y las Uvas",
            listOf(
                "Un zorro vio uvas colgando altas en una parra.",
                "Saltó varias veces pero no pudo alcanzarlas.",
                "Al no conseguirlas, dijo: '¡Están agrias!' y se fue."
            ),
            listOf(
                Question("¿Por qué no alcanzó las uvas?", listOf("Altas", "Verdes", "Pocas"), "Altas", 10),
                Question("¿Qué hizo finalmente?", listOf("Se fue", "Intentó más", "Pidió ayuda"), "Se fue", 10)
            )
        )
    )

    private var currentStoryIndex = 0
    private var currentPage = 0
    private var currentQuestion = 0
    private var score = 0

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

        // Asignar imagen según el cuento
        val imageResId = when (story.title) {
            "El León y el Ratón" -> R.drawable.cuento
            "La Tortuga y la Liebre" -> R.drawable.cuento
            "El Zorro y las Uvas" -> R.drawable.cuento
            else -> 0
        }

        if (imageResId != 0) {
            storyImage.setImageResource(imageResId)
            storyImage.visibility = View.VISIBLE
        } else {
            storyImage.visibility = View.GONE
        }

        findViewById<Button>(R.id.nextButton).apply {
            text = if (currentPage < story.content.size - 1) "Siguiente" else "Preguntas"
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

        // Ocultar imagen en preguntas
        findViewById<ImageView>(R.id.storyImage).visibility = View.GONE

        val questionText = findViewById<TextView>(R.id.questionText)
        questionText.text = question.text

        findViewById<LinearLayout>(R.id.optionsContainer).apply {
            removeAllViews()

            question.options.forEach { option ->
                val button = Button(this@CuentosDivertidosActivity).apply {
                    text = option
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
        if (selectedAnswer == correctAnswer) {
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
            showResult()
        }
    }

    private fun showResult() {
        Toast.makeText(this, "Puntuación final: $score puntos", Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            showStoriesList()
        }, 2000)
    }

    private fun updateScore() {
        findViewById<TextView>(R.id.scoreText).text = "Puntos: $score"
    }

    private fun resetGameState() {
        currentPage = 0
        currentQuestion = 0
    }

    data class Story(val title: String, val content: List<String>, val questions: List<Question>)
    data class Question(val text: String, val options: List<String>, val correctAnswer: String, val points: Int)
}
