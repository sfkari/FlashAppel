package com.cmc.flashapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import android.content.SharedPreferences

class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            // L'appel est en train de sonner
            Toast.makeText(context, "Phone is ringing", Toast.LENGTH_SHORT).show()

            // Vérifier si le switch est activé
            val sharedPreferences = context.getSharedPreferences("FlashAppPrefs", Context.MODE_PRIVATE)
            val isFlashEnabled = sharedPreferences.getBoolean("flash_enabled", false)

            if (isFlashEnabled) {
                // Allumer le flash
                toggleFlashlight(context, true)
            }
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            // L'appel est terminé (ou pas en cours)
            Toast.makeText(context, "Phone is idle", Toast.LENGTH_SHORT).show()

            // Éteindre le flash après l'appel
            val sharedPreferences = context.getSharedPreferences("FlashAppPrefs", Context.MODE_PRIVATE)
            val isFlashEnabled = sharedPreferences.getBoolean("flash_enabled", false)

            if (isFlashEnabled) {
                toggleFlashlight(context, false)
            }
        }
    }

    private fun toggleFlashlight(context: Context, on: Boolean) {
        // Utilisation du CameraManager pour contrôler le flash
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        val cameraId = cameraManager.cameraIdList[0]  // Utilisation de la première caméra
        val params = cameraManager.getCameraCharacteristics(cameraId)

        // Vérifiez si le flash est disponible
        if (params.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {

            for (i in 0..10) {
                cameraManager.setTorchMode(cameraId, on)
                Thread.sleep(500)
                cameraManager.setTorchMode(cameraId, false)
                Thread.sleep(500)
            }
        } else {
            Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
        }
    }
}
