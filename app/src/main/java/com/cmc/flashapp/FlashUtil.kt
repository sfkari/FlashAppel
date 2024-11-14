package com.cmc.flashapp

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import android.util.Log
import android.view.Surface

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object FlashUtil {
    private var isFlashing = false
    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraId: String? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private val handler = Handler(Looper.getMainLooper())

    // Variable pour savoir si le flash est actuellement allumé
    private var isFlashOn = false

    fun startFlashing(context: Context) {
        try {
            if (isFlashing) return // Ne commence pas un nouveau clignotement si un autre est en cours

            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraId = getBackCameraId(cameraManager!!)

            if (cameraId != null) {
                cameraManager?.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        startFlashSession() // Démarre la session de flash
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        cameraDevice?.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e("FlashUtil", "Camera error: $error")
                    }
                }, handler)
            }

            isFlashing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopFlashing() {
        try {
            if (isFlashing && cameraCaptureSession != null) {
                // Éteindre le flash
                turnFlashOff()

                // Fermer la session et la caméra
                cameraCaptureSession?.close()
                cameraDevice?.close()

                cameraCaptureSession = null
                cameraDevice = null
                isFlashing = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBackCameraId(cameraManager: CameraManager): String? {
        val cameraIds = cameraManager.cameraIdList
        for (id in cameraIds) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        return null
    }

    private fun startFlashSession() {
        try {
            val surface = Surface(SurfaceTexture(0)) // Faux, utilisé pour la session de capture
            val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)

            // Créer une session de capture avec le mode de flash
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        // Commencer le clignotement
                        toggleFlash()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("FlashUtil", "Configuration failed.")
                    }
                },
                handler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFlash() {
        if (cameraCaptureSession != null) {
            try {
                // Alterner l'état du flash entre allumé et éteint
                val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                if (isFlashOn) {
                    captureRequestBuilder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                } else {
                    captureRequestBuilder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                }

                // Exécuter la requête de capture pour changer l'état du flash
                cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder!!.build(), null, handler)

                // Mettre à jour l'état du flash
                isFlashOn = !isFlashOn

                // Répéter l'action après un certain délai pour créer l'effet de clignotement
                handler.postDelayed({ toggleFlash() }, 500)  // Alterner toutes les 500ms
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun turnFlashOff() {
        try {
            if (cameraCaptureSession != null) {
                val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequestBuilder?.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder!!.build(), null, handler)
                isFlashOn = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
