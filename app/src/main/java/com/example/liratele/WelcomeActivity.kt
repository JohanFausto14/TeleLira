package com.example.liratele

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val nombre = intent.getStringExtra("nombre") ?: "Usuario"
        welcomeText.text = "Bienvenido, $nombre"

        // Transición automática después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("nombre", nombre)
            startActivity(intent)
            finish()
        }, 3000)
    }
}