package com.usi.bullsort


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern


class ActivityMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoActivity(view: View) {
        val storeId = txtStoreId.text.toString()
        if(!Pattern.matches("\\d{4}", storeId)){
            txtStoreId.setText(R.string.txtStoreId);
            Toast.makeText(applicationContext, "Store# must be 4 digits!", Toast.LENGTH_LONG).show()
        }
        else {
            val intent = Intent(this, ActivityCapture::class.java).apply {
                putExtra("storeId", storeId)

            }
            startActivity(intent)
        }
    }
}