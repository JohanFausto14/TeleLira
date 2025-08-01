package com.example.liratele

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Configuración básica
        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val userName = findViewById<TextView>(R.id.userName)
        val nombre = intent.getStringExtra("nombre") ?: "Usuario"

        profileImage.setImageResource(R.drawable.lira)
        userName.text = nombre

        // Cards de juego
        val game1 = findViewById<MaterialCardView>(R.id.game1)
        val game2 = findViewById<MaterialCardView>(R.id.game2)
        val game3 = findViewById<MaterialCardView>(R.id.game3)
        val game4 = findViewById<MaterialCardView>(R.id.game4)
        val game5 = findViewById<MaterialCardView>(R.id.game5)

        // Colores personalizados
        val colors = listOf(
            Color.parseColor("#FFA500"),  // Orange
            Color.parseColor("#800080"),  // Purple
            Color.parseColor("#FFC0CB"),  // Pink
            Color.parseColor("#0000FF"),  // Blue
            Color.parseColor("#008000")   // Green
        )

        val gameCards = listOf(game1, game2, game3, game4, game5)

        // Cargar animación de flotación
        val floatAnim = AnimationUtils.loadAnimation(this, R.anim.animacion)

        gameCards.forEachIndexed { index, card ->
            card.setCardBackgroundColor(colors[index])
            card.startAnimation(floatAnim) // Aplicar animación de flotación
        }

        // Navegación a cada actividad
        game1.setOnClickListener {
            startActivity(Intent(this, LetrasMagicasActivity::class.java))
        }

        game2.setOnClickListener {
            startActivity(Intent(this, FormarPalabrasActivity::class.java))
        }

        game3.setOnClickListener {
            startActivity(Intent(this, CuentosDivertidosActivity::class.java))
        }

        game4.setOnClickListener {
            startActivity(Intent(this, DesafiosLiraActivity::class.java))
        }

        game5.setOnClickListener {
            startActivity(Intent(this, ProximamenteActivity::class.java))
        }
    }
}
