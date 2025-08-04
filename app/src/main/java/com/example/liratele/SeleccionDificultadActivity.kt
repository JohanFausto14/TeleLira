package com.example.liratele

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SeleccionDificultadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_dificultad)

        val btnFacil = findViewById<Button>(R.id.btnFacil)
        val btnMedio = findViewById<Button>(R.id.btnMedio)
        val btnDificil = findViewById<Button>(R.id.btnDificil)

        btnFacil.setOnClickListener { startJuego(1) }
        btnMedio.setOnClickListener { startJuego(2) }
        btnDificil.setOnClickListener { startJuego(3) }
    }

    private fun startJuego(dificultad: Int) {
        val intent = Intent(this, JuegoVocalesActivity::class.java)
        intent.putExtra("dificultad", dificultad)
        startActivity(intent)
        finish()
    }
}
