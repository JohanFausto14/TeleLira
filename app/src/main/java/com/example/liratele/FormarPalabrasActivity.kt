package com.example.liratele

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.util.Locale
import java.util.concurrent.TimeUnit

class FormarPalabrasActivity : AppCompatActivity() {

    private lateinit var letrasDesordenadas: LinearLayout
    private lateinit var zonaRespuesta: LinearLayout
    private lateinit var verificarBtn: Button
    private lateinit var hintButton: Button
    private lateinit var resetButton: Button
    private lateinit var hintText: TextView
    private lateinit var pointsText: TextView
    private lateinit var timeText: TextView
    private lateinit var difficultyText: TextView
    private lateinit var levelText: TextView
    private lateinit var wordImage: ImageView

    private var showHint = false
    private var points = 0
    private var timeLeft = 0L
    private var currentLevel = 0
    private var gameOver = false
    private var timer: CountDownTimer? = null
    private var dificultadNum = 1
    private lateinit var dificultadStr: String

    private val POINTS_BY_DIFFICULTY = mapOf(
        "fácil" to 50,
        "medio" to 100,
        "difícil" to 150
    )
    private val HINT_PENALTY = 20
    private val TIME_BONUS_MULTIPLIER = 5
    private val INITIAL_TIME = mapOf(
        "fácil" to 60000L,
        "medio" to 75000L,
        "difícil" to 90000L
    )

    private val wordChallenges = listOf(
        WordChallenge("SOL", "https://png.pngtree.com/png-clipart/20230425/original/pngtree-sun-yellow-cartoon-png-image_9096447.png",
            listOf("S", "O", "L", "N", "T", "R"), "Nos da luz y calor durante el día", "fácil"),
        WordChallenge("PAN", "https://cdn.pixabay.com/photo/2014/07/22/09/59/bread-399286_640.jpg",
            listOf("P", "A", "N", "M", "O", "L"), "Alimento que se hornea y es básico en la dieta", "fácil"),
        WordChallenge("LUZ", "https://img.freepik.com/vector-gratis/lampara-luces-foco-composicion-realista-humo-paisaje-oscuro-lampara-colgante-rayos-particulas-ilustracion-vectorial_1284-75722.jpg",
            listOf("L", "U", "Z", "D", "A", "S"), "Lo que enciendes cuando está oscuro", "fácil"),
        WordChallenge("MAR", "https://img.freepik.com/foto-gratis/mar-tropical-hermoso-mar-playa-cielo-azul-nube-blanca-copyspace_74190-8663.jpg",
            listOf("M", "A", "R", "L", "T", "S"), "Gran extensión de agua salada", "fácil"),
        WordChallenge("MESA", "https://img.freepik.com/vector-premium/dibujos-animados-mesa_119631-412.jpg",
            listOf("M", "E", "S", "A", "L", "T", "O"), "Mueble con patas donde comes o trabajas", "medio"),
        WordChallenge("FLOR", "https://img.freepik.com/foto-gratis/primer-disparo-flor-morada_181624-25863.jpg",
            listOf("F", "L", "O", "R", "P", "D", "S"), "Crece en el jardín y huele bien", "medio"),
        WordChallenge("GATO", "https://img.freepik.com/vector-gratis/personaje-dibujos-animados-gatito-ojos-dulces_1308-135596.jpg",
            listOf("G", "A", "T", "O", "C", "M", "P"), "Animal doméstico que maúlla", "medio"),
        WordChallenge("CASA", "https://images.pexels.com/photos/106399/pexels-photo-106399.jpeg",
            listOf("C", "A", "S", "A", "L", "M", "T"), "Lugar donde vives con tu familia", "medio"),
        WordChallenge("VENTANA", "https://static8.depositphotos.com/1041088/887/i/450/depositphotos_8877408-stock-photo-open-window-to-the-back.jpg",
            listOf("V", "E", "N", "T", "A", "N", "A", "M"), "Abertura en la pared para ver afuera", "difícil"),
        WordChallenge("JARDÍN", "https://collectionworld.net/modules/ph_simpleblog/covers/281.jpg",
            listOf("J", "A", "R", "D", "Í", "N", "P", "L", "O"), "Espacio con plantas y flores alrededor de una casa", "difícil"),
        WordChallenge("TIGRE", "https://media.istockphoto.com/id/1218694103/es/foto/tigre-real-aislado-en-el-trazado-de-recorte-de-fondo-blanco-incluido-el-tigre-est%C3%A1-mirando-a.jpg",
            listOf("T", "I", "G", "R", "E", "A", "L", "O", "N"), "Felino grande con rayas negras", "difícil"),
        WordChallenge("ESCUELA", "https://static6.depositphotos.com/1005738/610/v/450/depositphotos_6105444-stock-illustration-back-to-school-time.jpg",
            listOf("E", "S", "C", "U", "E", "L", "A", "D", "O"), "Lugar donde los niños van a aprender", "difícil")
    )

