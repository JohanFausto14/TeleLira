package com.example.liratele

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var qrImage: ImageView
    private lateinit var statusText: TextView
    private val client = OkHttpClient()
    private lateinit var token: String

    private val loginWebBaseUrl = "https://educacion-lira.vercel.app/mision"
    private val apiBaseUrl = "https://api-lira.onrender.com/api"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qrImage = findViewById(R.id.qrImage)
        statusText = findViewById(R.id.statusText)

        token = UUID.randomUUID().toString()

        val qrUrl = "$loginWebBaseUrl?token=$token"

        qrImage.setImageBitmap(generateQR(qrUrl))
        statusText.text = "Escanea el código QR para iniciar sesión"

        startPolling()
    }

    private fun generateQR(text: String): Bitmap {
        val size = 600
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun finalizarJuego() {
        // Más adelante aquí podrías guardar el progreso...

        // Ir al menú principal
        val intent = Intent(this, MainMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }


    private fun startPolling() {
        val handler = Handler(Looper.getMainLooper())

        lateinit var poll: Runnable
        poll = object : Runnable {
            override fun run() {
                val request = Request.Builder()
                    .url("$apiBaseUrl/tv-login-status/$token")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        handler.postDelayed(poll, 3000)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()

                        if (responseBody.isNullOrBlank()) {
                            handler.postDelayed(poll, 3000)
                            return
                        }

                        try {
                            val json = JSONObject(responseBody)

                            if (json.has("user")) {
                                val userObj = json.getJSONObject("user")

                                val parentId = userObj.optString("_id")           // _id: usuario.parentId
                                val idUsuario = userObj.optString("id_usuario")   // id_usuario: usuario._id
                                val correo = userObj.optString("correo")
                                val role = userObj.optString("role")
                                val nombre = userObj.optString("nombre")
                                val idNino = userObj.optString("id_niño")

                                val tokenJwt = json.optString("token")

                                // Guardar en SharedPreferences
                                val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
                                prefs.edit()
                                    .putString("parentId", parentId)
                                    .putString("idUsuario", idUsuario)
                                    .putString("correo", correo)
                                    .putString("role", role)
                                    .putString("nombre", nombre)
                                    .putString("idNino", idNino)
                                    .putString("token", tokenJwt)
                                    .apply()

                                runOnUiThread {
                                    statusText.text = "Sesión iniciada como $nombre"
                                    val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                                    intent.putExtra("nombre", nombre)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                handler.postDelayed(poll, 3000)
                            }
                        } catch (e: Exception) {
                            handler.postDelayed(poll, 3000)
                        }
                    }
                })
            }
        }

        handler.post(poll)
    }
}