package com.usi.bullsort


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_capture.*
import kotlinx.coroutines.Runnable
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread


class ActivityCapture : AppCompatActivity() {
    private lateinit var storeId: String
    private lateinit var patterns: Array<Pattern>
    var certificationCosts = ConcurrentHashMap<String, Any>()
    var concurrentHashSet: ConcurrentHashMap.KeySetView<String, Any> = certificationCosts.keySet(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        storeId = intent.getStringExtra("storeId").toString()
        val barcodePattern: Pattern = Pattern.compile("""\b$storeId\d{9}\b""")
        val dpciPattern: Pattern = Pattern.compile("\\d{3}-\\d{2}-\\d{4}")
        val brPattern: Pattern = Pattern.compile("\\d{2}[A-FM]\\s*\\d{3}\\D\\d{2}")
        patterns = arrayOf<Pattern>(barcodePattern, dpciPattern, brPattern)


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
                .setTargetResolution(Size(1920, 1080))
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

                                val text = processTextRecognitionResult(visionText)
                                concurrentHashSet.addAll(text)
                                thread { // launch a new coroutine in background and continue
                                    if (concurrentHashSet.size > 0) {
                                        val newText = concurrentHashSet.joinToString(
                                            prefix = "",
                                            postfix = "",
                                            separator = "\n"
                                        )
                                        txtBarcodes.setText(newText)
                                        txtCount.text = concurrentHashSet.size.toString()
                                    }
                                }

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

    private fun processTextRecognitionResult(texts: Text): LinkedHashSet<String> {
        val barcodes: LinkedHashSet<String> = LinkedHashSet<String>()
        val blocks = texts.text
        patterns.forEach{
            val m: Matcher = it.matcher(blocks)
            while (m.find()) {
                barcodes.add(m.group().replace("-", "").replace(" ", ""))
            }
        }
        return barcodes
    }

    private fun getOutputMediaFile(fileName: String): String? {
        val folderMain = "barcodes"
        val mediaStorageDir = applicationInfo.dataDir
        // Create the storage directory if it does not exist
        val f = File(mediaStorageDir + File.separator + folderMain)
        if (!f.exists()) {
            f.mkdirs()
        }
        Log.d("SAVINGB", f.path + File.separator + fileName + ".PNG")
        return f.path + File.separator + fileName + ".PNG"
    }

    private fun generateBarcode(barcode: String){
        val codePath = getOutputMediaFile(barcode)
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap =
                barcodeEncoder.encodeBitmap(barcode, BarcodeFormat.CODE_128, 800, 175)
            val fos: FileOutputStream = FileOutputStream(codePath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close()

        } catch (e: java.lang.Exception) {

        }
    }

    fun gotoActivity(view: View) {
        if (concurrentHashSet.size > 0){
            for (entry in concurrentHashSet) {
                generateBarcode(entry)
            }
        }
        val intent = Intent(this, ActivityBarcodes::class.java)
        startActivity(intent)
    }

    fun clear(view: View) {
        concurrentHashSet.clear()
        txtCount.text = "0"
        txtBarcodes.setText("")
    }

}


