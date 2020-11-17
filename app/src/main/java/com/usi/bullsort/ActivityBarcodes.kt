package com.usi.bullsort

import android.content.Intent
import android.os.Bundle

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File


class ActivityBarcodes : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcodes)
        val barcodes = getBarcodes()

        viewManager = LinearLayoutManager(this)
        viewAdapter = RecyclerAdapter(barcodes)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }



    private fun getBarcodes(): ArrayList<Pair<String, String>> {
        val barcodesDir = applicationInfo.dataDir + File.separator + "barcodes"
        val f = File(barcodesDir)
        val files = File(f.path).list()
        val barcodes = ArrayList<Pair<String, String>>()
        if (files != null && files.isNotEmpty()) files.forEach {
            val path = barcodesDir + File.separator + it

            val pathArr = it.split(File.separator)
            val barcode = pathArr[pathArr.size - 1].split(".")[0];
            barcodes.add(Pair(path, barcode))
        }
    return barcodes
}

        fun gotoActivity(view: View) {
            val barcodesDir = applicationInfo.dataDir + File.separator + "barcodes"
            val files = File(barcodesDir).list()
            if (files != null && files.isNotEmpty()) files.forEach {
                File(barcodesDir + File.separator + it ).delete()
            }
            val intent = Intent(this, ActivityMain::class.java)
            startActivity(intent)
        }
    }

