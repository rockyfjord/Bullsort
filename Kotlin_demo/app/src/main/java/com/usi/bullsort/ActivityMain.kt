package com.usi.bullsort


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class ActivityMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoActivity(view: View) {
        val intent = Intent(this, ActivityCapture::class.java)
        intent.putExtra("storeId", txtStoreId.text.toString())
        startActivity(intent)
    }
}