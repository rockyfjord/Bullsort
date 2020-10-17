package com.usi.bullsort

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_barcodes.*
import java.io.File


class ActivityBarcodes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcodes)

        val bmImg = BitmapFactory.decodeFile("/data/user/0/com.usi.bullsort/barcodes/0108696969696.PNG")
        imageView3.setImageBitmap(bmImg)
    }

    fun gotoActivity(view: View) {
        val intent = Intent(this, ActivityMain::class.java)
        startActivity(intent)
    }
}