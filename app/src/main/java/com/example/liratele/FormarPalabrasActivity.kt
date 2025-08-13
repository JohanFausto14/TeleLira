package com.example.liratele

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

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

    private val selectedLetters = mutableListOf<TextView>()



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
        wordImage = findViewById(R.id.wordImage)

        verificarBtn.setOnClickListener { verificarRespuesta() }
        hintButton.setOnClickListener { toggleHint() }
        resetButton.setOnClickListener { resetGame() }

        // Configurar botón de atrás
        findViewById<Button>(R.id.backButton).setOnClickListener { exitToMenu() }

        // Configurar botones de la pantalla final
        findViewById<Button>(R.id.btnJugarNuevo).setOnClickListener { reiniciarJuego() }
        findViewById<Button>(R.id.btnCambiarDificultad).setOnClickListener { cambiarDificultad() }
        findViewById<Button>(R.id.btnVolverInicio).setOnClickListener { exitToMenu() }
    }

    private fun setupGame() {
        letrasDesordenadas.removeAllViews()
        zonaRespuesta.removeAllViews()
        selectedLetters.clear()
        showHint = false
        hintText.visibility = View.GONE
        
        // Debug log
        android.util.Log.d("FormarPalabras", "Setup game iniciado")

        val filteredWords = wordChallenges.filter { it.difficulty == dificultadStr }
        if (filteredWords.isEmpty()) {
            Toast.makeText(this, "No hay palabras para esta dificultad.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val currentChallenge = filteredWords[currentLevel % filteredWords.size]

        difficultyText.text = dificultadStr.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        hintText.text = currentChallenge.hint

        Glide.with(this)
            .load(currentChallenge.image)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(wordImage)

        currentChallenge.letters.shuffled().forEach { letra ->
            letrasDesordenadas.addView(crearLetraView(letra))
        }

        for (i in currentChallenge.word.indices) {
            val slot = crearEspacioDestino()
            zonaRespuesta.addView(slot)
            android.util.Log.d("FormarPalabras", "Slot $i creado con ID: ${slot.id}")
        }
        
        // Debug log
        android.util.Log.d("FormarPalabras", "Slots creados: ${zonaRespuesta.childCount}")
        android.util.Log.d("FormarPalabras", "Zona respuesta height: ${zonaRespuesta.height}")

        // Forzar layout
        zonaRespuesta.requestLayout()
        zonaRespuesta.invalidate()

        startTimer()
        updatePointsUI()
    }

    private fun crearLetraView(letra: String): TextView {
        val tv = TextView(this)
        tv.text = letra
        tv.textSize = 28f
        tv.setPadding(16, 16, 16, 16)
        tv.setBackgroundResource(R.drawable.letter_bg)
        tv.setTextColor(Color.WHITE)
        tv.isClickable = true
        tv.isFocusable = true

        tv.setOnClickListener {
            if (tv.visibility == View.VISIBLE) {
                val espacioVacio = (0 until zonaRespuesta.childCount)
                    .map { zonaRespuesta.getChildAt(it) as TextView }
                    .firstOrNull { it.text.isEmpty() }

                if (espacioVacio != null) {
                    espacioVacio.text = letra
                    espacioVacio.setBackgroundResource(R.drawable.slot_bg_filled)
                    espacioVacio.setTextColor(Color.BLACK)
                    espacioVacio.textSize = 28f  // Mantener tamaño consistente
                    espacioVacio.visibility = View.VISIBLE
                    selectedLetters.add(espacioVacio)
                    tv.visibility = View.INVISIBLE
                    
                    // Debug log
                    android.util.Log.d("FormarPalabras", "Letra colocada: $letra en slot")
                }
            }
        }

        return tv
    }

    private fun crearEspacioDestino(): TextView {
        val tv = TextView(this)
        tv.text = ""
        tv.textSize = 28f
        tv.setPadding(8, 4, 8, 4)  // Menos padding vertical
        tv.setBackgroundResource(R.drawable.slot_bg)
        tv.setTextColor(Color.BLACK)
        tv.isClickable = true
        
        // Hacer el slot más compacto horizontalmente
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(2, 2, 2, 2)  // Márgenes más pequeños
        tv.layoutParams = layoutParams

        // Al hacer clic sobre una letra ya colocada, regresa la letra a la zona de letras
        tv.setOnClickListener {
            if (tv.text.isNotEmpty()) {
                val letra = tv.text.toString()
                tv.text = ""
                tv.setBackgroundResource(R.drawable.slot_bg)
                tv.setTextColor(Color.BLACK)
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
            val puntosFijos = when (dificultadStr) {
                "fácil" -> 50
                "medio" -> 70
                "difícil" -> 90
                else -> 50
            }

            points += puntosFijos

            currentLevel++
            timer?.cancel()

            if (currentLevel >= filteredWords.size) {
                mostrarResumenFinal()
            } else {
                Toast.makeText(this, "¡Correcto! Siguiente nivel.", Toast.LENGTH_SHORT).show()
                setupGame()
            }
        } else {
            Toast.makeText(this, "Respuesta incorrecta. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            resetRespuesta()
        }
        updatePointsUI()
    }


    private fun resetRespuesta() {
        // Vaciar zonaRespuesta y mostrar todas las letras en letrasDesordenadas
        selectedLetters.forEach {
            it.text = ""
            it.setBackgroundResource(R.drawable.slot_bg)
            it.setTextColor(Color.BLACK)
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

    private fun reiniciarJuego() {
        // Ocultar pantalla final
        findViewById<LinearLayout>(R.id.layoutFinal).visibility = View.GONE
        
        // Mostrar TODOS los elementos del juego
        findViewById<LinearLayout>(R.id.letrasDesordenadas).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.zonaRespuesta).visibility = View.VISIBLE
        findViewById<Button>(R.id.verificarBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.hintButton).visibility = View.VISIBLE
        findViewById<Button>(R.id.resetButton).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.wordImage).visibility = View.VISIBLE
        findViewById<TextView>(R.id.pointsText).visibility = View.VISIBLE
        findViewById<TextView>(R.id.timeText).visibility = View.VISIBLE
        findViewById<TextView>(R.id.difficultyText).visibility = View.VISIBLE
        findViewById<TextView>(R.id.titleText).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.indicatorsLayout).visibility = View.VISIBLE
        
        // Mostrar los textos de "Tu palabra:" y "Letras disponibles:"
        val tuPalabraText = findViewById<TextView>(R.id.tuPalabraText)
        val letrasDisponiblesText = findViewById<TextView>(R.id.letrasDisponiblesText)
        if (tuPalabraText != null) tuPalabraText.visibility = View.VISIBLE
        if (letrasDisponiblesText != null) letrasDisponiblesText.visibility = View.VISIBLE
        
        // Mostrar el botón de atrás
        findViewById<Button>(R.id.backButton).visibility = View.VISIBLE
        
        // Mostrar el contenedor principal del juego
        findViewById<RelativeLayout>(R.id.gameContainer).visibility = View.VISIBLE
        
        // Reiniciar juego
        resetGame()
    }

    private fun cambiarDificultad() {
        val intent = Intent(this, SeleccionDificultadActivity::class.java)
        intent.putExtra("juego", "FormarPalabras")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun updatePointsUI() {
        pointsText.text = "Puntos: $points"
        timeText.text = "Tiempo: ${formatTime(timeLeft)}"
        difficultyText.text = "Dificultad: ${dificultadStr.replaceFirstChar { it.uppercase(Locale.getDefault()) }}"
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


// Dentro de FormarPalabrasActivity

    private fun guardarProgreso() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
                val childId = prefs.getString("idNino", null)
                val token = prefs.getString("token", null)
                val puntosTotalesGuardados = prefs.getInt("puntosTotales", 0)

                // NO sumes aquí puntos acumulados con puntos de la sesión actual,
                // porque la suma debe hacerse solo UNA VEZ en mostrarResumenFinal().

                // Guardar puntos totales ya actualizados previamente
                // (Aquí solo envías points de esta sesión)
                android.util.Log.d("FormarPalabras", "Guardando progreso -> childId: $childId, token: $token, puntos de sesión: $points, total acumulado: $puntosTotalesGuardados")

                if (childId.isNullOrEmpty() || token.isNullOrEmpty()) {
                    android.util.Log.e("FormarPalabras", "No se pudo guardar: childId o token vacío")
                    return@launch
                }

                val json = """
            {
                "childId": "$childId",
                "gameData": {
                    "gameName": "FormarPalabras",
                    "points": $points,
                    "levelsCompleted": $currentLevel,
                    "highestDifficulty": "$dificultadStr",
                    "lastPlayed": "${Date()}"
                },
                "totalPoints": $puntosTotalesGuardados
            }
            """.trimIndent()

                android.util.Log.d("FormarPalabras", "JSON enviado: $json")

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

                android.util.Log.d("FormarPalabras", "Respuesta servidor: ${connection.responseCode} - ${connection.responseMessage}")

            } catch (e: Exception) {
                android.util.Log.e("FormarPalabras", "Error al guardar progreso", e)
            }
        }
    }

    private fun mostrarResumenFinal() {
        timer?.cancel()

        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val puntosTotalesGuardados = prefs.getInt("puntosTotales", 0)
        val puntosTotalesActualizados = puntosTotalesGuardados + points
        prefs.edit().putInt("puntosTotales", puntosTotalesActualizados).apply()

        guardarProgreso()

        // Ocultar TODOS los elementos del juego
        findViewById<LinearLayout>(R.id.letrasDesordenadas).visibility = View.GONE
        findViewById<LinearLayout>(R.id.zonaRespuesta).visibility = View.GONE
        findViewById<Button>(R.id.verificarBtn).visibility = View.GONE
        findViewById<Button>(R.id.hintButton).visibility = View.GONE
        findViewById<Button>(R.id.resetButton).visibility = View.GONE
        findViewById<TextView>(R.id.hintText).visibility = View.GONE
        findViewById<ImageView>(R.id.wordImage).visibility = View.GONE
        findViewById<TextView>(R.id.pointsText).visibility = View.GONE
        findViewById<TextView>(R.id.timeText).visibility = View.GONE
        findViewById<TextView>(R.id.difficultyText).visibility = View.GONE
        findViewById<TextView>(R.id.titleText).visibility = View.GONE

        // Ocultar el contenedor de indicadores
        findViewById<LinearLayout>(R.id.indicatorsLayout).visibility = View.GONE
        
        // Ocultar el botón de atrás
        findViewById<Button>(R.id.backButton).visibility = View.GONE
        
        // Ocultar el contenedor principal del juego (recuadro blanco)
        findViewById<RelativeLayout>(R.id.gameContainer).visibility = View.GONE
        
        // Ocultar el texto "Tu palabra:" y "Letras disponibles:"
        val tuPalabraText = findViewById<TextView>(R.id.tuPalabraText)
        val letrasDisponiblesText = findViewById<TextView>(R.id.letrasDisponiblesText)
        if (tuPalabraText != null) tuPalabraText.visibility = View.GONE
        if (letrasDisponiblesText != null) letrasDisponiblesText.visibility = View.GONE

        // Mostrar pantalla final
        val layoutFinal = findViewById<LinearLayout>(R.id.layoutFinal)
        val txtTituloFinal = findViewById<TextView>(R.id.txtTituloFinal)
        val txtPuntuacionFinal = findViewById<TextView>(R.id.txtPuntuacionFinal)
        val txtMensajeFinal = findViewById<TextView>(R.id.txtMensajeFinal)

        txtTituloFinal.text = "# Juego Completado!"
        txtPuntuacionFinal.text = "Puntuación final: $points puntos"
        
        val porcentajeAciertos = if (currentLevel > 0) (points.toDouble() / (currentLevel * 50)) * 100 else 0.0
        txtMensajeFinal.text = when {
            porcentajeAciertos >= 100.0 -> "¡Perfecto! 🎉"
            porcentajeAciertos >= 80.0 -> "¡Excelente trabajo! 👍"
            porcentajeAciertos >= 50.0 -> "¡Buen intento! 😊"
            else -> "¡Sigue practicando! 💪"
        }

        layoutFinal.visibility = View.VISIBLE
    }

    private fun exitToMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formar_palabras)

        // Configurar el callback para el botón de atrás
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@FormarPalabrasActivity, MainMenuActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        val dificultadEnIntent = intent.getIntExtra("dificultad", -1)
        if (dificultadEnIntent == -1) {
            Toast.makeText(this, "No se especificó dificultad. Volviendo al menú.", Toast.LENGTH_LONG).show()
            finish()
            return
        } else {
            dificultadNum = dificultadEnIntent
            dificultadStr = when (dificultadNum) {
                1 -> "fácil"
                2 -> "medio"
                3 -> "difícil"
                else -> "fácil"
            }
            setupViews()
            setupGame()
        }
    }


    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
