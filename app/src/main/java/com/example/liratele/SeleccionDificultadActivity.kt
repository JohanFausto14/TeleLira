package com.example.liratele

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SeleccionDificultadActivity : AppCompatActivity() {

    private lateinit var btnFacil: Button
    private lateinit var btnMedio: Button
    private lateinit var btnDificil: Button

    private var juego: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_dificultad)

        btnFacil = findViewById(R.id.btnFacil)
        btnMedio = findViewById(R.id.btnMedio)
        btnDificil = findViewById(R.id.btnDificil)

        juego = intent.getStringExtra("juego")

        btnFacil.setOnClickListener { lanzarJuegoConDificultad(1) }
        btnMedio.setOnClickListener { lanzarJuegoConDificultad(2) }
        btnDificil.setOnClickListener { lanzarJuegoConDificultad(3) }
    }

    private fun lanzarJuegoConDificultad(dificultad: Int) {
        if (juego == null) {
            finish()
            return
        }

        val intent = when (juego) {
            "formarpalabras" -> Intent(this, FormarPalabrasActivity::class.java)
            "vocales" -> Intent(this, JuegoVocalesActivity::class.java)
            "cuentosdivertidos" -> Intent(this, CuentosDivertidosActivity::class.java)
            else -> null
        }

        if (intent == null) {
            // Juego no soportado, cerrar selector
            finish()
            return
        }

        intent.putExtra("dificultad", dificultad)
        startActivity(intent)
        finish()
    }
}
