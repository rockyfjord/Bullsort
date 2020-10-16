package com.usi.bullsort

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_capture.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern


class ActivityCapture : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService


    //Move to a companion object, but retrieve storeId from the bundle
    //val bundle: Bundle? = intent.extras
    //val storeID = bundle?.getString("storeId")
    private val storeId  = "0108"
    private val p: Pattern = Pattern.compile("$storeId\\p{Digit}{9} | \\p{Digit}{2}[A-F]\\p{Space}*\\p{Digit}{3}\\D\\d{2}")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)


        val myToast = Toast.makeText(this@ActivityCapture, "STORE#: $storeId", Toast.LENGTH_LONG)
        myToast.show()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                ImageAnalysis.Analyzer { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                        val recognizer = TextRecognition.getClient()
                        val result = recognizer.process(image)
                            .addOnSuccessListener { visionText ->

                                processTextRecognitionResult(visionText)

                            }
                            .addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                            }
                            .addOnCompleteListener {
                                // When the image is from CameraX analysis use case, must call image.close() on received
                                // images when finished using them. Otherwise, new images may not be received or the camera
                                // may stall.
                                imageProxy.close()
                            }

                    }


                })


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processTextRecognitionResult(texts: Text){
        val barcodes: LinkedHashSet<String> = LinkedHashSet<String>()
        val blocks = texts.text

        val m: Matcher = p.matcher(blocks)
        while (m.find())
        {
            barcodes.add(m.group())

        }

        /*  This code is for Demo at the moment. We are not able to update
            the barcodes textView in the UI from outside the main thread.*/
        if(barcodes.size > 0) {
            var t = barcodes.joinToString(prefix = "", postfix = "\n", separator = "\n")
            Log.d("BARCODEs DETECTED:", barcodes.size.toString())
            val myToast = Toast.makeText(this@ActivityCapture, t, Toast.LENGTH_SHORT)
            myToast.show()
        }
        else{
            val myToast = Toast.makeText(this@ActivityCapture, "None detected.", Toast.LENGTH_SHORT)
            myToast.show()
        }



        /*
       //barcodes.joinToString(prefix = "", postfix = "", separator = "\n")
       //Log.d("\nBarcode list:\n", barcodes.joinToString(prefix = "", postfix = "", separator = "\n"))
       //var t: String = txtBarcodes.text.toString() + barcodes.joinToString(prefix = "", postfix = "", separator = "\n")
       //txtBarcodes.text = t
        */
    }
}