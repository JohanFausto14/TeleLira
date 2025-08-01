package com.example.liratele

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LetrasMagicasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_vocales)
    }

    private fun startGameWithDifficulty(difficulty: Int) {
        val intent = Intent(this, JuegoVocalesActivity::class.java)
        intent.putExtra("dificultad", difficulty)
        startActivity(intent)
    }

}
