package com.example.liratele

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL

class MainMenuActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val TAG = "MainMenuActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val profileImage = findViewById<ImageView>(R.id.profileImage)
        val userName = findViewById<TextView>(R.id.userName)
        val userEmail = findViewById<TextView>(R.id.userEmail)
        val userPoints = findViewById<TextView>(R.id.userPoints)

        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val parentId = prefs.getString("parentId", "No guardado")
        val idUsuario = prefs.getString("idUsuario", "No guardado")
        val correo = prefs.getString("correo", "No guardado")
        val role = prefs.getString("role", "No guardado")
        val nombre = prefs.getString("nombre", "No guardado")
        val idNino = prefs.getString("idNino", null)
        val token = prefs.getString("token", "No guardado")

        Log.d(TAG, "parentId: $parentId")
        Log.d(TAG, "idUsuario: $idUsuario")
        Log.d(TAG, "correo: $correo")
        Log.d(TAG, "role: $role")
        Log.d(TAG, "nombre: $nombre")
        Log.d(TAG, "idNino: $idNino")
        Log.d(TAG, "token: $token")

        profileImage.setImageResource(R.drawable.lira)
        userName.text = nombre
        userEmail.text = correo

        if (idNino != null) {
            obtenerProgreso(idNino, profileImage, userPoints)
        }

        val gameCards = listOf(
            findViewById<MaterialCardView>(R.id.game1),
            findViewById<MaterialCardView>(R.id.game2),
            findViewById<MaterialCardView>(R.id.game3),
            findViewById<MaterialCardView>(R.id.game4),
            findViewById<MaterialCardView>(R.id.game5)
        )

        val colors = listOf(
            Color.parseColor("#FFA500"),
            Color.parseColor("#800080"),
            Color.parseColor("#FFC0CB"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#008000")
        )

        val anim = AnimationUtils.loadAnimation(this, R.anim.animacion)
        gameCards.forEachIndexed { i, card ->
            card.setCardBackgroundColor(colors[i])
            card.startAnimation(anim)
        }

        // Aquí asignamos el juego correcto con su etiqueta "juego" para pasar a la dificultad

        gameCards[0].setOnClickListener {
            val intent = Intent(this, SeleccionDificultadActivity::class.java)
            intent.putExtra("juego", "vocales")
            startActivity(intent)
        }
        gameCards[1].setOnClickListener {
            val intent = Intent(this, SeleccionDificultadActivity::class.java)
            intent.putExtra("juego", "formarpalabras")
            startActivity(intent)
        }
        gameCards[2].setOnClickListener {
            val intent = Intent(this, CuentosDivertidosActivity::class.java)
            startActivity(intent)
        }

        gameCards[3].setOnClickListener {
            val intent = Intent(this, DesafiosLiraActivity::class.java)
            startActivity(intent)
        }
        gameCards[4].setOnClickListener {
            startActivity(Intent(this, ProximamenteActivity::class.java))
        }
    }
        private fun obtenerProgreso(idNino: String, profileImage: ImageView, userPoints: TextView) {
        val url = "https://api-lira.onrender.com/api/child-pro/$idNino"
        Log.d(TAG, "Llamando a la API: $url")

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error al conectar con la API: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainMenuActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Respuesta recibida: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val totalPoints = json.getInt("totalPoints")
                    val avatarUrl = json.getString("avatar")

                    Log.d(TAG, "Puntos totales: $totalPoints")
                    Log.d(TAG, "URL del avatar: $avatarUrl")

                    runOnUiThread {
                        userPoints.text = "Puntos: $totalPoints"
                        if (avatarUrl.isNotEmpty()) {
                            DownloadImageTask(profileImage).execute(avatarUrl)
                        }
                    }
                } else {
                    Log.e(TAG, "Respuesta no exitosa: ${response.code}")
                }
            }
        })
    }

    private class DownloadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val url = urls[0]
            return try {
                val input: InputStream = URL(url).openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e("DownloadImageTask", "Error descargando imagen: ${e.message}")
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            result?.let {
                imageView.setImageBitmap(it)
            }
        }
    }
}
