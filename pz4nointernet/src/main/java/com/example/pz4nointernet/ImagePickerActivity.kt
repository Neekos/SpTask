package com.example.pz4nointernet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImagePickerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager

        adapter = ImageAdapter(ImageManager.productImages, ImageManager.imageNames) { position ->
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_IMAGE_RES_ID", ImageManager.productImages[position])
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        recyclerView.adapter = adapter
    }

    class ImageAdapter(
        private val images: List<Int>,
        private val names: List<String>,
        private val onImageSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            val textView: TextView = itemView.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_picker, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.imageView.setImageResource(images[position])
            holder.textView.text = names[position]

            holder.itemView.setOnClickListener {
                onImageSelected(position)
            }
        }

        override fun getItemCount(): Int = images.size
    }
}