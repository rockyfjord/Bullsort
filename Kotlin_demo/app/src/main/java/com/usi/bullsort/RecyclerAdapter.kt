package com.usi.bullsort

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*

class RecyclerAdapter(private val data: ArrayList<Pair<String, String>>) :
RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return PhotoHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val bcodeData = data[position]
        holder.bindPhoto(bcodeData)
    }

    override fun getItemCount()= data.size


    //1
    class PhotoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        //2
        private var view: View = v

        //3
        init {
            v.setOnClickListener(this)
        }

        //4
        override fun onClick(v: View) {
            Log.d("RecyclerView", "CLICK!")
        }

        fun bindPhoto(bcodeData: Pair<String, String>) {
            view.itemImage.setImageBitmap(BitmapFactory.decodeFile(bcodeData.first));
            view.itemDescription.text = bcodeData.second
        }
    }
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}