    data class WordChallenge(
        val word: String,
        val image: String,
        val letters: List<String>,
        val hint: String,
        val difficulty: String
    )

    // Para guardar las letras seleccionadas en la zona de respuesta
    private val selectedLetters = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formar_palabras)

        dificultadNum = intent.getIntExtra("dificultad", 1)
        dificultadStr = when (dificultadNum) {
            1 -> "fácil"
            2 -> "medio"
            3 -> "difícil"
            else -> "fácil"
        }

        setupViews()
        setupGame()
    }

    private fun setupViews() {
        letrasDesordenadas = findViewById(R.id.letrasDesordenadas)
        zonaRespuesta = findViewById(R.id.zonaRespuesta)
        verificarBtn = findViewById(R.id.verificarBtn)
        hintButton = findViewById(R.id.hintButton)
        resetButton = findViewById(R.id.resetButton)
        hintText = findViewById(R.id.hintText)
        pointsText = findViewById(R.id.pointsText)
        timeText = findViewById(R.id.timeText)
        difficultyText = findViewById(R.id.difficultyText)
        levelText = findViewById(R.id.levelText)
        wordImage = findViewById(R.id.wordImage)

        verificarBtn.setOnClickListener { verificarRespuesta() }
        hintButton.setOnClickListener { toggleHint() }
        resetButton.setOnClickListener { resetGame() }


    }

    private fun setupGame() {
        letrasDesordenadas.removeAllViews()
        zonaRespuesta.removeAllViews()
        selectedLetters.clear()
        showHint = false
        hintText.visibility = View.GONE

        val filteredWords = wordChallenges.filter { it.difficulty == dificultadStr }
        if (filteredWords.isEmpty()) {
            Toast.makeText(this, "No hay palabras para esta dificultad.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val currentChallenge = filteredWords[currentLevel % filteredWords.size]

        levelText.text = "Nivel ${currentLevel + 1}"
        difficultyText.text = dificultadStr.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        hintText.text = currentChallenge.hint

        Glide.with(this)
            .load(currentChallenge.image)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(wordImage)

        // Mostrar letras desordenadas como botones clicables
        currentChallenge.letters.shuffled().forEach { letra ->
            letrasDesordenadas.addView(crearLetraView(letra))
        }

        // Crear espacios vacíos para la palabra respuesta
        for (i in currentChallenge.word.indices) {
            zonaRespuesta.addView(crearEspacioDestino())
        }

        startTimer()
        updatePointsUI()
    }

    private fun crearLetraView(letra: String): TextView {
        val tv = TextView(this)
        tv.text = letra
        tv.textSize = 36f
        tv.setPadding(24, 24, 24, 24)
        tv.setBackgroundResource(R.drawable.letter_bg)
        tv.setTextColor(Color.WHITE)
        tv.isClickable = true
        tv.isFocusable = true

        tv.setOnClickListener {
            if (tv.visibility == View.VISIBLE) {
                // Buscar primer espacio vacío en zonaRespuesta
                val espacioVacio = (0 until zonaRespuesta.childCount)
                    .map { zonaRespuesta.getChildAt(it) as TextView }
                    .firstOrNull { it.text.isEmpty() }

                if (espacioVacio != null) {
                    espacioVacio.text = letra
                    selectedLetters.add(espacioVacio)
                    tv.visibility = View.INVISIBLE
                }
            }
        }
        return tv
    }

    private fun crearEspacioDestino(): TextView {
        val tv = TextView(this)
        tv.text = ""
        tv.textSize = 36f
        tv.setPadding(24, 24, 24, 24)
        tv.setBackgroundResource(R.drawable.slot_bg)
        tv.setTextColor(Color.BLACK)
        tv.isClickable = true

        // Al hacer clic sobre una letra ya colocada, regresa la letra a la zona de letras
        tv.setOnClickListener {
            if (tv.text.isNotEmpty()) {
                val letra = tv.text.toString()
                tv.text = ""
                // Mostrar la letra correspondiente en letrasDesordenadas
                for (i in 0 until letrasDesordenadas.childCount) {
                    val letraView = letrasDesordenadas.getChildAt(i) as TextView
                    if (letraView.text == letra && letraView.visibility == View.INVISIBLE) {
                        letraView.visibility = View.VISIBLE
                        break
                    }
                }
                selectedLetters.remove(tv)
            }
        }
        return tv
    }

    private fun verificarRespuesta() {
        val userAnswer = (0 until zonaRespuesta.childCount)
            .map { (zonaRespuesta.getChildAt(it) as TextView).text.toString() }
            .joinToString("")

        val filteredWords = wordChallenges.filter { it.difficulty == dificultadStr }
        val currentWord = filteredWords[currentLevel % filteredWords.size].word.uppercase(Locale.getDefault())

        if (userAnswer.length != currentWord.length) {
            Toast.makeText(this, "Completa la palabra antes de verificar", Toast.LENGTH_SHORT).show()
            return
        }

        if (userAnswer == currentWord) {
            // Correcto
            val basePoints = POINTS_BY_DIFFICULTY[dificultadStr] ?: 50
            val timeBonus = (timeLeft / 1000) * TIME_BONUS_MULTIPLIER
            points += (basePoints + timeBonus).toInt()
            currentLevel++
            timer?.cancel()

            if (currentLevel >= filteredWords.size) {
                mostrarResumenFinal()
            } else {
                Toast.makeText(this, "¡Correcto! Siguiente nivel.", Toast.LENGTH_SHORT).show()
                setupGame()
            }
        } else {
            // Incorrecto
            Toast.makeText(this, "Respuesta incorrecta. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            resetRespuesta()
        }
        updatePointsUI()
    }

    private fun resetRespuesta() {
        // Vaciar zonaRespuesta y mostrar todas las letras en letrasDesordenadas
        selectedLetters.forEach {
            it.text = ""
        }
        selectedLetters.clear()

        for (i in 0 until letrasDesordenadas.childCount) {
            val letraView = letrasDesordenadas.getChildAt(i) as TextView
            letraView.visibility = View.VISIBLE
        }
    }

    private fun toggleHint() {
        if (!showHint) {
            showHint = true
            hintText.visibility = View.VISIBLE
            points -= HINT_PENALTY
            if (points < 0) points = 0
            updatePointsUI()
        } else {
            showHint = false
            hintText.visibility = View.GONE
        }
    }

    private fun resetGame() {
        timer?.cancel()
        points = 0
        currentLevel = 0
        setupGame()
    }

    private fun updatePointsUI() {
        pointsText.text = "Puntos: $points"
        timeText.text = "Tiempo: ${formatTime(timeLeft)}"
        difficultyText.text = "Dificultad: ${dificultadStr.replaceFirstChar { it.uppercase(Locale.getDefault()) }}"
        levelText.text = "Nivel: ${currentLevel + 1}"
    }

    private fun startTimer() {
        timer?.cancel()

        val initialTime = INITIAL_TIME[dificultadStr] ?: 60000L

        timer = object : CountDownTimer(initialTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                timeText.text = "Tiempo: ${formatTime(millisUntilFinished)}"
            }

            override fun onFinish() {
                gameOver = true
                timeLeft = 0
                timeText.text = "Tiempo: 00:00"
                mostrarResumenFinal()
            }
        }
        timer?.start()
    }

    private fun mostrarResumenFinal() {
        timer?.cancel()

        val mensaje = """
            Juego terminado.
            Puntos finales: $points
            Nivel alcanzado: ${currentLevel}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Resumen Final")
            .setMessage(mensaje)
            .setPositiveButton("Volver al menú") { _, _ ->
                exitToMenu()
            }
            .setCancelable(false)
            .show()
    }

    private fun exitToMenu() {
        val intent = Intent(this, SeleccionDificultadActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
