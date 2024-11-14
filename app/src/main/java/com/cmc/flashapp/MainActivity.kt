package com.cmc.flashapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    private lateinit var switchBtn: Switch
    private lateinit var torch: ImageView

    // Codes de demande de permissions
    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private val PHONE_STATE_PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(android.R.color.black)  // Change la couleur de la barre de statut
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false  // Icônes blanches
        }


        // Initialisation des vues
        switchBtn = findViewById(R.id.switchBtn)
        torch = findViewById(R.id.torch)

        // Vérification et demande des permissions
        checkPermissions()

        // Action sur le switch pour activer/désactiver le flash
        switchBtn.setOnCheckedChangeListener { _, isChecked ->
            val sharedPreferences = getSharedPreferences("FlashAppPrefs", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("flash_enabled", isChecked)  // Enregistrer l'état du switch
                apply()
            }

            if (isChecked) {
                torch.setImageResource(R.drawable.flash_on)
            } else {
                torch.setImageResource(R.drawable.flash_off)
            }
        }
    }

    // Vérification des permissions à l'exécution
    private fun checkPermissions() {
        // Vérifier la permission de la caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        // Vérifier la permission de l'état des appels
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), PHONE_STATE_PERMISSION_REQUEST_CODE)
        }
    }

    // Gestion des résultats de demande de permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            PHONE_STATE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Phone state permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Phone state permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
