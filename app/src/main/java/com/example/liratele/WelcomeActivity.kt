package com.example.liratele

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val nombre = intent.getStringExtra("nombre") ?: "Usuario"
        welcomeText.text = "Bienvenido, $nombre"
    }
}